package com.scf.secondbloom.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object RemotePreviewImageCache {
    private val images = ConcurrentHashMap<String, ImageBitmap>()

    fun get(url: String): ImageBitmap? = images[url]

    fun put(url: String, image: ImageBitmap) {
        images[url] = image
    }
}

@Composable
fun RemotePreviewImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val image by produceState<ImageBitmap?>(initialValue = RemotePreviewImageCache.get(imageUrl), imageUrl) {
        if (value == null) {
            value = loadRemotePreviewImage(imageUrl)
        }
    }

    image?.let {
        Image(
            bitmap = it,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

private suspend fun loadRemotePreviewImage(imageUrl: String): ImageBitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                setRequestProperty("Accept", "image/*")
            }
            try {
                connection.inputStream.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }?.also { RemotePreviewImageCache.put(imageUrl, it) }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
