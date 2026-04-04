package com.scf.secondbloom.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.PlanPreviewResult
import com.scf.secondbloom.domain.model.PreviewRenderStatus
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.RemodelStage
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.ui.components.SecondBloomWorkflowStep
import com.scf.secondbloom.ui.components.SecondBloomWorkflowStrip
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.i18n.localizedLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkbenchScreen(
    state: RemodelUiState,
    onGarmentTypeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onDefectsChange: (String) -> Unit,
    onIntentSelected: (RemodelIntent) -> Unit,
    onPreferencesChange: (String) -> Unit,
    onGeneratePlans: () -> Unit,
    onOpenPreviewEditor: (String) -> Unit,
    onOpenPreviewResult: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    var showEditor by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = localized(language, "Plan screen", "制衣方案页面") },
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WorkbenchHeader(language)
        }

        item {
            SecondBloomWorkflowStrip(steps = workbenchWorkflowSteps(state, language))
        }

        val draftAnalysis = state.draftAnalysis
        if (draftAnalysis == null) {
            item {
                CompactWorkbenchCard(
                    title = localized(language, "No analysis yet", "还没有识别结果"),
                    description = localized(language, "Complete garment analysis first, then come back to generate plans.", "先从 AI 改制入口完成识别，再回来生成方案。")
                ) {
                    Text(
                        text = localized(language, "This area will show intent selection and plan results after analysis.", "这里会在拿到识别摘要后显示目标选择和方案结果。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            return@LazyColumn
        }

        if (state.error != null) {
            item {
                CompactWorkbenchBanner(
                    title = localized(language, "Plan generation failed", "方案生成失败"),
                    message = state.error.message,
                    action = {
                        TextButton(onClick = onDismissError) {
                            Text(localized(language, "Dismiss", "关闭"))
                        }
                    }
                )
            }
        }

        if (state.stage == RemodelStage.LowConfidence) {
            item {
                CompactWorkbenchBanner(
                    title = localized(language, "Low-confidence analysis", "这份结果来自低置信度识别"),
                    message = localized(language, "Check garment type, color, and defects before generating plans.", "建议先核对衣物类型、颜色和瑕疵，再决定是否生成方案。")
                )
            }
        }

        item {
            SummarySection(
                analysis = draftAnalysis,
                showEditor = showEditor,
                onToggleEditor = { showEditor = !showEditor },
                language = language
            )
        }

        if (showEditor) {
            item {
                EditorSection(
                    analysis = draftAnalysis,
                    onGarmentTypeChange = onGarmentTypeChange,
                    onColorChange = onColorChange,
                    onMaterialChange = onMaterialChange,
                    onStyleChange = onStyleChange,
                    onDefectsChange = onDefectsChange
                )
            }
        }

        item {
            IntentSection(
                state = state,
                onIntentSelected = onIntentSelected,
                onPreferencesChange = onPreferencesChange,
                onGeneratePlans = onGeneratePlans
            )
        }

        if (state.stage == RemodelStage.GeneratingPlans) {
            item {
                CompactWorkbenchBanner(
                    title = localized(language, "Generating plans", "正在生成方案"),
                    message = localized(language, "Preparing difficulty, materials, and steps.", "正在整理难度、材料和步骤建议。")
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (state.plans.isEmpty()) {
            item {
                CompactWorkbenchCard(
                    title = localized(language, "No plans yet", "还没有方案"),
                    description = localized(language, "Choose an intent and plans will appear here.", "选择改制目标后，这里会出现紧凑的方案卡片。")
                ) {
                    Text(
                        text = localized(language, "Each card focuses on title, difficulty, timing, and key steps.", "优先展示标题、难度、耗时和关键步骤。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            item {
                Text(
                    text = localized(language, "Plan results", "方案结果"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                Text(
                    text = localized(language, "Plans have been saved to your profile automatically.", "方案已自动保存到我的主页。"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (state.isPreviewLoading) {
                item {
                    CompactWorkbenchBanner(
                        title = localized(language, "Generating final image", "正在生成最终效果图"),
                        message = localized(language, "Image editing has started for your confirmed plan.", "已按你确认的方案启动真图编辑，请稍候。")
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            state.previewErrorMessage?.let { message ->
                item {
                    CompactWorkbenchBanner(
                        title = localized(language, "Image status", "效果图状态"),
                        message = message
                    )
                }
            }
            items(state.plans) { plan ->
                PlanCard(
                    plan = plan,
                    preview = state.previewFor(plan.planId),
                    isSelected = state.selectedPlanId == plan.planId,
                    isGenerating = state.isPreviewLoading && state.selectedPlanId == plan.planId,
                    onOpenPreviewEditor = { onOpenPreviewEditor(plan.planId) },
                    onOpenPreviewResult = { onOpenPreviewResult(plan.planId) }
                )
            }
        }
    }
}

private fun workbenchWorkflowSteps(
    state: RemodelUiState,
    language: com.scf.secondbloom.domain.model.AppLanguage
): List<SecondBloomWorkflowStep> {
    val hasAnalysis = state.draftAnalysis != null
    val hasPlans = state.plans.isNotEmpty()
    val isGenerating = state.stage == RemodelStage.GeneratingPlans

    return listOf(
        SecondBloomWorkflowStep(
            number = 1,
            title = localized(language, "Summary", "摘要"),
            description = localized(language, "Review analysis", "查看识别摘要"),
            isCurrent = hasAnalysis && !isGenerating && !hasPlans,
            isComplete = hasAnalysis
        ),
        SecondBloomWorkflowStep(
            number = 2,
            title = localized(language, "Intent", "目标"),
            description = localized(language, "Choose remodel goal", "选择改制目标"),
            isCurrent = hasAnalysis && !isGenerating && !hasPlans,
            isComplete = state.selectedIntent != null
        ),
        SecondBloomWorkflowStep(
            number = 3,
            title = localized(language, "Plans", "方案"),
            description = localized(language, "Review plans", "查看方案"),
            isCurrent = isGenerating || hasPlans,
            isComplete = hasPlans
        )
    )
}

@Composable
private fun WorkbenchHeader(language: com.scf.secondbloom.domain.model.AppLanguage) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactWorkbenchEyebrow(text = localized(language, "Plans", "制衣方案"))
        Text(
            text = localized(language, "Turn analysis into remodel plans.", "把识别结果变成方案。"),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = localized(language, "Review the summary, choose an intent, then compare the results.", "先确认摘要，再选择目标，最后查看结果。"),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(
    analysis: GarmentAnalysis,
    showEditor: Boolean,
    onToggleEditor: () -> Unit,
    language: com.scf.secondbloom.domain.model.AppLanguage
) {
    CompactWorkbenchCard(
        title = localized(language, "Analysis summary", "识别摘要"),
        description = localized(language, "Review the key fields first, then expand to edit only if needed.", "先看关键字段，只有需要时再展开编辑。")
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                analysis.garmentType,
                analysis.color,
                analysis.material,
                analysis.style
            ).forEach { label ->
                CompactWorkbenchTag(label = label)
            }
        }
        Text(
            text = if (language == com.scf.secondbloom.domain.model.AppLanguage.ENGLISH) {
                "Confidence ${(analysis.confidence * 100).toInt()}% · ${if (analysis.backgroundComplexity == BackgroundComplexity.HIGH) "Complex background" else "Clean background"}"
            } else {
                "置信度 ${(analysis.confidence * 100).toInt()}% · 背景${if (analysis.backgroundComplexity == BackgroundComplexity.HIGH) "较复杂" else "较干净"}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (analysis.defects.isNotEmpty()) {
            Text(
                text = if (language == com.scf.secondbloom.domain.model.AppLanguage.ENGLISH) {
                    "Defects: ${analysis.defects.joinToString(", ") { it.name }}"
                } else {
                    "瑕疵：${analysis.defects.joinToString("、") { it.name }}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onToggleEditor) {
            Text(if (showEditor) localized(language, "Hide editor", "收起编辑") else localized(language, "Edit fields", "展开编辑"))
        }
    }
}

@Composable
private fun EditorSection(
    analysis: GarmentAnalysis,
    onGarmentTypeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onDefectsChange: (String) -> Unit
) {
    val language = LocalAppLanguage.current
    CompactWorkbenchCard(
        title = localized(language, "Edit fields", "编辑字段"),
        description = localized(language, "Only change the parts that look inaccurate.", "只调整你觉得不准确的部分。")
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = localized(language, "Start with garment type, color, and defects.", "建议优先核对衣物类型、颜色和瑕疵。"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        OutlinedTextField(
            value = analysis.garmentType,
            onValueChange = onGarmentTypeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Garment type", "衣物类型")) }
        )
        OutlinedTextField(
            value = analysis.color,
            onValueChange = onColorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Color", "颜色")) }
        )
        OutlinedTextField(
            value = analysis.material,
            onValueChange = onMaterialChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Material", "材质")) }
        )
        OutlinedTextField(
            value = analysis.style,
            onValueChange = onStyleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Style", "风格")) }
        )
        OutlinedTextField(
            value = analysis.defects.joinToString("，") { it.name },
            onValueChange = onDefectsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Defects", "瑕疵描述")) },
            supportingText = { Text(localized(language, "Use commas to separate multiple defects.", "多个瑕疵可使用中文逗号分隔")) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentSection(
    state: RemodelUiState,
    onIntentSelected: (RemodelIntent) -> Unit,
    onPreferencesChange: (String) -> Unit,
    onGeneratePlans: () -> Unit
) {
    val language = LocalAppLanguage.current
    CompactWorkbenchCard(
        title = localized(language, "Remodel intent", "改制目标"),
        description = localized(language, "Choose one direction, then add a short preference note.", "选一个方向，再补充一句偏好。")
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RemodelIntent.entries.forEach { intent ->
                val selected = state.selectedIntent == intent
                Card(
                    modifier = Modifier.clickable { onIntentSelected(intent) },
                    shape = RoundedCornerShape(999.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Style,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = intent.localizedLabel(language),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = state.userPreferences,
            onValueChange = onPreferencesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized(language, "Preferences", "补充偏好")) },
            supportingText = { Text(localized(language, "For example: keep the original silhouette and preserve a polished feel.", "例如：尽量少改动版型、保留正式感")) }
        )
        Button(
            onClick = onGeneratePlans,
            enabled = state.canGeneratePlans,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(localized(language, "Generate remodel plans", "生成改制方案"))
        }
    }
}

@Composable
private fun PlanCard(
    plan: RemodelPlan,
    preview: PlanPreviewResult?,
    isSelected: Boolean,
    isGenerating: Boolean,
    onOpenPreviewEditor: () -> Unit,
    onOpenPreviewResult: () -> Unit
) {
    val language = LocalAppLanguage.current
    var expanded by rememberSaveable(plan.title) { mutableStateOf(false) }
    val visibleSteps = if (expanded) plan.steps else plan.steps.take(2)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onOpenPreviewEditor,
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isGenerating -> localized(language, "Generating final image", "正在生成最终效果图")
                        isSelected -> localized(language, "Edit this plan again", "重新编辑该方案")
                        else -> localized(language, "Open image editor", "进入真图编辑")
                    }
                )
            }
            if (preview != null || isSelected) {
                TextButton(
                    onClick = onOpenPreviewResult,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isGenerating -> localized(language, "View generation progress", "查看生成进度")
                            preview?.renderStatus == PreviewRenderStatus.COMPLETED -> localized(language, "View final result", "查看最终效果图")
                            else -> localized(language, "View image status", "查看效果图状态")
                        }
                    )
                }
            }
            Text(
                text = plan.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactWorkbenchTag(label = plan.difficulty.localizedLabel(language))
                CompactWorkbenchTag(label = plan.estimatedTime)
            }
            Text(
                text = if (language == com.scf.secondbloom.domain.model.AppLanguage.ENGLISH) {
                    "Materials: ${plan.materials.joinToString(", ")}"
                } else {
                    "材料：${plan.materials.joinToString("、")}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                visibleSteps.forEachIndexed { index, step ->
                    Text(
                        text = if (language == com.scf.secondbloom.domain.model.AppLanguage.ENGLISH) {
                            "${index + 1}. ${step.title}: ${step.detail}"
                        } else {
                            "${index + 1}. ${step.title}：${step.detail}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            PreviewSection(preview = preview)
            if (plan.steps.size > 2) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) localized(language, "Hide steps", "收起步骤") else localized(language, "Show all steps", "展开全部步骤"))
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(preview: PlanPreviewResult?) {
    val language = LocalAppLanguage.current
    if (preview == null) {
        Text(
            text = localized(language, "The final image will appear on a dedicated result page.", "最终效果图会在单独页面中展示。"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    when (preview.renderStatus) {
        PreviewRenderStatus.COMPLETED -> {
            Text(
                text = localized(language, "The final image is ready. Open the dedicated page to compare before and after.", "最终效果图已生成，可进入单独页面查看 Before / After / Compare。"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        PreviewRenderStatus.RUNNING,
        PreviewRenderStatus.QUEUED -> {
            Text(
                text = localized(language, "The final image is still generating.", "最终效果图仍在生成中。"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PreviewRenderStatus.FILTERED,
        PreviewRenderStatus.FAILED -> {
            Text(
                text = preview.errorMessage ?: "该方案的效果图暂时不可用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CompactWorkbenchBanner(
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null,
    extra: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            extra?.invoke()
            action?.invoke()
        }
    }
}

@Composable
private fun CompactWorkbenchCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            content()
        }
    }
}

@Composable
private fun CompactWorkbenchEyebrow(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CompactWorkbenchTag(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
