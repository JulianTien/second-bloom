package com.scf.secondbloom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scf.secondbloom.auth.SecondBloomAuthUiState
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.ui.components.SecondBloomAuthCard
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.i18n.localizedLabel
import com.scf.secondbloom.ui.model.SecondBloomShowcaseContent
import com.scf.secondbloom.ui.model.ProfileWorkUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    state: RemodelUiState,
    authState: SecondBloomAuthUiState = SecondBloomAuthUiState.Guest,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    var activeTab by rememberSaveable { mutableStateOf(ProfileTab.Works) }
    val works = when (activeTab) {
        ProfileTab.Works -> profileWorksFromState(state, language)
        ProfileTab.Saved -> SecondBloomShowcaseContent.savedCollection(language)
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = localized(language, "Profile screen", "我的主页页面") },
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val openEntryLabel = when (authState) {
                    is SecondBloomAuthUiState.SignedIn -> localized(
                        language,
                        "Open account center",
                        "打开账号中心"
                    )
                    SecondBloomAuthUiState.Unconfigured -> localized(
                        language,
                        "Login is not available yet",
                        "登录尚未启用"
                    )
                    SecondBloomAuthUiState.Guest -> localized(
                        language,
                        "Log in or sign up",
                        "登录 / 注册"
                    )
                }
                val openEntryAction: (() -> Unit)? = when (authState) {
                    is SecondBloomAuthUiState.SignedIn -> onAccountClick
                    SecondBloomAuthUiState.Unconfigured,
                    SecondBloomAuthUiState.Guest -> onLoginClick
                }
                val entryRowModifier = if (openEntryAction != null) {
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = openEntryAction)
                        .semantics {
                            contentDescription = openEntryLabel
                        }
                } else {
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = openEntryLabel
                        }
                }
                Row(
                    modifier = entryRowModifier,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
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
                        when (val currentAuthState = authState) {
                            is SecondBloomAuthUiState.SignedIn -> {
                                if (!currentAuthState.profile.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = currentAuthState.profile.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = currentAuthState.profile.displayName.take(2).ifBlank { "Ci" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Ci",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when (val currentAuthState = authState) {
                                is SecondBloomAuthUiState.SignedIn -> currentAuthState.profile.displayName
                                SecondBloomAuthUiState.Unconfigured,
                                SecondBloomAuthUiState.Guest -> "Cici"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (authState) {
                                is SecondBloomAuthUiState.SignedIn -> localized(
                                    language,
                                    "You are signed in and ready to sync this wardrobe across devices.",
                                    "你已登录，可以开始跨设备同步你的衣橱。"
                                )
                                SecondBloomAuthUiState.Unconfigured -> localized(
                                    language,
                                    "Guest-first mode is active until Clerk is configured.",
                                    "在 Clerk 配置完成前，当前处于游客优先模式。"
                                )
                                SecondBloomAuthUiState.Guest -> localized(
                                    language,
                                    "A coder who loves design can still become a great tailor.",
                                    "不想做设计的程序员不是好裁缝。"
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = openEntryAction ?: {},
                            enabled = openEntryAction != null,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            val buttonText = when (authState) {
                                is SecondBloomAuthUiState.SignedIn -> localized(
                                    language,
                                    "Account center",
                                    "账号中心"
                                )
                                SecondBloomAuthUiState.Unconfigured -> localized(
                                    language,
                                    "Login not available",
                                    "登录尚未启用"
                                )
                                SecondBloomAuthUiState.Guest -> localized(
                                    language,
                                    "Log in or sign up",
                                    "登录 / 注册"
                                )
                            }
                            Text(buttonText)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            ProfileStat(label = localized(language, "Following", "关注"), value = "12")
                            ProfileStat(label = localized(language, "Followers", "粉丝"), value = "856")
                            ProfileStat(label = localized(language, "Likes", "获赞"), value = "3.2k")
                        }
                    }
                }

                SecondBloomAuthCard(
                    authState = authState,
                    onLoginClick = onLoginClick,
                    onAccountClick = onAccountClick,
                    onLogoutClick = onLogoutClick
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = localized(language, "Language", "语言"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.entries.forEach { appLanguage ->
                                AssistChip(
                                    onClick = { onLanguageSelected(appLanguage) },
                                    label = { Text(appLanguage.displayLabel(language)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (state.appLanguage == appLanguage) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerLow
                                        },
                                        labelColor = if (state.appLanguage == appLanguage) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                )
                            }
                        }
                        Text(
                            text = localized(language, "Plan prompts and generated content will follow the selected language.", "改制方案提示词和生成结果会跟随当前语言切换。"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            text = localized(language, "Activity summary", "记录摘要"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = localized(
                                language,
                                "${state.recentAnalysisRecords.size} analyses · ${state.recentPlanGenerationRecords.size} plan runs",
                                "识别 ${state.recentAnalysisRecords.size} 条 · 方案 ${state.recentPlanGenerationRecords.size} 条"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        state.latestPlanGenerationRecord?.let { record ->
                            Text(
                                text = localized(
                                    language,
                                    "Latest plan: ${record.plans.firstOrNull()?.title ?: record.intent.localizedLabel(language)}",
                                    "最近一次方案：${record.plans.firstOrNull()?.title ?: record.intent.localizedLabel(language)}"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("LV.${state.sustainabilitySummary.level} ${state.sustainabilitySummary.levelTitle}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = {
                                    Text(
                                        localized(
                                            language,
                                            "${state.sustainabilitySummary.estimatedWaterSavedLiters} L water saved",
                                            "${state.sustainabilitySummary.estimatedWaterSavedLiters} L 节水估算"
                                        )
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                if (state.recentActivities.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = localized(language, "Recent activity", "最近动态"),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            state.recentActivities.take(3).forEach { activity ->
                                RecentActivityRow(
                                    badgeLabel = activity.badgeLabel,
                                    title = activity.title,
                                    subtitle = activity.subtitle,
                                    supportingText = activity.supportingText
                                )
                            }
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
                    text = localized(language, "My remakes", "我的改造"),
                    selected = activeTab == ProfileTab.Works,
                    icon = { Text("✂️") },
                    onClick = { activeTab = ProfileTab.Works },
                    modifier = Modifier.weight(1f)
                )
                ProfileTabChip(
                    text = localized(language, "Saved", "收藏夹"),
                    selected = activeTab == ProfileTab.Saved,
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (activeTab == ProfileTab.Saved) {
                                MaterialTheme.colorScheme.onPrimary
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
                            text = localized(language, "No works yet", "还没有作品"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = localized(language, "Finish one AI remake and your gallery will appear here automatically.", "完成一次 AI 改制后，这里会自动生成你的作品墙。"),
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
private fun RecentActivityRow(
    badgeLabel: String,
    title: String,
    subtitle: String,
    supportingText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = badgeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (selected) "Selected, $text" else "Switch to $text"
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
                fontWeight = FontWeight.SemiBold,
                color = contentColor
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
            .semantics { contentDescription = "Work: ${work.title}" }
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
                    imageVector = if (work.id.startsWith("saved-")) Icons.Outlined.BookmarkBorder else Icons.Outlined.GridView,
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

private fun profileWorksFromState(
    state: RemodelUiState,
    language: AppLanguage
): List<ProfileWorkUiModel> {
    val savedWorks = state.recentPlanGenerationRecords.mapIndexed { index, record ->
        val plan = record.plans.firstOrNull()
        ProfileWorkUiModel(
            id = record.recordId,
            title = plan?.title ?: record.intent.localizedLabel(language),
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
        SecondBloomShowcaseContent.fallbackProfileWorks(language)
    }
}

private enum class ProfileTab {
    Works,
    Saved
}
