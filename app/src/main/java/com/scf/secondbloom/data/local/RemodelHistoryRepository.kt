package com.scf.secondbloom.data.local

import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PlanPreviewResult
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage

interface RemodelHistoryRepository {
    suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        savedAtEpochMillis: Long = System.currentTimeMillis()
    ): SavedAnalysisRecord

    suspend fun savePlanGeneration(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        userPreferences: String,
        plans: List<RemodelPlan>,
        savedAtEpochMillis: Long = System.currentTimeMillis()
    ): SavedPlanGenerationRecord

    suspend fun getLatestAnalysis(): SavedAnalysisRecord?

    suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord?

    suspend fun getRecentAnalyses(limit: Int = DefaultListLimit): List<SavedAnalysisRecord>

    suspend fun getRecentPlanGenerations(limit: Int = DefaultListLimit): List<SavedPlanGenerationRecord>

    suspend fun savePublishedRemodel(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        selectedPlan: RemodelPlan,
        previewResult: PlanPreviewResult,
        publishedAtEpochMillis: Long = System.currentTimeMillis()
    ): PublishedRemodelRecord

    suspend fun getRecentPublishedRemodels(limit: Int = DefaultListLimit): List<PublishedRemodelRecord>

    suspend fun saveInspirationEngagement(record: InspirationEngagementRecord): InspirationEngagementRecord

    suspend fun getInspirationEngagements(limit: Int = DefaultListLimit): List<InspirationEngagementRecord>

    companion object {
        const val DefaultListLimit: Int = 10
    }
}
