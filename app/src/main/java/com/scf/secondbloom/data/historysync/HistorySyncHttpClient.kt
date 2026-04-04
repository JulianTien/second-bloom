package com.scf.secondbloom.data.historysync

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class HttpHistorySyncApi(
    private val baseUrl: String,
    private val connectionFactory: (String) -> HttpURLConnection = { url ->
        URL(url).openConnection() as HttpURLConnection
    },
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) : HistorySyncApi {

    override suspend fun getMe(accessToken: String): UserProfileDto =
        getJson(
            path = "/me",
            accessToken = accessToken,
            serializer = UserProfileDto.serializer()
        )

    override suspend fun getHistory(accessToken: String): HistoryEnvelopeDto =
        getJson(
            path = "/me/history",
            accessToken = accessToken,
            serializer = HistoryEnvelopeDto.serializer()
        )

    override suspend fun bootstrapHistory(
        accessToken: String,
        request: BootstrapHistoryRequestDto
    ): BootstrapHistoryResponseDto =
        postJson(
            path = "/me/history/bootstrap",
            accessToken = accessToken,
            body = json.encodeToString(BootstrapHistoryRequestDto.serializer(), request),
            serializer = BootstrapHistoryResponseDto.serializer()
        )

    override suspend fun updateHistory(
        accessToken: String,
        request: UpdateHistoryRequestDto
    ): HistoryEnvelopeDto =
        putJson(
            path = "/me/history",
            accessToken = accessToken,
            body = json.encodeToString(UpdateHistoryRequestDto.serializer(), request),
            serializer = HistoryEnvelopeDto.serializer()
        )

    private fun <T> getJson(
        path: String,
        accessToken: String,
        serializer: DeserializationStrategy<T>
    ): T = executeJsonRequest(
        method = "GET",
        path = path,
        accessToken = accessToken,
        body = null,
        serializer = serializer
    )

    private fun <T> postJson(
        path: String,
        accessToken: String,
        body: String,
        serializer: DeserializationStrategy<T>
    ): T = executeJsonRequest(
        method = "POST",
        path = path,
        accessToken = accessToken,
        body = body,
        serializer = serializer
    )

    private fun <T> putJson(
        path: String,
        accessToken: String,
        body: String,
        serializer: DeserializationStrategy<T>
    ): T = executeJsonRequest(
        method = "PUT",
        path = path,
        accessToken = accessToken,
        body = body,
        serializer = serializer
    )

    private fun <T> executeJsonRequest(
        method: String,
        path: String,
        accessToken: String,
        body: String?,
        serializer: DeserializationStrategy<T>
    ): T {
        val connection = openConnection(path).apply {
            requestMethod = method
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${accessToken.trim()}")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            body?.let { payload ->
                connection.outputStream.use { it.write(payload.encodeToByteArray()) }
            }

            val statusCode = connection.responseCode
            val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (statusCode in 200..299) {
                return decode(serializer, responseBody)
            }

            throw mapError(statusCode, responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(path: String): HttpURLConnection =
        connectionFactory(baseUrl.trimEnd('/') + path).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
        }

    private fun <T> decode(serializer: DeserializationStrategy<T>, rawBody: String): T = try {
        if (rawBody.isBlank()) {
            throw SerializationException("Blank response body")
        }
        json.decodeFromString(serializer, rawBody)
    } catch (_: SerializationException) {
        throw IOException("History sync server returned an unreadable response.")
    } catch (_: IllegalArgumentException) {
        throw IOException("History sync server returned an unreadable response.")
    }

    private fun mapError(statusCode: Int, rawBody: String): IOException {
        val message = extractReadableMessage(rawBody, fallback = fallbackMessageForStatus(statusCode))
        if (statusCode == 409) {
            parseConflict(rawBody, message)?.let { conflict ->
                return HistorySyncConflictException(conflict, message)
            }
        }
        return IOException(message)
    }

    private fun parseConflict(rawBody: String, message: String): HistorySyncConflictDto? {
        val trimmed = rawBody.trim()
        if (trimmed.isBlank()) {
            return null
        }

        val jsonElement = runCatching { json.parseToJsonElement(trimmed) }.getOrNull() ?: return null
        val jsonObject = jsonElement as? JsonObject ?: return null

        val revision = jsonObject["revision"]?.let { element ->
            (element as? JsonPrimitive)?.contentOrNull?.toLongOrNull()
        } ?: return null

        val snapshotElement = jsonObject["snapshot"] ?: return null
        val snapshot = runCatching {
            json.decodeFromJsonElement(HistorySnapshotPayload.serializer(), snapshotElement)
        }.getOrNull() ?: return null

        val schemaVersion = jsonObject["schemaVersion"]?.let { element ->
            (element as? JsonPrimitive)?.contentOrNull?.toLongOrNull()?.toInt()
        } ?: SchemaVersion

        return HistorySyncConflictDto(
            schemaVersion = schemaVersion,
            revision = revision,
            snapshot = snapshot,
            message = message
        )
    }

    private fun fallbackMessageForStatus(statusCode: Int): String = when (statusCode) {
        401 -> "需要登录后才能同步云端历史。"
        403 -> "当前账号没有权限访问云端历史。"
        404 -> "云端历史不存在。"
        409 -> "云端历史版本已更新，请重新同步。"
        else -> "网络请求失败，请稍后重试。"
    }

    private fun extractReadableMessage(rawBody: String, fallback: String): String {
        val trimmedBody = rawBody.trim()
        if (trimmedBody.isBlank()) {
            return fallback
        }

        extractJsonMessage(trimmedBody)?.let { return it }

        if (trimmedBody.startsWith("<")) {
            return fallback
        }

        return trimmedBody.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?: fallback
    }

    private fun extractJsonMessage(rawBody: String): String? {
        val jsonElement = runCatching { json.parseToJsonElement(rawBody) }.getOrNull() ?: return null
        val objectElement = jsonElement as? JsonObject ?: return null

        return listOf(
            objectElement["message"],
            objectElement["detail"],
            objectElement["title"],
            objectElement["error"]
        ).firstNotNullOfOrNull { elementToMessage(it) }
    }

    private fun elementToMessage(element: JsonElement?): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        is JsonObject -> listOf(
            element["message"],
            element["detail"],
            element["title"]
        ).firstNotNullOfOrNull { elementToMessage(it) }
        else -> null
    }
}

class HistorySyncConflictException(
    val conflict: HistorySyncConflictDto,
    message: String
) : IOException(message)
