package com.scf.secondbloom.domain.model

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
    val englishTitle: String,
    val chineseTitle: String,
    val englishDescription: String,
    val chineseDescription: String,
    val englishExpectedOutcome: String,
    val chineseExpectedOutcome: String,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val sizeBytes: Long = 180_000
) {
    NORMAL(
        order = 1,
        englishTitle = "Clean recognition",
        chineseTitle = "正常识别",
        englishDescription = "Clear subject and simple background for the smoothest full-flow demo.",
        chineseDescription = "主体清晰、背景干净，适合演示顺滑识别与方案生成。",
        englishExpectedOutcome = "Should move straight into the recognition summary and the main demo path.",
        chineseExpectedOutcome = "直接进入识别结果摘要，适合拍完整主流程。",
        fileName = "demo_plain_white_shirt.jpg"
    ),
    LOW_CONFIDENCE(
        order = 2,
        englishTitle = "Complex background",
        chineseTitle = "复杂背景",
        englishDescription = "Dark garment with visual clutter, useful for low-confidence warnings and manual edits.",
        chineseDescription = "包含复杂背景与深色衣物，适合展示低置信度提醒与手动修正。",
        englishExpectedOutcome = "Should show a low-confidence warning before manual confirmation.",
        chineseExpectedOutcome = "会先出现低置信度提示，再进入人工确认流程。",
        fileName = "demo_messy_dark_hoodie.jpg",
        sizeBytes = 246_000
    ),
    NETWORK_ERROR(
        order = 3,
        englishTitle = "Failure state",
        chineseTitle = "异常提示",
        englishDescription = "Simulates a network issue so you can rehearse the fallback UI.",
        chineseDescription = "模拟网络异常，适合展示演示环境中的兜底反馈。",
        englishExpectedOutcome = "The analyze step should show a network error state.",
        chineseExpectedOutcome = "识别阶段会返回网络异常提示。",
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
enum class RemodelIntent(
    val englishLabel: String,
    val chineseLabel: String
) {
    DAILY("Daily wear refresh", "日常穿着改造"),
    OCCASION("Occasion upgrade", "特殊场合升级"),
    DIY("Creative DIY", "创意DIY改造"),
    SIZE_ADJUSTMENT("Size adjustment", "尺码调整")
}

@Serializable
enum class RemodelDifficulty(
    val englishLabel: String,
    val chineseLabel: String
) {
    EASY("Easy", "简单"),
    MEDIUM("Medium", "中等"),
    HARD("Hard", "较难")
}

@Serializable
data class RemodelStep(
    val title: String,
    val detail: String
)

@Serializable
data class RemodelPlan(
    val planId: String = "",
    val title: String,
    val summary: String,
    val difficulty: RemodelDifficulty,
    val materials: List<String>,
    val estimatedTime: String,
    val steps: List<RemodelStep>
)

@Serializable
enum class PreviewEditSilhouette(
    val englishLabel: String,
    val chineseLabel: String,
    val wireValue: String
) {
    PRESERVE("Preserve silhouette", "尽量保持原版型", "preserve"),
    RELAXED("Relaxed", "更宽松", "relaxed"),
    FITTED("Fitted", "更利落合身", "fitted"),
    STRUCTURED("Structured", "更有结构感", "structured"),
    ASYMMETRIC("Asymmetric", "尝试不对称", "asymmetric")
}

@Serializable
enum class PreviewEditLength(
    val englishLabel: String,
    val chineseLabel: String,
    val wireValue: String
) {
    KEEP("Keep length", "保持长度", "keep"),
    CROPPED("Shorter", "更短一些", "cropped"),
    TUNIC("Longer", "更长一些", "longer"),
    WAIST("Around waist", "腰线附近", "waist"),
    HIP("To hip", "落到胯部", "hip")
}

@Serializable
enum class PreviewEditNeckline(
    val englishLabel: String,
    val chineseLabel: String,
    val wireValue: String
) {
    KEEP("Keep neckline", "保持领口", "keep"),
    ROUND("Round neck", "圆领", "round"),
    V_NECK("V-neck", "V 领", "v_neck"),
    SQUARE("Square neck", "方领", "square"),
    OPEN("More open", "更开阔", "open")
}

@Serializable
enum class PreviewEditSleeve(
    val englishLabel: String,
    val chineseLabel: String,
    val wireValue: String
) {
    KEEP("Keep sleeves", "保持袖型", "keep"),
    SLEEVELESS("Sleeveless", "无袖", "sleeveless"),
    SHORTER("Shorter sleeves", "更短袖", "shorter"),
    ROLLED("Rolled sleeves", "卷边袖", "rolled"),
    CAP("Cap sleeve", "盖袖", "cap")
}

@Serializable
enum class PreviewEditFidelity(
    val englishLabel: String,
    val chineseLabel: String,
    val wireValue: String
) {
    STRICT("High fidelity", "高保真", "strict"),
    BALANCED("Balanced", "平衡", "balanced"),
    CREATIVE("Creative first", "创意优先", "creative")
}

@Serializable
data class PreviewEditOptions(
    val silhouette: PreviewEditSilhouette = PreviewEditSilhouette.PRESERVE,
    val length: PreviewEditLength = PreviewEditLength.KEEP,
    val neckline: PreviewEditNeckline = PreviewEditNeckline.KEEP,
    val sleeve: PreviewEditSleeve = PreviewEditSleeve.KEEP,
    val fidelity: PreviewEditFidelity = PreviewEditFidelity.STRICT,
    val extraInstructions: String = ""
)

@Serializable
enum class PreviewJobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    COMPLETED_WITH_FAILURES,
    FAILED,
    EXPIRED;

    companion object {
        fun fromWire(value: String): PreviewJobStatus = when (value.lowercase()) {
            "running" -> RUNNING
            "completed" -> COMPLETED
            "completed_with_failures" -> COMPLETED_WITH_FAILURES
            "failed" -> FAILED
            "expired" -> EXPIRED
            else -> QUEUED
        }
    }
}

@Serializable
enum class PreviewRenderStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    FILTERED;

    companion object {
        fun fromWire(value: String): PreviewRenderStatus = when (value.lowercase()) {
            "running" -> RUNNING
            "completed" -> COMPLETED
            "failed" -> FAILED
            "filtered" -> FILTERED
            else -> QUEUED
        }
    }
}

@Serializable
data class PreviewAsset(
    val assetId: String,
    val url: String,
    val expiresAt: String
)

@Serializable
data class PlanPreviewResult(
    val planId: String,
    val renderStatus: PreviewRenderStatus,
    val beforeImage: PreviewAsset? = null,
    val afterImage: PreviewAsset? = null,
    val comparisonImage: PreviewAsset? = null,
    val disclaimer: String = "",
    val errorMessage: String? = null
)

@Serializable
data class PreviewJobSnapshot(
    val previewJobId: String,
    val analysisId: String,
    val status: PreviewJobStatus,
    val requestedPlanCount: Int,
    val completedPlanCount: Int,
    val failedPlanCount: Int,
    val results: List<PlanPreviewResult>,
    val pollPath: String? = null
)

@Serializable
data class GeneratePreviewJobResult(
    val previewJobId: String,
    val status: String,
    val requestedPlanCount: Int,
    val pollPath: String
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
data class PublishedRemodelRecord(
    val recordId: String,
    val publishedAtEpochMillis: Long,
    val sourceImage: SelectedImage,
    val analysis: GarmentAnalysis,
    val intent: RemodelIntent,
    val selectedPlan: RemodelPlan,
    val previewResult: PlanPreviewResult
)

@Serializable
data class InspirationComment(
    val commentId: String,
    val authorName: String,
    val message: String,
    val createdAtEpochMillis: Long
)

@Serializable
data class InspirationEngagementRecord(
    val itemId: String,
    val liked: Boolean = false,
    val bookmarked: Boolean = false,
    val likeCount: Int = 0,
    val comments: List<InspirationComment> = emptyList()
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
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val stage: RemodelStage = RemodelStage.Idle,
    val selectedImage: SelectedImage? = null,
    val analysis: GarmentAnalysis? = null,
    val draftAnalysis: GarmentAnalysis? = null,
    val selectedIntent: RemodelIntent? = null,
    val userPreferences: String = "",
    val plans: List<RemodelPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val editingPlanId: String? = null,
    val previewEditOptions: PreviewEditOptions = PreviewEditOptions(),
    val previewJob: PreviewJobSnapshot? = null,
    val isPreviewLoading: Boolean = false,
    val previewErrorMessage: String? = null,
    val error: RemodelError? = null,
    val latestAnalysisRecord: SavedAnalysisRecord? = null,
    val latestPlanGenerationRecord: SavedPlanGenerationRecord? = null,
    val recentAnalysisRecords: List<SavedAnalysisRecord> = emptyList(),
    val recentPlanGenerationRecords: List<SavedPlanGenerationRecord> = emptyList(),
    val publishedRemodelRecords: List<PublishedRemodelRecord> = emptyList(),
    val inspirationEngagementRecords: List<InspirationEngagementRecord> = emptyList(),
    val wardrobeEntries: List<WardrobeEntryUiModel> = emptyList(),
    val wardrobeCategories: List<String> = listOf("全部"),
    val sustainabilitySummary: SustainabilityImpactSummary = SustainabilityImpactSummary(),
    val recentActivities: List<RecentActivityUiModel> = emptyList()
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

    val canGeneratePreview: Boolean
        get() = draftAnalysis != null &&
            plans.isNotEmpty() &&
            !selectedPlanId.isNullOrBlank() &&
            !isPreviewLoading

    val canGenerateFinalEffectImage: Boolean
        get() = editingPlanId != null && !isPreviewLoading

    val currentEditingPlan: RemodelPlan?
        get() = editingPlanId?.let { planId ->
            plans.firstOrNull { it.planId == planId }
        }

    fun previewFor(planId: String): PlanPreviewResult? =
        previewJob?.results?.firstOrNull { it.planId == planId }

    fun publishedRemodelFor(planId: String): PublishedRemodelRecord? =
        publishedRemodelRecords.firstOrNull { it.selectedPlan.planId == planId }

    fun inspirationEngagementFor(itemId: String): InspirationEngagementRecord? =
        inspirationEngagementRecords.firstOrNull { it.itemId == itemId }
}
