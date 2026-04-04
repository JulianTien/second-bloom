package com.scf.secondbloom.data.historysync

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySyncHttpClientTest {

    @Test
    fun bootstrapHistory_sendsBearerToken_andParsesResponse() = runTest {
        val connection = FakeHttpURLConnection(
            url = URL("https://secondbloom.test/me/history/bootstrap"),
            responseCodeValue = 200,
            successBody = """
                {
                  "schemaVersion": 1,
                  "revision": 7,
                  "snapshot": {
                    "analyses": [],
                    "planGenerations": [],
                    "publishedRemodels": [],
                    "inspirationEngagements": []
                  },
                  "mergeApplied": true
                }
            """.trimIndent()
        )
        val api = HttpHistorySyncApi(
            baseUrl = "https://secondbloom.test",
            connectionFactory = { connection }
        )

        val response = api.bootstrapHistory(
            accessToken = "token-123",
            request = BootstrapHistoryRequestDto(
                snapshot = HistorySnapshotPayload()
            )
        )

        assertEquals("POST", connection.requestMethodValue)
        assertEquals("Bearer token-123", connection.requestHeaders["Authorization"])
        assertTrue(connection.requestBodyAsText().contains("\"schemaVersion\":1"))
        assertEquals(7L, response.revision)
        assertTrue(response.mergeApplied)
    }

    @Test
    fun updateHistory_throwsConflictException_whenServerReturns409Payload() = runTest {
        val connection = FakeHttpURLConnection(
            url = URL("https://secondbloom.test/me/history"),
            responseCodeValue = 409,
            errorBody = """
                {
                  "message": "云端版本已更新",
                  "revision": 11,
                  "snapshot": {
                    "analyses": [],
                    "planGenerations": [],
                    "publishedRemodels": [],
                    "inspirationEngagements": []
                  }
                }
            """.trimIndent()
        )
        val api = HttpHistorySyncApi(
            baseUrl = "https://secondbloom.test",
            connectionFactory = { connection }
        )

        val exception = runCatching {
            api.updateHistory(
                accessToken = "token-123",
                request = UpdateHistoryRequestDto(
                    baseRevision = 10L,
                    snapshot = HistorySnapshotPayload()
                )
            )
        }.exceptionOrNull()

        assertTrue(exception is HistorySyncConflictException)
        val conflict = (exception as HistorySyncConflictException).conflict
        assertEquals(11L, conflict.revision)
        assertTrue(conflict.message?.contains("云端版本已更新") == true)
    }
}

private class FakeHttpURLConnection(
    url: URL,
    private val responseCodeValue: Int,
    private val successBody: String = "",
    private val errorBody: String? = null
) : HttpURLConnection(url) {

    private val requestBuffer = ByteArrayOutputStream()
    val requestHeaders: MutableMap<String, String> = linkedMapOf()
    var requestMethodValue: String = ""
        private set

    override fun setRequestProperty(key: String, value: String) {
        requestHeaders[key] = value
        super.setRequestProperty(key, value)
    }

    override fun setRequestMethod(method: String) {
        requestMethodValue = method
        super.setRequestMethod(method)
    }

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
