package com.scf.secondbloom.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scf.secondbloom.auth.rememberSecondBloomAuthUiState
import com.scf.secondbloom.auth.signOutCurrentUser
import com.scf.secondbloom.navigation.Screen
import com.scf.secondbloom.presentation.remodel.RemodelViewModel
import com.scf.secondbloom.ui.components.SecondBloomBottomNavBar
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.screens.AuthScreen
import com.scf.secondbloom.ui.screens.AccountScreen
import com.scf.secondbloom.ui.screens.HomeScreen
import com.scf.secondbloom.ui.screens.InspirationDetailScreen
import com.scf.secondbloom.ui.screens.InspirationScreen
import com.scf.secondbloom.ui.screens.PlanetScreen
import com.scf.secondbloom.ui.screens.PreviewEditorScreen
import com.scf.secondbloom.ui.screens.PreviewResultScreen
import com.scf.secondbloom.ui.screens.ProfileScreen
import com.scf.secondbloom.ui.screens.WardrobeScreen
import com.scf.secondbloom.ui.screens.WorkbenchScreen
import kotlinx.coroutines.launch

private const val AnimationDuration = 300

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    remodelViewModel: RemodelViewModel = viewModel(factory = RemodelViewModel.Factory)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by remodelViewModel.uiState.collectAsStateWithLifecycle()
    val authState = rememberSecondBloomAuthUiState()
    val coroutineScope = rememberCoroutineScope()
    var highlightedTopLevelRoute by rememberSaveable { mutableStateOf(Screen.Inspiration.route) }

    LaunchedEffect(currentRoute) {
        if (currentRoute in Screen.topLevelItems().map { it.route }) {
            highlightedTopLevelRoute = currentRoute ?: Screen.Inspiration.route
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides uiState.appLanguage) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (currentRoute in Screen.topLevelItems().map { it.route }) {
                    SecondBloomBottomNavBar(
                        highlightedRoute = highlightedTopLevelRoute,
                        onNavigate = { route ->
                            if (currentRoute != route) {
                                val restoredExistingDestination = navController.popBackStack(
                                    route = route,
                                    inclusive = false
                                )
                                if (!restoredExistingDestination) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        onOpenRemodelFlow = {
                            if (currentRoute != Screen.CameraRecognition.route) {
                                navController.navigate(Screen.CameraRecognition.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Inspiration.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            composable(
                route = Screen.Inspiration.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                InspirationScreen(
                    state = uiState,
                    onOpenInspirationDetail = { itemId ->
                        navController.navigate(Screen.InspirationDetail.createRoute(itemId)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenRemodelFlow = {
                        navController.navigate(Screen.CameraRecognition.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.InspirationDetail.route,
                arguments = listOf(
                    navArgument(Screen.InspirationDetail.itemIdArg) {
                        type = NavType.StringType
                    }
                ),
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString(Screen.InspirationDetail.itemIdArg)
                val item = itemId?.let {
                    com.scf.secondbloom.ui.model.SecondBloomShowcaseContent.inspirationCardById(
                        itemId = it,
                        records = uiState.publishedRemodelRecords,
                        engagements = uiState.inspirationEngagementRecords,
                        language = uiState.appLanguage
                    )
                }
                InspirationDetailScreen(
                    state = uiState,
                    item = item,
                    onBack = { navController.popBackStack() },
                    onToggleLike = remodelViewModel::toggleInspirationLike,
                    onToggleBookmark = remodelViewModel::toggleInspirationBookmark,
                    onAddComment = remodelViewModel::addInspirationComment,
                    onOpenPublishedResult = { planId ->
                        navController.navigate(Screen.PreviewResult.createRoute(planId)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenRemodelFlow = {
                        navController.navigate(Screen.CameraRecognition.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.Wardrobe.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                WardrobeScreen(
                    state = uiState,
                    onOpenRemodelFlow = {
                        navController.navigate(Screen.CameraRecognition.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenSavedPlan = { recordId ->
                        val restored = remodelViewModel.restoreSavedPlanGeneration(recordId)
                        if (restored) {
                            navController.navigate(Screen.Plan.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.Planet.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                PlanetScreen(
                    state = uiState,
                    onOpenRemodelFlow = {
                        navController.navigate(Screen.CameraRecognition.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.Profile.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                ProfileScreen(
                    state = uiState,
                    authState = authState,
                    onLanguageSelected = remodelViewModel::setAppLanguage,
                    onLoginClick = {
                        navController.navigate(Screen.Auth.route) {
                            launchSingleTop = true
                        }
                    },
                    onAccountClick = {
                        navController.navigate(Screen.Account.route) {
                            launchSingleTop = true
                        }
                    },
                    onLogoutClick = {
                        coroutineScope.launch {
                            signOutCurrentUser()
                        }
                    }
                )
            }

            composable(
                route = Screen.Auth.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                AuthScreen(
                    onDismiss = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Account.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                AccountScreen(
                    authState = authState,
                    onDismiss = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CameraRecognition.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                HomeScreen(
                    state = uiState,
                    onImageSelected = remodelViewModel::onImageSelected,
                    onLoadDemoScenario = remodelViewModel::loadDemoScenario,
                    onAnalyze = remodelViewModel::analyzeSelectedImage,
                    onContinueLowConfidence = remodelViewModel::continueWithLowConfidence,
                    onDismissError = remodelViewModel::clearError,
                    onOpenWorkbench = {
                        navController.navigate(Screen.Plan.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.Plan.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                WorkbenchScreen(
                    state = uiState,
                    onGarmentTypeChange = remodelViewModel::updateGarmentType,
                    onColorChange = remodelViewModel::updateColor,
                    onMaterialChange = remodelViewModel::updateMaterial,
                    onStyleChange = remodelViewModel::updateStyle,
                    onDefectsChange = remodelViewModel::updateDefects,
                    onIntentSelected = remodelViewModel::selectIntent,
                    onPreferencesChange = remodelViewModel::updateUserPreferences,
                    onGeneratePlans = remodelViewModel::generatePlans,
                    onOpenPreviewEditor = { planId ->
                        navController.navigate(Screen.PreviewEditor.createRoute(planId)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenPreviewResult = { planId ->
                        navController.navigate(Screen.PreviewResult.createRoute(planId)) {
                            launchSingleTop = true
                        }
                    },
                    onDismissError = remodelViewModel::clearError
                )
            }

            composable(
                route = Screen.PreviewEditor.route,
                arguments = listOf(
                    navArgument(Screen.PreviewEditor.planIdArg) {
                        type = NavType.StringType
                    }
                ),
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString(Screen.PreviewEditor.planIdArg)
                PreviewEditorScreen(
                    state = uiState,
                    planId = planId,
                    onBack = { navController.popBackStack() },
                    onOpenPreviewEditor = remodelViewModel::openPreviewEditor,
                    onClosePreviewEditor = remodelViewModel::closePreviewEditor,
                    onSilhouetteChange = remodelViewModel::updatePreviewEditSilhouette,
                    onLengthChange = remodelViewModel::updatePreviewEditLength,
                    onNecklineChange = remodelViewModel::updatePreviewEditNeckline,
                    onSleeveChange = remodelViewModel::updatePreviewEditSleeve,
                    onFidelityChange = remodelViewModel::updatePreviewEditFidelity,
                    onInstructionsChange = remodelViewModel::updatePreviewEditInstructions,
                    onGenerateFinalImage = remodelViewModel::generatePreviewFromEditor,
                    onOpenPreviewResult = { selectedPlanId ->
                        navController.navigate(Screen.PreviewResult.createRoute(selectedPlanId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.PreviewResult.route,
                arguments = listOf(
                    navArgument(Screen.PreviewResult.planIdArg) {
                        type = NavType.StringType
                    }
                ),
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString(Screen.PreviewResult.planIdArg)
                PreviewResultScreen(
                    state = uiState,
                    planId = planId,
                    onBack = { navController.popBackStack() },
                    onBackToPlans = {
                        navController.navigate(Screen.Plan.route) {
                            popUpTo(Screen.Plan.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onEditPlan = { selectedPlanId ->
                        navController.navigate(Screen.PreviewEditor.createRoute(selectedPlanId)) {
                            launchSingleTop = true
                        }
                    },
                    onPublish = remodelViewModel::publishPreviewResult,
                    onOpenInspiration = {
                        navController.navigate(Screen.Inspiration.route) {
                            popUpTo(Screen.Inspiration.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onResumePolling = remodelViewModel::resumePreviewPolling,
                    onDismissError = remodelViewModel::clearError
                )
            }
        }
    }
}
}

private fun AnimatedContentTransitionScope<*>.slideInForward() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(
            durationMillis = AnimationDuration,
            easing = FastOutSlowInEasing
        )
    )

private fun AnimatedContentTransitionScope<*>.slideOutForward() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(
            durationMillis = AnimationDuration,
            easing = FastOutSlowInEasing
        )
    )

private fun AnimatedContentTransitionScope<*>.slideInBack() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(
            durationMillis = AnimationDuration,
            easing = FastOutSlowInEasing
        )
    )

private fun AnimatedContentTransitionScope<*>.slideOutBack() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(
            durationMillis = AnimationDuration,
            easing = FastOutSlowInEasing
        )
    )
