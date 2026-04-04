package com.scf.secondbloom.data.local

import android.content.Context
import android.content.SharedPreferences
import com.scf.secondbloom.data.historysync.HistorySnapshotPayload
import com.scf.secondbloom.data.historysync.HistorySnapshotStore
import com.scf.secondbloom.data.historysync.HistorySyncState
import com.scf.secondbloom.data.historysync.HistorySyncStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun RemodelHistorySnapshot.toPayload(): HistorySnapshotPayload = HistorySnapshotPayload(
    analyses = analyses,
    planGenerations = planGenerations,
    publishedRemodels = publishedRemodels,
    inspirationEngagements = inspirationEngagements
)

internal fun HistorySnapshotPayload.toLocalSnapshot(): RemodelHistorySnapshot = RemodelHistorySnapshot(
    analyses = analyses,
    planGenerations = planGenerations,
    publishedRemodels = publishedRemodels,
    inspirationEngagements = inspirationEngagements
)

internal class FileRemodelHistorySnapshotStore(
    private val localDataSource: RemodelHistoryLocalDataSource
) : HistorySnapshotStore {
    override suspend fun readSnapshot(): HistorySnapshotPayload =
        localDataSource.readSnapshot().toPayload()

    override suspend fun writeSnapshot(snapshot: HistorySnapshotPayload) {
        localDataSource.writeSnapshot(snapshot.toLocalSnapshot())
    }
}

internal class SharedPrefsHistorySyncStateStore(
    context: Context,
    namespace: String = DefaultHistorySyncNamespace,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = false
    }
) : HistorySyncStateStore {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "second_bloom_history_sync_${namespace.trim().ifBlank { DefaultHistorySyncNamespace }}",
        Context.MODE_PRIVATE
    )

    override suspend fun readState(): HistorySyncState = withContext(Dispatchers.IO) {
        val rawState = sharedPreferences.getString(KeyState, null)
        if (rawState.isNullOrBlank()) {
            return@withContext HistorySyncState()
        }

        runCatching {
            json.decodeFromString(HistorySyncState.serializer(), rawState)
        }.getOrElse { HistorySyncState() }
    }

    override suspend fun writeState(state: HistorySyncState) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KeyState, json.encodeToString(HistorySyncState.serializer(), state))
            .apply()
    }

    private companion object {
        const val KeyState = "history_sync_state"
    }
}

private const val DefaultHistorySyncNamespace = "default"
