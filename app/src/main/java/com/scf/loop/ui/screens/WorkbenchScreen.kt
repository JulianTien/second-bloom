package com.scf.loop.ui.screens

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
import com.scf.loop.domain.model.BackgroundComplexity
import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.RemodelStage
import com.scf.loop.domain.model.RemodelUiState
import com.scf.loop.ui.components.LoopWorkflowStep
import com.scf.loop.ui.components.LoopWorkflowStrip

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
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "制衣方案页面" },
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WorkbenchHeader()
        }

        item {
            LoopWorkflowStrip(steps = workbenchWorkflowSteps(state))
        }

        val draftAnalysis = state.draftAnalysis
        if (draftAnalysis == null) {
            item {
                CompactWorkbenchCard(
                    title = "还没有识别结果",
                    description = "先从 AI 改制入口完成识别，再回来生成方案。"
                ) {
                    Text(
                        text = "这里会在拿到识别摘要后显示目标选择和方案结果。",
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
                    title = "方案生成失败",
                    message = state.error.message,
                    action = {
                        TextButton(onClick = onDismissError) {
                            Text("关闭")
                        }
                    }
                )
            }
        }

        if (state.stage == RemodelStage.LowConfidence) {
            item {
                CompactWorkbenchBanner(
                    title = "这份结果来自低置信度识别",
                    message = "建议先核对衣物类型、颜色和瑕疵，再决定是否生成方案。"
                )
            }
        }

        item {
            SummarySection(
                analysis = draftAnalysis,
                showEditor = showEditor,
                onToggleEditor = { showEditor = !showEditor }
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
                    title = "正在生成方案",
                    message = "正在整理难度、材料和步骤建议。"
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (state.plans.isEmpty()) {
            item {
                CompactWorkbenchCard(
                    title = "还没有方案",
                    description = "选择改制目标后，这里会出现紧凑的方案卡片。"
                ) {
                    Text(
                        text = "优先展示标题、难度、耗时和关键步骤。",
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
                    text = "方案结果",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                Text(
                    text = "方案已自动保存到我的主页。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(state.plans) { plan ->
                PlanCard(plan = plan)
            }
        }
    }
}

private fun workbenchWorkflowSteps(state: RemodelUiState): List<LoopWorkflowStep> {
    val hasAnalysis = state.draftAnalysis != null
    val hasPlans = state.plans.isNotEmpty()
    val isGenerating = state.stage == RemodelStage.GeneratingPlans

    return listOf(
        LoopWorkflowStep(
            number = 1,
            title = "摘要",
            description = "查看识别摘要",
            isCurrent = hasAnalysis && !isGenerating && !hasPlans,
            isComplete = hasAnalysis
        ),
        LoopWorkflowStep(
            number = 2,
            title = "目标",
            description = "选择改制目标",
            isCurrent = hasAnalysis && !isGenerating && !hasPlans,
            isComplete = state.selectedIntent != null
        ),
        LoopWorkflowStep(
            number = 3,
            title = "方案",
            description = "查看方案",
            isCurrent = isGenerating || hasPlans,
            isComplete = hasPlans
        )
    )
}

@Composable
private fun WorkbenchHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactWorkbenchEyebrow(text = "制衣方案")
        Text(
            text = "把识别结果变成方案。",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "先确认摘要，再选择目标，最后查看结果。",
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
    onToggleEditor: () -> Unit
) {
    CompactWorkbenchCard(
        title = "识别摘要",
        description = "先看关键字段，只有需要时再展开编辑。"
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
            text = "置信度 ${(analysis.confidence * 100).toInt()}% · 背景${if (analysis.backgroundComplexity == BackgroundComplexity.HIGH) "较复杂" else "较干净"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (analysis.defects.isNotEmpty()) {
            Text(
                text = "瑕疵：${analysis.defects.joinToString("、") { it.name }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onToggleEditor) {
            Text(if (showEditor) "收起编辑" else "展开编辑")
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
    CompactWorkbenchCard(
        title = "编辑字段",
        description = "只调整你觉得不准确的部分。"
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
                text = "建议优先核对衣物类型、颜色和瑕疵。",
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
            label = { Text("衣物类型") }
        )
        OutlinedTextField(
            value = analysis.color,
            onValueChange = onColorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("颜色") }
        )
        OutlinedTextField(
            value = analysis.material,
            onValueChange = onMaterialChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("材质") }
        )
        OutlinedTextField(
            value = analysis.style,
            onValueChange = onStyleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("风格") }
        )
        OutlinedTextField(
            value = analysis.defects.joinToString("，") { it.name },
            onValueChange = onDefectsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("瑕疵描述") },
            supportingText = { Text("多个瑕疵可使用中文逗号分隔") }
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
    CompactWorkbenchCard(
        title = "改制目标",
        description = "选一个方向，再补充一句偏好。"
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
                            text = intent.label,
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
            label = { Text("补充偏好") },
            supportingText = { Text("例如：尽量少改动版型、保留正式感") }
        )
        Button(
            onClick = onGeneratePlans,
            enabled = state.canGeneratePlans,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成改制方案")
        }
    }
}

@Composable
private fun PlanCard(plan: RemodelPlan) {
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
            Text(
                text = plan.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactWorkbenchTag(label = plan.difficulty.label)
                CompactWorkbenchTag(label = plan.estimatedTime)
            }
            Text(
                text = "材料：${plan.materials.joinToString("、")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                visibleSteps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. ${step.title}：${step.detail}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (plan.steps.size > 2) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起步骤" else "展开全部步骤")
                }
            }
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
