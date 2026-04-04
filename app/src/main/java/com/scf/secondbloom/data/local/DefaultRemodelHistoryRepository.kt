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

    override suspend fun savePublishedRemodel(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        selectedPlan: RemodelPlan,
        previewResult: PlanPreviewResult,
        publishedAtEpochMillis: Long
    ): PublishedRemodelRecord {
        val record = PublishedRemodelRecord(
            recordId = UUID.randomUUID().toString(),
            publishedAtEpochMillis = publishedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis,
            intent = intent,
            selectedPlan = selectedPlan,
            previewResult = previewResult
        )
        val snapshot = localDataSource.readSnapshot()
        localDataSource.writeSnapshot(
            snapshot.copy(
                publishedRemodels = listOf(record) + snapshot.publishedRemodels.filterNot {
                    it.selectedPlan.planId == selectedPlan.planId
                }
            )
        )
        return record
    }

    override suspend fun getRecentPublishedRemodels(limit: Int): List<PublishedRemodelRecord> =
        localDataSource.readSnapshot().publishedRemodels.take(limit)

    override suspend fun saveInspirationEngagement(
        record: InspirationEngagementRecord
    ): InspirationEngagementRecord {
        val snapshot = localDataSource.readSnapshot()
        localDataSource.writeSnapshot(
            snapshot.copy(
                inspirationEngagements = listOf(record) + snapshot.inspirationEngagements.filterNot {
                    it.itemId == record.itemId
                }
            )
        )
        return record
    }

    override suspend fun getInspirationEngagements(limit: Int): List<InspirationEngagementRecord> =
        localDataSource.readSnapshot().inspirationEngagements.take(limit)
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

    override suspend fun savePublishedRemodel(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        selectedPlan: RemodelPlan,
        previewResult: PlanPreviewResult,
        publishedAtEpochMillis: Long
    ): PublishedRemodelRecord = PublishedRemodelRecord(
        recordId = "noop-published-${selectedPlan.planId}",
        publishedAtEpochMillis = publishedAtEpochMillis,
        sourceImage = sourceImage,
        analysis = analysis,
        intent = intent,
        selectedPlan = selectedPlan,
        previewResult = previewResult
    )

    override suspend fun getRecentPublishedRemodels(limit: Int): List<PublishedRemodelRecord> = emptyList()

    override suspend fun saveInspirationEngagement(
        record: InspirationEngagementRecord
    ): InspirationEngagementRecord = record

    override suspend fun getInspirationEngagements(limit: Int): List<InspirationEngagementRecord> = emptyList()
}
