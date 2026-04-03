package com.scf.loop.presentation.remodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scf.loop.data.local.NoOpRemodelHistoryRepository
import com.scf.loop.data.local.RemodelHistoryRepository
import com.scf.loop.data.local.RemodelHistoryRepositoryFactory
import com.scf.loop.data.remote.InvalidImageException
import com.scf.loop.data.remote.ModelResponseException
import com.scf.loop.data.remote.mock.MockRemodelApi
import com.scf.loop.data.repository.DefaultRemodelRepository
import com.scf.loop.data.repository.RemodelRepository
import com.scf.loop.data.repository.RemodelRepositoryFactory
import com.scf.loop.domain.model.BackgroundComplexity
import com.scf.loop.domain.model.DemoScenario
import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.GarmentDefect
import com.scf.loop.domain.model.RemodelError
import com.scf.loop.domain.model.RemodelErrorType
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelStage
import com.scf.loop.domain.model.RemodelUiState
import com.scf.loop.domain.model.SelectedImage
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val LowConfidenceThreshold = 0.75f

class RemodelViewModel(
    private val repository: RemodelRepository,
    private val historyRepository: RemodelHistoryRepository = NoOpRemodelHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemodelUiState())
    val uiState: StateFlow<RemodelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshHistory()
        }
    }

    fun onImageSelected(image: SelectedImage) {
        _uiState.update { current ->
            RemodelUiState(
                stage = RemodelStage.ImageSelected,
                selectedImage = image,
                latestAnalysisRecord = current.latestAnalysisRecord,
                latestPlanGenerationRecord = current.latestPlanGenerationRecord,
                recentAnalysisRecords = current.recentAnalysisRecords,
                recentPlanGenerationRecords = current.recentPlanGenerationRecords
            )
        }
    }

    fun loadDemoScenario(scenario: DemoScenario) {
        onImageSelected(scenario.toSelectedImage())
    }

    fun analyzeSelectedImage() {
        val image = _uiState.value.selectedImage
        if (image == null) {
            setError(
                stage = RemodelStage.InvalidImage,
                error = RemodelError(
                    type = RemodelErrorType.INVALID_IMAGE,
                    message = "请先选择一张旧衣图片。"
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
                val analysis = repository.analyze(image)
                historyRepository.saveAnalysis(image, analysis)
                refreshHistory()

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
                    error = RemodelError(RemodelErrorType.INVALID_IMAGE, exception.message.orEmpty())
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
                stage = if (state.plans.isNotEmpty()) RemodelStage.EditingAnalysis else state.stage
            )
        }
    }

    fun updateUserPreferences(value: String) {
        _uiState.update { state ->
            state.copy(
                userPreferences = value,
                stage = if (state.plans.isNotEmpty()) RemodelStage.EditingAnalysis else state.stage,
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
                    userPreferences = _uiState.value.userPreferences
                )

                if (plans.isEmpty()) {
                    throw ModelResponseException("服务端未返回可用改制方案。")
                }

                historyRepository.savePlanGeneration(
                    sourceImage = sourceImage,
                    analysis = analysis,
                    intent = intent,
                    userPreferences = _uiState.value.userPreferences,
                    plans = plans
                )
                refreshHistory()

                _uiState.update {
                    it.copy(
                        stage = RemodelStage.PlansReady,
                        analysis = analysis,
                        draftAnalysis = analysis,
                        plans = plans,
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

    private fun updateDraftAnalysis(transform: (GarmentAnalysis) -> GarmentAnalysis) {
        _uiState.update { state ->
            val current = state.draftAnalysis ?: return@update state
            val updated = transform(current)
            state.copy(
                draftAnalysis = updated,
                plans = emptyList(),
                stage = RemodelStage.EditingAnalysis
            )
        }
    }

    private suspend fun refreshHistory() {
        val latestAnalysis = historyRepository.getLatestAnalysis()
        val latestPlanGeneration = historyRepository.getLatestPlanGeneration()
        val recentAnalyses = historyRepository.getRecentAnalyses()
        val recentPlanGenerations = historyRepository.getRecentPlanGenerations()

        _uiState.update {
            it.copy(
                latestAnalysisRecord = latestAnalysis,
                latestPlanGenerationRecord = latestPlanGeneration,
                recentAnalysisRecords = recentAnalyses,
                recentPlanGenerationRecords = recentPlanGenerations
            )
        }
    }

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
                    historyRepository = RemodelHistoryRepositoryFactory.create(application)
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
