package com.scf.secondbloom.data.historysync

import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import kotlinx.serialization.Serializable

@Serializable
data class HistorySnapshotPayload(
    val analyses: List<SavedAnalysisRecord> = emptyList(),
    val planGenerations: List<SavedPlanGenerationRecord> = emptyList(),
    val publishedRemodels: List<PublishedRemodelRecord> = emptyList(),
    val inspirationEngagements: List<InspirationEngagementRecord> = emptyList()
)

@Serializable
data class HistoryEnvelopeDto(
    val schemaVersion: Int = SchemaVersion,
    val revision: Long,
    val snapshot: HistorySnapshotPayload
)

@Serializable
data class BootstrapHistoryRequestDto(
    val schemaVersion: Int = SchemaVersion,
    val snapshot: HistorySnapshotPayload
)

@Serializable
data class BootstrapHistoryResponseDto(
    val schemaVersion: Int = SchemaVersion,
    val revision: Long,
    val snapshot: HistorySnapshotPayload,
    val mergeApplied: Boolean
)

@Serializable
data class UpdateHistoryRequestDto(
    val schemaVersion: Int = SchemaVersion,
    val baseRevision: Long,
    val snapshot: HistorySnapshotPayload
)

@Serializable
data class UserProfileDto(
    val clerkUserId: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val lastSeenAt: String? = null
)

@Serializable
data class HistorySyncConflictDto(
    val schemaVersion: Int = SchemaVersion,
    val revision: Long,
    val snapshot: HistorySnapshotPayload,
    val message: String? = null
)

@Serializable
data class HistorySyncState(
    val schemaVersion: Int = SchemaVersion,
    val revision: Long = 0L,
    val bootstrapped: Boolean = false,
    val lastSyncedAtEpochMillis: Long? = null
)

sealed interface HistorySyncResult {
    data object SkippedNoAuth : HistorySyncResult

    data object SkippedAlreadyBootstrapped : HistorySyncResult

    data class Synced(
        val revision: Long,
        val snapshot: HistorySnapshotPayload,
        val mergeApplied: Boolean = false,
        val conflictResolved: Boolean = false
    ) : HistorySyncResult
}

interface HistorySnapshotStore {
    suspend fun readSnapshot(): HistorySnapshotPayload

    suspend fun writeSnapshot(snapshot: HistorySnapshotPayload)
}

interface HistorySyncStateStore {
    suspend fun readState(): HistorySyncState

    suspend fun writeState(state: HistorySyncState)
}

interface HistoryAuthTokenProvider {
    suspend fun currentAccessToken(): String?
}

interface HistorySyncApi {
    suspend fun getMe(accessToken: String): UserProfileDto

    suspend fun getHistory(accessToken: String): HistoryEnvelopeDto

    suspend fun bootstrapHistory(
        accessToken: String,
        request: BootstrapHistoryRequestDto
    ): BootstrapHistoryResponseDto

    suspend fun updateHistory(
        accessToken: String,
        request: UpdateHistoryRequestDto
    ): HistoryEnvelopeDto
}

internal const val SchemaVersion = 1
