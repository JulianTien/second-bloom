package com.scf.loop.data.repository

import com.scf.loop.data.remote.RemodelApi
import com.scf.loop.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.loop.data.remote.dto.toAnalyzeRequestDto
import com.scf.loop.data.remote.dto.toDomain
import com.scf.loop.data.remote.dto.toDto
import com.scf.loop.data.remote.dto.toRequestString
import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.SelectedImage

class DefaultRemodelRepository(
    private val api: RemodelApi
) : RemodelRepository {

    override suspend fun analyze(image: SelectedImage): GarmentAnalysis =
        api.analyzeGarment(image.toAnalyzeRequestDto()).analysis.toDomain()

    override suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String
    ): List<RemodelPlan> =
        api.generatePlans(
            GenerateRemodelPlansRequestDto(
                intent = intent.toRequestString(),
                confirmedAnalysis = confirmedAnalysis.toDto(),
                userPreferences = userPreferences.ifBlank { null }
            )
        ).plans.map { it.toDomain() }
}
