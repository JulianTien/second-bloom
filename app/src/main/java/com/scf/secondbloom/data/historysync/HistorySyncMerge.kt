package com.scf.secondbloom.data.historysync

import com.scf.secondbloom.domain.model.InspirationComment
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord

object HistorySyncMerge {

    fun mergeForBootstrap(
        localSnapshot: HistorySnapshotPayload,
        remoteSnapshot: HistorySnapshotPayload
    ): HistorySnapshotPayload = HistorySnapshotPayload(
        analyses = mergeRecordsByTimestamp(
            primary = remoteSnapshot.analyses,
            secondary = localSnapshot.analyses,
            keySelector = SavedAnalysisRecord::recordId,
            timestampSelector = SavedAnalysisRecord::savedAtEpochMillis
        ),
        planGenerations = mergeRecordsByTimestamp(
            primary = remoteSnapshot.planGenerations,
            secondary = localSnapshot.planGenerations,
            keySelector = SavedPlanGenerationRecord::recordId,
            timestampSelector = SavedPlanGenerationRecord::savedAtEpochMillis
        ),
        publishedRemodels = mergeRecordsByTimestamp(
            primary = remoteSnapshot.publishedRemodels,
            secondary = localSnapshot.publishedRemodels,
            keySelector = PublishedRemodelRecord::recordId,
            timestampSelector = PublishedRemodelRecord::publishedAtEpochMillis
        ),
        inspirationEngagements = mergeEngagementRecords(
            primary = remoteSnapshot.inspirationEngagements,
            secondary = localSnapshot.inspirationEngagements
        )
    )

    fun mergeForRevisionConflict(
        pendingSnapshot: HistorySnapshotPayload,
        latestRemoteSnapshot: HistorySnapshotPayload
    ): HistorySnapshotPayload = mergeForBootstrap(
        localSnapshot = pendingSnapshot,
        remoteSnapshot = latestRemoteSnapshot
    )

    private fun <T> mergeRecordsByTimestamp(
        primary: List<T>,
        secondary: List<T>,
        keySelector: (T) -> String,
        timestampSelector: (T) -> Long
    ): List<T> {
        val merged = linkedMapOf<String, T>()

        fun put(record: T) {
            val key = keySelector(record)
            val existing = merged[key]
            merged[key] = when {
                existing == null -> record
                timestampSelector(record) > timestampSelector(existing) -> record
                timestampSelector(record) == timestampSelector(existing) -> record
                else -> existing
            }
        }

        primary.forEach(::put)
        secondary.forEach(::put)

        return merged.values.sortedWith(
            compareByDescending<T> { timestampSelector(it) }.thenBy { keySelector(it) }
        )
    }

    private fun mergeEngagementRecords(
        primary: List<InspirationEngagementRecord>,
        secondary: List<InspirationEngagementRecord>
    ): List<InspirationEngagementRecord> {
        val merged = linkedMapOf<String, InspirationEngagementRecord>()

        fun put(record: InspirationEngagementRecord) {
            val existing = merged[record.itemId]
            merged[record.itemId] = if (existing == null) {
                record
            } else {
                mergeEngagementRecord(existing, record)
            }
        }

        primary.forEach(::put)
        secondary.forEach(::put)

        return merged.values.toList()
    }

    private fun mergeEngagementRecord(
        primary: InspirationEngagementRecord,
        secondary: InspirationEngagementRecord
    ): InspirationEngagementRecord = InspirationEngagementRecord(
        itemId = primary.itemId,
        liked = primary.liked || secondary.liked,
        bookmarked = primary.bookmarked || secondary.bookmarked,
        likeCount = maxOf(primary.likeCount, secondary.likeCount),
        comments = mergeComments(primary.comments, secondary.comments)
    )

    private fun mergeComments(
        primary: List<InspirationComment>,
        secondary: List<InspirationComment>
    ): List<InspirationComment> {
        val merged = linkedMapOf<String, InspirationComment>()

        fun put(comment: InspirationComment) {
            val existing = merged[comment.commentId]
            merged[comment.commentId] = when {
                existing == null -> comment
                comment.createdAtEpochMillis > existing.createdAtEpochMillis -> comment
                comment.createdAtEpochMillis == existing.createdAtEpochMillis -> comment
                else -> existing
            }
        }

        primary.forEach(::put)
        secondary.forEach(::put)

        return merged.values.sortedWith(
            compareBy<InspirationComment> { it.createdAtEpochMillis }.thenBy { it.commentId }
        )
    }
}
