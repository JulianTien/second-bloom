package com.scf.secondbloom.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scf.secondbloom.domain.model.BackgroundComplexity
import com.scf.secondbloom.domain.model.DemoScenario
import com.scf.secondbloom.domain.model.ProcessingWarningCode
import com.scf.secondbloom.domain.model.RemodelStage
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.domain.model.SelectedImage
import com.scf.secondbloom.ui.components.SecondBloomWorkflowStep
import com.scf.secondbloom.ui.components.SecondBloomWorkflowStrip
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.i18n.localizedExpectedOutcome
import com.scf.secondbloom.ui.i18n.localizedTitle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: RemodelUiState,
    onImageSelected: (SelectedImage) -> Unit,
    onLoadDemoScenario: (DemoScenario) -> Unit,
    onAnalyze: () -> Unit,
    onContinueLowConfidence: () -> Unit,
    onDismissError: () -> Unit,
    onOpenWorkbench: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            onImageSelected(context.toSelectedImage(selectedUri))
        }
    }
    val selectedScenario = state.selectedDemoScenario
    val selectedImage = state.selectedImage

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = localized(language, "Upload and analyze screen", "上传识别页面")
            },
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader()
        }

        item {
            SecondBloomWorkflowStrip(steps = homeWorkflowSteps(state, language))
        }

        item {
            PrimaryUploadCard(
                state = state,
                selectedImage = selectedImage,
                selectedScenario = selectedScenario,
                onPickImage = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onAnalyze = onAnalyze
            )
        }

        if (state.stage == RemodelStage.Analyzing) {
            item {
                CompactStatusBanner(
                    title = localized(language, "Analyzing", "正在识别"),
                    message = localized(language, "Analyzing color, material, and defects.", "正在分析颜色、材质和瑕疵，请稍候。"),
                    showProgress = true
                )
            }
        }

        if (state.showLowConfidenceWarning) {
            item {
                LowConfidenceDecisionCard(
                    state = state,
                    onRepick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onContinue = onContinueLowConfidence
                )
            }
        }

        if (state.error != null) {
            item {
                CompactStatusBanner(
                    title = localized(language, "Recognition failed", "识别失败"),
                    message = state.error.message,
                    showProgress = false,
                    accentError = true,
                    action = {
                        TextButton(onClick = onDismissError) {
                            Text(localized(language, "Dismiss", "关闭"))
                        }
                    }
                )
            }
        }

        if (state.draftAnalysis != null) {
            item {
                RecognitionSummaryCard(
                    state = state,
                    onOpenWorkbench = onOpenWorkbench
                )
            }
        }

        item {
            DemoScenarioSection(
                selectedScenario = selectedScenario,
                onScenarioSelected = onLoadDemoScenario
            )
        }
    }
}

private fun homeWorkflowSteps(
    state: RemodelUiState,
    language: com.scf.secondbloom.domain.model.AppLanguage
): List<SecondBloomWorkflowStep> {
    val hasImage = state.selectedImage != null
    val hasAnalysis = state.draftAnalysis != null

    return listOf(
        SecondBloomWorkflowStep(
            number = 1,
            title = localized(language, "Upload", "上传"),
            description = localized(language, "Choose photo", "选择照片"),
            isCurrent = !hasImage,
            isComplete = hasImage
        ),
        SecondBloomWorkflowStep(
            number = 2,
            title = localized(language, "Review", "确认"),
            description = localized(language, "Check analysis", "确认识别结果"),
            isCurrent = hasImage && !hasAnalysis || state.stage == RemodelStage.Analyzing,
            isComplete = hasAnalysis
        ),
        SecondBloomWorkflowStep(
            number = 3,
            title = localized(language, "Plans", "方案"),
            description = localized(language, "Open plan page", "进入改造方案页"),
            isCurrent = false,
            isComplete = false
        )
    )
}

@Composable
private fun HomeHeader() {
    val language = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactEyebrow(text = "Second Bloom")
        Text(
            text = localized(language, "Upload garment", "上传旧衣"),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = localized(language, "Take a clear photo, confirm the analysis, then move to plans.", "拍一张清晰照片，先确认识别，再进入方案。"),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PrimaryUploadCard(
    state: RemodelUiState,
    selectedImage: SelectedImage?,
    selectedScenario: DemoScenario?,
    onPickImage: () -> Unit,
    onAnalyze: () -> Unit
) {
    val language = LocalAppLanguage.current
    val primaryLabel = when {
        state.stage == RemodelStage.Analyzing -> localized(language, "Analyzing", "正在识别")
        selectedImage == null -> localized(language, "Choose photo", "选择照片")
        else -> localized(language, "Start analysis", "开始识别")
    }

    Card(
        modifier = Modifier.semantics { contentDescription = localized(language, "Primary upload card", "主上传卡") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedImage == null) {
                            localized(language, "Single image upload", "单图上传")
                        } else {
                            localized(language, "Image ready", "素材已就位")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            selectedScenario != null -> localized(language, "Demo asset: ${selectedScenario.localizedTitle(language)}", "示例素材：${selectedScenario.localizedTitle(language)}")
                            selectedImage != null -> "${selectedImage.fileName} · ${formatFileSize(selectedImage.sizeBytes)}"
                            else -> localized(language, "Best results come from a full garment shot with a clean background.", "建议使用主体完整、背景干净的衣物照片")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (selectedImage == null) {
                    localized(language, "After recognition, you will get an editable garment summary.", "识别完成后会返回可编辑的衣物摘要。")
                } else {
                    localized(language, "Next, we will identify color, material, style, and defects.", "下一步会识别颜色、材质、风格和瑕疵。")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Button(
                onClick = if (selectedImage == null) onPickImage else onAnalyze,
                enabled = selectedImage == null || state.canAnalyze,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(primaryLabel)
            }

            if (selectedImage != null) {
                TextButton(
                    onClick = onPickImage,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(localized(language, "Replace photo", "更换照片"))
                }
            }
        }
    }
}

@Composable
private fun CompactStatusBanner(
    title: String,
    message: String,
    showProgress: Boolean,
    accentError: Boolean = false,
    action: (@Composable () -> Unit)? = null
) {
    val containerColor = if (accentError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (accentError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (accentError) Icons.Outlined.WarningAmber else Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = if (accentError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (accentError) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            action?.invoke()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LowConfidenceDecisionCard(
    state: RemodelUiState,
    onRepick: () -> Unit,
    onContinue: () -> Unit
) {
    val language = LocalAppLanguage.current
    val signals = buildLowConfidenceSignals(state)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "This photo needs one more review", "这张照片需要再确认一次"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = signals.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onRepick) {
                    Text(localized(language, "Retake photo", "重拍照片"))
                }
                Button(onClick = onContinue) {
                    Text(localized(language, "Continue", "继续确认"))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecognitionSummaryCard(
    state: RemodelUiState,
    onOpenWorkbench: () -> Unit
) {
    val language = LocalAppLanguage.current
    val analysis = state.draftAnalysis ?: return

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localized(language, "Recognition summary is ready", "识别结果已准备好"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                    CompactTag(label = label)
                }
            }
            Text(
                text = if (language == com.scf.secondbloom.domain.model.AppLanguage.ENGLISH) {
                    "Confidence ${(analysis.confidence * 100).toInt()}% · Open the plan page to continue."
                } else {
                    "置信度 ${(analysis.confidence * 100).toInt()}% · 下一步进入方案页继续确认"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = localized(language, "This analysis has already been saved to your profile.", "识别结果已自动保存到我的主页。"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = onOpenWorkbench,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localized(language, "View plans", "去看方案"))
            }
        }
    }
}

@Composable
private fun DemoScenarioSection(
    selectedScenario: DemoScenario?,
    onScenarioSelected: (DemoScenario) -> Unit
) {
    val language = LocalAppLanguage.current
    Card(
        shape = RoundedCornerShape(22.dp),
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
                text = localized(language, "Demo assets", "示例素材"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            DemoScenario.entries.forEach { scenario ->
                val selected = selectedScenario == scenario
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${scenario.order}. ${scenario.localizedTitle(language)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = scenario.localizedExpectedOutcome(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TextButton(onClick = { onScenarioSelected(scenario) }) {
                        Text(if (selected) localized(language, "Loaded", "已载入") else localized(language, "Use", "使用"))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactEyebrow(text: String) {
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
private fun CompactTag(label: String) {
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

private fun buildLowConfidenceSignals(state: RemodelUiState): List<String> {
    val analysis = state.draftAnalysis ?: return listOf("建议人工确认")
    return buildList {
        if (analysis.backgroundComplexity == BackgroundComplexity.HIGH) {
            add("背景较复杂")
        }
        if (analysis.confidence < 0.75f) {
            add("置信度偏低")
        }
        analysis.warnings.forEach { warning ->
            when (warning.code) {
                ProcessingWarningCode.MULTIPLE_GARMENTS -> add("可能有多件衣物")
                ProcessingWarningCode.BLURRY_IMAGE -> add("图片偏模糊")
                ProcessingWarningCode.UNKNOWN_OBJECT -> add("主体不够明确")
                else -> Unit
            }
        }
        if (isEmpty()) {
            add("建议人工确认")
        }
    }
}

private fun Context.toSelectedImage(uri: Uri): SelectedImage {
    var displayName = uri.lastPathSegment ?: "selected-image"
    var fileSize: Long? = null

    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex >= 0) {
                displayName = cursor.getString(nameIndex) ?: displayName
            }
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
    }

    return SelectedImage(
        uri = uri.toString(),
        fileName = displayName,
        mimeType = contentResolver.getType(uri) ?: "image/*",
        sizeBytes = fileSize
    )
}

private fun formatFileSize(sizeBytes: Long?): String {
    if (sizeBytes == null) return "大小未知"
    val sizeInKb = sizeBytes / 1024f
    return if (sizeInKb < 1024) {
        String.format("%.0f KB", sizeInKb)
    } else {
        String.format("%.1f MB", sizeInKb / 1024f)
    }
}
