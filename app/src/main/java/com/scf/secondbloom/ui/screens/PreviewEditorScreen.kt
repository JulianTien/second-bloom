package com.scf.secondbloom.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.scf.secondbloom.domain.model.PreviewEditFidelity
import com.scf.secondbloom.domain.model.PreviewEditLength
import com.scf.secondbloom.domain.model.PreviewEditNeckline
import com.scf.secondbloom.domain.model.PreviewEditSilhouette
import com.scf.secondbloom.domain.model.PreviewEditSleeve
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.i18n.localizedLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewEditorScreen(
    state: RemodelUiState,
    planId: String?,
    onBack: () -> Unit,
    onOpenPreviewEditor: (String) -> Unit,
    onClosePreviewEditor: () -> Unit,
    onSilhouetteChange: (PreviewEditSilhouette) -> Unit,
    onLengthChange: (PreviewEditLength) -> Unit,
    onNecklineChange: (PreviewEditNeckline) -> Unit,
    onSleeveChange: (PreviewEditSleeve) -> Unit,
    onFidelityChange: (PreviewEditFidelity) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onGenerateFinalImage: () -> Unit,
    onOpenPreviewResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    LaunchedEffect(planId) {
        planId?.let(onOpenPreviewEditor)
    }

    val currentPlan = state.currentEditingPlan ?: planId?.let { requestedId ->
        state.plans.firstOrNull { it.planId == requestedId }
    }

    fun closePage() {
        onClosePreviewEditor()
        onBack()
    }

    BackHandler(onBack = ::closePage)

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
                IconButton(onClick = ::closePage) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = localized(language, "Back to plans", "返回方案页")
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = localized(language, "Image editor", "真图编辑"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = localized(language, "Fine-tune silhouette and details before generating the final image.", "先微调版型与细节，再生成该方案的最终效果图。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (currentPlan == null) {
            item {
                PreviewEditorCard(
                    title = localized(language, "No editable plan", "没有可编辑的方案"),
                    description = localized(language, "Go back to plans, generate one, then return to the image editor.", "先回到方案页生成并选择一个方案，再进入真图编辑。")
                ) {
                    Button(
                        onClick = ::closePage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localized(language, "Back to plans", "返回方案页"))
                    }
                }
            }
            return@LazyColumn
        }

        item {
            PreviewEditorCard(
                title = localized(language, "Current plan", "当前方案"),
                description = localized(language, "You are refining this generated remodel plan before final rendering.", "你正在微调这套已生成的改制方案。")
            ) {
                state.selectedImage?.let { image ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = localized(language, "Original garment image", "当前旧衣原图"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
                PlanSummaryCard(plan = currentPlan)
            }
        }

        item {
            PreviewEditorCard(
                title = localized(language, "Shape and fidelity", "版型与保真"),
                description = localized(language, "These options will be sent to the image-edit model as tuning instructions.", "这些选项会作为微调指令传给真图编辑模型。")
            ) {
                PreviewOptionGroup(
                    title = localized(language, "Silhouette", "整体廓形"),
                    options = PreviewEditSilhouette.entries,
                    selected = state.previewEditOptions.silhouette,
                    label = { it.localizedLabel(language) },
                    onSelect = onSilhouetteChange
                )
                PreviewOptionGroup(
                    title = localized(language, "Length", "衣长"),
                    options = PreviewEditLength.entries,
                    selected = state.previewEditOptions.length,
                    label = { it.localizedLabel(language) },
                    onSelect = onLengthChange
                )
                PreviewOptionGroup(
                    title = localized(language, "Neckline", "领口"),
                    options = PreviewEditNeckline.entries,
                    selected = state.previewEditOptions.neckline,
                    label = { it.localizedLabel(language) },
                    onSelect = onNecklineChange
                )
                PreviewOptionGroup(
                    title = localized(language, "Sleeves", "袖型"),
                    options = PreviewEditSleeve.entries,
                    selected = state.previewEditOptions.sleeve,
                    label = { it.localizedLabel(language) },
                    onSelect = onSleeveChange
                )
                PreviewOptionGroup(
                    title = localized(language, "Fidelity", "保真策略"),
                    options = PreviewEditFidelity.entries,
                    selected = state.previewEditOptions.fidelity,
                    label = { it.localizedLabel(language) },
                    onSelect = onFidelityChange
                )
            }
        }

        item {
            PreviewEditorCard(
                title = localized(language, "Extra notes", "额外说明"),
                description = localized(language, "Add one or two details you want to preserve or emphasize.", "可以补充一两句想保留或想强调的细节。")
            ) {
                OutlinedTextField(
                    value = state.previewEditOptions.extraInstructions,
                    onValueChange = onInstructionsChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text(localized(language, "Tuning notes", "补充微调说明")) },
                    supportingText = {
                        Text(localized(language, "For example: preserve the worn texture, add only slight waist shaping, and do not change the person or background.", "例如：保留旧衣质感，只轻微收腰；不要改动人物与背景。"))
                    }
                )
            }
        }

        if (state.isPreviewLoading) {
            item {
                PreviewEditorCard(
                    title = localized(language, "Generating final image", "正在生成最终效果图"),
                    description = localized(language, "Image editing is running. You can stay here or open the result page to watch progress.", "真图编辑已经启动，返回方案页后可以继续查看状态。")
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        state.previewErrorMessage?.let { message ->
            item {
                PreviewEditorCard(
                    title = localized(language, "Current image status", "当前效果图状态"),
                    description = message
                ) {
                    Text(
                        text = localized(language, "Adjust the tuning and try again.", "你可以继续微调后再次发起生成。"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onGenerateFinalImage()
                        onOpenPreviewResult(currentPlan.planId)
                    },
                    enabled = state.canGenerateFinalEffectImage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localized(language, "Generate final image", "生成最终效果图"))
                }
                TextButton(
                    onClick = { onOpenPreviewResult(currentPlan.planId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localized(language, "Open result page", "直接查看该方案效果图页"))
                }
            }
        }
    }
}

@Composable
private fun PlanSummaryCard(plan: RemodelPlan) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = plan.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewMetaTag(plan.difficulty.localizedLabel(LocalAppLanguage.current))
                PreviewMetaTag(plan.estimatedTime)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> PreviewOptionGroup(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Card(
                    modifier = Modifier.clickable { onSelect(option) },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewEditorCard(
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
                color = MaterialTheme.colorScheme.onSurface
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
private fun PreviewMetaTag(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
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
