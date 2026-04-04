package com.scf.secondbloom.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageUploadPreprocessorTest {

    @Test
    fun targetDimensions_scalesLargeImageToLongEdgeLimit() {
        val (targetWidth, targetHeight) = UploadCompressionPolicy.targetDimensions(
            width = 4032,
            height = 3024
        )

        assertEquals(1600, targetWidth)
        assertEquals(1200, targetHeight)
    }

    @Test
    fun calculateSampleSize_returnsPowerOfTwoForLargeSource() {
        val sampleSize = UploadCompressionPolicy.calculateSampleSize(
            width = 4032,
            height = 3024,
            targetWidth = 1600,
            targetHeight = 1200
        )

        assertEquals(2, sampleSize)
    }

    @Test
    fun normalizedJpegFileName_rewritesExtensionToJpg() {
        assertEquals(
            "holiday-look.jpg",
            UploadCompressionPolicy.normalizedJpegFileName("holiday-look.PNG")
        )
    }

    @Test
    fun chooseQuality_fallsBackToLowestConfiguredQualityWhenAllSizesExceedLimit() {
        val chosenQuality = UploadCompressionPolicy.chooseQuality(
            encodedSizes = mapOf(
                82 to 5_200_000,
                76 to 4_900_000,
                70 to 4_600_000,
                64 to 4_300_000,
                60 to 4_250_000
            )
        )

        assertEquals(60, chosenQuality)
    }
}
