package com.scf.loop

import com.scf.loop.data.local.RemodelHistoryRepository
import com.scf.loop.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.loop.data.remote.dto.GarmentAnalysisDto
import com.scf.loop.data.remote.dto.GarmentDefectDto
import com.scf.loop.data.remote.dto.ProcessingWarningDto
import com.scf.loop.data.remote.mock.MockRemodelApi
import com.scf.loop.data.repository.DefaultRemodelRepository
import com.scf.loop.domain.model.BackgroundComplexity
import com.scf.loop.domain.model.DemoScenario
import com.scf.loop.domain.model.ProcessingWarningCode
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelStage
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.SavedAnalysisRecord
import com.scf.loop.domain.model.SavedPlanGenerationRecord
import com.scf.loop.domain.model.SelectedImage
import com.scf.loop.presentation.remodel.RemodelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
                uri = "content://loop/messy-shirt.jpg",
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
    fun generatePlans_afterEditingAnalysis_usesLatestStateAndReturnsPlans() = runTest {
        val viewModel = RemodelViewModel(DefaultRemodelRepository(MockRemodelApi()))

        viewModel.onImageSelected(
            SelectedImage(
                uri = "content://loop/plain-shirt.jpg",
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
                uri = "content://loop/plain-shirt.jpg",
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
                uri = "content://loop/plain-shirt.jpg",
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

private class FakeHistoryRepository : RemodelHistoryRepository {
    private val analyses = mutableListOf<SavedAnalysisRecord>()
    private val plans = mutableListOf<SavedPlanGenerationRecord>()

    override suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: com.scf.loop.domain.model.GarmentAnalysis,
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
        analysis: com.scf.loop.domain.model.GarmentAnalysis,
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
}
