package com.scf.secondbloom.data.local

import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import kotlinx.serialization.Serializable

internal interface RemodelHistoryLocalDataSource {
    suspend fun readSnapshot(): RemodelHistorySnapshot

    suspend fun writeSnapshot(snapshot: RemodelHistorySnapshot)
}

@Serializable
internal data class RemodelHistorySnapshot(
    val analyses: List<SavedAnalysisRecord> = emptyList(),
    val planGenerations: List<SavedPlanGenerationRecord> = emptyList(),
    val publishedRemodels: List<PublishedRemodelRecord> = emptyList(),
    val inspirationEngagements: List<InspirationEngagementRecord> = emptyList()
)
