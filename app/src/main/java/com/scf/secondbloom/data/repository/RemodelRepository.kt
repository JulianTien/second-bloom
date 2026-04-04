package com.scf.secondbloom.data.repository

import com.scf.secondbloom.domain.model.GarmentAnalysis
import com.scf.secondbloom.domain.model.GeneratePreviewJobResult
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.PreviewEditOptions
import com.scf.secondbloom.domain.model.PreviewJobSnapshot
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.domain.model.RemodelPlan
import com.scf.secondbloom.domain.model.SelectedImage

interface RemodelRepository {
    suspend fun analyze(image: SelectedImage, responseLanguage: AppLanguage): GarmentAnalysis

    suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String,
        responseLanguage: AppLanguage
    ): List<RemodelPlan>

    suspend fun createPreviewJob(
        analysisId: String,
        planId: String,
        editOptions: PreviewEditOptions? = null
    ): GeneratePreviewJobResult

    suspend fun getPreviewJob(previewJobId: String): PreviewJobSnapshot
}
