package com.scf.secondbloom

import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStep
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage
import com.scf.secondbloom.domain.model.deriveRecentActivities
import com.scf.secondbloom.domain.model.deriveSustainabilityImpactSummary
import com.scf.secondbloom.domain.model.deriveWardrobeCategories
import com.scf.secondbloom.domain.model.deriveWardrobeEntries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RemodelDerivedModelsTest {

    @Test
    fun deriveWardrobeEntries_marksGeneratedPlans_andFallsBackToOtherCategory() {
        val shirtAnalysis = analysis(analysisId = "analysis-shirt", garmentType = "白色衬衫")
        val accessoryAnalysis = analysis(analysisId = "analysis-accessory", garmentType = "旧围巾")
        val shirtRecord = analysisRecord(
            recordId = "record-shirt",
            fileName = "plain-shirt.jpg",
            analysis = shirtAnalysis
        )
        val accessoryRecord = analysisRecord(
            recordId = "record-accessory",
            fileName = "old-scarf.jpg",
            analysis = accessoryAnalysis
        )
        val planRecord = planRecord(
            recordId = "plan-shirt",
            fileName = "plain-shirt.jpg",
            analysis = shirtAnalysis
        )

        val entries = deriveWardrobeEntries(
            recentAnalyses = listOf(shirtRecord, accessoryRecord),
            recentPlanGenerations = listOf(planRecord),
            language = AppLanguage.CHINESE
        )

        assertEquals(2, entries.size)
        assertEquals("已生成方案", entries.first().statusLabel)
        assertEquals("上装", entries.first().category)
        assertEquals("plan-shirt", entries.first().latestPlanRecordId)
        assertEquals(true, entries.first().hasSavedPlan)
        assertEquals("待改造", entries.last().statusLabel)
        assertEquals("其他", entries.last().category)
        assertEquals(null, entries.last().latestPlanRecordId)
        assertEquals(false, entries.last().hasSavedPlan)
        assertEquals(
            listOf("全部", "上装", "其他"),
            deriveWardrobeCategories(entries, AppLanguage.CHINESE)
        )
    }

    @Test
    fun deriveWardrobeEntries_linksToLatestPlanRecordForSameAnalysis() {
        val analysis = analysis(analysisId = "analysis-shirt", garmentType = "白色衬衫")
        val shirtRecord = analysisRecord(
            recordId = "record-shirt",
            fileName = "plain-shirt.jpg",
            analysis = analysis
        )
        val olderPlanRecord = planRecord(
            recordId = "plan-shirt-old",
            fileName = "plain-shirt.jpg",
            analysis = analysis
        ).copy(savedAtEpochMillis = 10L)
        val latestPlanRecord = planRecord(
            recordId = "plan-shirt-new",
            fileName = "plain-shirt.jpg",
            analysis = analysis
        ).copy(savedAtEpochMillis = 20L)

        val entries = deriveWardrobeEntries(
            recentAnalyses = listOf(shirtRecord),
            recentPlanGenerations = listOf(olderPlanRecord, latestPlanRecord),
            language = AppLanguage.ENGLISH
        )

        assertEquals(1, entries.size)
        assertEquals("plan-shirt-new", entries.first().latestPlanRecordId)
        assertEquals("Plan ready", entries.first().statusLabel)
    }

    @Test
    fun deriveSustainabilityImpactSummary_usesPlanCountForImpactAndProgression() {
        val analyses = listOf(
            analysisRecord("analysis-1-record", "shirt-1.jpg", analysis("analysis-1", "白色衬衫")),
            analysisRecord("analysis-2-record", "shirt-2.jpg", analysis("analysis-2", "牛仔裤")),
            analysisRecord("analysis-3-record", "shirt-3.jpg", analysis("analysis-3", "连衣裙"))
        )
        val plans = listOf(
            planRecord("plan-1", "shirt-1.jpg", analyses[0].analysis),
            planRecord("plan-2", "shirt-2.jpg", analyses[1].analysis),
            planRecord("plan-3", "shirt-3.jpg", analyses[2].analysis),
            planRecord("plan-4", "shirt-1b.jpg", analyses[0].analysis)
        )

        val summary = deriveSustainabilityImpactSummary(
            recentAnalyses = analyses,
            recentPlanGenerations = plans,
            language = AppLanguage.CHINESE
        )

        assertEquals(3, summary.analyzedGarmentCount)
        assertEquals(4, summary.completedRemodelCount)
        assertEquals(6000, summary.estimatedWaterSavedLiters)
        assertEquals(16.8f, summary.estimatedCarbonSavedKg, 0.001f)
        assertEquals(4, summary.level)
        assertEquals("生态卫士", summary.levelTitle)
        assertFalse(summary.badges.any { !it.active })
    }

    @Test
    fun deriveRecentActivities_ordersLatestAnalysisAndPlanEventsTogether() {
        val firstAnalysis = analysis("analysis-1", "白色衬衫")
        val secondAnalysis = analysis("analysis-2", "牛仔裤")

        val activities = deriveRecentActivities(
            recentAnalyses = listOf(
                analysisRecord("analysis-1-record", "shirt-1.jpg", firstAnalysis).copy(savedAtEpochMillis = 10L),
                analysisRecord("analysis-2-record", "jeans-1.jpg", secondAnalysis).copy(savedAtEpochMillis = 30L)
            ),
            recentPlanGenerations = listOf(
                planRecord("plan-1", "shirt-1.jpg", firstAnalysis).copy(savedAtEpochMillis = 20L)
            ),
            language = AppLanguage.CHINESE
        )

        assertEquals(3, activities.size)
        assertEquals("识别完成", activities[0].badgeLabel)
        assertEquals("牛仔裤", activities[0].title)
        assertEquals("方案生成", activities[1].badgeLabel)
        assertEquals("日常焕新 方案一", activities[1].title)
        assertEquals("识别完成", activities[2].badgeLabel)
    }

    private fun analysis(
        analysisId: String,
        garmentType: String
    ) = GarmentAnalysis(
        analysisId = analysisId,
        garmentType = garmentType,
        color = "白色",
        material = "棉质",
        style = "简约",
        defects = listOf(GarmentDefect("袖口磨损")),
        backgroundComplexity = BackgroundComplexity.LOW,
        confidence = 0.92f,
        warnings = emptyList()
    )

    private fun analysisRecord(
        recordId: String,
        fileName: String,
        analysis: GarmentAnalysis
    ) = SavedAnalysisRecord(
        recordId = recordId,
        savedAtEpochMillis = 1L,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$fileName",
            fileName = fileName,
            mimeType = "image/jpeg"
        ),
        analysis = analysis
    )

    private fun planRecord(
        recordId: String,
        fileName: String,
        analysis: GarmentAnalysis
    ) = SavedPlanGenerationRecord(
        recordId = recordId,
        savedAtEpochMillis = 2L,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$fileName",
            fileName = fileName,
            mimeType = "image/jpeg"
        ),
        analysis = analysis,
        intent = RemodelIntent.DAILY,
        userPreferences = "",
        plans = listOf(
            RemodelPlan(
                title = "日常焕新 方案一",
                summary = "保留原有轮廓并优化细节。",
                difficulty = com.scf.secondbloom.domain.model.RemodelDifficulty.EASY,
                materials = listOf("布用剪刀"),
                estimatedTime = "1-2 小时",
                steps = listOf(RemodelStep("整理衣片", "先检查磨损区域。"))
            )
        )
    )
}
