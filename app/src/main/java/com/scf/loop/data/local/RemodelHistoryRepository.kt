package com.scf.loop.data.local

import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.SavedAnalysisRecord
import com.scf.loop.domain.model.SavedPlanGenerationRecord
import com.scf.loop.domain.model.SelectedImage

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

    companion object {
        const val DefaultListLimit: Int = 10
    }
}
