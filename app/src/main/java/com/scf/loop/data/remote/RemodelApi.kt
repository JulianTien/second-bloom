package com.scf.loop.data.remote

import com.scf.loop.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.loop.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.loop.data.remote.dto.GenerateRemodelPlansResponseDto

interface RemodelApi {
    suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto

    suspend fun generatePlans(request: GenerateRemodelPlansRequestDto): GenerateRemodelPlansResponseDto
}
