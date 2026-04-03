package com.scf.loop.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scf.loop.navigation.Screen

@Composable
fun LoopBottomNavBar(
    highlightedRoute: String?,
    onNavigate: (String) -> Unit,
    onOpenRemodelFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fabPulse = rememberInfiniteTransition(label = "fabPulse")
    val fabScale = fabPulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )
    val fabHaloAlpha = fabPulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabHaloAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 10.dp,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = Screen.topLevelItems()
                LoopNavigationItem(
                    screen = items[0],
                    selected = highlightedRoute == items[0].route,
                    onClick = { onNavigate(items[0].route) }
                )
                LoopNavigationItem(
                    screen = items[1],
                    selected = highlightedRoute == items[1].route,
                    onClick = { onNavigate(items[1].route) }
                )
                Spacer(modifier = Modifier.width(68.dp))
                LoopNavigationItem(
                    screen = items[2],
                    selected = highlightedRoute == items[2].route,
                    onClick = { onNavigate(items[2].route) }
                )
                LoopNavigationItem(
                    screen = items[3],
                    selected = highlightedRoute == items[3].route,
                    onClick = { onNavigate(items[3].route) }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(76.dp)
                .scale(fabScale.value)
                .alpha(fabHaloAlpha.value)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )

        FloatingActionButton(
            onClick = onOpenRemodelFlow,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics {
                    contentDescription = "AI改制入口，双击进入上传识别流程"
                },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Screen.CameraRecognition.selectedIcon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun LoopNavigationItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val selectedBackground = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (selected) {
                    "已选中，${screen.title}，${screen.contentDescription}"
                } else {
                    "${screen.title}，${screen.contentDescription}，双击切换页面"
                }
            }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = selectedBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = null,
                tint = tint
            )
        }
        Text(
            text = screen.title,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
