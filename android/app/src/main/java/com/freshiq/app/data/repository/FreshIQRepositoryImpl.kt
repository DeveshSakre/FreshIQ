package com.freshiq.app.data.repository

import com.freshiq.app.data.api.FreshIQApiService
import com.freshiq.app.data.model.HealthResponseDto
import com.freshiq.app.data.model.PredictionResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreshIQRepositoryImpl @Inject constructor(
    private val apiService: FreshIQApiService
) : FreshIQRepository {

    override suspend fun getHealth(): Result<HealthResponseDto> {
        return try {
            val response = apiService.getHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun predictProduce(
        imageFile: File,
        storageCondition: String?,
        foodType: String
    ): Result<PredictionResponseDto> {
        return try {
            val mediaType = when (imageFile.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()

            val requestFile = imageFile.asRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val foodTypePart = foodType.toRequestBody("text/plain".toMediaTypeOrNull())
            val conditionPart = storageCondition?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.predictProduce(filePart, foodTypePart, conditionPart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Prediction failed"
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
