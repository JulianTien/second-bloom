package com.scf.secondbloom.ui.components

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier

fun Modifier.secondBloomTopLevelScreenInsets(): Modifier =
    statusBarsPadding()

fun Modifier.secondBloomFlowScreenInsets(): Modifier =
    systemBarsPadding().imePadding()

fun Modifier.secondBloomBottomNavigationInsets(): Modifier =
    navigationBarsPadding()
