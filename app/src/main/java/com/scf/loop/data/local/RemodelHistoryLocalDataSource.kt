package com.scf.loop.data.local

import com.scf.loop.domain.model.SavedAnalysisRecord
import com.scf.loop.domain.model.SavedPlanGenerationRecord
import kotlinx.serialization.Serializable

internal interface RemodelHistoryLocalDataSource {
    suspend fun readSnapshot(): RemodelHistorySnapshot

    suspend fun writeSnapshot(snapshot: RemodelHistorySnapshot)
}

@Serializable
internal data class RemodelHistorySnapshot(
    val analyses: List<SavedAnalysisRecord> = emptyList(),
    val planGenerations: List<SavedPlanGenerationRecord> = emptyList()
)
