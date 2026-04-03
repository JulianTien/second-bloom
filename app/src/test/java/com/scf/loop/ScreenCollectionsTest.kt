package com.scf.loop

import com.scf.loop.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenCollectionsTest {

    @Test
    fun topLevelItems_returnsAllBottomTabsInStableOrder() {
        val items = Screen.topLevelItems()

        assertEquals(
            listOf(
                Screen.Inspiration.route,
                Screen.Wardrobe.route,
                Screen.Planet.route,
                Screen.Profile.route
            ),
            items.map { it.route }
        )
        assertTrue(items.all { it.showInBottomBar })
    }

    @Test
    fun flowItems_returnsRemodelFlowScreens() {
        val items = Screen.flowItems()

        assertEquals(
            listOf(
                Screen.CameraRecognition.route,
                Screen.Plan.route
            ),
            items.map { it.route }
        )
        assertTrue(items.none { it.showInBottomBar })
    }
}
