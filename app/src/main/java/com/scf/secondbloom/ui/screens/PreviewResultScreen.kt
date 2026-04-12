package com.scf.secondbloom.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scf.secondbloom.domain.model.PlanPreviewResult
import com.scf.secondbloom.domain.model.PreviewJobStatus
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.i18n.localizedLabel

@Composable
fun PreviewResultScreen(
    state: RemodelUiState,
    planId: String?,
    onBack: () -> Unit,
    onBackToPlans: () -> Unit,
    onEditPlan: (String) -> Unit,
    onPublish: (String) -> Unit,
    onOpenInspiration: () -> Unit,
    onResumePolling: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val currentPlan = planId?.let { requestedId ->
        state.plans.firstOrNull { it.planId == requestedId }
    }
    val preview = planId?.let(state::previewFor)
    val publishedRemodel = planId?.let(state::publishedRemodelFor)
    val isPublished = publishedRemodel != null
    val hasRenderableAssets = preview?.hasRenderableAssets() == true
    val hasActiveJobForPlan = !planId.isNullOrBlank() &&
        state.selectedPlanId == planId &&
        state.previewJob?.status in setOf(PreviewJobStatus.QUEUED, PreviewJobStatus.RUNNING)
    val isPreviewPending = preview?.renderStatus in setOf(
        PreviewRenderStatus.QUEUED,
        PreviewRenderStatus.RUNNING
    ) || hasActiveJobForPlan

    BackHandler(onBack = onBack)

    LaunchedEffect(planId, preview?.renderStatus, state.previewJob?.previewJobId, state.isPreviewLoading) {
        if (!planId.isNullOrBlank() && isPreviewPending && !state.isPreviewLoading) {
            onResumePolling(planId)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = localized(language, "Back", "返回上一步")
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = localized(language, "Final result", "最终效果图"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = localized(language, "Track generation progress and compare the final before-and-after result here.", "在这里集中查看生成进度、最终成图和对比结果。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (currentPlan == null) {
            item {
                PreviewResultCard(
                    title = localized(language, "No result to show", "没有可展示的方案"),
                    description = localized(language, "Pick a plan first, then open the final result page.", "先回到方案页选择一个方案，再进入最终效果图页面。")
                ) {
                    Button(
                        onClick = onBackToPlans,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localized(language, "Back to plans", "返回方案页"))
                    }
                }
            }
            return@LazyColumn
        }

        item {
            PreviewResultCard(
                title = currentPlan.title,
                description = currentPlan.summary
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultMetaTag(currentPlan.difficulty.localizedLabel(language))
                    ResultMetaTag(currentPlan.estimatedTime)
                }
            }
        }

        item {
            when {
                state.isPreviewLoading && state.selectedPlanId == currentPlan.planId -> {
                    PreviewResultCard(
                        title = localized(language, "Generating", "正在生成中"),
                        description = localized(language, "Image editing has started. Stay on this page to watch the result arrive.", "真图编辑已经启动，请留在此页等待结果返回。")
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                preview == null && hasActiveJobForPlan -> {
                    PreviewResultCard(
                        title = localized(language, "Still generating", "仍在生成中"),
                        description = state.previewErrorMessage ?: localized(
                            language,
                            "The final image job is active and the result payload has not arrived yet. Stay here for updates or continue editing while it runs.",
                            "最终效果图任务仍在进行中，结果数据还没返回。你可以留在这里等待更新，或先继续调整方案。"
                        )
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                preview == null && !state.previewErrorMessage.isNullOrBlank() -> {
                    PreviewResultCard(
                        title = localized(language, "Current image status", "当前效果图状态"),
                        description = state.previewErrorMessage
                    ) {
                        Button(
                            onClick = { onEditPlan(currentPlan.planId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Back to editor", "返回编辑后重试"))
                        }
                        TextButton(
                            onClick = onDismissError,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Dismiss", "关闭提示"))
                        }
                    }
                }
                preview == null -> {
                    PreviewResultCard(
                        title = localized(language, "No result yet", "还没有效果图结果"),
                        description = localized(language, "Start generation from the image editor and the result will appear here.", "先从真图编辑页发起生成，结果会展示在这里。")
                    ) {
                        Button(
                            onClick = { onEditPlan(currentPlan.planId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Open image editor", "前往真图编辑"))
                        }
                    }
                }
                preview.renderStatus in setOf(PreviewRenderStatus.FAILED, PreviewRenderStatus.FILTERED) -> {
                    PreviewResultCard(
                        title = localized(language, "Generation did not finish", "本次生成未完成"),
                        description = preview.errorMessage ?: "当前效果图暂时不可用。"
                    ) {
                        Button(
                            onClick = { onEditPlan(currentPlan.planId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Back to editor", "返回编辑后重试"))
                        }
                        TextButton(
                            onClick = onDismissError,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Dismiss", "关闭提示"))
                        }
                    }
                }
                preview.renderStatus in setOf(PreviewRenderStatus.QUEUED, PreviewRenderStatus.RUNNING) -> {
                    PreviewResultCard(
                        title = localized(language, "Still generating", "仍在生成中"),
                        description = state.previewErrorMessage ?: localized(
                            language,
                            "The final image is still processing. Stay here for updates or continue editing while it runs.",
                            "最终效果图仍在生成中。你可以留在这里等待更新，或先继续调整方案。"
                        )
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                preview.renderStatus == PreviewRenderStatus.COMPLETED && !hasRenderableAssets -> {
                    PreviewResultCard(
                        title = localized(language, "Image assets unavailable", "当前效果图素材暂不可用"),
                        description = localized(
                            language,
                            "The generation finished, but the image files are missing or expired. Open the editor to generate a fresh result.",
                            "生成流程已经完成，但图片素材缺失或已过期。请回到编辑页重新生成一版最新效果图。"
                        )
                    ) {
                        Button(
                            onClick = { onEditPlan(currentPlan.planId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Generate again from editor", "返回编辑页重新生成"))
                        }
                    }
                }
                preview.renderStatus == PreviewRenderStatus.COMPLETED -> {
                    PreviewGalleryCard(preview = preview)
                }
                else -> {
                    PreviewResultCard(
                        title = localized(language, "Current image status", "当前效果图状态"),
                        description = state.previewErrorMessage ?: localized(
                            language,
                            "This plan has not produced a displayable final image yet.",
                            "当前方案还没有产出可展示的最终效果图。"
                        )
                    ) {
                        Button(
                            onClick = { onEditPlan(currentPlan.planId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localized(language, "Back to editor", "返回编辑页"))
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (preview?.renderStatus == PreviewRenderStatus.COMPLETED && hasRenderableAssets) {
                    Button(
                        onClick = {
                            if (isPublished) {
                                onOpenInspiration()
                            } else {
                                onPublish(currentPlan.planId)
                                onOpenInspiration()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isPublished) {
                                localized(language, "View in inspiration", "在灵感空间查看")
                            } else {
                                localized(language, "Publish to inspiration", "发布到灵感空间")
                            }
                        )
                    }
                }
                Button(
                    onClick = { onEditPlan(currentPlan.planId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localized(language, "Continue editing", "继续编辑该方案"))
                }
                TextButton(
                    onClick = onBackToPlans,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localized(language, "Back to plans", "返回方案页"))
                }
            }
        }
    }
}

@Composable
private fun PreviewGalleryCard(preview: PlanPreviewResult) {
    val language = LocalAppLanguage.current
    PreviewResultCard(
        title = localized(language, "Final image ready", "最终效果图已生成"),
        description = localized(language, "Compare the before-and-after result here.", "你可以在这里集中对比改造前后效果。")
    ) {
        preview.beforeImage?.url?.let {
            PreviewResultImageCard(
                title = localized(language, "Before", "改造前"),
                imageUrl = it
            )
        }
        preview.afterImage?.url?.let {
            PreviewResultImageCard(
                title = localized(language, "After", "改造后"),
                imageUrl = it
            )
        }
        preview.comparisonImage?.url?.let {
            PreviewResultImageCard(
                title = localized(language, "Compare", "对比图"),
                imageUrl = it
            )
        }
        Text(
            text = preview.disclaimer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun PlanPreviewResult.hasRenderableAssets(): Boolean =
    !beforeImage?.url.isNullOrBlank() ||
        !afterImage?.url.isNullOrBlank() ||
        !comparisonImage?.url.isNullOrBlank()

@Composable
private fun PreviewResultImageCard(
    title: String,
    imageUrl: String?
) {
    val language = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            if (imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localized(language, "Waiting for result", "等待生成"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviewResultCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun ResultMetaTag(text: String) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Checkroom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
