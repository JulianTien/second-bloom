package com.scf.secondbloom

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.CompositionLocalProvider
import com.scf.secondbloom.data.local.RemodelHistoryRepository
import com.scf.secondbloom.data.repository.RemodelRepository
import com.scf.secondbloom.auth.SecondBloomAuthProfile
import com.scf.secondbloom.auth.SecondBloomAuthUiState
import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.DemoScenario
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GarmentDefect
import com.scf.secondbloom.domain.model.ProcessingWarning
import com.scf.secondbloom.domain.model.ProcessingWarningCode
import com.scf.secondbloom.domain.model.RemodelDifficulty
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStage
import com.scf.secondbloom.domain.model.RemodelStep
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.domain.model.PlanPreviewResult
import com.scf.secondbloom.domain.model.PublishedRemodelRecord
import com.scf.secondbloom.domain.model.SavedAnalysisRecord
import com.scf.secondbloom.domain.model.SavedPlanGenerationRecord
import com.scf.secondbloom.domain.model.SelectedImage
import com.scf.secondbloom.domain.model.deriveRecentActivities
import com.scf.secondbloom.domain.model.deriveSustainabilityImpactSummary
import com.scf.secondbloom.domain.model.deriveWardrobeCategories
import com.scf.secondbloom.domain.model.deriveWardrobeEntries
import com.scf.secondbloom.presentation.remodel.RemodelViewModel
import com.scf.secondbloom.ui.MainScreen
import com.scf.secondbloom.ui.i18n.localizedLabel
import com.scf.secondbloom.ui.screens.HomeScreen
import com.scf.secondbloom.ui.screens.InspirationScreen
import com.scf.secondbloom.ui.screens.PlanetScreen
import com.scf.secondbloom.ui.screens.PreviewEditorScreen
import com.scf.secondbloom.ui.screens.PreviewResultScreen
import com.scf.secondbloom.ui.screens.ProfileScreen
import com.scf.secondbloom.ui.screens.WardrobeScreen
import com.scf.secondbloom.ui.screens.WorkbenchScreen
import com.scf.secondbloom.ui.theme.SecondBloomTheme
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class RemodelScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_showsFourTabs_andFabOpensRemodelFlow() {
        composeTestRule.setContent {
            SecondBloomTheme {
                MainScreen()
            }
        }

        composeTestRule.onNodeWithText("灵感空间").assertIsDisplayed()
        composeTestRule.onNodeWithText("数字衣橱").assertIsDisplayed()
        composeTestRule.onNodeWithText("可持续星球").assertIsDisplayed()
        composeTestRule.onNodeWithText("我的主页").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("AI改制入口，双击进入上传识别流程")
            .performClick()

        composeTestRule.onNodeWithText("上传旧衣").assertIsDisplayed()
    }

    @Test
    fun mainScreen_bottomTabReturnsToInspiration_fromRemodelFlow() {
        composeTestRule.setContent {
            SecondBloomTheme {
                MainScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("AI改制入口，双击进入上传识别流程")
            .performClick()
        composeTestRule.onNodeWithText("上传旧衣").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("灵感空间，灵感空间页面，双击切换页面")
            .performClick()

        composeTestRule.onNodeWithText("灵感空间").assertIsDisplayed()
    }

    @Test
    fun inspirationScreen_showsWaterfallFeed() {
        composeTestRule.setContent {
            SecondBloomTheme {
                InspirationScreen(
                    state = RemodelUiState(),
                    onOpenInspirationDetail = {},
                    onOpenRemodelFlow = {}
                )
            }
        }

        composeTestRule.onNodeWithText("灵感空间").assertIsDisplayed()
        composeTestRule.onNodeWithText("先看 Before / After 灵感，再开始你的 AI 改制。").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始 AI 改制").assertIsDisplayed()
        composeTestRule.onNodeWithText("旧男士衬衫 -> 夏日挂脖上衣").assertIsDisplayed()
    }

    @Test
    fun wardrobeScreen_showsCategoryAndGrid() {
        composeTestRule.setContent {
            SecondBloomTheme {
                WardrobeScreen()
            }
        }

        composeTestRule.onNodeWithText("数字衣橱").assertIsDisplayed()
        composeTestRule.onNodeWithText("拍照录入新衣物").assertIsDisplayed()
        composeTestRule.onNodeWithText("条纹针织衫").assertIsDisplayed()
    }

    @Test
    fun wardrobeScreen_showsSavedHistoryWhenWardrobeEntriesExist() {
        val analysis = GarmentAnalysis(
            analysisId = "analysis-1",
            garmentType = "白色衬衫",
            color = "白色",
            material = "棉质",
            style = "简约",
            defects = emptyList(),
            backgroundComplexity = BackgroundComplexity.LOW,
            confidence = 0.92f,
            warnings = emptyList()
        )
        val savedPlan = savedPlanRecord(
            recordId = "plan-1",
            fileName = "plain-shirt.jpg",
            analysis = analysis
        )
        val entries = deriveWardrobeEntries(
            recentAnalyses = listOf(
                SavedAnalysisRecord(
                    recordId = "analysis-1",
                    savedAtEpochMillis = 1L,
                    sourceImage = SelectedImage(
                        uri = "content://secondbloom/plain-shirt.jpg",
                        fileName = "plain-shirt.jpg",
                        mimeType = "image/jpeg"
                    ),
                    analysis = analysis
                )
            ),
            recentPlanGenerations = listOf(savedPlan)
        )

        composeTestRule.setContent {
            SecondBloomTheme {
                WardrobeScreen(
                    state = RemodelUiState(
                        wardrobeEntries = entries,
                        wardrobeCategories = deriveWardrobeCategories(entries)
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("继续录入旧衣").assertIsDisplayed()
        composeTestRule.onNodeWithText("白色衬衫").assertIsDisplayed()
        composeTestRule.onNodeWithText("plain-shirt.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("已生成方案").assertIsDisplayed()
    }

    @Test
    fun planetScreen_showsHeroAndStats() {
        composeTestRule.setContent {
            SecondBloomTheme {
                PlanetScreen()
            }
        }

        composeTestRule.onNodeWithText("可持续星球").assertIsDisplayed()
        composeTestRule.onNodeWithText("LV.1 萌芽新手").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 L").assertIsDisplayed()
        composeTestRule.onNodeWithText("SDG 贡献徽章").assertIsDisplayed()
    }

    @Test
    fun planetScreen_showsDerivedImpactFromSavedPlans() {
        val analyses = listOf(
            SavedAnalysisRecord(
                recordId = "analysis-1",
                savedAtEpochMillis = 1L,
                sourceImage = SelectedImage(
                    uri = "content://secondbloom/look-1.jpg",
                    fileName = "look-1.jpg",
                    mimeType = "image/jpeg"
                ),
                analysis = demoAnalysis("analysis-1")
            ),
            SavedAnalysisRecord(
                recordId = "analysis-2",
                savedAtEpochMillis = 2L,
                sourceImage = SelectedImage(
                    uri = "content://secondbloom/look-2.jpg",
                    fileName = "look-2.jpg",
                    mimeType = "image/jpeg"
                ),
                analysis = demoAnalysis("analysis-2")
            )
        )
        val plans = listOf(
            savedPlanRecord("plan-1", "look-1.jpg", analyses[0].analysis),
            savedPlanRecord("plan-2", "look-2.jpg", analyses[1].analysis)
        )

        composeTestRule.setContent {
            SecondBloomTheme {
                PlanetScreen(
                    state = RemodelUiState(
                        sustainabilitySummary = deriveSustainabilityImpactSummary(
                            recentAnalyses = analyses,
                            recentPlanGenerations = plans
                        )
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("LV.3 零废弃先锋").assertIsDisplayed()
        composeTestRule.onNodeWithText("已识别 2 件 · 已生成方案 2 次").assertIsDisplayed()
        composeTestRule.onNodeWithText("3,000 L").assertIsDisplayed()
        composeTestRule.onNodeWithText("8.4 kg").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsAnalyzingFeedback() {
        composeTestRule.setContent {
            SecondBloomTheme {
                HomeScreen(
                    state = RemodelUiState(
                        stage = RemodelStage.Analyzing,
                        selectedImage = SelectedImage(
                            uri = "content://secondbloom/image.jpg",
                            fileName = "image.jpg",
                            mimeType = "image/jpeg"
                        )
                    ),
                    onImageSelected = {},
                    onLoadDemoScenario = {},
                    onAnalyze = {},
                    onContinueLowConfidence = {},
                    onDismissError = {},
                    onOpenWorkbench = {}
                )
            }
        }

        composeTestRule.onNodeWithText("上传旧衣").assertIsDisplayed()
        composeTestRule.onNodeWithText("正在识别").assertIsDisplayed()
        composeTestRule.onNodeWithText("示例素材").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsLowConfidenceDecisionActions() {
        composeTestRule.setContent {
            SecondBloomTheme {
                HomeScreen(
                    state = RemodelUiState(
                        stage = RemodelStage.LowConfidence,
                        selectedImage = DemoScenario.LOW_CONFIDENCE.toSelectedImage(),
                        draftAnalysis = GarmentAnalysis(
                            analysisId = "analysis-low",
                            garmentType = "深色卫衣",
                            color = "黑色",
                            material = "棉质",
                            style = "休闲",
                            defects = listOf(GarmentDefect("袖口磨损")),
                            backgroundComplexity = BackgroundComplexity.HIGH,
                            confidence = 0.63f,
                            warnings = listOf(
                                ProcessingWarning(
                                    code = ProcessingWarningCode.COMPLEX_BACKGROUND,
                                    message = "检测到背景较复杂，建议重拍或确认后继续。"
                                )
                            )
                        )
                    ),
                    onImageSelected = {},
                    onLoadDemoScenario = {},
                    onAnalyze = {},
                    onContinueLowConfidence = {},
                    onDismissError = {},
                    onOpenWorkbench = {}
                )
            }
        }

        composeTestRule.onNodeWithText("这张照片需要再确认一次").assertIsDisplayed()
        composeTestRule.onNodeWithText("继续确认").assertIsDisplayed()
        composeTestRule.onNodeWithText("重拍照片").assertIsDisplayed()
    }

    @Test
    fun workbenchScreen_showsEditableAnalysisAndPlans() {
        composeTestRule.setContent {
            SecondBloomTheme {
                WorkbenchScreen(
                    state = RemodelUiState(
                        stage = RemodelStage.PlansReady,
                        draftAnalysis = GarmentAnalysis(
                            analysisId = "analysis-1",
                            garmentType = "白色衬衫",
                            color = "白色",
                            material = "棉质",
                            style = "简约",
                            defects = listOf(GarmentDefect("袖口磨损")),
                            backgroundComplexity = BackgroundComplexity.LOW,
                            confidence = 0.92f,
                            warnings = emptyList()
                        ),
                        selectedIntent = RemodelIntent.DAILY,
                        plans = listOf(
                            RemodelPlan(
                                title = "日常焕新 方案一",
                                summary = "保留原有轮廓并优化细节。",
                                difficulty = RemodelDifficulty.EASY,
                                materials = listOf("布用剪刀", "同色线"),
                                estimatedTime = "1-2 小时",
                                steps = listOf(
                                    RemodelStep("整理衣片", "先检查磨损区域。")
                                )
                            )
                        )
                    ),
                    onGarmentTypeChange = {},
                    onColorChange = {},
                    onMaterialChange = {},
                    onStyleChange = {},
                    onDefectsChange = {},
                    onIntentSelected = {},
                    onPreferencesChange = {},
                    onGeneratePlans = {},
                    onOpenPreviewEditor = {},
                    onOpenPreviewResult = {},
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("识别摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText("展开编辑").assertIsDisplayed()
        composeTestRule.onNodeWithText("改制目标").assertIsDisplayed()
        composeTestRule.onNodeWithText("日常焕新 方案一").assertIsDisplayed()
    }

    @Test
    fun previewEditorScreen_showsMicroAdjustmentControlsAndGenerateButton() {
        val plan = RemodelPlan(
            planId = "plan-1",
            title = "真图编辑方案",
            summary = "在保留原图轮廓的前提下做轻度改造。",
            difficulty = RemodelDifficulty.MEDIUM,
            materials = listOf("同色线", "布用剪刀"),
            estimatedTime = "2 小时",
            steps = listOf(
                RemodelStep("确认轮廓", "先锁定原图结构。"),
                RemodelStep("微调细节", "再做袖型和领口调整。")
            )
        )

        composeTestRule.setContent {
            SecondBloomTheme {
                PreviewEditorScreen(
                    state = RemodelUiState(
                        plans = listOf(plan),
                        editingPlanId = plan.planId
                    ),
                    planId = plan.planId,
                    onBack = {},
                    onOpenPreviewEditor = {},
                    onClosePreviewEditor = {},
                    onSilhouetteChange = {},
                    onLengthChange = {},
                    onNecklineChange = {},
                    onSleeveChange = {},
                    onFidelityChange = {},
                    onInstructionsChange = {},
                    onGenerateFinalImage = {},
                    onOpenPreviewResult = {}
                )
            }
        }

        composeTestRule.onNodeWithText("真图编辑").assertIsDisplayed()
        composeTestRule.onNodeWithText("生成最终效果图").assertIsDisplayed()
        composeTestRule.onNodeWithText("整体廓形").assertIsDisplayed()
        composeTestRule.onNodeWithText("领口").assertIsDisplayed()
        composeTestRule.onNodeWithText("补充微调说明").assertIsDisplayed()
    }

    @Test
    fun previewResultScreen_showsDedicatedGalleryLayout() {
        val plan = RemodelPlan(
            planId = "plan-1",
            title = "最终展示方案",
            summary = "集中展示最终效果图。",
            difficulty = RemodelDifficulty.MEDIUM,
            materials = listOf("同色线"),
            estimatedTime = "2 小时",
            steps = listOf(RemodelStep("步骤一", "先完成编辑"))
        )

        composeTestRule.setContent {
            SecondBloomTheme {
                PreviewResultScreen(
                    state = RemodelUiState(
                        plans = listOf(plan),
                        selectedPlanId = plan.planId,
                        previewJob = com.scf.secondbloom.domain.model.PreviewJobSnapshot(
                            previewJobId = "preview-job-1",
                            analysisId = "analysis-1",
                            status = com.scf.secondbloom.domain.model.PreviewJobStatus.COMPLETED,
                            requestedPlanCount = 1,
                            completedPlanCount = 1,
                            failedPlanCount = 0,
                            results = listOf(
                                com.scf.secondbloom.domain.model.PlanPreviewResult(
                                    planId = plan.planId,
                                    renderStatus = com.scf.secondbloom.domain.model.PreviewRenderStatus.COMPLETED,
                                    beforeImage = com.scf.secondbloom.domain.model.PreviewAsset("before", "https://example.com/before.png", "2099-01-01T00:00:00Z"),
                                    afterImage = com.scf.secondbloom.domain.model.PreviewAsset("after", "https://example.com/after.png", "2099-01-01T00:00:00Z"),
                                    comparisonImage = com.scf.secondbloom.domain.model.PreviewAsset("compare", "https://example.com/compare.png", "2099-01-01T00:00:00Z"),
                                    disclaimer = "AI visual simulation only. Final garment may differ."
                                )
                            )
                        )
                    ),
                    planId = plan.planId,
                    onBack = {},
                    onBackToPlans = {},
                    onEditPlan = {},
                    onPublish = {},
                    onOpenInspiration = {},
                    onResumePolling = {},
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("最终效果图").assertIsDisplayed()
        composeTestRule.onNodeWithText("Before").assertIsDisplayed()
        composeTestRule.onNodeWithText("After").assertIsDisplayed()
        composeTestRule.onNodeWithText("Compare").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsWorksWallFromSavedHistory() {
        val analysis = GarmentAnalysis(
            analysisId = "analysis-1",
            garmentType = "白色衬衫",
            color = "白色",
            material = "棉质",
            style = "简约",
            defects = emptyList(),
            backgroundComplexity = BackgroundComplexity.LOW,
            confidence = 0.92f,
            warnings = emptyList()
        )
        val savedPlan = SavedPlanGenerationRecord(
            recordId = "plan-1",
            savedAtEpochMillis = 2L,
            sourceImage = SelectedImage(
                uri = "content://secondbloom/plain-shirt.jpg",
                fileName = "plain-shirt.jpg",
                mimeType = "image/jpeg"
            ),
            analysis = analysis,
            intent = RemodelIntent.DAILY,
            userPreferences = "",
            plans = listOf(
                RemodelPlan(
                    title = "日常焕新 方案一",
                    summary = "保留原有轮廓并优化细节。",
                    difficulty = RemodelDifficulty.EASY,
                    materials = listOf("布用剪刀"),
                    estimatedTime = "1-2 小时",
                    steps = listOf(RemodelStep("整理衣片", "先检查磨损区域。"))
                )
            )
        )

        composeTestRule.setContent {
            SecondBloomTheme {
                ProfileScreen(
                    state = RemodelUiState(
                        latestAnalysisRecord = SavedAnalysisRecord(
                            recordId = "analysis-1",
                            savedAtEpochMillis = 1L,
                            sourceImage = SelectedImage(
                                uri = "content://secondbloom/plain-shirt.jpg",
                                fileName = "plain-shirt.jpg",
                                mimeType = "image/jpeg"
                            ),
                            analysis = analysis
                        ),
                        latestPlanGenerationRecord = savedPlan,
                        recentAnalysisRecords = listOf(),
                        recentPlanGenerationRecords = listOf(savedPlan),
                        sustainabilitySummary = deriveSustainabilityImpactSummary(
                            recentAnalyses = emptyList(),
                            recentPlanGenerations = listOf(savedPlan)
                        ),
                        recentActivities = deriveRecentActivities(
                            recentAnalyses = emptyList(),
                            recentPlanGenerations = listOf(savedPlan)
                        )
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("记录摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText("最近动态").assertIsDisplayed()
        composeTestRule.onNodeWithText("方案生成").assertIsDisplayed()
        composeTestRule.onNodeWithText("1500 L 节水估算").assertIsDisplayed()
        composeTestRule.onNodeWithText("我的改造").assertIsDisplayed()
        composeTestRule.onNodeWithText("日常焕新 方案一").assertIsDisplayed()
    }

    @Test
    fun profileScreen_guestShowsVisibleLoginEntry() {
        var loginClicked = false

        composeTestRule.setContent {
            SecondBloomTheme {
                CompositionLocalProvider(LocalAppLanguage provides AppLanguage.CHINESE) {
                    ProfileScreen(
                        state = RemodelUiState(appLanguage = AppLanguage.CHINESE),
                        authState = SecondBloomAuthUiState.Guest,
                        onLoginClick = { loginClicked = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("登录 / 注册").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("登录 / 注册").performClick()
        assertTrue(loginClicked)
    }

    @Test
    fun profileScreen_signedInShowsAccountCenterEntry() {
        var accountClicked = false

        composeTestRule.setContent {
            SecondBloomTheme {
                CompositionLocalProvider(LocalAppLanguage provides AppLanguage.CHINESE) {
                    ProfileScreen(
                        state = RemodelUiState(appLanguage = AppLanguage.CHINESE),
                        authState = SecondBloomAuthUiState.SignedIn(
                            profile = SecondBloomAuthProfile(
                                userId = "user_1",
                                displayName = "Cici",
                                avatarUrl = null
                            )
                        ),
                        onAccountClick = { accountClicked = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("账号中心").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("打开账号中心").performClick()
        assertTrue(accountClicked)
    }

    @Test
    fun mainScreen_completedFlowUpdatesWardrobeProfileAndPlanet() {
        val historyRepository = AndroidFakeHistoryRepository()
        val viewModel = RemodelViewModel(
            repository = AndroidFakeRemodelRepository(),
            historyRepository = historyRepository
        )
        viewModel.loadDemoScenario(DemoScenario.NORMAL)

        composeTestRule.setContent {
            SecondBloomTheme {
                MainScreen(remodelViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("AI改制入口，双击进入上传识别流程")
            .performClick()
        composeTestRule.onNodeWithText("开始识别").performClick()
        waitForText("去看方案")
        composeTestRule.onNodeWithText("去看方案").performClick()
        composeTestRule.onNodeWithText(RemodelIntent.DAILY.localizedLabel(AppLanguage.CHINESE)).performClick()
        composeTestRule.onNodeWithText("生成改制方案").performClick()
        waitForText("日常焕新 方案一")

        composeTestRule.onNodeWithContentDescription("数字衣橱，数字衣橱页面，双击切换页面")
            .performClick()
        composeTestRule.onNodeWithText("演示-白色衬衫").assertIsDisplayed()
        composeTestRule.onNodeWithText(DemoScenario.NORMAL.fileName).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("我的主页，我的主页页面，双击切换页面")
            .performClick()
        composeTestRule.onNodeWithText("记录摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText("日常焕新 方案一").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("可持续星球，可持续星球页面，双击切换页面")
            .performClick()
        composeTestRule.onNodeWithText("已识别 1 件 · 已生成方案 1 次").assertIsDisplayed()
        composeTestRule.onNodeWithText("LV.2 初级裁缝").assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private class AndroidFakeRemodelRepository : RemodelRepository {
    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ): GarmentAnalysis = GarmentAnalysis(
        analysisId = "analysis-${image.fileName}",
        garmentType = "演示-白色衬衫",
        color = "白色",
        material = "棉质",
        style = "简约",
        defects = listOf(GarmentDefect("袖口轻微磨损")),
        backgroundComplexity = BackgroundComplexity.LOW,
        confidence = 0.92f,
        warnings = emptyList()
    )

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> = listOf(
        RemodelPlan(
            title = "日常焕新 方案一",
            summary = "围绕${confirmedAnalysis.garmentType}做轻改造。",
            difficulty = RemodelDifficulty.EASY,
            materials = listOf("布用剪刀", "定位针"),
            estimatedTime = "1-2 小时",
            steps = listOf(RemodelStep("整理衣片", "先检查磨损区域。"))
        )
    )

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: com.scf.secondbloom.domain.model.PreviewEditOptions?
    ): com.scf.secondbloom.domain.model.GeneratePreviewJobResult =
        com.scf.secondbloom.domain.model.GeneratePreviewJobResult(
            previewJobId = "preview-job-$analysisId-$planId",
            status = "queued",
            requestedPlanCount = 1,
            pollPath = "/remodel-preview-jobs/preview-job-$analysisId-$planId"
        )

    override suspend fun getPreviewJob(
        previewJobId: String
    ): com.scf.secondbloom.domain.model.PreviewJobSnapshot =
        com.scf.secondbloom.domain.model.PreviewJobSnapshot(
            previewJobId = previewJobId,
            analysisId = "analysis-1",
            status = com.scf.secondbloom.domain.model.PreviewJobStatus.COMPLETED,
            requestedPlanCount = 1,
            completedPlanCount = 1,
            failedPlanCount = 0,
            results = emptyList(),
            pollPath = "/remodel-preview-jobs/$previewJobId"
        )
}

private class AndroidFakeHistoryRepository : RemodelHistoryRepository {
    private val analyses = mutableListOf<SavedAnalysisRecord>()
    private val plans = mutableListOf<SavedPlanGenerationRecord>()
    private val publishedRemodels = mutableListOf<PublishedRemodelRecord>()
    private val engagements = mutableListOf<com.scf.secondbloom.domain.model.InspirationEngagementRecord>()

    override suspend fun saveAnalysis(
        sourceImage: SelectedImage,
        analysis: GarmentAnalysis,
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
        analysis: GarmentAnalysis,
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
        analysis: GarmentAnalysis,
        intent: RemodelIntent,
        selectedPlan: RemodelPlan,
        previewResult: PlanPreviewResult,
        publishedAtEpochMillis: Long
    ): PublishedRemodelRecord {
        val record = PublishedRemodelRecord(
            recordId = "${analysis.analysisId}-${selectedPlan.title}",
            publishedAtEpochMillis = publishedAtEpochMillis,
            sourceImage = sourceImage,
            analysis = analysis,
            intent = intent,
            selectedPlan = selectedPlan,
            previewResult = previewResult
        )
        publishedRemodels.add(0, record)
        return record
    }

    override suspend fun getRecentPublishedRemodels(limit: Int): List<PublishedRemodelRecord> =
        publishedRemodels.take(limit)

    override suspend fun saveInspirationEngagement(
        record: com.scf.secondbloom.domain.model.InspirationEngagementRecord
    ): com.scf.secondbloom.domain.model.InspirationEngagementRecord {
        engagements.removeAll { it.itemId == record.itemId }
        engagements.add(0, record)
        return record
    }

    override suspend fun getInspirationEngagements(
        limit: Int
    ): List<com.scf.secondbloom.domain.model.InspirationEngagementRecord> = engagements.take(limit)
}

private fun demoAnalysis(analysisId: String) = GarmentAnalysis(
    analysisId = analysisId,
    garmentType = "白色衬衫",
    color = "白色",
    material = "棉质",
    style = "简约",
    defects = emptyList(),
    backgroundComplexity = BackgroundComplexity.LOW,
    confidence = 0.92f,
    warnings = emptyList()
)

private fun savedPlanRecord(
    recordId: String,
    fileName: String,
    analysis: GarmentAnalysis
) = SavedPlanGenerationRecord(
    recordId = recordId,
    savedAtEpochMillis = 2L,
    sourceImage = SelectedImage(
        uri = "content://secondbloom/$fileName",
        fileName = fileName,
        mimeType = "image/jpeg"
    ),
    analysis = analysis,
    intent = RemodelIntent.DAILY,
    userPreferences = "",
    plans = listOf(
        RemodelPlan(
            title = "日常焕新 方案一",
            summary = "保留原有轮廓并优化细节。",
            difficulty = RemodelDifficulty.EASY,
            materials = listOf("布用剪刀"),
            estimatedTime = "1-2 小时",
            steps = listOf(RemodelStep("整理衣片", "先检查磨损区域。"))
        )
    )
)
