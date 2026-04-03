package com.scf.loop.data.remote.mock

import com.scf.loop.data.remote.InvalidImageException
import com.scf.loop.data.remote.ModelResponseException
import com.scf.loop.data.remote.RemodelApi
import com.scf.loop.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.loop.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.loop.data.remote.dto.GarmentAnalysisDto
import com.scf.loop.data.remote.dto.GarmentDefectDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansResponseDto
import com.scf.loop.data.remote.dto.ProcessingWarningDto
import com.scf.loop.data.remote.dto.RemodelPlanDto
import com.scf.loop.data.remote.dto.RemodelStepDto
import java.io.IOException
import kotlinx.coroutines.delay

class MockRemodelApi : RemodelApi {

    override suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto {
        delay(900)

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
            "jean" in normalizedName -> "牛仔外套"
            "hoodie" in normalizedName -> "卫衣"
            "dress" in normalizedName -> "连衣裙"
            else -> "白色衬衫"
        }

        val warnings = buildList {
            if (lowConfidence) {
                add(
                    ProcessingWarningDto(
                        code = "complex_background",
                        message = "检测到背景较复杂，建议重拍或确认后继续。"
                    )
                )
                add(
                    ProcessingWarningDto(
                        code = "low_confidence",
                        message = "识别置信度较低，部分属性可能需要你手动修正。"
                    )
                )
            }
        }

        return AnalyzeGarmentResponseDto(
            analysis = GarmentAnalysisDto(
                analysisId = "analysis-${request.fileName.hashCode().toUInt()}",
                garmentType = garmentType,
                color = if ("black" in normalizedName) "黑色" else "白色",
                material = if ("jean" in normalizedName) "牛仔布" else "棉质",
                style = if ("oversize" in normalizedName) "宽松休闲" else "简约基础",
                defects = listOf(
                    GarmentDefectDto(name = "袖口轻微磨损", severity = "medium"),
                    GarmentDefectDto(name = "领口略有褶皱", severity = "low")
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

        val garmentType = request.confirmedAnalysis.garmentType.ifBlank {
            throw ModelResponseException("缺少衣物类型，无法生成改制方案。")
        }

        val intentLabel = when (request.intent.lowercase()) {
            "daily" -> "日常焕新"
            "occasion" -> "场合升级"
            "diy" -> "创意 DIY"
            "size_adjustment" -> "尺码调整"
            else -> "旧衣改造"
        }

        val styleHint = request.confirmedAnalysis.style
        val preferenceHint = request.userPreferences?.takeIf { it.isNotBlank() }

        val plans = listOf(
            RemodelPlanDto(
                title = "$intentLabel 方案一",
                summary = buildString {
                    append("围绕${garmentType}做轻改造，保留原有轮廓并强化${styleHint}风格。")
                    preferenceHint?.let { append(" 同时参考你的偏好：$it。") }
                },
                difficulty = "easy",
                materials = listOf("布用剪刀", "定位针", "同色线"),
                estimatedTime = "1-2 小时",
                steps = listOf(
                    RemodelStepDto("整理衣片", "先熨平并标记需要保留与调整的区域。"),
                    RemodelStepDto("局部改造", "根据目标风格修改袖口、下摆或领口细节。"),
                    RemodelStepDto("收尾检查", "检查走线与版型平衡，确认穿着舒适度。")
                )
            ),
            RemodelPlanDto(
                title = "$intentLabel 方案二",
                summary = "将${garmentType}改造成更鲜明的层次款式，强调材质与色彩的再利用。",
                difficulty = "medium",
                materials = listOf("装饰织带", "缝纫机", "辅料扣件"),
                estimatedTime = "2-3 小时",
                steps = listOf(
                    RemodelStepDto("结构规划", "先决定主要变化点，例如短款化、拼接或束腰。"),
                    RemodelStepDto("辅料搭配", "加入小面积辅料以增强成衣完成度。"),
                    RemodelStepDto("试穿微调", "试穿后修正松量和线条，避免局部过紧。")
                )
            )
        )

        return GenerateRemodelPlansResponseDto(
            plans = plans,
            reasoningNote = "Mock provider generated ${plans.size} plans."
        )
    }
}
