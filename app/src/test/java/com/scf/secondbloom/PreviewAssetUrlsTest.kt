package com.scf.secondbloom

import com.scf.secondbloom.ui.preview.previewAssetDisplayUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewAssetUrlsTest {
    @Test
    fun previewAssetDisplayUrl_rewritesVercelBlobUrl_toBackendProxyUrl() {
        val result = previewAssetDisplayUrl(
            assetId = "asset-after-1",
            url = "https://store.public.blob.vercel-storage.com/preview/after.png",
            backendBaseUrl = "https://api.example"
        )

        assertEquals("https://api.example/preview-assets/asset-after-1", result)
    }

    @Test
    fun previewAssetDisplayUrl_keepsNonBlobUrl() {
        val result = previewAssetDisplayUrl(
            assetId = "asset-after-1",
            url = "https://cdn.example/preview/after.png",
            backendBaseUrl = "https://api.example"
        )

        assertEquals("https://cdn.example/preview/after.png", result)
    }
}
