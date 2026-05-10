package com.scf.secondbloom.ui.preview

import com.scf.secondbloom.BuildConfig
import com.scf.secondbloom.domain.model.PreviewAsset
import java.net.URI

private const val VERCEL_BLOB_HOST_SUFFIX = ".blob.vercel-storage.com"

fun PreviewAsset.displayUrl(): String =
    previewAssetDisplayUrl(
        assetId = assetId,
        url = url,
        backendBaseUrl = BuildConfig.REMODEL_API_BASE_URL
    ) ?: url

fun previewAssetDisplayUrl(
    assetId: String?,
    url: String?,
    backendBaseUrl: String = BuildConfig.REMODEL_API_BASE_URL
): String? {
    val normalizedUrl = url?.takeIf { it.isNotBlank() } ?: return null
    val normalizedAssetId = assetId?.takeIf { it.isNotBlank() } ?: return normalizedUrl
    val baseUrl = backendBaseUrl.trim().trimEnd('/').takeIf { it.isNotBlank() } ?: return normalizedUrl

    return if (normalizedUrl.isVercelBlobUrl()) {
        "$baseUrl/preview-assets/$normalizedAssetId"
    } else {
        normalizedUrl
    }
}

private fun String.isVercelBlobUrl(): Boolean {
    val host = runCatching { URI(this).host }.getOrNull()?.lowercase() ?: return false
    return host.endsWith(VERCEL_BLOB_HOST_SUFFIX)
}
