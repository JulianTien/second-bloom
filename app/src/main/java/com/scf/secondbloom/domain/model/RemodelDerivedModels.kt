package com.scf.secondbloom.domain.model

private const val EstimatedWaterSavedPerPlanLiters = 1_500
private const val EstimatedCarbonSavedPerPlanKg = 4.2f

data class WardrobeEntryUiModel(
    val id: String,
    val analysisId: String,
    val garmentType: String,
    val color: String,
    val material: String,
    val sourceFileName: String,
    val category: String,
    val statusLabel: String,
    val latestPlanRecordId: String? = null
) {
    val tags: List<String>
        get() = listOf(color, material).filter { it.isNotBlank() }.distinct()

    val hasSavedPlan: Boolean
        get() = !latestPlanRecordId.isNullOrBlank()
}

data class PlanetBadgeUiModel(
    val emoji: String,
    val label: String,
    val active: Boolean
)

data class RecentActivityUiModel(
    val id: String,
    val badgeLabel: String,
    val title: String,
    val subtitle: String,
    val supportingText: String
)

data class SustainabilityImpactSummary(
    val completedRemodelCount: Int = 0,
    val analyzedGarmentCount: Int = 0,
    val estimatedWaterSavedLiters: Int = 0,
    val estimatedCarbonSavedKg: Float = 0f,
    val level: Int = 1,
    val levelTitle: String = "New sprout",
    val levelDescription: String = "Complete one remodel and this page will start tracking your impact.",
    val badges: List<PlanetBadgeUiModel> = defaultPlanetBadges()
)

fun deriveWardrobeEntries(
    recentAnalyses: List<SavedAnalysisRecord>,
    recentPlanGenerations: List<SavedPlanGenerationRecord>,
    language: AppLanguage = AppLanguage.ENGLISH
): List<WardrobeEntryUiModel> {
    val latestPlanRecordIdsByAnalysisId = recentPlanGenerations
        .groupBy { it.analysis.analysisId }
        .mapValues { (_, records) ->
            records.maxByOrNull { it.savedAtEpochMillis }?.recordId
        }

    return recentAnalyses.map { record ->
        val latestPlanRecordId = latestPlanRecordIdsByAnalysisId[record.analysis.analysisId]
        WardrobeEntryUiModel(
            id = record.recordId,
            analysisId = record.analysis.analysisId,
            garmentType = record.analysis.garmentType,
            color = record.analysis.color,
            material = record.analysis.material,
            sourceFileName = record.sourceImage.fileName,
            category = deriveWardrobeCategory(record.analysis.garmentType, language),
            statusLabel = if (latestPlanRecordId != null) {
                localized(language, "Plan ready", "已生成方案")
            } else {
                localized(language, "Awaiting remodel", "待改造")
            },
            latestPlanRecordId = latestPlanRecordId
        )
    }
}

fun deriveWardrobeCategories(
    entries: List<WardrobeEntryUiModel>,
    language: AppLanguage = AppLanguage.ENGLISH
): List<String> {
    val presentCategories = entries.map { it.category }.toSet()
    return listOf(localized(language, "All", "全部")) +
        wardrobeCategoryOrder(language).filter { it in presentCategories }
}

fun deriveSustainabilityImpactSummary(
    recentAnalyses: List<SavedAnalysisRecord>,
    recentPlanGenerations: List<SavedPlanGenerationRecord>,
    language: AppLanguage = AppLanguage.ENGLISH
): SustainabilityImpactSummary {
    val completedRemodelCount = recentPlanGenerations.size
    val analyzedGarmentCount = recentAnalyses.size
    val levelInfo = derivePlanetLevel(completedRemodelCount, language)

    return SustainabilityImpactSummary(
        completedRemodelCount = completedRemodelCount,
        analyzedGarmentCount = analyzedGarmentCount,
        estimatedWaterSavedLiters = completedRemodelCount * EstimatedWaterSavedPerPlanLiters,
        estimatedCarbonSavedKg = completedRemodelCount * EstimatedCarbonSavedPerPlanKg,
        level = levelInfo.level,
        levelTitle = levelInfo.title,
        levelDescription = levelInfo.description,
        badges = listOf(
            PlanetBadgeUiModel(
                emoji = "✂️",
                label = localized(language, "First tailor", "初级裁缝"),
                active = analyzedGarmentCount >= 1
            ),
            PlanetBadgeUiModel(
                emoji = "🌱",
                label = localized(language, "Zero-waste pioneer", "零废弃先锋"),
                active = completedRemodelCount >= 2
            ),
            PlanetBadgeUiModel(
                emoji = "👗",
                label = localized(language, "Remodel master", "重塑大师"),
                active = completedRemodelCount >= 4
            )
        )
    )
}

fun deriveRecentActivities(
    recentAnalyses: List<SavedAnalysisRecord>,
    recentPlanGenerations: List<SavedPlanGenerationRecord>,
    language: AppLanguage = AppLanguage.ENGLISH,
    limit: Int = 5
): List<RecentActivityUiModel> {
    val analysisActivities = recentAnalyses.map { record ->
        TimedRecentActivity(
            timestamp = record.savedAtEpochMillis,
            item = RecentActivityUiModel(
                id = "analysis-${record.recordId}",
                badgeLabel = localized(language, "Recognized", "识别完成"),
                title = record.analysis.garmentType,
                subtitle = record.sourceImage.fileName,
                supportingText = "${record.analysis.color} · ${record.analysis.material}"
            )
        )
    }
    val planActivities = recentPlanGenerations.map { record ->
        val firstPlan = record.plans.firstOrNull()
        TimedRecentActivity(
            timestamp = record.savedAtEpochMillis,
            item = RecentActivityUiModel(
                id = "plan-${record.recordId}",
                badgeLabel = localized(language, "Plan generated", "方案生成"),
                title = firstPlan?.title ?: if (language == AppLanguage.ENGLISH) {
                    record.intent.englishLabel
                } else {
                    record.intent.chineseLabel
                },
                subtitle = record.sourceImage.fileName,
                supportingText = if (language == AppLanguage.ENGLISH) {
                    "${record.intent.englishLabel} · ${record.plans.size} suggestions"
                } else {
                    "${record.intent.chineseLabel} · ${record.plans.size} 套建议"
                }
            )
        )
    }

    return (analysisActivities + planActivities)
        .sortedByDescending { it.timestamp }
        .map { it.item }
        .take(limit)
}

private data class PlanetLevelInfo(
    val level: Int,
    val title: String,
    val description: String
)

private data class TimedRecentActivity(
    val timestamp: Long,
    val item: RecentActivityUiModel
)

private fun derivePlanetLevel(
    completedRemodelCount: Int,
    language: AppLanguage
): PlanetLevelInfo = when {
    completedRemodelCount >= 4 -> PlanetLevelInfo(
        level = 4,
        title = localized(language, "Eco guardian", "生态卫士"),
        description = localized(
            language,
            "You have turned repeated remodels into a habit, and your impact keeps growing.",
            "你已经把多次改制沉淀成稳定习惯，环保影响正在持续放大。"
        )
    )
    completedRemodelCount >= 2 -> PlanetLevelInfo(
        level = 3,
        title = localized(language, "Zero-waste pioneer", "零废弃先锋"),
        description = localized(
            language,
            "You are extending the life of old garments again and again.",
            "你的改制动作已经不止一次，旧衣价值正在被持续延长。"
        )
    )
    completedRemodelCount >= 1 -> PlanetLevelInfo(
        level = 2,
        title = localized(language, "First tailor", "初级裁缝"),
        description = localized(
            language,
            "Your first remodel is complete, so your sustainability impact is now visible.",
            "第一批改制方案已经完成，环保贡献开始变得可见。"
        )
    )
    else -> PlanetLevelInfo(
        level = 1,
        title = localized(language, "New sprout", "萌芽新手"),
        description = localized(
            language,
            "Complete one remodel and this page will start tracking your impact.",
            "完成一次改制后，这里会开始累计你的环保贡献。"
        )
    )
}

private fun deriveWardrobeCategory(garmentType: String, language: AppLanguage): String {
    val normalized = garmentType.lowercase()
    return when {
        listOf("裙", "dress", "skirt").any { it in normalized } -> localized(language, "Dresses", "裙装")
        listOf("裤", "jean", "pants", "trouser", "shorts").any { it in normalized } -> localized(language, "Bottoms", "下装")
        listOf(
            "衬衫",
            "shirt",
            "上衣",
            "t恤",
            "tee",
            "卫衣",
            "hoodie",
            "毛衣",
            "knit",
            "针织",
            "外套",
            "coat",
            "夹克",
            "jacket",
            "背心",
            "vest",
            "西装",
            "blazer",
            "马甲"
        ).any { it in normalized } -> localized(language, "Tops", "上装")
        else -> localized(language, "Other", "其他")
    }
}

private fun defaultPlanetBadges(): List<PlanetBadgeUiModel> = listOf(
    PlanetBadgeUiModel(emoji = "✂️", label = "First tailor", active = false),
    PlanetBadgeUiModel(emoji = "🌱", label = "Zero-waste pioneer", active = false),
    PlanetBadgeUiModel(emoji = "👗", label = "Remodel master", active = false)
)

private fun wardrobeCategoryOrder(language: AppLanguage): List<String> = listOf(
    localized(language, "Tops", "上装"),
    localized(language, "Bottoms", "下装"),
    localized(language, "Dresses", "裙装"),
    localized(language, "Other", "其他")
)

private fun localized(language: AppLanguage, english: String, chinese: String): String =
    if (language == AppLanguage.ENGLISH) english else chinese
