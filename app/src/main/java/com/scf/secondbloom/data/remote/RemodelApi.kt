package com.scf.secondbloom.data.remote

import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentRequestDto
import com.scf.secondbloom.data.remote.dto.AnalyzeGarmentResponseDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPreviewJobsResponseDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansRequestDto
import com.scf.secondbloom.data.remote.dto.GenerateRemodelPlansResponseDto
import com.scf.secondbloom.data.remote.dto.RemodelPreviewJobDto

interface RemodelApi {
    suspend fun analyzeGarment(request: AnalyzeGarmentRequestDto): AnalyzeGarmentResponseDto

    suspend fun generatePlans(request: GenerateRemodelPlansRequestDto): GenerateRemodelPlansResponseDto

    suspend fun createPreviewJob(
        request: GenerateRemodelPreviewJobsRequestDto
    ): GenerateRemodelPreviewJobsResponseDto

    suspend fun getPreviewJob(previewJobId: String): RemodelPreviewJobDto
}
