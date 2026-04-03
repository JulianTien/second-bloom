package com.scf.loop.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.scf.loop.navigation.Screen
import com.scf.loop.presentation.remodel.RemodelViewModel
import com.scf.loop.ui.components.LoopBottomNavBar
import com.scf.loop.ui.screens.HomeScreen
import com.scf.loop.ui.screens.InspirationScreen
import com.scf.loop.ui.screens.PlanetScreen
import com.scf.loop.ui.screens.ProfileScreen
import com.scf.loop.ui.screens.WardrobeScreen
import com.scf.loop.ui.screens.WorkbenchScreen

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
    var highlightedTopLevelRoute by rememberSaveable { mutableStateOf(Screen.Inspiration.route) }

    LaunchedEffect(currentRoute) {
        if (currentRoute in Screen.topLevelItems().map { it.route }) {
            highlightedTopLevelRoute = currentRoute ?: Screen.Inspiration.route
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            LoopBottomNavBar(
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
                WardrobeScreen()
            }

            composable(
                route = Screen.Planet.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                PlanetScreen()
            }

            composable(
                route = Screen.Profile.route,
                enterTransition = { slideInForward() },
                exitTransition = { slideOutForward() },
                popEnterTransition = { slideInBack() },
                popExitTransition = { slideOutBack() }
            ) {
                ProfileScreen(state = uiState)
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
                    onDismissError = remodelViewModel::clearError
                )
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
