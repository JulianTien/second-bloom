package com.scf.loop.data.repository

import com.scf.loop.domain.model.GarmentAnalysis
import com.scf.loop.domain.model.RemodelIntent
import com.scf.loop.domain.model.RemodelPlan
import com.scf.loop.domain.model.SelectedImage

interface RemodelRepository {
    suspend fun analyze(image: SelectedImage): GarmentAnalysis

    suspend fun generatePlans(
        intent: RemodelIntent,
        confirmedAnalysis: GarmentAnalysis,
        userPreferences: String
    ): List<RemodelPlan>
}
