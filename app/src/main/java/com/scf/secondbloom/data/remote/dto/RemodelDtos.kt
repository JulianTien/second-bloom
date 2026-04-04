package com.scf.secondbloom.data.remote.dto

import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.GeneratePreviewJobResult
import com.scf.secondbloom.domain.model.PlanPreviewResult
import com.scf.secondbloom.domain.model.PreviewAsset
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewJobSnapshot
import com.scf.secondbloom.domain.model.PreviewJobStatus
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.ProcessingWarning
import com.scf.secondbloom.domain.model.ProcessingWarningCode
import com.scf.secondbloom.domain.model.RemodelDifficulty
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStep
import com.scf.secondbloom.domain.model.SelectedImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeGarmentRequestDto(
    val imageUri: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long? = null,
    val responseLanguage: String = "en"
)

@Serializable
data class AnalyzeGarmentResponseDto(
    val analysis: GarmentAnalysisDto
)

@Serializable
data class GarmentAnalysisDto(
    val analysisId: String,
    val garmentType: String,
    val color: String,
    val material: String,
    val style: String,
    val defects: List<GarmentDefectDto>,
    val backgroundComplexity: String,
    val confidence: Float,
    val warnings: List<ProcessingWarningDto>
)

@Serializable
data class GarmentDefectDto(
    val name: String,
    val severity: String? = null
)

@Serializable
data class ProcessingWarningDto(
    val code: String,
    val message: String
)

@Serializable
data class GenerateRemodelPlansRequestDto(
    val intent: String,
    val confirmedAnalysis: GarmentAnalysisDto,
    val userPreferences: String? = null,
    val responseLanguage: String = "en"
)

@Serializable
data class GenerateRemodelPlansResponseDto(
    val plans: List<RemodelPlanDto>,
    val reasoningNote: String? = null
)

@Serializable
data class RemodelPlanDto(
    val planId: String = "",
    val title: String,
    val summary: String,
    val difficulty: String,
    val materials: List<String>,
    val estimatedTime: String,
    val steps: List<RemodelStepDto>
)

@Serializable
data class RemodelStepDto(
    val title: String,
    val detail: String
)

@Serializable
data class GenerateRemodelPreviewJobsRequestDto(
    val analysisId: String,
    val planId: String,
    @SerialName("tuning")
    val tuning: PreviewEditTuningDto? = null,
    val renderMode: String = "simulation"
)

@Serializable
data class PreviewEditTuningDto(
    val silhouette: String,
    val length: String,
    val neckline: String,
    val sleeve: String,
    val fidelity: String,
    val extraInstructions: String? = null
)

@Serializable
data class GenerateRemodelPreviewJobsResponseDto(
    val previewJobId: String,
    val status: String,
    val requestedPlanCount: Int,
    val pollPath: String
)

@Serializable
data class RemodelPreviewJobDto(
    val previewJobId: String,
    val analysisId: String,
    val renderMode: String,
    val status: String,
    val requestedPlanCount: Int,
    val completedPlanCount: Int,
    val failedPlanCount: Int,
    val pollPath: String? = null,
    val results: List<PreviewPlanRenderResultDto> = emptyList()
)

@Serializable
data class PreviewPlanRenderResultDto(
    val planId: String,
    val renderStatus: String,
    val beforeImage: PreviewAssetDto? = null,
    val afterImage: PreviewAssetDto? = null,
    val comparisonImage: PreviewAssetDto? = null,
    val disclaimer: String = "",
    val errorMessage: String? = null
)

@Serializable
data class PreviewAssetDto(
    val assetId: String,
    val url: String,
    val expiresAt: String
)

fun SelectedImage.toAnalyzeRequestDto(responseLanguage: AppLanguage): AnalyzeGarmentRequestDto = AnalyzeGarmentRequestDto(
    imageUri = uri,
    fileName = fileName,
    mimeType = mimeType,
    fileSizeBytes = sizeBytes,
    responseLanguage = responseLanguage.wireValue
)

fun GarmentAnalysis.toDto(): GarmentAnalysisDto = GarmentAnalysisDto(
    analysisId = analysisId,
    garmentType = garmentType,
    color = color,
    material = material,
    style = style,
    defects = defects.map { GarmentDefectDto(name = it.name, severity = it.severity) },
    backgroundComplexity = backgroundComplexity.name.lowercase(),
    confidence = confidence,
    warnings = warnings.map { ProcessingWarningDto(code = it.code.name.lowercase(), message = it.message) }
)

fun GarmentAnalysisDto.toDomain(): GarmentAnalysis = GarmentAnalysis(
    analysisId = analysisId,
    garmentType = garmentType,
    color = color,
    material = material,
    style = style,
    defects = defects.map { GarmentDefect(name = it.name, severity = it.severity) },
    backgroundComplexity = BackgroundComplexity.fromWire(backgroundComplexity),
    confidence = confidence.coerceIn(0f, 1f),
    warnings = warnings.map {
        ProcessingWarning(
            code = ProcessingWarningCode.fromWire(it.code),
            message = it.message
        )
    }
)

fun RemodelIntent.toRequestString(): String = name.lowercase()

fun RemodelPlanDto.toDomain(): RemodelPlan = RemodelPlan(
    planId = planId.ifBlank { "plan-${title.hashCode().toUInt()}" },
    title = title,
    summary = summary,
    difficulty = when (difficulty.lowercase()) {
        "easy" -> RemodelDifficulty.EASY
        "hard" -> RemodelDifficulty.HARD
        else -> RemodelDifficulty.MEDIUM
    },
    materials = materials,
    estimatedTime = estimatedTime,
    steps = steps.map { RemodelStep(title = it.title, detail = it.detail) }
)

fun GenerateRemodelPreviewJobsResponseDto.toDomain(): GeneratePreviewJobResult = GeneratePreviewJobResult(
    previewJobId = previewJobId,
    status = status,
    requestedPlanCount = requestedPlanCount,
    pollPath = pollPath
)

fun RemodelPreviewJobDto.toDomain(): PreviewJobSnapshot = PreviewJobSnapshot(
    previewJobId = previewJobId,
    analysisId = analysisId,
    status = PreviewJobStatus.fromWire(status),
    requestedPlanCount = requestedPlanCount,
    completedPlanCount = completedPlanCount,
    failedPlanCount = failedPlanCount,
    results = results.map { it.toDomain() },
    pollPath = pollPath
)

fun PreviewPlanRenderResultDto.toDomain(): PlanPreviewResult = PlanPreviewResult(
    planId = planId,
    renderStatus = PreviewRenderStatus.fromWire(renderStatus),
    beforeImage = beforeImage?.toDomain(),
    afterImage = afterImage?.toDomain(),
    comparisonImage = comparisonImage?.toDomain(),
    disclaimer = disclaimer,
    errorMessage = errorMessage
)

fun PreviewAssetDto.toDomain(): PreviewAsset = PreviewAsset(
    assetId = assetId,
    url = url,
    expiresAt = expiresAt
)

fun PreviewEditOptions.toDto(): PreviewEditTuningDto = PreviewEditTuningDto(
    silhouette = silhouette.wireValue,
    length = length.wireValue,
    neckline = neckline.wireValue,
    sleeve = sleeve.wireValue,
    fidelity = fidelity.wireValue,
    extraInstructions = extraInstructions.trim().ifBlank { null }
)
