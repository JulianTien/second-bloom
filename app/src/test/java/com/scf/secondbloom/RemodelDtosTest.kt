package com.scf.secondbloom.data.remote.dto

import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.PreviewEditFidelity
import com.scf.secondbloom.domain.model.PreviewEditLength
import com.scf.secondbloom.domain.model.PreviewEditNeckline
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewEditSilhouette
import com.scf.secondbloom.domain.model.PreviewEditSleeve
import com.scf.secondbloom.domain.model.PreviewJobStatus
import com.scf.secondbloom.domain.model.ProcessingWarningCode
import com.scf.secondbloom.domain.model.RemodelDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RemodelDtosTest {

    @Test
    fun garmentAnalysisDto_toDomain_mapsUnknownWarningsSafely() {
        val domain = GarmentAnalysisDto(
            analysisId = "analysis-1",
            garmentType = "衬衫",
            color = "白色",
            material = "棉质",
            style = "简约",
            defects = emptyList(),
            backgroundComplexity = "high",
            confidence = 0.68f,
            warnings = listOf(
                ProcessingWarningDto(
                    code = "future_warning_code",
                    message = "未来新增警告"
                )
            )
        ).toDomain()

        assertEquals(BackgroundComplexity.HIGH, domain.backgroundComplexity)
        assertEquals(ProcessingWarningCode.UNKNOWN, domain.warnings.first().code)
    }

    @Test
    fun remodelPlanDto_toDomain_defaultsUnknownDifficultyToMedium() {
        val domain = RemodelPlanDto(
            planId = "",
            title = "计划",
            summary = "说明",
            difficulty = "expert",
            materials = listOf("剪刀"),
            estimatedTime = "2 小时",
            steps = listOf(RemodelStepDto(title = "步骤一", detail = "处理衣片"))
        ).toDomain()

        assertEquals(RemodelDifficulty.MEDIUM, domain.difficulty)
        assertEquals(true, domain.planId.startsWith("plan-"))
    }

    @Test
    fun previewJobDto_toDomain_mapsQueuedStatusSafely() {
        val domain = RemodelPreviewJobDto(
            previewJobId = "preview-job-1",
            analysisId = "analysis-1",
            renderMode = "simulation",
            status = "queued",
            requestedPlanCount = 1,
            completedPlanCount = 0,
            failedPlanCount = 0,
            results = emptyList()
        ).toDomain()

        assertEquals(PreviewJobStatus.QUEUED, domain.status)
    }

    @Test
    fun previewEditOptions_toDto_usesStableWireValues() {
        val json = Json.encodeToString(
            GenerateRemodelPreviewJobsRequestDto.serializer(),
            GenerateRemodelPreviewJobsRequestDto(
                analysisId = "analysis-1",
                planId = "plan-1",
                tuning = PreviewEditOptions(
                    silhouette = PreviewEditSilhouette.ASYMMETRIC,
                    length = PreviewEditLength.CROPPED,
                    neckline = PreviewEditNeckline.V_NECK,
                    sleeve = PreviewEditSleeve.SLEEVELESS,
                    fidelity = PreviewEditFidelity.BALANCED,
                    extraInstructions = "  保留旧衣纹理  "
                ).toDto()
            )
        )

        assertTrue(json.contains("\"tuning\""))
        assertTrue(json.contains("\"silhouette\":\"asymmetric\""))
        assertTrue(json.contains("\"length\":\"cropped\""))
        assertTrue(json.contains("\"neckline\":\"v_neck\""))
        assertTrue(json.contains("\"sleeve\":\"sleeveless\""))
        assertTrue(json.contains("\"fidelity\":\"balanced\""))
        assertTrue(json.contains("\"extraInstructions\":\"保留旧衣纹理\""))
    }
}
