package com.scf.loop.data.remote

import com.scf.loop.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.loop.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansResponseDto
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RealRemodelApi(
    private val baseUrl: String,
    private val openImageStream: (String) -> InputStream?,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) : RemodelApi {

    override suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto {
        val stream = openImageStream(request.imageUri)
            ?: throw InvalidImageException("无法读取所选图片，请重新选择一张衣物照片。")

        stream.use { imageStream ->
            val response = performMultipartRequest(
                endpoint = "/analyze-garment",
                textParts = mapOf(
                    "fileName" to request.fileName,
                    "mimeType" to request.mimeType,
                    "fileSizeBytes" to request.fileSizeBytes?.toString().orEmpty()
                ),
                filePartName = "image",
                fileName = request.fileName,
                mimeType = request.mimeType,
                fileBytes = imageStream.readBytes()
            )
            return json.decodeFromString(AnalyzeGarmentResponseDto.serializer(), response)
        }
    }

    override suspend fun generatePlans(
        request: GenerateRemodelPlansRequestDto
    ): GenerateRemodelPlansResponseDto {
        val response = performJsonRequest(
            endpoint = "/generate-remodel-plans",
            payload = json.encodeToString(GenerateRemodelPlansRequestDto.serializer(), request)
        )
        return json.decodeFromString(GenerateRemodelPlansResponseDto.serializer(), response)
    }

    private fun performJsonRequest(endpoint: String, payload: String): String {
        val connection = openConnection(endpoint).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        return connection.useAndRead { http ->
            http.outputStream.use { it.write(payload.encodeToByteArray()) }
        }
    }

    private fun performMultipartRequest(
        endpoint: String,
        textParts: Map<String, String>,
        filePartName: String,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray
    ): String {
        val boundary = "LoopBoundary-${UUID.randomUUID()}"
        val connection = openConnection(endpoint).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }
        return connection.useAndRead { http ->
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
        (URL(baseUrl.trimEnd('/') + endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
        }

    private inline fun HttpURLConnection.useAndRead(block: (HttpURLConnection) -> Unit): String {
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

            when (statusCode) {
                400, 415 -> throw InvalidImageException(rawBody.ifBlank {
                    "当前文件不是可识别的衣物图片，请重新选择。"
                })
                422 -> throw ModelResponseException(rawBody.ifBlank {
                    "服务端无法生成有效结果，请稍后重试。"
                })
                else -> throw IOException(rawBody.ifBlank {
                    "网络请求失败，请稍后重试。"
                })
            }
        } finally {
            disconnect()
        }
    }
}
