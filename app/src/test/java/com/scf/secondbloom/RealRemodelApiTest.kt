package com.scf.secondbloom.data.remote

import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.secondbloom.data.remote.dto.GarmentAnalysisDto
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RealRemodelApiTest {

    @Test
    fun analyzeGarment_usesFallbackInvalidImageMessage_whenErrorBodyIsMissing() = runTest {
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { ByteArrayInputStream("image".encodeToByteArray()) },
            prepareImageUpload = {
                PreparedImageUpload(
                    fileName = "prepared.jpg",
                    mimeType = "image/jpeg",
                    fileSizeBytes = 120_000,
                    bytes = "prepared-image".encodeToByteArray()
                )
            },
            connectionFactory = {
                FakeHttpURLConnection(
                    url = URL(it),
                    responseCodeValue = 400,
                    errorBody = null
                )
            }
        )

        val exception = expectFailure<InvalidImageException> {
            api.analyzeGarment(
                AnalyzeGarmentRequestDto(
                    imageUri = "content://secondbloom/image.jpg",
                    fileName = "image.jpg",
                    mimeType = "image/jpeg",
                    fileSizeBytes = 120_000
                )
            )
        }

        assertEquals("当前文件不是可识别的衣物图片，请重新选择。", exception.message)
    }

    @Test
    fun generatePlans_usesJsonErrorMessage_whenServerReturnsStructured422Payload() = runTest {
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { null },
            connectionFactory = {
                FakeHttpURLConnection(
                    url = URL(it),
                    responseCodeValue = 422,
                    errorBody = """{"error":{"message":"服务端暂时无法生成方案。"}}"""
                )
            }
        )

        val exception = expectFailure<ModelResponseException> {
            api.generatePlans(validGeneratePlansRequest())
        }

        assertEquals("服务端暂时无法生成方案。", exception.message)
    }

    @Test
    fun generatePlans_usesFallbackNetworkMessage_whenServerReturnsHtmlError() = runTest {
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { null },
            connectionFactory = {
                FakeHttpURLConnection(
                    url = URL(it),
                    responseCodeValue = 500,
                    errorBody = "<html><body>upstream failed</body></html>"
                )
            }
        )

        val exception = expectFailure<java.io.IOException> {
            api.generatePlans(validGeneratePlansRequest())
        }

        assertEquals("网络请求失败，请稍后重试。", exception.message)
    }

    @Test
    fun generatePlans_throwsModelResponseException_whenSuccessPayloadIsInvalid() = runTest {
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { null },
            connectionFactory = {
                FakeHttpURLConnection(
                    url = URL(it),
                    responseCodeValue = 200,
                    successBody = """{"plans":"not-an-array"}"""
                )
            }
        )

        val exception = expectFailure<ModelResponseException> {
            api.generatePlans(validGeneratePlansRequest())
        }

        assertEquals("服务端返回了无法识别的结果，请稍后重试。", exception.message)
    }

    @Test
    fun createPreviewJob_mapsBadRequestToModelResponseException_insteadOfInvalidImage() = runTest {
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { null },
            connectionFactory = {
                FakeHttpURLConnection(
                    url = URL(it),
                    responseCodeValue = 400,
                    errorBody = """{"message":"Extra inputs are not permitted"}"""
                )
            }
        )

        val exception = expectFailure<ModelResponseException> {
            api.createPreviewJob(
                GenerateRemodelPreviewJobsRequestDto(
                    analysisId = "analysis-1",
                    planId = "plan-1"
                )
            )
        }

        assertEquals("Extra inputs are not permitted", exception.message)
    }

    @Test
    fun analyzeGarment_usesPreparedUploadMetadataAndBytes() = runTest {
        val connection = FakeHttpURLConnection(
            url = URL("https://secondbloom.test/analyze-garment"),
            responseCodeValue = 200,
            successBody = """
                {
                  "analysis": {
                    "analysisId": "analysis-1",
                    "garmentType": "shirt",
                    "color": "white",
                    "material": "cotton",
                    "style": "simple",
                    "defects": [],
                    "backgroundComplexity": "low",
                    "confidence": 0.9,
                    "warnings": []
                  }
                }
            """.trimIndent()
        )
        val api = RealRemodelApi(
            baseUrl = "https://secondbloom.test",
            openImageStream = { null },
            prepareImageUpload = {
                PreparedImageUpload(
                    fileName = "compressed.jpg",
                    mimeType = "image/jpeg",
                    fileSizeBytes = 1234,
                    bytes = "prepared-image".encodeToByteArray()
                )
            },
            connectionFactory = { connection }
        )

        api.analyzeGarment(
            AnalyzeGarmentRequestDto(
                imageUri = "content://secondbloom/image.png",
                fileName = "image.png",
                mimeType = "image/png",
                fileSizeBytes = 9_999
            )
        )

        val requestBody = connection.requestBodyAsText()
        assertTrue(requestBody.contains("name=\"fileName\"\r\n\r\ncompressed.jpg"))
        assertTrue(requestBody.contains("name=\"mimeType\"\r\n\r\nimage/jpeg"))
        assertTrue(requestBody.contains("name=\"fileSizeBytes\"\r\n\r\n1234"))
        assertTrue(requestBody.contains("filename=\"compressed.jpg\""))
        assertTrue(requestBody.contains("prepared-image"))
    }

    private fun validGeneratePlansRequest() = GenerateRemodelPlansRequestDto(
        intent = "daily",
        confirmedAnalysis = GarmentAnalysisDto(
            analysisId = "analysis-1",
            garmentType = "白色衬衫",
            color = "白色",
            material = "棉质",
            style = "简约",
            defects = emptyList(),
            backgroundComplexity = "low",
            confidence = 0.92f,
            warnings = emptyList()
        ),
        userPreferences = "保留正式感"
    )
}

private class FakeHttpURLConnection(
    url: URL,
    private val responseCodeValue: Int,
    private val successBody: String = "",
    private val errorBody: String? = null
) : HttpURLConnection(url) {

    private val requestBuffer = ByteArrayOutputStream()

    override fun getResponseCode(): Int = responseCodeValue

    override fun getInputStream(): InputStream = ByteArrayInputStream(successBody.encodeToByteArray())

    override fun getErrorStream(): InputStream? = errorBody?.let {
        ByteArrayInputStream(it.encodeToByteArray())
    }

    override fun getOutputStream(): OutputStream = requestBuffer

    fun requestBodyAsText(): String = requestBuffer.toString(Charsets.UTF_8.name())

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit
}

private suspend inline fun <reified T : Throwable> expectFailure(
    block: suspend () -> Unit
): T {
    try {
        block()
    } catch (error: Throwable) {
        if (error is T) {
            return error
        }
        throw error
    }

    fail("Expected ${T::class.java.simpleName} to be thrown.")
    throw IllegalStateException("Unreachable")
}
