package com.scf.secondbloom.data.historysync

import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.InspirationComment
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.RemodelDifficulty
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStep
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySyncMergeTest {

    @Test
    fun mergeForBootstrap_deduplicatesByStableKeys_andMergesNestedComments() {
        val sharedAnalysisRemote = analysisRecord(
            recordId = "analysis-shared",
            savedAtEpochMillis = 100L,
            analysis = garmentAnalysis("analysis-shared", "remote shirt")
        )
        val sharedAnalysisLocal = analysisRecord(
            recordId = "analysis-shared",
            savedAtEpochMillis = 200L,
            analysis = garmentAnalysis("analysis-shared", "local shirt")
        )
        val remoteUniqueAnalysis = analysisRecord(
            recordId = "analysis-remote",
            savedAtEpochMillis = 50L,
            analysis = garmentAnalysis("analysis-remote", "remote only")
        )
        val localUniqueAnalysis = analysisRecord(
            recordId = "analysis-local",
            savedAtEpochMillis = 300L,
            analysis = garmentAnalysis("analysis-local", "local only")
        )

        val remoteSnapshot = HistorySnapshotPayload(
            analyses = listOf(sharedAnalysisRemote, remoteUniqueAnalysis),
            planGenerations = listOf(
                planRecord(
                    recordId = "plan-shared",
                    savedAtEpochMillis = 90L,
                    analysis = garmentAnalysis("analysis-shared", "remote shirt"),
                    planTitle = "remote plan"
                )
            ),
            publishedRemodels = listOf(
                publishedRecord(
                    recordId = "published-shared",
                    publishedAtEpochMillis = 120L,
                    analysis = garmentAnalysis("analysis-shared", "remote shirt"),
                    planTitle = "remote publish"
                )
            ),
            inspirationEngagements = listOf(
                InspirationEngagementRecord(
                    itemId = "item-1",
                    liked = false,
                    bookmarked = false,
                    likeCount = 2,
                    comments = listOf(
                        InspirationComment(
                            commentId = "comment-1",
                            authorName = "Remote",
                            message = "remote note",
                            createdAtEpochMillis = 10L
                        )
                    )
                )
            )
        )

        val localSnapshot = HistorySnapshotPayload(
            analyses = listOf(sharedAnalysisLocal, localUniqueAnalysis),
            planGenerations = listOf(
                planRecord(
                    recordId = "plan-shared",
                    savedAtEpochMillis = 110L,
                    analysis = garmentAnalysis("analysis-shared", "local shirt"),
                    planTitle = "local plan"
                )
            ),
            publishedRemodels = listOf(
                publishedRecord(
                    recordId = "published-shared",
                    publishedAtEpochMillis = 160L,
                    analysis = garmentAnalysis("analysis-shared", "local shirt"),
                    planTitle = "local publish"
                )
            ),
            inspirationEngagements = listOf(
                InspirationEngagementRecord(
                    itemId = "item-1",
                    liked = true,
                    bookmarked = true,
                    likeCount = 5,
                    comments = listOf(
                        InspirationComment(
                            commentId = "comment-1",
                            authorName = "Local",
                            message = "local note",
                            createdAtEpochMillis = 20L
                        ),
                        InspirationComment(
                            commentId = "comment-2",
                            authorName = "Local",
                            message = "another note",
                            createdAtEpochMillis = 30L
                        )
                    )
                )
            )
        )

        val merged = HistorySyncMerge.mergeForBootstrap(
            localSnapshot = localSnapshot,
            remoteSnapshot = remoteSnapshot
        )

        assertEquals(
            listOf("analysis-local", "analysis-shared", "analysis-remote"),
            merged.analyses.map { it.recordId }
        )
        assertEquals("local shirt", merged.analyses.first { it.recordId == "analysis-shared" }.analysis.garmentType)
        assertEquals("local plan", merged.planGenerations.first { it.recordId == "plan-shared" }.plans.first().title)
        assertEquals("local publish", merged.publishedRemodels.first { it.recordId == "published-shared" }.selectedPlan.title)

        val engagement = merged.inspirationEngagements.single { it.itemId == "item-1" }
        assertTrue(engagement.liked)
        assertTrue(engagement.bookmarked)
        assertEquals(5, engagement.likeCount)
        assertEquals(listOf("comment-1", "comment-2"), engagement.comments.map { it.commentId })
        assertEquals("local note", engagement.comments.first { it.commentId == "comment-1" }.message)
    }

    @Test
    fun mergeForRevisionConflict_usesLatestRemoteAsBase_forReplay() {
        val latestRemote = HistorySnapshotPayload(
            analyses = listOf(
                analysisRecord(
                    recordId = "analysis-remote",
                    savedAtEpochMillis = 100L,
                    analysis = garmentAnalysis("analysis-remote", "remote")
                )
            )
        )
        val pendingLocal = HistorySnapshotPayload(
            analyses = listOf(
                analysisRecord(
                    recordId = "analysis-local",
                    savedAtEpochMillis = 200L,
                    analysis = garmentAnalysis("analysis-local", "local")
                )
            )
        )

        val merged = HistorySyncMerge.mergeForRevisionConflict(
            pendingSnapshot = pendingLocal,
            latestRemoteSnapshot = latestRemote
        )

        assertEquals(setOf("analysis-local", "analysis-remote"), merged.analyses.mapTo(mutableSetOf()) { it.recordId })
    }

    private fun garmentAnalysis(analysisId: String, garmentType: String) = GarmentAnalysis(
        analysisId = analysisId,
        garmentType = garmentType,
        color = "white",
        material = "cotton",
        style = "simple",
        defects = listOf(GarmentDefect("cuff wear")),
        backgroundComplexity = BackgroundComplexity.LOW,
        confidence = 0.91f,
        warnings = emptyList()
    )

    private fun analysisRecord(
        recordId: String,
        savedAtEpochMillis: Long,
        analysis: GarmentAnalysis
    ) = SavedAnalysisRecord(
        recordId = recordId,
        savedAtEpochMillis = savedAtEpochMillis,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$recordId.jpg",
            fileName = "$recordId.jpg",
            mimeType = "image/jpeg"
        ),
        analysis = analysis
    )

    private fun planRecord(
        recordId: String,
        savedAtEpochMillis: Long,
        analysis: GarmentAnalysis,
        planTitle: String
    ) = SavedPlanGenerationRecord(
        recordId = recordId,
        savedAtEpochMillis = savedAtEpochMillis,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$recordId.jpg",
            fileName = "$recordId.jpg",
            mimeType = "image/jpeg"
        ),
        analysis = analysis,
        intent = RemodelIntent.DAILY,
        userPreferences = "",
        plans = listOf(
            RemodelPlan(
                title = planTitle,
                summary = "summary",
                difficulty = RemodelDifficulty.EASY,
                materials = listOf("scissors"),
                estimatedTime = "1h",
                steps = listOf(RemodelStep("step", "detail"))
            )
        )
    )

    private fun publishedRecord(
        recordId: String,
        publishedAtEpochMillis: Long,
        analysis: GarmentAnalysis,
        planTitle: String
    ) = PublishedRemodelRecord(
        recordId = recordId,
        publishedAtEpochMillis = publishedAtEpochMillis,
        sourceImage = SelectedImage(
            uri = "content://secondbloom/$recordId.jpg",
            fileName = "$recordId.jpg",
            mimeType = "image/jpeg"
        ),
        analysis = analysis,
        intent = RemodelIntent.DAILY,
        selectedPlan = RemodelPlan(
            title = planTitle,
            summary = "summary",
            difficulty = RemodelDifficulty.EASY,
            materials = listOf("scissors"),
            estimatedTime = "1h",
            steps = listOf(RemodelStep("step", "detail"))
        ),
        previewResult = com.scf.secondbloom.domain.model.PlanPreviewResult(
            planId = recordId,
            renderStatus = com.scf.secondbloom.domain.model.PreviewRenderStatus.COMPLETED
        )
    )
}
