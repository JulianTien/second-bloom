package com.scf.secondbloom.data.repository

import com.scf.secondbloom.data.remote.RemodelApi
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.secondbloom.data.remote.dto.toDomain
import com.scf.secondbloom.data.remote.dto.toAnalyzeRequestDto
import com.scf.secondbloom.data.remote.dto.toDto
import com.scf.secondbloom.data.remote.dto.toRequestString
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GeneratePreviewJobResult
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewJobSnapshot
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.SelectedImage

class DefaultRemodelRepository(
    private val api: RemodelApi
) : RemodelRepository {

    override suspend fun analyze(
        image: SelectedImage,
        responseLanguage: AppLanguage
    ): GarmentAnalysis =
        api.analyzeGarment(image.toAnalyzeRequestDto(responseLanguage)).analysis.toDomain()

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan> =
        api.generatePlans(
            GenerateRemodelPlansRequestDto(
                intent = intent.toRequestString(),
                confirmedAnalysis = confirmedAnalysis.toDto(),
                userPreferences = userPreferences.ifBlank { null },
                responseLanguage = responseLanguage.wireValue
            )
        ).plans.map { it.toDomain() }

    override suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: PreviewEditOptions?
    ): GeneratePreviewJobResult =
        api.createPreviewJob(
            GenerateRemodelPreviewJobsRequestDto(
                analysisId = analysisId,
                planId = planId,
                tuning = editOptions?.toDto()
            )
        ).toDomain()

    override suspend fun getPreviewJob(previewJobId: String): PreviewJobSnapshot =
        api.getPreviewJob(previewJobId).toDomain()
}
