package com.freshiq.app.data.api

import com.freshiq.app.data.model.HealthResponseDto
import com.freshiq.app.data.model.PredictionResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FreshIQApiService {

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponseDto>

    @Multipart
    @POST("api/predict")
    suspend fun predictProduce(
        @Part file: MultipartBody.Part,
        @Part("food_type") foodType: RequestBody? = null,
        @Part("storage_condition") storageCondition: RequestBody? = null
    ): Response<PredictionResponseDto>
}
