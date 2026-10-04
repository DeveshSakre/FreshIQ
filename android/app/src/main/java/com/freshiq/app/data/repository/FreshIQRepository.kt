package com.freshiq.app.data.repository

import com.freshiq.app.data.model.HealthResponseDto
import com.freshiq.app.data.model.PredictionResponseDto
import java.io.File

interface FreshIQRepository {
    suspend fun getHealth(): Result<HealthResponseDto>
    suspend fun predictProduce(
        imageFile: File,
        storageCondition: String?,
        foodType: String = "avocado"
    ): Result<PredictionResponseDto>
}
