package com.scf.secondbloom.data.remote.mock

import com.scf.secondbloom.data.remote.InvalidImageException
import com.scf.secondbloom.data.remote.ModelResponseException
import com.scf.secondbloom.data.remote.RemodelApi
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.secondbloom.data.remote.dto.GarmentAnalysisDto
import com.scf.secondbloom.data.remote.dto.GarmentDefectDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsResponseDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansResponseDto
import com.scf.secondbloom.data.remote.dto.PreviewAssetDto
import com.scf.secondbloom.data.remote.dto.PreviewPlanRenderResultDto
import com.scf.secondbloom.data.remote.dto.ProcessingWarningDto
import com.scf.secondbloom.data.remote.dto.RemodelPreviewJobDto
import com.scf.secondbloom.data.remote.dto.RemodelPlanDto
import com.scf.secondbloom.data.remote.dto.RemodelStepDto
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.delay

class MockRemodelApi : RemodelApi {
    private val previewJobs = mutableMapOf<String, MockPreviewJobState>()

    override suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto {
        delay(900)
        val useChinese = request.responseLanguage.equals("zh", ignoreCase = true)

        if (!request.mimeType.startsWith("image/")) {
            throw InvalidImageException("当前文件不是可识别的图片，请重新选择衣物照片。")
        }

        val normalizedName = request.fileName.lowercase()
        if ("network" in normalizedName) {
            throw IOException("网络连接失败，请稍后重试。")
        }
        if ("model" in normalizedName) {
            throw ModelResponseException("模型返回了无效的识别 JSON。")
        }

        val lowConfidence = listOf("messy", "clutter", "blur", "dark").any { it in normalizedName }
        val garmentType = when {
            "jean" in normalizedName -> if (useChinese) "牛仔外套" else "Denim jacket"
            "hoodie" in normalizedName -> if (useChinese) "卫衣" else "Hoodie"
            "dress" in normalizedName -> if (useChinese) "连衣裙" else "Dress"
            else -> if (useChinese) "白色衬衫" else "White shirt"
        }

        val warnings = buildList {
            if (lowConfidence) {
                add(
                    ProcessingWarningDto(
                        code = "complex_background",
                        message = if (useChinese) {
                            "检测到背景较复杂，建议重拍或确认后继续。"
                        } else {
                            "The background looks complex. Consider retaking the photo or review carefully."
                        }
                    )
                )
                add(
                    ProcessingWarningDto(
                        code = "low_confidence",
                        message = if (useChinese) {
                            "识别置信度较低，部分属性可能需要你手动修正。"
                        } else {
                            "Recognition confidence is low, so you may want to adjust some fields manually."
                        }
                    )
                )
            }
        }

        return AnalyzeGarmentResponseDto(
            analysis = GarmentAnalysisDto(
                analysisId = "analysis-${request.fileName.hashCode().toUInt()}",
                garmentType = garmentType,
                color = if ("black" in normalizedName) {
                    if (useChinese) "黑色" else "Black"
                } else {
                    if (useChinese) "白色" else "White"
                },
                material = if ("jean" in normalizedName) {
                    if (useChinese) "牛仔布" else "Denim"
                } else {
                    if (useChinese) "棉质" else "Cotton"
                },
                style = if ("oversize" in normalizedName) {
                    if (useChinese) "宽松休闲" else "Relaxed casual"
                } else {
                    if (useChinese) "简约基础" else "Minimal basic"
                },
                defects = listOf(
                    GarmentDefectDto(
                        name = if (useChinese) "袖口轻微磨损" else "Light cuff wear",
                        severity = "medium"
                    ),
                    GarmentDefectDto(
                        name = if (useChinese) "领口略有褶皱" else "Slight neckline wrinkling",
                        severity = "low"
                    )
                ),
                backgroundComplexity = if (lowConfidence) "high" else "low",
                confidence = if (lowConfidence) 0.63f else 0.92f,
                warnings = warnings
            )
        )
    }

    override suspend fun generatePlans(
        request: GenerateRemodelPlansRequestDto
    ): GenerateRemodelPlansResponseDto {
        delay(1100)
        val useChinese = request.responseLanguage.equals("zh", ignoreCase = true)

        val garmentType = request.confirmedAnalysis.garmentType.ifBlank {
            throw ModelResponseException("缺少衣物类型，无法生成改制方案。")
        }

        val intentLabel = when (request.intent.lowercase()) {
            "daily" -> if (useChinese) "日常焕新" else "Daily Refresh"
            "occasion" -> if (useChinese) "场合升级" else "Occasion Upgrade"
            "diy" -> if (useChinese) "创意 DIY" else "Creative DIY"
            "size_adjustment" -> if (useChinese) "尺码调整" else "Size Adjustment"
            else -> if (useChinese) "旧衣改造" else "Remodel"
        }

        val styleHint = request.confirmedAnalysis.style
        val preferenceHint = request.userPreferences?.takeIf { it.isNotBlank() }

        val plans = listOf(
            RemodelPlanDto(
                planId = "plan-daily-primary",
                title = if (useChinese) "$intentLabel 方案一" else "$intentLabel Plan 1",
                summary = buildString {
                    if (useChinese) {
                        append("围绕${garmentType}做轻改造，保留原有轮廓并强化${styleHint}风格。")
                        preferenceHint?.let { append(" 同时参考你的偏好：$it。") }
                    } else {
                        append("A light remodel for the $garmentType that keeps the main silhouette while sharpening the $styleHint direction.")
                        preferenceHint?.let { append(" Preference note: $it.") }
                    }
                },
                difficulty = "easy",
                materials = if (useChinese) {
                    listOf("布用剪刀", "定位针", "同色线")
                } else {
                    listOf("Fabric scissors", "Pins", "Matching thread")
                },
                estimatedTime = if (useChinese) "1-2 小时" else "1-2 hours",
                steps = listOf(
                    if (useChinese) {
                        RemodelStepDto("整理衣片", "先熨平并标记需要保留与调整的区域。")
                    } else {
                        RemodelStepDto("Prep the garment", "Press the piece flat and mark the areas you want to keep or adjust.")
                    },
                    if (useChinese) {
                        RemodelStepDto("局部改造", "根据目标风格修改袖口、下摆或领口细节。")
                    } else {
                        RemodelStepDto("Refine key details", "Adjust cuffs, hem, or neckline details to match the target direction.")
                    },
                    if (useChinese) {
                        RemodelStepDto("收尾检查", "检查走线与版型平衡，确认穿着舒适度。")
                    } else {
                        RemodelStepDto("Finish and review", "Check stitch balance and fit to keep the piece comfortable to wear.")
                    }
                )
            ),
            RemodelPlanDto(
                planId = "plan-daily-secondary",
                title = if (useChinese) "$intentLabel 方案二" else "$intentLabel Plan 2",
                summary = if (useChinese) {
                    "将${garmentType}改造成更鲜明的层次款式，强调材质与色彩的再利用。"
                } else {
                    "Turn the $garmentType into a more layered look that highlights material and color reuse."
                },
                difficulty = "medium",
                materials = if (useChinese) {
                    listOf("装饰织带", "缝纫机", "辅料扣件")
                } else {
                    listOf("Decorative tape", "Sewing machine", "Closures")
                },
                estimatedTime = if (useChinese) "2-3 小时" else "2-3 hours",
                steps = listOf(
                    if (useChinese) {
                        RemodelStepDto("结构规划", "先决定主要变化点，例如短款化、拼接或束腰。")
                    } else {
                        RemodelStepDto("Plan the structure", "Decide the main changes first, such as cropping, paneling, or waist shaping.")
                    },
                    if (useChinese) {
                        RemodelStepDto("辅料搭配", "加入小面积辅料以增强成衣完成度。")
                    } else {
                        RemodelStepDto("Add supporting trims", "Use small amounts of trims or closures to make the result feel finished.")
                    },
                    if (useChinese) {
                        RemodelStepDto("试穿微调", "试穿后修正松量和线条，避免局部过紧。")
                    } else {
                        RemodelStepDto("Fit and refine", "Try it on and refine shape lines or ease so nothing feels too tight.")
                    }
                )
            ),
            RemodelPlanDto(
                planId = "plan-daily-tertiary",
                title = if (useChinese) "$intentLabel 方案三" else "$intentLabel Plan 3",
                summary = if (useChinese) {
                    "保留主体结构，通过口袋、下摆或领口细节增强日常功能性。"
                } else {
                    "Keep the main structure and improve everyday function through pockets, hem work, or neckline details."
                },
                difficulty = "easy",
                materials = if (useChinese) {
                    listOf("口袋布", "珠针", "同色线")
                } else {
                    listOf("Pocket fabric", "Pins", "Matching thread")
                },
                estimatedTime = if (useChinese) "1-2 小时" else "1-2 hours",
                steps = listOf(
                    if (useChinese) {
                        RemodelStepDto("选择重点区域", "优先挑选最适合增强功能感的位置。")
                    } else {
                        RemodelStepDto("Choose the focus area", "Start with the place that will gain the most useful function.")
                    },
                    if (useChinese) {
                        RemodelStepDto("完成细节改造", "让改动保持克制，适合长期反复穿着。")
                    } else {
                        RemodelStepDto("Finish restrained edits", "Keep the changes subtle so the piece stays easy to wear often.")
                    }
                )
            )
        )

        return GenerateRemodelPlansResponseDto(
            plans = plans,
            reasoningNote = if (useChinese) {
                "Mock provider generated ${plans.size} plans."
            } else {
                "Mock provider generated ${plans.size} plans."
            }
        )
    }

    override suspend fun createPreviewJob(
        request: GenerateRemodelPreviewJobsRequestDto
    ): GenerateRemodelPreviewJobsResponseDto {
        delay(250)
        val previewJobId = "preview-job-${UUID.randomUUID()}"
        previewJobs[previewJobId] = MockPreviewJobState(
            previewJobId = previewJobId,
            analysisId = request.analysisId,
            planIds = listOf(request.planId)
        )
        return GenerateRemodelPreviewJobsResponseDto(
            previewJobId = previewJobId,
            status = "queued",
            requestedPlanCount = 1,
            pollPath = "/remodel-preview-jobs/$previewJobId"
        )
    }

    override suspend fun getPreviewJob(previewJobId: String): RemodelPreviewJobDto {
        delay(350)
        val job = previewJobs[previewJobId] ?: throw IOException("预览任务不存在，请重新生成。")
        job.pollCount += 1
        val status = if (job.pollCount == 1) "running" else "completed"
        val completedCount = if (status == "completed") job.planIds.size else 0
        return RemodelPreviewJobDto(
            previewJobId = previewJobId,
            analysisId = job.analysisId,
            renderMode = "simulation",
            status = status,
            requestedPlanCount = job.planIds.size,
            completedPlanCount = completedCount,
            failedPlanCount = 0,
            pollPath = "/remodel-preview-jobs/$previewJobId",
            results = job.planIds.map { planId ->
                val completed = status == "completed"
                PreviewPlanRenderResultDto(
                    planId = planId,
                    renderStatus = if (completed) "completed" else "running",
                    beforeImage = if (completed) placeholderAsset("$planId-before", "Before") else null,
                    afterImage = if (completed) placeholderAsset("$planId-after", "After") else null,
                    comparisonImage = if (completed) placeholderAsset("$planId-compare", "Compare") else null,
                    disclaimer = "AI visual simulation only. Final garment may differ."
                )
            }
        )
    }

    private fun placeholderAsset(seed: String, label: String): PreviewAssetDto = PreviewAssetDto(
        assetId = "asset-$seed",
        url = "https://placehold.co/640x420/png?text=$label",
        expiresAt = "2099-12-31T00:00:00Z"
    )
}

private data class MockPreviewJobState(
    val previewJobId: String,
    val analysisId: String,
    val planIds: List<String>,
    var pollCount: Int = 0
)
