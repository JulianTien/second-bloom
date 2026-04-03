package com.scf.loop

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.scf.loop.domain.model.BackgroundComplexity
import com.scf.loop.domain.model.DemoScenario
import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.GarmentDefect
import com.scf.loop.domain.model.ProcessingWarning
import com.scf.loop.domain.model.ProcessingWarningCode
import com.scf.loop.domain.model.RemodelDifficulty
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.RemodelStage
import com.scf.loop.domain.model.RemodelStep
import com.scf.loop.domain.model.RemodelUiState
import com.scf.loop.domain.model.SavedAnalysisRecord
import com.scf.loop.domain.model.SavedPlanGenerationRecord
import com.scf.loop.domain.model.SelectedImage
import com.scf.loop.ui.MainScreen
import com.scf.loop.ui.screens.HomeScreen
import com.scf.loop.ui.screens.InspirationScreen
import com.scf.loop.ui.screens.PlanetScreen
import com.scf.loop.ui.screens.ProfileScreen
import com.scf.loop.ui.screens.WardrobeScreen
import com.scf.loop.ui.screens.WorkbenchScreen
import com.scf.loop.ui.theme.LoopTheme
import org.junit.Rule
import org.junit.Test

class RemodelScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_showsFourTabs_andFabOpensRemodelFlow() {
        composeTestRule.setContent {
            LoopTheme {
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
            LoopTheme {
                MainScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("AI改制入口，双击进入上传识别流程")
            .performClick()
        composeTestRule.onNodeWithText("上传旧衣").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("灵感空间，灵感空间页面，双击切换页面")
            .performClick()

        composeTestRule.onNodeWithText("旧衣新生的灵感流。").assertIsDisplayed()
    }

    @Test
    fun inspirationScreen_showsWaterfallFeed() {
        composeTestRule.setContent {
            LoopTheme {
                InspirationScreen(onOpenRemodelFlow = {})
            }
        }

        composeTestRule.onNodeWithText("旧衣新生的灵感流。").assertIsDisplayed()
        composeTestRule.onNodeWithText("一键同款").assertIsDisplayed()
        composeTestRule.onNodeWithText("旧男士衬衫 -> 夏日挂脖上衣").assertIsDisplayed()
    }

    @Test
    fun wardrobeScreen_showsCategoryAndGrid() {
        composeTestRule.setContent {
            LoopTheme {
                WardrobeScreen()
            }
        }

        composeTestRule.onNodeWithText("数字衣橱").assertIsDisplayed()
        composeTestRule.onNodeWithText("拍照录入新衣物").assertIsDisplayed()
        composeTestRule.onNodeWithText("条纹针织衫").assertIsDisplayed()
    }

    @Test
    fun planetScreen_showsHeroAndStats() {
        composeTestRule.setContent {
            LoopTheme {
                PlanetScreen()
            }
        }

        composeTestRule.onNodeWithText("可持续星球").assertIsDisplayed()
        composeTestRule.onNodeWithText("LV.4 生态卫士").assertIsDisplayed()
        composeTestRule.onNodeWithText("4,500 L").assertIsDisplayed()
        composeTestRule.onNodeWithText("SDG 贡献徽章").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsAnalyzingFeedback() {
        composeTestRule.setContent {
            LoopTheme {
                HomeScreen(
                    state = RemodelUiState(
                        stage = RemodelStage.Analyzing,
                        selectedImage = SelectedImage(
                            uri = "content://loop/image.jpg",
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
            LoopTheme {
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
            LoopTheme {
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
                uri = "content://loop/plain-shirt.jpg",
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
            LoopTheme {
                ProfileScreen(
                    state = RemodelUiState(
                        latestAnalysisRecord = SavedAnalysisRecord(
                            recordId = "analysis-1",
                            savedAtEpochMillis = 1L,
                            sourceImage = SelectedImage(
                                uri = "content://loop/plain-shirt.jpg",
                                fileName = "plain-shirt.jpg",
                                mimeType = "image/jpeg"
                            ),
                            analysis = analysis
                        ),
                        latestPlanGenerationRecord = savedPlan,
                        recentAnalysisRecords = listOf(),
                        recentPlanGenerationRecords = listOf(savedPlan)
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("记录摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText("我的改造").assertIsDisplayed()
        composeTestRule.onNodeWithText("日常焕新 方案一").assertIsDisplayed()
    }
}
