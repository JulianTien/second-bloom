package com.scf.secondbloom

import com.scf.secondbloom.data.local.RemodelHistoryRepository
import com.scf.secondbloom.data.remote.ModelResponseException
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.secondbloom.data.remote.dto.GarmentAnalysisDto
import com.scf.secondbloom.data.remote.dto.GarmentDefectDto
import com.scf.secondbloom.data.remote.dto.ProcessingWarningDto
import com.scf.secondbloom.data.remote.mock.MockRemodelApi
import com.scf.secondbloom.data.historysync.BootstrapHistoryRequestDto
import com.scf.secondbloom.data.historysync.BootstrapHistoryResponseDto
import com.scf.secondbloom.data.historysync.HistoryAuthTokenProvider
import com.scf.secondbloom.data.historysync.HistoryEnvelopeDto
import com.scf.secondbloom.data.historysync.HistorySnapshotPayload
import com.scf.secondbloom.data.historysync.HistorySyncApi
import com.scf.secondbloom.data.historysync.HistorySyncRepository
import com.scf.secondbloom.data.historysync.HistorySyncState
import com.scf.secondbloom.data.historysync.HistorySyncStateStore
import com.scf.secondbloom.data.historysync.HistorySnapshotStore
import com.scf.secondbloom.data.historysync.UpdateHistoryRequestDto
import com.scf.secondbloom.data.repository.DefaultRemodelRepository
import com.scf.secondbloom.data.repository.RemodelRepository
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.DemoScenario
import com.scf.secondbloom.domain.model.ProcessingWarningCode
import com.scf.secondbloom.domain.model.PreviewEditFidelity
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewAsset
import com.scf.secondbloom.domain.model.PreviewJobSnapshot
import com.scf.secondbloom.domain.model.PreviewJobStatus
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.PreviewEditLength
import com.scf.secondbloom.domain.model.PreviewEditSilhouette
import com.scf.secondbloom.domain.model.GeneratePreviewJobResult
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelStage
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage
import com.scf.secondbloom.presentation.remodel.RemodelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemodelViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun analyzeSelectedImage_movesToLowConfidence_whenBackgroundIsComplex() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/messy-shirt.jpg",
                fileName = "messy-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 120_000
            )
        )

        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.LowConfidence, state.stage)
        assertTrue(state.draftAnalysis != null)
        assertEquals(BackgroundComplexity.HIGH, state.draftAnalysis?.backgroundComplexity)
    }

    @Test
    fun loadDemoScenario_setsStableSelectedImageForFilming() {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.loadDemoScenario(DemoScenario.NORMAL)

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.ImageSelected, state.stage)
        assertEquals(DemoScenario.NORMAL.fileName, state.selectedImage?.fileName)
        assertEquals(DemoScenario.NORMAL, state.selectedDemoScenario)
    }

    @Test
    fun analyzeSelectedImage_showsNetworkError_forErrorDemoScenario() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.loadDemoScenario(DemoScenario.NETWORK_ERROR)
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.NetworkError, state.stage)
        assertEquals(DemoScenario.NETWORK_ERROR, state.selectedDemoScenario)
        assertTrue(state.error?.message?.contains("网络") == true)
    }

    @Test
    fun analyzeSelectedImage_usesDemoRepository_forNormalDemoScenario() = runTest {
        val primaryRepository = CountingRemodelRepository(
            analyzeFailure = IllegalStateException("No content provider: demo://scenario/normal")
        )
        val demoRepository = CountingRemodelRepository(
            delegate = DefaultRemodelRepository(MockRemodelApi())
        )
        val viewModel = RemodelViewModel(
            repository = primaryRepository,
            demoRepository = demoRepository
        )

        viewModel.loadDemoScenario(DemoScenario.NORMAL)
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.AnalysisReady, state.stage)
        assertEquals(DemoScenario.NORMAL, state.selectedDemoScenario)
        assertEquals(0, primaryRepository.analyzeCalls)
        assertEquals(1, demoRepository.analyzeCalls)
    }

    @Test
    fun demoScenarioFlow_routesPlanAndPreviewLifecycle_toDemoRepository() = runTest {
        val primaryRepository = CountingRemodelRepository(
            analyzeFailure = IllegalStateException("Primary repository should not be used for demo scenarios."),
            generatePlansFailure = IllegalStateException("Primary repository should not be used for demo scenarios."),
            createPreviewFailure = IllegalStateException("Primary repository should not be used for demo scenarios."),
            getPreviewFailure = IllegalStateException("Primary repository should not be used for demo scenarios.")
        )
        val demoRepository = CountingRemodelRepository(
            delegate = DefaultRemodelRepository(MockRemodelApi())
        )
        val viewModel = RemodelViewModel(
            repository = primaryRepository,
            demoRepository = demoRepository
        )

        viewModel.loadDemoScenario(DemoScenario.NORMAL)
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()
        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans.first().planId
        viewModel.confirmPlan(selectedPlanId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals(0, primaryRepository.analyzeCalls)
        assertEquals(0, primaryRepository.generatePlanCalls)
        assertEquals(0, primaryRepository.createPreviewCalls)
        assertEquals(0, primaryRepository.getPreviewCalls)
        assertEquals(1, demoRepository.analyzeCalls)
        assertEquals(1, demoRepository.generatePlanCalls)
        assertEquals(1, demoRepository.createPreviewCalls)
        assertTrue(demoRepository.getPreviewCalls > 0)
        assertEquals(PreviewJobStatus.COMPLETED, state.previewJob?.status)
        assertEquals(
            PreviewRenderStatus.COMPLETED,
            state.previewFor(selectedPlanId)?.renderStatus
        )
    }

    @Test
    fun analyzeSelectedImage_usesDemoRepository_forLowConfidenceDemoScenario() = runTest {
        val primaryRepository = CountingRemodelRepository(
            analyzeFailure = IllegalStateException("Primary repository should not be used for demo scenarios.")
        )
        val demoRepository = CountingRemodelRepository(
            delegate = DefaultRemodelRepository(MockRemodelApi())
        )
        val viewModel = RemodelViewModel(
            repository = primaryRepository,
            demoRepository = demoRepository
        )

        viewModel.loadDemoScenario(DemoScenario.LOW_CONFIDENCE)
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.LowConfidence, state.stage)
        assertEquals(0, primaryRepository.analyzeCalls)
        assertEquals(1, demoRepository.analyzeCalls)
    }

    @Test
    fun analyzeSelectedImage_usesDemoRepository_forNetworkErrorDemoScenario() = runTest {
        val primaryRepository = CountingRemodelRepository(
            analyzeFailure = IllegalStateException("Primary repository should not be used for demo scenarios.")
        )
        val demoRepository = CountingRemodelRepository(
            delegate = DefaultRemodelRepository(MockRemodelApi())
        )
        val viewModel = RemodelViewModel(
            repository = primaryRepository,
            demoRepository = demoRepository
        )

        viewModel.loadDemoScenario(DemoScenario.NETWORK_ERROR)
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.NetworkError, state.stage)
        assertEquals(0, primaryRepository.analyzeCalls)
        assertEquals(1, demoRepository.analyzeCalls)
    }

    @Test
    fun analyzeSelectedImage_usesPrimaryRepository_forRealImages() = runTest {
        val primaryRepository = CountingRemodelRepository(
            delegate = DefaultRemodelRepository(MockRemodelApi())
        )
        val demoRepository = CountingRemodelRepository(
            analyzeFailure = IllegalStateException("Demo repository should not be used for real images.")
        )
        val viewModel = RemodelViewModel(
            repository = primaryRepository,
            demoRepository = demoRepository
        )

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.AnalysisReady, state.stage)
        assertEquals(1, primaryRepository.analyzeCalls)
        assertEquals(0, demoRepository.analyzeCalls)
    }

    @Test
    fun generatePlans_afterEditingAnalysis_usesLatestStateAndReturnsPlans() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.updateGarmentType("亚麻衬衫")
        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.updateUserPreferences("保留正式感，适合春季穿搭")
        viewModel.generatePlans()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals("亚麻衬衫", state.analysis?.garmentType)
        assertFalse(state.plans.isEmpty())
        assertTrue(state.plans.first().summary.contains("亚麻衬衫"))
    }

    @Test
    fun analyzeSelectedImage_updatesLatestAnalysisHistory() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = historyRepository
        )

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )

        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("plain-shirt.jpg", state.latestAnalysisRecord?.sourceImage?.fileName)
        assertFalse(state.recentAnalysisRecords.isEmpty())
    }

    @Test
    fun generatePlans_updatesLatestPlanHistory() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = historyRepository
        )

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelIntent.DAILY, state.latestPlanGenerationRecord?.intent)
        assertFalse(state.recentPlanGenerationRecords.isEmpty())
    }

    @Test
    fun restoreSavedPlanGeneration_restoresEditablePlanContextFromHistory() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = historyRepository
        )

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()
        viewModel.selectIntent(RemodelIntent.DIY)
        viewModel.updateUserPreferences("Keep the fabric texture visible")
        viewModel.generatePlans()
        advanceUntilIdle()

        val savedRecordId = viewModel.uiState.value.latestPlanGenerationRecord?.recordId.orEmpty()

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/other-item.jpg",
                fileName = "other-item.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 120_000
            )
        )

        val restored = viewModel.restoreSavedPlanGeneration(savedRecordId)
        val state = viewModel.uiState.value

        assertTrue(restored)
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals("plain-shirt.jpg", state.selectedImage?.fileName)
        assertEquals(RemodelIntent.DIY, state.selectedIntent)
        assertEquals("Keep the fabric texture visible", state.userPreferences)
        assertFalse(state.plans.isEmpty())
        assertEquals(null, state.editingPlanId)
        assertEquals(null, state.previewJob)
    }

    @Test
    fun generatePlans_doesNotAutoStartPreviewUntilPlanIsConfirmed() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals(null, state.previewJob)
        assertEquals(null, state.selectedPlanId)
    }

    @Test
    fun logout_clearsActiveHistorySyncAndKeepsLocalHistoryWritable() = runTest {
        val authFlow = MutableStateFlow<String?>(null)
        val syncApi = RecordingHistorySyncApi()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = FakeHistoryRepository(),
            historySyncRepositoryFactory = { userId ->
                HistorySyncRepository(
                    api = syncApi,
                    snapshotStore = InMemorySnapshotStore(),
                    stateStore = InMemoryStateStore(),
                    accessTokenProvider = object : HistoryAuthTokenProvider {
                        override suspend fun currentAccessToken(): String? = "token-$userId"
                    },
                    ioDispatcher = dispatcher
                )
            },
            authUserIdFlow = authFlow
        )

        authFlow.value = "user-a"
        advanceUntilIdle()

        viewModel.toggleInspirationLike(itemId = "tee-crop", baseLikeCount = 3100)
        advanceUntilIdle()
        assertEquals(1, syncApi.bootstrapCalls)
        val updateCallsBeforeLogout = syncApi.updateCalls

        authFlow.value = null
        advanceUntilIdle()

        viewModel.toggleInspirationBookmark(itemId = "tee-crop", baseLikeCount = 3100)
        advanceUntilIdle()

        val engagement = viewModel.uiState.value.inspirationEngagementFor("tee-crop")
        assertTrue(engagement?.bookmarked == true)
        assertEquals(1, syncApi.bootstrapCalls)
        assertEquals(updateCallsBeforeLogout, syncApi.updateCalls)
    }

    @Test
    fun confirmPlan_startsPreviewJobForOnlyTheSelectedPlan() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans[1].planId
        viewModel.confirmPlan(selectedPlanId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals(selectedPlanId, state.selectedPlanId)
        assertEquals(PreviewJobStatus.COMPLETED, state.previewJob?.status)
        assertEquals(1, state.previewJob?.requestedPlanCount)
        assertEquals(selectedPlanId, state.previewJob?.results?.first()?.planId)
        assertEquals(PreviewRenderStatus.COMPLETED, state.previewJob?.results?.first()?.renderStatus)
        assertTrue(state.previewJob?.results?.first()?.afterImage?.url?.isNotBlank() == true)
    }

    @Test
    fun publishPreviewResult_savesPublishedWorkIntoStateHistory() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = historyRepository
        )

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans.first().planId
        viewModel.confirmPlan(selectedPlanId)
        advanceUntilIdle()
        viewModel.publishPreviewResult(selectedPlanId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.publishedRemodelRecords.size)
        assertEquals(selectedPlanId, state.publishedRemodelRecords.first().selectedPlan.planId)
        assertEquals(selectedPlanId, state.publishedRemodelFor(selectedPlanId)?.selectedPlan?.planId)
    }

    @Test
    fun inspirationInteractions_arePersistedIntoUiState() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = DefaultRemodelRepository(MockRemodelApi()),
            historyRepository = historyRepository
        )

        viewModel.toggleInspirationLike(itemId = "tee-crop", baseLikeCount = 3100)
        advanceUntilIdle()
        viewModel.toggleInspirationBookmark(itemId = "tee-crop", baseLikeCount = 3100)
        advanceUntilIdle()
        viewModel.addInspirationComment(
            itemId = "tee-crop",
            baseLikeCount = 3100,
            message = "Love the drawstring idea"
        )
        advanceUntilIdle()

        val engagement = viewModel.uiState.value.inspirationEngagementFor("tee-crop")
        assertEquals(true, engagement?.liked)
        assertEquals(true, engagement?.bookmarked)
        assertEquals(3101, engagement?.likeCount)
        assertEquals(1, engagement?.comments?.size)
        assertEquals("Love the drawstring idea", engagement?.comments?.first()?.message)
    }

    @Test
    fun openPreviewEditor_tracksDraftAndGeneratePreviewFromEditor_startsPreview() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans.first().planId
        viewModel.openPreviewEditor(selectedPlanId)
        viewModel.updatePreviewEditSilhouette(PreviewEditSilhouette.RELAXED)
        viewModel.updatePreviewEditLength(PreviewEditLength.CROPPED)
        viewModel.updatePreviewEditInstructions("尽量保留原图质感")

        val stateBeforeSubmit = viewModel.uiState.value
        assertEquals(selectedPlanId, stateBeforeSubmit.editingPlanId)
        assertEquals(PreviewEditSilhouette.RELAXED, stateBeforeSubmit.previewEditOptions.silhouette)
        assertEquals(PreviewEditLength.CROPPED, stateBeforeSubmit.previewEditOptions.length)

        viewModel.generatePreviewFromEditor()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(selectedPlanId, state.selectedPlanId)
        assertEquals(PreviewJobStatus.COMPLETED, state.previewJob?.status)
        assertTrue(state.previewJob?.results?.first()?.afterImage?.url?.isNotBlank() == true)
    }

    @Test
    fun generatePreviewFromEditor_passesMicroAdjustmentsIntoPreviewRequest() = runTest {
        val repository = CapturingPreviewRepository()
        val viewModel = RemodelViewModel(repository = repository)

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DIY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans.first().planId
        viewModel.openPreviewEditor(selectedPlanId)
        viewModel.updatePreviewEditSilhouette(PreviewEditSilhouette.ASYMMETRIC)
        viewModel.updatePreviewEditLength(PreviewEditLength.CROPPED)
        viewModel.updatePreviewEditFidelity(PreviewEditFidelity.BALANCED)
        viewModel.updatePreviewEditInstructions("保留旧衣纹理，只轻微调整下摆")
        viewModel.generatePreviewFromEditor()
        advanceUntilIdle()

        assertEquals(selectedPlanId, repository.lastPlanId)
        assertEquals(PreviewEditSilhouette.ASYMMETRIC, repository.lastEditOptions?.silhouette)
        assertEquals(PreviewEditLength.CROPPED, repository.lastEditOptions?.length)
        assertEquals(PreviewEditFidelity.BALANCED, repository.lastEditOptions?.fidelity)
        assertEquals("保留旧衣纹理，只轻微调整下摆", repository.lastEditOptions?.extraInstructions)
    }

    @Test
    fun confirmPlan_recoversWhenBackendLosesAnalysisContext() = runTest {
        val repository = RecoveringPreviewRepository()
        val viewModel = RemodelViewModel(repository = repository)

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val originalPlanTitle = viewModel.uiState.value.plans.first().title
        viewModel.confirmPlan(viewModel.uiState.value.plans.first().planId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RemodelStage.PlansReady, state.stage)
        assertEquals(null, state.selectedPlanId)
        assertEquals(null, state.previewJob)
        assertTrue(
            state.previewErrorMessage?.contains("backend session was reset", ignoreCase = true) == true ||
                state.previewErrorMessage?.contains("后端会话已重置") == true
        )
        assertTrue(state.previewErrorMessage?.contains(originalPlanTitle) == true)
        assertTrue(repository.analyzeCalls >= 2)
        assertTrue(repository.generatePlanCalls >= 2)
    }

    @Test
    fun resumePreviewPolling_completesPendingPreviewAfterInitialTimeout() = runTest {
        val repository = PendingPreviewRepository()
        val viewModel = RemodelViewModel(repository = repository)

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg",
                sizeBytes = 180_000
            )
        )
        viewModel.analyzeSelectedImage()
        advanceUntilIdle()

        viewModel.selectIntent(RemodelIntent.DAILY)
        viewModel.generatePlans()
        advanceUntilIdle()

        val selectedPlanId = viewModel.uiState.value.plans.first().planId
        viewModel.confirmPlan(selectedPlanId)
        advanceUntilIdle()

        val pendingState = viewModel.uiState.value
        assertEquals(PreviewJobStatus.RUNNING, pendingState.previewJob?.status)
        assertEquals(PreviewRenderStatus.RUNNING, pendingState.previewFor(selectedPlanId)?.renderStatus)
        assertEquals(selectedPlanId, pendingState.selectedPlanId)
        assertFalse(pendingState.isPreviewLoading)
        assertTrue(
            pendingState.previewErrorMessage?.contains("still processing", ignoreCase = true) == true ||
                pendingState.previewErrorMessage?.contains("仍在后台处理中") == true
        )
        assertEquals(1, repository.createPreviewCalls)
        assertEquals(30, repository.previewFetchCalls)

        viewModel.resumePreviewPolling(selectedPlanId)
        advanceUntilIdle()

        val completedState = viewModel.uiState.value
        assertEquals(1, repository.createPreviewCalls)
        assertEquals(31, repository.previewFetchCalls)
        assertEquals(PreviewJobStatus.COMPLETED, completedState.previewJob?.status)
        assertEquals(
            PreviewRenderStatus.COMPLETED,
            completedState.previewFor(selectedPlanId)?.renderStatus
        )
        assertTrue(completedState.previewFor(selectedPlanId)?.afterImage?.url?.isNotBlank() == true)
        assertEquals(null, completedState.previewErrorMessage)
    }

    @Test
    fun garmentAnalysisDto_serializationAndUnknownWarningFallback_areStable() {
        val dto = AnalyzeGarmentResponseDto(
            analysis = GarmentAnalysisDto(
                analysisId = "analysis-1",
                garmentType = "衬衫",
                color = "白色",
                material = "棉质",
                style = "简约",
                defects = listOf(GarmentDefectDto(name = "袖口磨损", severity = "low")),
                backgroundComplexity = "high",
                confidence = 0.68f,
                warnings = listOf(
                    ProcessingWarningDto(code = "unknown_future_warning", message = "未来新增警告")
                )
            )
        )

        val json = Json.encodeToString(AnalyzeGarmentResponseDto.serializer(), dto)
        val decoded = Json.decodeFromString(AnalyzeGarmentResponseDto.serializer(), json)
        assertEquals("analysis-1", decoded.analysis.analysisId)
        assertEquals(BackgroundComplexity.HIGH, BackgroundComplexity.fromWire(decoded.analysis.backgroundComplexity))
        assertEquals(
            ProcessingWarningCode.UNKNOWN,
            ProcessingWarningCode.fromWire(decoded.analysis.warnings.first().code)
        )
    }
}

private class RecoveringPreviewRepository : RemodelRepository {
    var analyzeCalls = 0
    var generatePlanCalls = 0
    private var previewCreateCalls = 0

    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ): com.scf.secondbloom.domain.model.GarmentAnalysis {
        analyzeCalls += 1
        val suffix = analyzeCalls
        return com.scf.secondbloom.domain.model.GarmentAnalysis(
            analysisId = "analysis-$suffix",
            garmentType = "灰色T恤",
            color = "灰色",
            material = "棉",
            style = "休闲",
            defects = emptyList(),
            backgroundComplexity = BackgroundComplexity.LOW,
            confidence = 0.92f,
            warnings = emptyList()
        )
    }

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> {
        generatePlanCalls += 1
        val seed = generatePlanCalls
        return listOf(
            RemodelPlan(
                planId = "plan-$seed-a",
                title = "Recovered Plan A",
                summary = "summary",
                difficulty = com.scf.secondbloom.domain.model.RemodelDifficulty.EASY,
                materials = listOf("scissors", "thread"),
                estimatedTime = "30 minutes",
                steps = listOf(
                    com.scf.secondbloom.domain.model.RemodelStep("step 1", "detail 1"),
                    com.scf.secondbloom.domain.model.RemodelStep("step 2", "detail 2")
                )
            ),
            RemodelPlan(
                planId = "plan-$seed-b",
                title = "Recovered Plan B",
                summary = "summary",
                difficulty = com.scf.secondbloom.domain.model.RemodelDifficulty.MEDIUM,
                materials = listOf("needle", "thread"),
                estimatedTime = "45 minutes",
                steps = listOf(
                    com.scf.secondbloom.domain.model.RemodelStep("step 1", "detail 1"),
                    com.scf.secondbloom.domain.model.RemodelStep("step 2", "detail 2")
                )
            ),
            RemodelPlan(
                planId = "plan-$seed-c",
                title = "Recovered Plan C",
                summary = "summary",
                difficulty = com.scf.secondbloom.domain.model.RemodelDifficulty.HARD,
                materials = listOf("machine", "fabric"),
                estimatedTime = "60 minutes",
                steps = listOf(
                    com.scf.secondbloom.domain.model.RemodelStep("step 1", "detail 1"),
                    com.scf.secondbloom.domain.model.RemodelStep("step 2", "detail 2")
                )
            )
        )
    }

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: com.scf.secondbloom.domain.model.PreviewEditOptions?
    ): com.scf.secondbloom.domain.model.GeneratePreviewJobResult {
        previewCreateCalls += 1
        throw ModelResponseException("analysisId 不存在，无法生成预览。")
    }

    override suspend fun getPreviewJob(previewJobId: String): com.scf.secondbloom.domain.model.PreviewJobSnapshot {
        error("not used in this test")
    }
}

private class PendingPreviewRepository : RemodelRepository {
    private val analysis = com.scf.secondbloom.domain.model.GarmentAnalysis(
        analysisId = "analysis-pending",
        garmentType = "灰色T恤",
        color = "灰色",
        material = "棉",
        style = "休闲",
        defects = emptyList(),
        backgroundComplexity = BackgroundComplexity.LOW,
        confidence = 0.95f,
        warnings = emptyList()
    )

    private val plans = listOf(
        RemodelPlan(
            planId = "plan-pending-1",
            title = "Pending Plan",
            summary = "summary",
            difficulty = com.scf.secondbloom.domain.model.RemodelDifficulty.MEDIUM,
            materials = listOf("needle", "thread"),
            estimatedTime = "45 minutes",
            steps = listOf(
                com.scf.secondbloom.domain.model.RemodelStep("step 1", "detail 1"),
                com.scf.secondbloom.domain.model.RemodelStep("step 2", "detail 2")
            )
        )
    )

    var createPreviewCalls = 0
        private set

    var previewFetchCalls = 0
        private set

    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ) = analysis

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> = plans

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: PreviewEditOptions?
    ): GeneratePreviewJobResult = GeneratePreviewJobResult(
        previewJobId = "preview-job-pending",
        status = "queued",
        requestedPlanCount = 1,
        pollPath = "/remodel-preview-jobs/preview-job-pending"
    ).also {
        createPreviewCalls += 1
    }

    override suspend fun getPreviewJob(previewJobId: String): PreviewJobSnapshot {
        previewFetchCalls += 1
        return if (previewFetchCalls <= 30) {
            PreviewJobSnapshot(
                previewJobId = previewJobId,
                analysisId = analysis.analysisId,
                status = PreviewJobStatus.RUNNING,
                requestedPlanCount = 1,
                completedPlanCount = 0,
                failedPlanCount = 0,
                results = listOf(
                    com.scf.secondbloom.domain.model.PlanPreviewResult(
                        planId = plans.first().planId,
                        renderStatus = PreviewRenderStatus.RUNNING,
                        disclaimer = "AI visual simulation only. Final garment may differ."
                    )
                ),
                pollPath = "/remodel-preview-jobs/$previewJobId"
            )
        } else {
            PreviewJobSnapshot(
                previewJobId = previewJobId,
                analysisId = analysis.analysisId,
                status = PreviewJobStatus.COMPLETED,
                requestedPlanCount = 1,
                completedPlanCount = 1,
                failedPlanCount = 0,
                results = listOf(
                    com.scf.secondbloom.domain.model.PlanPreviewResult(
                        planId = plans.first().planId,
                        renderStatus = PreviewRenderStatus.COMPLETED,
                        beforeImage = PreviewAsset("before-1", "https://example.com/before.png", "2099-01-01T00:00:00Z"),
                        afterImage = PreviewAsset("after-1", "https://example.com/after.png", "2099-01-01T00:00:00Z"),
                        comparisonImage = PreviewAsset("compare-1", "https://example.com/compare.png", "2099-01-01T00:00:00Z"),
                        disclaimer = "AI visual simulation only. Final garment may differ."
                    )
                ),
                pollPath = "/remodel-preview-jobs/$previewJobId"
            )
        }
    }
}

private class CountingRemodelRepository(
    private val delegate: RemodelRepository? = null,
    private val analyzeFailure: Exception? = null,
    private val generatePlansFailure: Exception? = null,
    private val createPreviewFailure: Exception? = null,
    private val getPreviewFailure: Exception? = null
) : RemodelRepository {
    var analyzeCalls: Int = 0
        private set
    var generatePlanCalls: Int = 0
        private set
    var createPreviewCalls: Int = 0
        private set
    var getPreviewCalls: Int = 0
        private set

    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ): com.scf.secondbloom.domain.model.GarmentAnalysis {
        analyzeCalls += 1
        analyzeFailure?.let { throw it }
        return requireNotNull(delegate) { "delegate is required when no failure is configured" }
            .analyze(image, responseLanguage)
    }

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> {
        generatePlanCalls += 1
        generatePlansFailure?.let { throw it }
        return requireNotNull(delegate) { "delegate is required when no failure is configured" }
            .generatePlans(intent, confirmedAnalysis, userPreferences, responseLanguage)
    }

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: PreviewEditOptions?
    ): GeneratePreviewJobResult {
        createPreviewCalls += 1
        createPreviewFailure?.let { throw it }
        return requireNotNull(delegate) { "delegate is required when no failure is configured" }
            .createPreviewJob(analysisId, planId, editOptions)
    }

    override suspend fun getPreviewJob(previewJobId: String): PreviewJobSnapshot {
        getPreviewCalls += 1
        getPreviewFailure?.let { throw it }
        return requireNotNull(delegate) { "delegate is required when no failure is configured" }
            .getPreviewJob(previewJobId)
    }
}

private class RecordingHistorySyncApi : HistorySyncApi {
    var bootstrapCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set

    override suspend fun getMe(accessToken: String): com.scf.secondbloom.data.historysync.UserProfileDto {
        error("unused")
    }

    override suspend fun getHistory(accessToken: String): HistoryEnvelopeDto {
        return HistoryEnvelopeDto(
            revision = 1L,
            snapshot = HistorySnapshotPayload()
        )
    }

    override suspend fun bootstrapHistory(
        accessToken: String,
        request: BootstrapHistoryRequestDto
    ): BootstrapHistoryResponseDto {
        bootstrapCalls += 1
        return BootstrapHistoryResponseDto(
            revision = bootstrapCalls.toLong(),
            snapshot = request.snapshot,
            mergeApplied = bootstrapCalls > 1
        )
    }

    override suspend fun updateHistory(
        accessToken: String,
        request: UpdateHistoryRequestDto
    ): HistoryEnvelopeDto {
        updateCalls += 1
        return HistoryEnvelopeDto(
            revision = updateCalls.toLong(),
            snapshot = request.snapshot
        )
    }
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

private class FakeHistoryRepository : RemodelHistoryRepository {
    private val analyses = mutableListOf<SavedAnalysisRecord>()
    private val plans = mutableListOf<SavedPlanGenerationRecord>()
    private val publishedRemodels = mutableListOf<com.scf.secondbloom.domain.model.PublishedRemodelRecord>()
    private val inspirationEngagements = mutableListOf<InspirationEngagementRecord>()

    override suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        savedAtEpochMillis: Long
    ): SavedAnalysisRecord {
        val record = SavedAnalysisRecord(
            recordId = analysis.analysisId,
            savedAtEpochMillis = savedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis
        )
        analyses.removeAll { it.recordId == record.recordId }
        analyses.add(0, record)
        return record
    }

    override suspend fun savePlanGeneration(
        sourceImage: SelectedImage,
        analysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        intent: RemodelIntent,
        userPreferences: String,
        plans: List<RemodelPlan>,
        savedAtEpochMillis: Long
    ): SavedPlanGenerationRecord {
        val record = SavedPlanGenerationRecord(
            recordId = "${analysis.analysisId}-${intent.name}",
            savedAtEpochMillis = savedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis,
            intent = intent,
            userPreferences = userPreferences,
            plans = plans
        )
        this.plans.add(0, record)
        return record
    }

    override suspend fun getLatestAnalysis(): SavedAnalysisRecord? = analyses.firstOrNull()

    override suspend fun getLatestPlanGeneration(): SavedPlanGenerationRecord? = plans.firstOrNull()

    override suspend fun getRecentAnalyses(limit: Int): List<SavedAnalysisRecord> = analyses.take(limit)

    override suspend fun getRecentPlanGenerations(limit: Int): List<SavedPlanGenerationRecord> = plans.take(limit)

    override suspend fun savePublishedRemodel(
        sourceImage: SelectedImage,
        analysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        intent: RemodelIntent,
        selectedPlan: RemodelPlan,
        previewResult: com.scf.secondbloom.domain.model.PlanPreviewResult,
        publishedAtEpochMillis: Long
    ): com.scf.secondbloom.domain.model.PublishedRemodelRecord {
        val record = com.scf.secondbloom.domain.model.PublishedRemodelRecord(
            recordId = "published-${selectedPlan.planId}",
            publishedAtEpochMillis = publishedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis,
            intent = intent,
            selectedPlan = selectedPlan,
            previewResult = previewResult
        )
        publishedRemodels.removeAll { it.selectedPlan.planId == selectedPlan.planId }
        publishedRemodels.add(0, record)
        return record
    }

    override suspend fun getRecentPublishedRemodels(limit: Int): List<com.scf.secondbloom.domain.model.PublishedRemodelRecord> =
        publishedRemodels.take(limit)

    override suspend fun saveInspirationEngagement(
        record: InspirationEngagementRecord
    ): InspirationEngagementRecord {
        inspirationEngagements.removeAll { it.itemId == record.itemId }
        inspirationEngagements.add(0, record)
        return record
    }

    override suspend fun getInspirationEngagements(limit: Int): List<InspirationEngagementRecord> =
        inspirationEngagements.take(limit)
}

private class CapturingPreviewRepository : RemodelRepository {
    private val delegate = DefaultRemodelRepository(MockRemodelApi())

    var lastPlanId: String? = null
    var lastEditOptions: PreviewEditOptions? = null

    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ) = delegate.analyze(image, responseLanguage)

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: com.scf.secondbloom.domain.model.GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> = delegate.generatePlans(
        intent,
        confirmedAnalysis,
        userPreferences,
        responseLanguage
    )

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: PreviewEditOptions?
    ): com.scf.secondbloom.domain.model.GeneratePreviewJobResult {
        lastPlanId = planId
        lastEditOptions = editOptions
        return delegate.createPreviewJob(analysisId, planId, editOptions)
    }

    override suspend fun getPreviewJob(previewJobId: String) = delegate.getPreviewJob(previewJobId)
}
