package com.scf.secondbloom.data.historysync

import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.RemodelDifficulty
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStep
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySyncRepositoryTest {

    @Test
    fun bootstrapLocalHistory_writesMergedSnapshotAndState() = runTest {
        val localStore = InMemorySnapshotStore(
            HistorySnapshotPayload(
                analyses = listOf(
                    analysisRecord("analysis-local", 200L, "local")
                )
            )
        )
        val stateStore = InMemoryStateStore()
        val api = object : HistorySyncApi {
            override suspend fun getMe(accessToken: String): UserProfileDto = error("unused")

            override suspend fun getHistory(accessToken: String): HistoryEnvelopeDto = error("unused")

            override suspend fun bootstrapHistory(
                accessToken: String,
                request: BootstrapHistoryRequestDto
            ): BootstrapHistoryResponseDto = BootstrapHistoryResponseDto(
                revision = 42L,
                snapshot = HistorySyncMerge.mergeForBootstrap(
                    localSnapshot = request.snapshot,
                    remoteSnapshot = HistorySnapshotPayload(
                        analyses = listOf(
                            analysisRecord("analysis-remote", 100L, "remote")
                        )
                    )
                ),
                mergeApplied = true
            )

            override suspend fun updateHistory(
                accessToken: String,
                request: UpdateHistoryRequestDto
            ): HistoryEnvelopeDto = error("unused")
        }

        val repository = HistorySyncRepository(
            api = api,
            snapshotStore = localStore,
            stateStore = stateStore,
            accessTokenProvider = object : HistoryAuthTokenProvider {
                override suspend fun currentAccessToken(): String? = "token-123"
            }
        )

        val result = repository.bootstrapLocalHistory()

        assertTrue(result is HistorySyncResult.Synced)
        val synced = result as HistorySyncResult.Synced
        assertEquals(42L, synced.revision)
        assertTrue(synced.mergeApplied)
        assertEquals(
            setOf("analysis-local", "analysis-remote"),
            localStore.snapshot.analyses.mapTo(mutableSetOf()) { it.recordId }
        )
        assertEquals(42L, stateStore.state.revision)
        assertTrue(stateStore.state.bootstrapped)
    }

    private fun analysisRecord(
        recordId: String,
        savedAtEpochMillis: Long,
        garmentType: String
    ) = SavedAnalysisRecord(
        recordId = recordId,
        savedAtEpochMillis = savedAtEpochMillis,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$recordId.jpg",
            fileName = "$recordId.jpg",
            mimeType = "image/jpeg"
        ),
        analysis = GarmentAnalysis(
            analysisId = recordId,
            garmentType = garmentType,
            color = "white",
            material = "cotton",
            style = "simple",
            defects = listOf(GarmentDefect("wear")),
            backgroundComplexity = BackgroundComplexity.LOW,
            confidence = 0.9f,
            warnings = emptyList()
        )
    )
}

private class InMemorySnapshotStore(
    initial: HistorySnapshotPayload = HistorySnapshotPayload()
) : HistorySnapshotStore {
    var snapshot: HistorySnapshotPayload = initial
        private set

    override suspend fun readSnapshot(): HistorySnapshotPayload = snapshot

    override suspend fun writeSnapshot(snapshot: HistorySnapshotPayload) {
        this.snapshot = snapshot
    }
}

private class InMemoryStateStore(
    initial: HistorySyncState = HistorySyncState()
) : HistorySyncStateStore {
    var state: HistorySyncState = initial
        private set

    override suspend fun readState(): HistorySyncState = state

    override suspend fun writeState(state: HistorySyncState) {
        this.state = state
    }
}
