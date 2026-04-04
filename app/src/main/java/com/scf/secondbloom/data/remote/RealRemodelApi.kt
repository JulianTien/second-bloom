package com.scf.secondbloom.data.remote

import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsResponseDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansResponseDto
import com.scf.secondbloom.data.remote.dto.RemodelPreviewJobDto
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class RealRemodelApi(
    private val baseUrl: String,
    private val openImageStream: (String) -> InputStream?,
    private val prepareImageUpload: (AnalyzeGarmentRequestDto) -> PreparedImageUpload =
        ImageUploadPreprocessor(openImageStream)::prepareAnalyzeUpload,
    private val connectionFactory: (String) -> HttpURLConnection = { url ->
        URL(url).openConnection() as HttpURLConnection
    },
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) : RemodelApi {

    private enum class ErrorMappingMode {
        IMAGE_UPLOAD,
        JSON_API,
        READ_ONLY
    }

    override suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto {
        return withContext(Dispatchers.IO) {
            val preparedUpload = prepareImageUpload(request)
            val response = performMultipartRequest(
                endpoint = "/analyze-garment",
                errorMappingMode = ErrorMappingMode.IMAGE_UPLOAD,
                textParts = mapOf(
                    "fileName" to preparedUpload.fileName,
                    "mimeType" to preparedUpload.mimeType,
                    "fileSizeBytes" to preparedUpload.fileSizeBytes.toString(),
                    "responseLanguage" to request.responseLanguage
                ),
                filePartName = "image",
                fileName = preparedUpload.fileName,
                mimeType = preparedUpload.mimeType,
                fileBytes = preparedUpload.bytes
            )
            decodeResponse(AnalyzeGarmentResponseDto.serializer(), response)
        }
    }

    override suspend fun generatePlans(
        request: GenerateRemodelPlansRequestDto
    ): GenerateRemodelPlansResponseDto {
        return withContext(Dispatchers.IO) {
            val response = performJsonRequest(
                endpoint = "/generate-remodel-plans",
                errorMappingMode = ErrorMappingMode.JSON_API,
                payload = json.encodeToString(GenerateRemodelPlansRequestDto.serializer(), request)
            )
            decodeResponse(GenerateRemodelPlansResponseDto.serializer(), response)
        }
    }

    override suspend fun createPreviewJob(
        request: GenerateRemodelPreviewJobsRequestDto
    ): GenerateRemodelPreviewJobsResponseDto {
        return withContext(Dispatchers.IO) {
            val response = performJsonRequest(
                endpoint = "/generate-remodel-preview-jobs",
                errorMappingMode = ErrorMappingMode.JSON_API,
                payload = json.encodeToString(
                    GenerateRemodelPreviewJobsRequestDto.serializer(),
                    request
                )
            )
            decodeResponse(GenerateRemodelPreviewJobsResponseDto.serializer(), response)
        }
    }

    override suspend fun getPreviewJob(previewJobId: String): RemodelPreviewJobDto {
        return withContext(Dispatchers.IO) {
            val response = performGetRequest(endpoint = "/remodel-preview-jobs/$previewJobId")
            decodeResponse(RemodelPreviewJobDto.serializer(), response)
        }
    }

    private fun performJsonRequest(
        endpoint: String,
        payload: String,
        errorMappingMode: ErrorMappingMode
    ): String {
        val connection = openConnection(endpoint).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        return connection.useAndRead(errorMappingMode) { http ->
            http.outputStream.use { it.write(payload.encodeToByteArray()) }
        }
    }

    private fun performGetRequest(endpoint: String): String {
        val connection = openConnection(endpoint).apply {
            requestMethod = "GET"
        }
        return connection.useAndRead(ErrorMappingMode.READ_ONLY) { }
    }

    private fun performMultipartRequest(
        endpoint: String,
        errorMappingMode: ErrorMappingMode,
        textParts: Map<String, String>,
        filePartName: String,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray
    ): String {
        val boundary = "SecondBloomBoundary-${UUID.randomUUID()}"
        val connection = openConnection(endpoint).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }
        return connection.useAndRead(errorMappingMode) { http ->
            http.outputStream.use { output ->
                textParts.forEach { (name, value) ->
                    output.write("--$boundary\r\n".encodeToByteArray())
                    output.write(
                        "Content-Disposition: form-data; name=\"$name\"\r\n\r\n".encodeToByteArray()
                    )
                    output.write(value.encodeToByteArray())
                    output.write("\r\n".encodeToByteArray())
                }

                output.write("--$boundary\r\n".encodeToByteArray())
                output.write(
                    "Content-Disposition: form-data; name=\"$filePartName\"; filename=\"$fileName\"\r\n".encodeToByteArray()
                )
                output.write("Content-Type: $mimeType\r\n\r\n".encodeToByteArray())
                output.write(fileBytes)
                output.write("\r\n--$boundary--\r\n".encodeToByteArray())
            }
        }
    }

    private fun openConnection(endpoint: String): HttpURLConnection =
        connectionFactory(baseUrl.trimEnd('/') + endpoint).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
        }

    private inline fun HttpURLConnection.useAndRead(
        errorMappingMode: ErrorMappingMode,
        block: (HttpURLConnection) -> Unit
    ): String {
        try {
            block(this)
            val statusCode = responseCode
            val rawBody = (if (statusCode in 200..299) inputStream else errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (statusCode in 200..299) {
                return rawBody
            }

            val message = extractReadableMessage(
                rawBody = rawBody,
                fallback = fallbackMessageForStatus(statusCode)
            )

            when (statusCode) {
                400, 415 -> when (errorMappingMode) {
                    ErrorMappingMode.IMAGE_UPLOAD -> throw InvalidImageException(message)
                    ErrorMappingMode.JSON_API,
                    ErrorMappingMode.READ_ONLY -> throw ModelResponseException(message)
                }
                422 -> throw ModelResponseException(message)
                else -> throw IOException(message)
            }
        } finally {
            disconnect()
        }
    }

    private fun <T> decodeResponse(
        serializer: DeserializationStrategy<T>,
        rawBody: String
    ): T = try {
        if (rawBody.isBlank()) {
            throw SerializationException("Blank response body")
        }
        json.decodeFromString(serializer, rawBody)
    } catch (_: SerializationException) {
        throw ModelResponseException("服务端返回了无法识别的结果，请稍后重试。")
    } catch (_: IllegalArgumentException) {
        throw ModelResponseException("服务端返回了无法识别的结果，请稍后重试。")
    }

    private fun fallbackMessageForStatus(statusCode: Int): String = when (statusCode) {
        400, 415 -> "当前文件不是可识别的衣物图片，请重新选择。"
        422 -> "服务端无法生成有效结果，请稍后重试。"
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
