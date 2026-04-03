package com.scf.loop.data.remote.dto

import com.scf.loop.domain.model.BackgroundComplexity
import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.GarmentDefect
import com.scf.loop.domain.model.ProcessingWarning
import com.scf.loop.domain.model.ProcessingWarningCode
import com.scf.loop.domain.model.RemodelDifficulty
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.RemodelStep
import com.scf.loop.domain.model.SelectedImage
import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeGarmentRequestDto(
    val imageUri: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long? = null
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
    val userPreferences: String? = null
)

@Serializable
data class GenerateRemodelPlansResponseDto(
    val plans: List<RemodelPlanDto>,
    val reasoningNote: String? = null
)

@Serializable
data class RemodelPlanDto(
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

fun SelectedImage.toAnalyzeRequestDto(): AnalyzeGarmentRequestDto = AnalyzeGarmentRequestDto(
    imageUri = uri,
    fileName = fileName,
    mimeType = mimeType,
    fileSizeBytes = sizeBytes
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
