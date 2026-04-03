package com.scf.loop.data.local

import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.SavedAnalysisRecord
import com.scf.loop.domain.model.SavedPlanGenerationRecord
import com.scf.loop.domain.model.SelectedImage
import java.util.UUID

internal class DefaultRemodelHistoryRepository(
    private val localDataSource: RemodelHistoryLocalDataSource
) : RemodelHistoryRepository {

    override suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        savedAtEpochMillis: Long
    ): SavedAnalysisRecord {
        val record = SavedAnalysisRecord(
            recordId = analysis.analysisId,
            savedAtEpochMillis = savedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis
        )
        val snapshot = localDataSource.readSnapshot()
        val updatedAnalyses = buildList {
            add(record)
            addAll(snapshot.analyses.filterNot { it.recordId == analysis.analysisId })
        }
        localDataSource.writeSnapshot(
            snapshot.copy(analyses = updatedAnalyses)
        )
        return record
    }

    override suspend fun savePlanGeneration(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        userPreferences: String,
        plans: List<RemodelPlan>,
        savedAtEpochMillis: Long
    ): SavedPlanGenerationRecord {
        val record = SavedPlanGenerationRecord(
            recordId = UUID.randomUUID().toString(),
            savedAtEpochMillis = savedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis,
            intent = intent,
            userPreferences = userPreferences,
            plans = plans
        )
        val snapshot = localDataSource.readSnapshot()
        localDataSource.writeSnapshot(
            snapshot.copy(
                planGenerations = listOf(record) + snapshot.planGenerations
            )
        )
        return record
    }

    override suspend fun getLatestAnalysis(): SavedAnalysisRecord? =
        localDataSource.readSnapshot().analyses.firstOrNull()

    override suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord? =
        localDataSource.readSnapshot().planGenerations.firstOrNull()

    override suspend fun getRecentAnalyses(limit: Int): List<SavedAnalysisRecord> =
        localDataSource.readSnapshot().analyses.take(limit)

    override suspend fun getRecentPlanGenerations(limit: Int): List<SavedPlanGenerationRecord> =
        localDataSource.readSnapshot().planGenerations.take(limit)
}

object NoOpRemodelHistoryRepository : RemodelHistoryRepository {
    override suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        savedAtEpochMillis: Long
    ): SavedAnalysisRecord = SavedAnalysisRecord(
        recordId = analysis.analysisId,
        savedAtEpochMillis = savedAtEpochMillis,
        sourceImage = sourceImage,
        analysis = analysis
    )

    override suspend fun savePlanGeneration(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        userPreferences: String,
        plans: List<RemodelPlan>,
        savedAtEpochMillis: Long
    ): SavedPlanGenerationRecord = SavedPlanGenerationRecord(
        recordId = "noop-${analysis.analysisId}",
        savedAtEpochMillis = savedAtEpochMillis,
        sourceImage = sourceImage,
        analysis = analysis,
        intent = intent,
        userPreferences = userPreferences,
        plans = plans
    )

    override suspend fun getLatestAnalysis(): SavedAnalysisRecord? = null

    override suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord? = null

    override suspend fun getRecentAnalyses(limit: Int): List<SavedAnalysisRecord> = emptyList()

    override suspend fun getRecentPlanGenerations(limit: Int): List<SavedPlanGenerationRecord> = emptyList()
}
