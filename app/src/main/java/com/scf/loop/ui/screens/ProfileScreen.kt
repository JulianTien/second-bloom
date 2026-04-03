package com.scf.loop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scf.loop.domain.model.RemodelUiState
import com.scf.loop.ui.model.LoopShowcaseContent
import com.scf.loop.ui.model.ProfileWorkUiModel

@Composable
fun ProfileScreen(
    state: RemodelUiState,
    modifier: Modifier = Modifier
) {
    var activeTab by rememberSaveable { mutableStateOf(ProfileTab.Works) }
    val works = when (activeTab) {
        ProfileTab.Works -> profileWorksFromState(state)
        ProfileTab.Saved -> LoopShowcaseContent.savedCollection
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "我的主页页面" },
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primary
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ci",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Cici",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "不想做设计的程序员不是好裁缝。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            ProfileStat(label = "关注", value = "12")
                            ProfileStat(label = "粉丝", value = "856")
                            ProfileStat(label = "获赞", value = "3.2k")
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "记录摘要",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "识别 ${state.recentAnalysisRecords.size} 条 · 方案 ${state.recentPlanGenerationRecords.size} 条",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        state.latestPlanGenerationRecord?.let { record ->
                            Text(
                                text = "最近一次方案：${record.plans.firstOrNull()?.title ?: record.intent.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileTabChip(
                    text = "我的改造",
                    selected = activeTab == ProfileTab.Works,
                    icon = { Text("✂️") },
                    onClick = { activeTab = ProfileTab.Works },
                    modifier = Modifier.weight(1f)
                )
                ProfileTabChip(
                    text = "收藏夹",
                    selected = activeTab == ProfileTab.Saved,
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (activeTab == ProfileTab.Saved) {
                                MaterialTheme.colorScheme.background
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    onClick = { activeTab = ProfileTab.Saved },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (works.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "还没有作品",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "完成一次 AI 改制后，这里会自动生成你的作品墙。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(works, key = { it.id }) { work ->
                ProfileWorkTile(work = work)
            }
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileTabChip(
    text: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (selected) "已选中，$text" else "切换到$text"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = text,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileWorkTile(
    work: ProfileWorkUiModel
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(work.gradientStart, work.gradientEnd)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
            .fillMaxWidth()
            .semantics { contentDescription = "作品：${work.title}" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (work.subtitle == "收藏灵感") Icons.Outlined.BookmarkBorder else Icons.Outlined.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = work.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun profileWorksFromState(state: RemodelUiState): List<ProfileWorkUiModel> {
    val savedWorks = state.recentPlanGenerationRecords.mapIndexed { index, record ->
        val plan = record.plans.firstOrNull()
        ProfileWorkUiModel(
            id = record.recordId,
            title = plan?.title ?: record.intent.label,
            subtitle = record.sourceImage.fileName,
            gradientStart = when (index % 3) {
                0 -> androidx.compose.ui.graphics.Color(0xFFE8DDF8)
                1 -> androidx.compose.ui.graphics.Color(0xFFD9F2E0)
                else -> androidx.compose.ui.graphics.Color(0xFFFCE1D7)
            },
            gradientEnd = when (index % 3) {
                0 -> androidx.compose.ui.graphics.Color(0xFFBCAAEF)
                1 -> androidx.compose.ui.graphics.Color(0xFF8ECDA1)
                else -> androidx.compose.ui.graphics.Color(0xFFF1A681)
            }
        )
    }

    return if (savedWorks.isNotEmpty()) {
        savedWorks.take(9)
    } else {
        LoopShowcaseContent.fallbackProfileWorks
    }
}

private enum class ProfileTab {
    Works,
    Saved
}
