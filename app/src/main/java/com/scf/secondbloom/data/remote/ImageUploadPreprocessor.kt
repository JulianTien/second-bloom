package com.scf.secondbloom.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentRequestDto
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class PreparedImageUpload(
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val bytes: ByteArray
)

internal object UploadCompressionPolicy {
    const val maxLongEdgePx: Int = 1600
    const val maxUploadBytes: Int = 4 * 1024 * 1024
    val jpegQualitySteps: IntArray = intArrayOf(82, 76, 70, 64, 60)

    fun normalizedJpegFileName(fileName: String): String {
        val trimmed = fileName.trim().ifBlank { "selected-image" }
        val dotIndex = trimmed.lastIndexOf('.')
        val baseName = if (dotIndex > 0) trimmed.substring(0, dotIndex) else trimmed
        return "$baseName.jpg"
    }

    fun targetDimensions(width: Int, height: Int): Pair<Int, Int> {
        if (width <= 0 || height <= 0) {
            return 0 to 0
        }

        val longEdge = max(width, height)
        if (longEdge <= maxLongEdgePx) {
            return width to height
        }

        val scale = maxLongEdgePx.toFloat() / longEdge.toFloat()
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
        return targetWidth to targetHeight
    }

    fun calculateSampleSize(
        width: Int,
        height: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var sampleSize = 1
        if (targetWidth <= 0 || targetHeight <= 0) {
            return sampleSize
        }

        var halfWidth = width / 2
        var halfHeight = height / 2
        while (halfWidth / sampleSize >= targetWidth && halfHeight / sampleSize >= targetHeight) {
            sampleSize *= 2
            halfWidth = width / 2
            halfHeight = height / 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    fun chooseQuality(encodedSizes: Map<Int, Int>): Int {
        for (quality in jpegQualitySteps) {
            val size = encodedSizes[quality] ?: continue
            if (size <= maxUploadBytes) {
                return quality
            }
        }
        return jpegQualitySteps.last()
    }
}

internal class ImageUploadPreprocessor(
    private val openImageStream: (String) -> InputStream?
) {
    fun prepareAnalyzeUpload(request: AnalyzeGarmentRequestDto): PreparedImageUpload {
        val sourceBytes = openImageStream(request.imageUri)
            ?.use { it.readBytes() }
            ?: throw InvalidImageException("无法读取所选图片，请重新选择一张衣物照片。")
        return prepareFromBytes(
            fileName = request.fileName,
            sourceBytes = sourceBytes
        )
    }

    internal fun prepareFromBytes(
        fileName: String,
        sourceBytes: ByteArray
    ): PreparedImageUpload {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw InvalidImageException("当前文件不是可识别的衣物图片，请重新选择。")
        }

        val (targetWidth, targetHeight) = UploadCompressionPolicy.targetDimensions(
            width = bounds.outWidth,
            height = bounds.outHeight
        )
        val sampleSize = UploadCompressionPolicy.calculateSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decodedBitmap = BitmapFactory.decodeByteArray(
            sourceBytes,
            0,
            sourceBytes.size,
            decodeOptions
        ) ?: throw InvalidImageException("当前文件不是可识别的衣物图片，请重新选择。")

        val scaledBitmap = if (
            decodedBitmap.width == targetWidth &&
            decodedBitmap.height == targetHeight
        ) {
            decodedBitmap
        } else {
            Bitmap.createScaledBitmap(decodedBitmap, targetWidth, targetHeight, true)
        }

        return try {
            val encodedByQuality = linkedMapOf<Int, ByteArray>()
            UploadCompressionPolicy.jpegQualitySteps.forEach { quality ->
                val output = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                encodedByQuality[quality] = output.toByteArray()
            }
            val selectedQuality = UploadCompressionPolicy.chooseQuality(
                encodedByQuality.mapValues { it.value.size }
            )
            val selectedBytes = encodedByQuality.getValue(selectedQuality)
            PreparedImageUpload(
                fileName = UploadCompressionPolicy.normalizedJpegFileName(fileName),
                mimeType = "image/jpeg",
                fileSizeBytes = selectedBytes.size.toLong(),
                bytes = selectedBytes
            )
        } finally {
            if (scaledBitmap !== decodedBitmap) {
                decodedBitmap.recycle()
            }
            scaledBitmap.recycle()
        }
    }
}
