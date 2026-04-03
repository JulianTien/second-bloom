package com.scf.loop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SelectedImage(
    val uri: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long? = null
)

@Serializable
enum class DemoScenario(
    val order: Int,
    val title: String,
    val description: String,
    val expectedOutcome: String,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val sizeBytes: Long = 180_000
) {
    NORMAL(
        order = 1,
        title = "正常识别",
        description = "主体清晰、背景干净，适合演示顺滑识别与方案生成。",
        expectedOutcome = "直接进入识别结果摘要，适合拍完整主流程。",
        fileName = "demo_plain_white_shirt.jpg"
    ),
    LOW_CONFIDENCE(
        order = 2,
        title = "复杂背景",
        description = "包含复杂背景与深色衣物，适合展示低置信度提醒与手动修正。",
        expectedOutcome = "会先出现低置信度提示，再进入人工确认流程。",
        fileName = "demo_messy_dark_hoodie.jpg",
        sizeBytes = 246_000
    ),
    NETWORK_ERROR(
        order = 3,
        title = "异常提示",
        description = "模拟网络异常，适合展示演示环境中的兜底反馈。",
        expectedOutcome = "识别阶段会返回网络异常提示。",
        fileName = "demo_network_jean_jacket.jpg",
        sizeBytes = 210_000
    );

    fun toSelectedImage(): SelectedImage = SelectedImage(
        uri = "demo://scenario/${name.lowercase()}",
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes
    )

    companion object {
        fun fromFileName(fileName: String?): DemoScenario? =
            entries.firstOrNull { it.fileName.equals(fileName, ignoreCase = true) }
    }
}

@Serializable
enum class BackgroundComplexity {
    LOW,
    HIGH;

    companion object {
        fun fromWire(value: String): BackgroundComplexity =
            if (value.equals("high", ignoreCase = true)) HIGH else LOW
    }
}

@Serializable
enum class ProcessingWarningCode {
    COMPLEX_BACKGROUND,
    LOW_CONFIDENCE,
    BLURRY_IMAGE,
    MULTIPLE_GARMENTS,
    UNKNOWN_OBJECT,
    UNKNOWN;

    companion object {
        fun fromWire(value: String): ProcessingWarningCode = when (value.lowercase()) {
            "complex_background" -> COMPLEX_BACKGROUND
            "low_confidence" -> LOW_CONFIDENCE
            "blurry_image" -> BLURRY_IMAGE
            "multiple_garments" -> MULTIPLE_GARMENTS
            "unknown_object" -> UNKNOWN_OBJECT
            else -> UNKNOWN
        }
    }
}

@Serializable
data class ProcessingWarning(
    val code: ProcessingWarningCode,
    val message: String
)

@Serializable
data class GarmentDefect(
    val name: String,
    val severity: String? = null
)

@Serializable
data class GarmentAnalysis(
    val analysisId: String,
    val garmentType: String,
    val color: String,
    val material: String,
    val style: String,
    val defects: List<GarmentDefect>,
    val backgroundComplexity: BackgroundComplexity,
    val confidence: Float,
    val warnings: List<ProcessingWarning>
)

@Serializable
enum class RemodelIntent(val label: String) {
    DAILY("日常穿着改造"),
    OCCASION("特殊场合升级"),
    DIY("创意DIY改造"),
    SIZE_ADJUSTMENT("尺码调整")
}

@Serializable
enum class RemodelDifficulty(val label: String) {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("较难")
}

@Serializable
data class RemodelStep(
    val title: String,
    val detail: String
)

@Serializable
data class RemodelPlan(
    val title: String,
    val summary: String,
    val difficulty: RemodelDifficulty,
    val materials: List<String>,
    val estimatedTime: String,
    val steps: List<RemodelStep>
)

@Serializable
data class SavedAnalysisRecord(
    val recordId: String,
    val savedAtEpochMillis: Long,
    val sourceImage: SelectedImage,
    val analysis: GarmentAnalysis
)

@Serializable
data class SavedPlanGenerationRecord(
    val recordId: String,
    val savedAtEpochMillis: Long,
    val sourceImage: SelectedImage,
    val analysis: GarmentAnalysis,
    val intent: RemodelIntent,
    val userPreferences: String,
    val plans: List<RemodelPlan>
)

@Serializable
enum class RemodelStage {
    Idle,
    ImageSelected,
    Analyzing,
    LowConfidence,
    AnalysisReady,
    EditingAnalysis,
    GeneratingPlans,
    PlansReady,
    InvalidImage,
    NetworkError,
    ModelError
}

@Serializable
enum class RemodelErrorType {
    INVALID_IMAGE,
    NETWORK_ERROR,
    MODEL_ERROR
}

@Serializable
data class RemodelError(
    val type: RemodelErrorType,
    val message: String
)

data class RemodelUiState(
    val stage: RemodelStage = RemodelStage.Idle,
    val selectedImage: SelectedImage? = null,
    val analysis: GarmentAnalysis? = null,
    val draftAnalysis: GarmentAnalysis? = null,
    val selectedIntent: RemodelIntent? = null,
    val userPreferences: String = "",
    val plans: List<RemodelPlan> = emptyList(),
    val error: RemodelError? = null,
    val latestAnalysisRecord: SavedAnalysisRecord? = null,
    val latestPlanGenerationRecord: SavedPlanGenerationRecord? = null,
    val recentAnalysisRecords: List<SavedAnalysisRecord> = emptyList(),
    val recentPlanGenerationRecords: List<SavedPlanGenerationRecord> = emptyList()
) {
    val selectedDemoScenario: DemoScenario?
        get() = DemoScenario.fromFileName(selectedImage?.fileName)

    val isBusy: Boolean
        get() = stage == RemodelStage.Analyzing || stage == RemodelStage.GeneratingPlans

    val showLowConfidenceWarning: Boolean
        get() = stage == RemodelStage.LowConfidence

    val canAnalyze: Boolean
        get() = selectedImage != null && stage != RemodelStage.Analyzing

    val canGeneratePlans: Boolean
        get() = draftAnalysis != null &&
            selectedIntent != null &&
            stage != RemodelStage.GeneratingPlans
}
