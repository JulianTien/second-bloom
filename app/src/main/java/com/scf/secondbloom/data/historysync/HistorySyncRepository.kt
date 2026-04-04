package com.scf.secondbloom.data.historysync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistorySyncRepository(
    private val api: HistorySyncApi,
    private val snapshotStore: HistorySnapshotStore,
    private val stateStore: HistorySyncStateStore,
    private val accessTokenProvider: HistoryAuthTokenProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getMe(): UserProfileDto? = withTokenOrNull { token ->
        api.getMe(token)
    }

    suspend fun fetchRemoteHistory(): HistoryEnvelopeDto? = withTokenOrNull { token ->
        api.getHistory(token)
    }

    suspend fun bootstrapLocalHistory(force: Boolean = false): HistorySyncResult = withTokenResult { token ->
        val state = stateStore.readState()
        if (state.bootstrapped && !force) {
            return@withTokenResult HistorySyncResult.SkippedAlreadyBootstrapped
        }

        val localSnapshot = snapshotStore.readSnapshot()
        val response = api.bootstrapHistory(
            accessToken = token,
            request = BootstrapHistoryRequestDto(snapshot = localSnapshot)
        )

        writeSnapshotAndState(
            snapshot = response.snapshot,
            revision = response.revision,
            bootstrapped = true
        )

        HistorySyncResult.Synced(
            revision = response.revision,
            snapshot = response.snapshot,
            mergeApplied = response.mergeApplied
        )
    }

    suspend fun refreshLocalHistory(): HistorySyncResult = withTokenResult { token ->
        val remoteHistory = api.getHistory(token)
        writeSnapshotAndState(
            snapshot = remoteHistory.snapshot,
            revision = remoteHistory.revision,
            bootstrapped = true
        )
        HistorySyncResult.Synced(
            revision = remoteHistory.revision,
            snapshot = remoteHistory.snapshot
        )
    }

    suspend fun pushLocalHistory(): HistorySyncResult = withTokenResult { token ->
        val localSnapshot = snapshotStore.readSnapshot()
        val state = stateStore.readState()
        try {
            val updated = api.updateHistory(
                accessToken = token,
                request = UpdateHistoryRequestDto(
                    baseRevision = state.revision,
                    snapshot = localSnapshot
                )
            )
            writeSnapshotAndState(
                snapshot = updated.snapshot,
                revision = updated.revision,
                bootstrapped = true
            )
            HistorySyncResult.Synced(
                revision = updated.revision,
                snapshot = updated.snapshot
            )
        } catch (conflict: HistorySyncConflictException) {
            val mergedSnapshot = HistorySyncMerge.mergeForRevisionConflict(
                pendingSnapshot = localSnapshot,
                latestRemoteSnapshot = conflict.conflict.snapshot
            )
            val retry = api.updateHistory(
                accessToken = token,
                request = UpdateHistoryRequestDto(
                    baseRevision = conflict.conflict.revision,
                    snapshot = mergedSnapshot
                )
            )
            writeSnapshotAndState(
                snapshot = retry.snapshot,
                revision = retry.revision,
                bootstrapped = true
            )
            HistorySyncResult.Synced(
                revision = retry.revision,
                snapshot = retry.snapshot,
                conflictResolved = true
            )
        }
    }

    private suspend fun writeSnapshotAndState(
        snapshot: HistorySnapshotPayload,
        revision: Long,
        bootstrapped: Boolean
    ) {
        snapshotStore.writeSnapshot(snapshot)
        stateStore.writeState(
            HistorySyncState(
                revision = revision,
                bootstrapped = bootstrapped,
                lastSyncedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    private suspend fun <T> withTokenOrNull(block: suspend (String) -> T): T? {
        val token = accessTokenProvider.currentAccessToken().orEmpty().trim()
        if (token.isBlank()) {
            return null
        }
        return withContext(ioDispatcher) { block(token) }
    }

    private suspend fun withTokenResult(
        block: suspend (String) -> HistorySyncResult
    ): HistorySyncResult {
        val token = accessTokenProvider.currentAccessToken().orEmpty().trim()
        if (token.isBlank()) {
            return HistorySyncResult.SkippedNoAuth
        }
        return withContext(ioDispatcher) { block(token) }
    }
}
