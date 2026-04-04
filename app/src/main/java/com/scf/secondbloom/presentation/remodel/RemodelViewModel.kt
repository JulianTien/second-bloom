package com.scf.secondbloom.presentation.remodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.scf.secondbloom.BuildConfig
import com.scf.secondbloom.auth.ClerkHistoryAuthTokenProvider
import com.scf.secondbloom.auth.SecondBloomClerkConfig
import com.scf.secondbloom.data.local.AppPreferencesRepository
import com.scf.secondbloom.data.local.AppPreferencesRepositoryFactory
import com.scf.secondbloom.data.local.NoOpRemodelHistoryRepository
import com.scf.secondbloom.data.local.RemodelHistoryRepository
import com.scf.secondbloom.data.local.RemodelHistoryRepositoryFactory
import com.scf.secondbloom.data.local.RemodelHistorySyncRepositoryFactory
import com.scf.secondbloom.data.remote.InvalidImageException
import com.scf.secondbloom.data.remote.ModelResponseException
import com.scf.secondbloom.data.remote.mock.MockRemodelApi
import com.scf.secondbloom.data.repository.DefaultRemodelRepository
import com.scf.secondbloom.data.repository.RemodelRepository
import com.scf.secondbloom.data.repository.RemodelRepositoryFactory
import com.scf.secondbloom.data.historysync.HistorySyncRepository
import com.scf.secondbloom.data.historysync.HistorySyncResult
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.DemoScenario
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.InspirationComment
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PreviewEditFidelity
import com.scf.secondbloom.domain.model.PreviewEditLength
import com.scf.secondbloom.domain.model.PreviewEditNeckline
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewEditSilhouette
import com.scf.secondbloom.domain.model.PreviewEditSleeve
import com.scf.secondbloom.domain.model.PreviewJobStatus
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.RemodelError
import com.scf.secondbloom.domain.model.RemodelErrorType
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelStage
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.SelectedImage
import com.scf.secondbloom.domain.model.deriveRecentActivities
import com.scf.secondbloom.domain.model.deriveSustainabilityImpactSummary
import com.scf.secondbloom.domain.model.deriveWardrobeCategories
import com.scf.secondbloom.domain.model.deriveWardrobeEntries
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

private const val LowConfidenceThreshold = 0.75f
private const val PreviewPollDelayMillis = 900L
private const val PreviewMaxPollAttempts = 6
private val ActivePreviewStatuses = setOf(PreviewJobStatus.QUEUED, PreviewJobStatus.RUNNING)
private val ActivePreviewRenderStatuses = setOf(PreviewRenderStatus.QUEUED, PreviewRenderStatus.RUNNING)

class RemodelViewModel(
    private val repository: RemodelRepository,
    private val historyRepository: RemodelHistoryRepository = NoOpRemodelHistoryRepository,
    private val historySyncRepositoryFactory: ((String) -> HistorySyncRepository)? = null,
    private val authUserIdFlow: Flow<String?> = if (SecondBloomClerkConfig.isConfigured) {
        Clerk.userFlow.map { user -> user?.id }
    } else {
        flowOf(null)
    },
    private val appPreferencesRepository: AppPreferencesRepository = object : AppPreferencesRepository {
        override fun getAppLanguage(): AppLanguage = AppLanguage.ENGLISH

        override fun setAppLanguage(language: AppLanguage) = Unit
    }
) : ViewModel() {

    private var activeHistorySyncRepository: HistorySyncRepository? = null
    private var activeHistorySyncUserId: String? = null
    private var activeHistorySyncJob: Job? = null
    private var pendingHistorySyncAfterActiveJob: Boolean = false

    private val _uiState = MutableStateFlow(
        RemodelUiState(appLanguage = appPreferencesRepository.getAppLanguage())
    )
    val uiState: StateFlow<RemodelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshHistory()
        }
        observeAuthState()
    }

    fun onImageSelected(image: SelectedImage) {
        _uiState.update { current ->
            current.copy(
                stage = RemodelStage.ImageSelected,
                selectedImage = image,
                analysis = null,
                draftAnalysis = null,
                selectedIntent = null,
                userPreferences = "",
                plans = emptyList(),
                selectedPlanId = null,
                editingPlanId = null,
                previewEditOptions = PreviewEditOptions(),
                previewJob = null,
                isPreviewLoading = false,
                previewErrorMessage = null,
                error = null
            )
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        if (_uiState.value.appLanguage == language) {
            return
        }
        appPreferencesRepository.setAppLanguage(language)
        _uiState.update { it.copy(appLanguage = language) }
        viewModelScope.launch {
            refreshHistory()
        }
    }

    fun loadDemoScenario(scenario: DemoScenario) {
        onImageSelected(scenario.toSelectedImage())
    }

    fun analyzeSelectedImage() {
        val image = _uiState.value.selectedImage
        if (image == null) {
            val language = _uiState.value.appLanguage
            setError(
                stage = RemodelStage.InvalidImage,
                error = RemodelError(
                    type = RemodelErrorType.INVALID_IMAGE,
                    message = tr(
                        language,
                        "Choose a garment photo before starting analysis.",
                        "请先选择一张旧衣图片。"
                    )
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stage = RemodelStage.Analyzing,
                    plans = emptyList(),
                    error = null
                )
            }

            try {
                val analysis = repository.analyze(image, _uiState.value.appLanguage)
                historyRepository.saveAnalysis(image, analysis)
                refreshHistory()
                syncHistoryInBackground()

                val nextStage = if (
                    analysis.backgroundComplexity == BackgroundComplexity.HIGH ||
                    analysis.confidence < LowConfidenceThreshold
                ) {
                    RemodelStage.LowConfidence
                } else {
                    RemodelStage.AnalysisReady
                }

                _uiState.update {
                    it.copy(
                        stage = nextStage,
                        analysis = analysis,
                        draftAnalysis = analysis,
                        plans = emptyList(),
                        error = null
                    )
                }
            } catch (exception: InvalidImageException) {
                setError(
                    stage = RemodelStage.InvalidImage,
                    error = RemodelError(
                        RemodelErrorType.INVALID_IMAGE,
                        exception.message.orEmpty()
                    )
                )
            } catch (exception: IOException) {
                setError(
                    stage = RemodelStage.NetworkError,
                    error = RemodelError(RemodelErrorType.NETWORK_ERROR, exception.message.orEmpty())
                )
            } catch (exception: ModelResponseException) {
                setError(
                    stage = RemodelStage.ModelError,
                    error = RemodelError(RemodelErrorType.MODEL_ERROR, exception.message.orEmpty())
                )
            }
        }
    }

    fun continueWithLowConfidence() {
        val draftAnalysis = _uiState.value.draftAnalysis ?: return
        _uiState.update {
            it.copy(
                stage = RemodelStage.AnalysisReady,
                analysis = draftAnalysis,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(
                stage = when {
                    state.plans.isNotEmpty() -> RemodelStage.PlansReady
                    state.draftAnalysis != null -> RemodelStage.AnalysisReady
                    state.selectedImage != null -> RemodelStage.ImageSelected
                    else -> RemodelStage.Idle
                },
                error = null
            )
        }
    }

    fun updateGarmentType(value: String) = updateDraftAnalysis { it.copy(garmentType = value) }

    fun updateColor(value: String) = updateDraftAnalysis { it.copy(color = value) }

    fun updateMaterial(value: String) = updateDraftAnalysis { it.copy(material = value) }

    fun updateStyle(value: String) = updateDraftAnalysis { it.copy(style = value) }

    fun updateDefects(rawValue: String) {
        val defects = rawValue.split(",", "，")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { GarmentDefect(name = it) }
        updateDraftAnalysis { it.copy(defects = defects) }
    }

    fun selectIntent(intent: RemodelIntent) {
        _uiState.update { state ->
            state.copy(
                selectedIntent = intent,
                stage = if (state.plans.isNotEmpty()) RemodelStage.EditingAnalysis else state.stage,
                plans = if (state.plans.isNotEmpty()) emptyList() else state.plans,
                selectedPlanId = null,
                editingPlanId = null,
                previewEditOptions = PreviewEditOptions(),
                previewJob = null,
                isPreviewLoading = false,
                previewErrorMessage = null
            )
        }
    }

    fun updateUserPreferences(value: String) {
        _uiState.update { state ->
            state.copy(
                userPreferences = value,
                stage = if (state.plans.isNotEmpty()) RemodelStage.EditingAnalysis else state.stage,
                selectedPlanId = null,
                editingPlanId = null,
                previewEditOptions = PreviewEditOptions(),
                previewJob = null,
                isPreviewLoading = false,
                previewErrorMessage = null,
                plans = if (state.plans.isNotEmpty()) emptyList() else state.plans
            )
        }
    }

    fun generatePlans() {
        val analysis = _uiState.value.draftAnalysis ?: return
        val intent = _uiState.value.selectedIntent ?: return
        val sourceImage = _uiState.value.selectedImage ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stage = RemodelStage.GeneratingPlans,
                    plans = emptyList(),
                    error = null
                )
            }

            try {
                val plans = repository.generatePlans(
                    intent = intent,
                    confirmedAnalysis = analysis,
                    userPreferences = _uiState.value.userPreferences,
                    responseLanguage = _uiState.value.appLanguage
                )

                if (plans.isEmpty()) {
                    throw ModelResponseException(
                        tr(
                            _uiState.value.appLanguage,
                            "The server did not return any usable remodel plans.",
                            "服务端未返回可用改制方案。"
                        )
                    )
                }

                historyRepository.savePlanGeneration(
                    sourceImage = sourceImage,
                    analysis = analysis,
                    intent = intent,
                    userPreferences = _uiState.value.userPreferences,
                    plans = plans
                )
                refreshHistory()
                syncHistoryInBackground()

                _uiState.update {
                    it.copy(
                        stage = RemodelStage.PlansReady,
                        analysis = analysis,
                        draftAnalysis = analysis,
                        plans = plans,
                        selectedPlanId = null,
                        editingPlanId = null,
                        previewEditOptions = PreviewEditOptions(),
                        previewJob = null,
                        isPreviewLoading = false,
                        previewErrorMessage = null,
                        error = null
                    )
                }
            } catch (exception: IOException) {
                setError(
                    stage = RemodelStage.NetworkError,
                    error = RemodelError(RemodelErrorType.NETWORK_ERROR, exception.message.orEmpty())
                )
            } catch (exception: ModelResponseException) {
                setError(
                    stage = RemodelStage.ModelError,
                    error = RemodelError(RemodelErrorType.MODEL_ERROR, exception.message.orEmpty())
                )
            }
        }
    }

    fun openPreviewEditor(planId: String) {
        val selectedPlan = _uiState.value.plans.firstOrNull { it.planId == planId } ?: return
        _uiState.update {
            it.copy(
                selectedPlanId = selectedPlan.planId,
                editingPlanId = selectedPlan.planId,
                previewEditOptions = if (it.editingPlanId == selectedPlan.planId) {
                    it.previewEditOptions
                } else {
                    PreviewEditOptions()
                },
                previewErrorMessage = null,
                error = null
            )
        }
    }

    fun closePreviewEditor() {
        _uiState.update {
            it.copy(
                editingPlanId = null
            )
        }
    }

    fun updatePreviewEditSilhouette(value: PreviewEditSilhouette) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(silhouette = value)) }
    }

    fun updatePreviewEditLength(value: PreviewEditLength) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(length = value)) }
    }

    fun updatePreviewEditNeckline(value: PreviewEditNeckline) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(neckline = value)) }
    }

    fun updatePreviewEditSleeve(value: PreviewEditSleeve) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(sleeve = value)) }
    }

    fun updatePreviewEditFidelity(value: PreviewEditFidelity) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(fidelity = value)) }
    }

    fun updatePreviewEditInstructions(value: String) {
        _uiState.update { it.copy(previewEditOptions = it.previewEditOptions.copy(extraInstructions = value)) }
    }

    fun confirmPlan(planId: String) {
        val analysis = _uiState.value.draftAnalysis ?: return
        val selectedPlan = _uiState.value.plans.firstOrNull { it.planId == planId } ?: return
        val editOptions = if (_uiState.value.editingPlanId == selectedPlan.planId) {
            _uiState.value.previewEditOptions
        } else {
            PreviewEditOptions()
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedPlanId = selectedPlan.planId,
                    previewJob = null,
                    isPreviewLoading = true,
                    previewErrorMessage = null,
                    error = null
                )
            }
            startPreviewGeneration(
                analysis = analysis,
                planId = selectedPlan.planId,
                selectedPlanTitle = selectedPlan.title,
                editOptions = editOptions
            )
        }
    }

    fun generatePreviewFromEditor() {
        val editingPlanId = _uiState.value.editingPlanId ?: return
        confirmPlan(editingPlanId)
    }

    fun resumePreviewPolling(planId: String) {
        val state = _uiState.value
        val previewJob = state.previewJob ?: return
        val preview = state.previewFor(planId) ?: return
        if (state.isPreviewLoading) {
            return
        }
        if (previewJob.status !in ActivePreviewStatuses &&
            preview.renderStatus !in ActivePreviewRenderStatuses
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedPlanId = planId,
                    isPreviewLoading = true,
                    previewErrorMessage = null,
                    error = null
                )
            }
            pollPreviewJob(previewJobId = previewJob.previewJobId)
        }
    }

    fun publishPreviewResult(planId: String) {
        val state = _uiState.value
        val selectedImage = state.selectedImage ?: return
        val analysis = state.analysis ?: state.draftAnalysis ?: return
        val selectedIntent = state.selectedIntent ?: return
        val selectedPlan = state.plans.firstOrNull { it.planId == planId } ?: return
        val preview = state.previewFor(planId) ?: return

        if (preview.renderStatus != PreviewRenderStatus.COMPLETED) {
            return
        }

        viewModelScope.launch {
            historyRepository.savePublishedRemodel(
                sourceImage = selectedImage,
                analysis = analysis,
                intent = selectedIntent,
                selectedPlan = selectedPlan,
                previewResult = preview
            )
            refreshHistory()
            syncHistoryInBackground()
        }
    }

    fun toggleInspirationLike(itemId: String, baseLikeCount: Int) {
        viewModelScope.launch {
            val current = _uiState.value.inspirationEngagementFor(itemId)
            val nextLiked = !(current?.liked ?: false)
            val nextLikeCount = if (current == null) {
                if (nextLiked) baseLikeCount + 1 else baseLikeCount
            } else if (nextLiked) {
                current.likeCount + 1
            } else {
                (current.likeCount - 1).coerceAtLeast(0)
            }
            saveInspirationEngagement(
                InspirationEngagementRecord(
                    itemId = itemId,
                    liked = nextLiked,
                    bookmarked = current?.bookmarked ?: false,
                    likeCount = nextLikeCount,
                    comments = current?.comments ?: emptyList()
                )
            )
        }
    }

    fun toggleInspirationBookmark(itemId: String, baseLikeCount: Int) {
        viewModelScope.launch {
            val current = _uiState.value.inspirationEngagementFor(itemId)
            saveInspirationEngagement(
                InspirationEngagementRecord(
                    itemId = itemId,
                    liked = current?.liked ?: false,
                    bookmarked = !(current?.bookmarked ?: false),
                    likeCount = current?.likeCount ?: baseLikeCount,
                    comments = current?.comments ?: emptyList()
                )
            )
        }
    }

    fun addInspirationComment(itemId: String, baseLikeCount: Int, message: String) {
        val trimmed = message.trim()
        if (trimmed.isBlank()) {
            return
        }

        viewModelScope.launch {
            val current = _uiState.value.inspirationEngagementFor(itemId)
            saveInspirationEngagement(
                InspirationEngagementRecord(
                    itemId = itemId,
                    liked = current?.liked ?: false,
                    bookmarked = current?.bookmarked ?: false,
                    likeCount = current?.likeCount ?: baseLikeCount,
                    comments = listOf(
                        InspirationComment(
                            commentId = "comment-${UUID.randomUUID()}",
                            authorName = tr(_uiState.value.appLanguage, "You", "你"),
                            message = trimmed,
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    ) + (current?.comments ?: emptyList())
                )
            )
        }
    }

    fun restoreSavedPlanGeneration(recordId: String): Boolean {
        val savedRecord = _uiState.value.recentPlanGenerationRecords
            .firstOrNull { it.recordId == recordId }
            ?: return false

        _uiState.update {
            it.copy(
                stage = RemodelStage.PlansReady,
                selectedImage = savedRecord.sourceImage,
                analysis = savedRecord.analysis,
                draftAnalysis = savedRecord.analysis,
                selectedIntent = savedRecord.intent,
                userPreferences = savedRecord.userPreferences,
                plans = savedRecord.plans,
                selectedPlanId = null,
                editingPlanId = null,
                previewEditOptions = PreviewEditOptions(),
                previewJob = null,
                isPreviewLoading = false,
                previewErrorMessage = null,
                error = null
            )
        }
        return true
    }

    private fun updateDraftAnalysis(transform: (GarmentAnalysis) -> GarmentAnalysis) {
        _uiState.update { state ->
            val current = state.draftAnalysis ?: return@update state
            val updated = transform(current)
            state.copy(
                draftAnalysis = updated,
                plans = emptyList(),
                selectedPlanId = null,
                editingPlanId = null,
                previewEditOptions = PreviewEditOptions(),
                previewJob = null,
                isPreviewLoading = false,
                previewErrorMessage = null,
                stage = RemodelStage.EditingAnalysis
            )
        }
    }

    private suspend fun startPreviewGeneration(
        analysis: GarmentAnalysis,
        planId: String,
        selectedPlanTitle: String,
        editOptions: PreviewEditOptions
    ) {
        if (planId.isBlank()) {
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    previewErrorMessage = tr(
                        it.appLanguage,
                        "The plan exists, but the current result is missing a preview identifier.",
                        "方案已生成，但当前结果缺少可视化预览标识。"
                    )
                )
            }
            return
        }

        try {
            val createResult = repository.createPreviewJob(
                analysisId = analysis.analysisId,
                planId = planId,
                editOptions = editOptions
            )
            pollPreviewJob(previewJobId = createResult.previewJobId)
        } catch (exception: IOException) {
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    previewErrorMessage = exception.message.orEmpty().ifBlank {
                        tr(it.appLanguage, "The plan is confirmed, but the final image request failed.", "方案已确认，但最终效果图请求失败。")
                    }
                )
            }
        } catch (exception: InvalidImageException) {
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    previewErrorMessage = exception.message.orEmpty().ifBlank {
                        tr(it.appLanguage, "The server rejected the final image request. Adjust the tuning options and try again.", "最终效果图请求被服务端拒绝，请调整微调参数后重试。")
                    }
                )
            }
        } catch (exception: ModelResponseException) {
            if (isMissingPreviewContextError(exception.message.orEmpty()) &&
                recoverPreviewContextAfterBackendReset(selectedPlanTitle)
            ) {
                return
            }
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    previewErrorMessage = exception.message.orEmpty().ifBlank {
                        tr(it.appLanguage, "The plan is confirmed, but the final image response was invalid.", "方案已确认，但最终效果图返回异常。")
                    }
                )
            }
        }
    }

    private suspend fun pollPreviewJob(previewJobId: String) {
        var reachedTerminalState = false
        repeat(PreviewMaxPollAttempts) { attempt ->
            if (attempt > 0) {
                delay(PreviewPollDelayMillis)
            }
            val previewJob = repository.getPreviewJob(previewJobId)
            val isTerminal = previewJob.status !in ActivePreviewStatuses
            val filteredMessage = previewJob.results
                .firstOrNull { it.renderStatus == PreviewRenderStatus.FILTERED }
                ?.errorMessage
            _uiState.update {
                it.copy(
                    previewJob = previewJob,
                    isPreviewLoading = !isTerminal,
                    previewErrorMessage = when {
                        previewJob.status == PreviewJobStatus.FAILED && filteredMessage != null ->
                            filteredMessage
                        previewJob.status == PreviewJobStatus.FAILED ->
                            tr(it.appLanguage, "The plan is confirmed, but final image generation failed. Please try again shortly.", "方案已确认，但最终效果图生成失败，请稍后重试。")
                        previewJob.status == PreviewJobStatus.EXPIRED ->
                            tr(it.appLanguage, "The image job expired. Please generate it again.", "效果图任务已过期，请重新生成。")
                        previewJob.status == PreviewJobStatus.COMPLETED_WITH_FAILURES ->
                            filteredMessage ?: tr(it.appLanguage, "The final image did not pass validation, so it will not be shown this time.", "最终效果图未通过校验，本次不展示结果。")
                        else -> null
                    }
                )
            }
            if (isTerminal) {
                reachedTerminalState = true
                return
            }
        }
        if (!reachedTerminalState) {
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    previewErrorMessage = tr(
                        it.appLanguage,
                        "The plan is confirmed and the final image is still processing in the background.",
                        "方案已确认，最终效果图仍在后台处理中。"
                    )
                )
            }
        }
    }

    private suspend fun recoverPreviewContextAfterBackendReset(selectedPlanTitle: String): Boolean {
        val state = _uiState.value
        val selectedImage = state.selectedImage ?: return false
        val selectedIntent = state.selectedIntent ?: return false
        val currentPreferences = state.userPreferences

        return try {
            val language = _uiState.value.appLanguage
            val refreshedAnalysis = repository.analyze(selectedImage, language)
            val refreshedPlans = repository.generatePlans(
                intent = selectedIntent,
                confirmedAnalysis = refreshedAnalysis,
                userPreferences = currentPreferences,
                responseLanguage = language
            )
            _uiState.update {
                it.copy(
                    stage = RemodelStage.PlansReady,
                    analysis = refreshedAnalysis,
                    draftAnalysis = refreshedAnalysis,
                    plans = refreshedPlans,
                    selectedPlanId = null,
                    editingPlanId = null,
                    previewEditOptions = PreviewEditOptions(),
                    previewJob = null,
                    isPreviewLoading = false,
                    previewErrorMessage = buildPreviewContextResetMessage(
                        previousPlanTitle = selectedPlanTitle,
                        refreshedPlans = refreshedPlans
                    ),
                    error = null
                )
            }
            true
        } catch (_: Exception) {
            _uiState.update {
                it.copy(
                    isPreviewLoading = false,
                    selectedPlanId = null,
                    previewJob = null,
                    previewErrorMessage = tr(
                        it.appLanguage,
                        "The backend session was reset. Re-run analysis for the current image before generating the final image again.",
                        "后端会话已重置，请重新识别当前图片后再生成最终效果图。"
                    )
                )
            }
            true
        }
    }

    private fun isMissingPreviewContextError(message: String): Boolean {
        val normalized = message.lowercase()
        return normalized.contains("analysisid 不存在") ||
            normalized.contains("planid 不存在")
    }

    private fun buildPreviewContextResetMessage(
        previousPlanTitle: String,
        refreshedPlans: List<RemodelPlan>
    ): String {
        val language = _uiState.value.appLanguage
        val matchedPlan = refreshedPlans.firstOrNull { it.title == previousPlanTitle }
        return if (matchedPlan != null) {
            tr(
                language,
                "The backend session was reset and the current image plus plans were synced again. Confirm \"${matchedPlan.title}\" or choose another plan before generating the final image.",
                "后端会话已重置，已重新同步当前图片与方案。请重新确认“${matchedPlan.title}”或选择其他方案后，再生成最终效果图。"
            )
        } else {
            tr(
                language,
                "The backend session was reset and the current image plus plans were synced again. Confirm a plan again before generating the final image.",
                "后端会话已重置，已重新同步当前图片与方案。请重新确认一个方案后，再生成最终效果图。"
            )
        }
    }

    private suspend fun refreshHistory() {
        val language = _uiState.value.appLanguage
        val latestAnalysis = historyRepository.getLatestAnalysis()
        val latestPlanGeneration = historyRepository.getLatestPlanGeneration()
        val recentAnalyses = historyRepository.getRecentAnalyses()
        val recentPlanGenerations = historyRepository.getRecentPlanGenerations()
        val recentPublishedRemodels = historyRepository.getRecentPublishedRemodels()
        val inspirationEngagements = historyRepository.getInspirationEngagements(limit = 100)

        _uiState.update {
            val wardrobeEntries = deriveWardrobeEntries(
                recentAnalyses,
                recentPlanGenerations,
                language
            )
            it.copy(
                latestAnalysisRecord = latestAnalysis,
                latestPlanGenerationRecord = latestPlanGeneration,
                recentAnalysisRecords = recentAnalyses,
                recentPlanGenerationRecords = recentPlanGenerations,
                publishedRemodelRecords = recentPublishedRemodels,
                inspirationEngagementRecords = inspirationEngagements,
                wardrobeEntries = wardrobeEntries,
                wardrobeCategories = deriveWardrobeCategories(wardrobeEntries, language),
                sustainabilitySummary = deriveSustainabilityImpactSummary(
                    recentAnalyses = recentAnalyses,
                    recentPlanGenerations = recentPlanGenerations,
                    language = language
                ),
                recentActivities = deriveRecentActivities(
                    recentAnalyses = recentAnalyses,
                    recentPlanGenerations = recentPlanGenerations,
                    language = language
                )
            )
        }
    }

    private suspend fun saveInspirationEngagement(record: InspirationEngagementRecord) {
        historyRepository.saveInspirationEngagement(record)
        refreshHistory()
        syncHistoryInBackground()
    }

    private fun observeAuthState() {
        if (historySyncRepositoryFactory == null) {
            return
        }

        viewModelScope.launch {
            authUserIdFlow.collect { userId ->
                handleAuthUserChanged(userId)
            }
        }
    }

    private suspend fun handleAuthUserChanged(userId: String?) {
        val normalizedUserId = userId?.trim().orEmpty()
        if (normalizedUserId == activeHistorySyncUserId.orEmpty()) {
            return
        }

        if (normalizedUserId.isBlank()) {
            clearActiveHistorySyncContext()
            return
        }

        clearActiveHistorySyncContext()

        val repository = historySyncRepositoryFactory?.invoke(normalizedUserId) ?: return
        activeHistorySyncUserId = normalizedUserId
        activeHistorySyncRepository = repository

        startActiveHistorySyncJob {
            when (repository.bootstrapLocalHistory()) {
                HistorySyncResult.SkippedNoAuth -> return@startActiveHistorySyncJob
                HistorySyncResult.SkippedAlreadyBootstrapped -> repository.refreshLocalHistory()
                is HistorySyncResult.Synced -> Unit
            }
            if (activeHistorySyncUserId == normalizedUserId) {
                refreshHistory()
            }
        }
    }

    private fun syncHistoryInBackground() {
        val repository = activeHistorySyncRepository ?: return
        val currentUserId = activeHistorySyncUserId ?: return
        if (activeHistorySyncJob?.isActive == true) {
            pendingHistorySyncAfterActiveJob = true
            return
        }
        startActiveHistorySyncJob {
            when (repository.bootstrapLocalHistory()) {
                HistorySyncResult.SkippedNoAuth -> return@startActiveHistorySyncJob
                HistorySyncResult.SkippedAlreadyBootstrapped -> Unit
                is HistorySyncResult.Synced -> Unit
            }
            if (currentUserId != activeHistorySyncUserId) {
                return@startActiveHistorySyncJob
            }
            when (repository.pushLocalHistory()) {
                HistorySyncResult.SkippedNoAuth -> Unit
                HistorySyncResult.SkippedAlreadyBootstrapped -> Unit
                is HistorySyncResult.Synced -> {
                    if (currentUserId == activeHistorySyncUserId) {
                        refreshHistory()
                    }
                }
            }
        }
    }

    private fun startActiveHistorySyncJob(block: suspend () -> Unit) {
        if (activeHistorySyncJob?.isActive == true) {
            return
        }

        val job = viewModelScope.launch {
            block()
        }
        activeHistorySyncJob = job
        job.invokeOnCompletion {
            if (activeHistorySyncJob == job) {
                activeHistorySyncJob = null
            }
            if (
                pendingHistorySyncAfterActiveJob &&
                activeHistorySyncRepository != null &&
                activeHistorySyncUserId != null
            ) {
                pendingHistorySyncAfterActiveJob = false
                syncHistoryInBackground()
            }
        }
    }

    private fun clearActiveHistorySyncContext() {
        activeHistorySyncJob?.cancel()
        activeHistorySyncJob = null
        pendingHistorySyncAfterActiveJob = false
        activeHistorySyncUserId = null
        activeHistorySyncRepository = null
    }

    internal fun activeHistorySyncUserIdForTest(): String? = activeHistorySyncUserId

    private fun tr(language: AppLanguage, english: String, chinese: String): String =
        if (language == AppLanguage.ENGLISH) english else chinese

    private fun setError(stage: RemodelStage, error: RemodelError) {
        _uiState.update {
            it.copy(stage = stage, error = error)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as Application

                return RemodelViewModel(
                    repository = RemodelRepositoryFactory.create(application),
                    historyRepository = RemodelHistoryRepositoryFactory.create(application),
                    historySyncRepositoryFactory = if (
                        BuildConfig.REMODEL_API_BASE_URL.isNotBlank() &&
                        SecondBloomClerkConfig.isConfigured
                    ) {
                        { userId ->
                            RemodelHistorySyncRepositoryFactory.create(
                                context = application,
                                baseUrl = BuildConfig.REMODEL_API_BASE_URL,
                                accessTokenProvider = ClerkHistoryAuthTokenProvider,
                                namespace = userId
                            )
                        }
                    } else {
                        null
                    },
                    appPreferencesRepository = AppPreferencesRepositoryFactory.create(application)
                ) as T
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RemodelViewModel(
                    repository = DefaultRemodelRepository(MockRemodelApi())
                ) as T
            }
        }
    }
}
