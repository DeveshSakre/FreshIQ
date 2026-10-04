package com.freshiq.app.data.repository

import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.domain.model.ProduceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanSessionManager @Inject constructor() {

    private val _activePrediction = MutableStateFlow<PredictionResponseDto?>(null)
    val activePrediction: StateFlow<PredictionResponseDto?> = _activePrediction.asStateFlow()

    private val _activeImageUri = MutableStateFlow<String?>(null)
    val activeImageUri: StateFlow<String?> = _activeImageUri.asStateFlow()

    private val _activeStorageCondition = MutableStateFlow<String>("ambient")
    val activeStorageCondition: StateFlow<String> = _activeStorageCondition.asStateFlow()

    private val _activeProduceType = MutableStateFlow<ProduceType>(ProduceType.AVOCADO)
    val activeProduceType: StateFlow<ProduceType> = _activeProduceType.asStateFlow()

    fun setActiveScan(
        prediction: PredictionResponseDto,
        imageUri: String?,
        storageCondition: String = "ambient",
        produceType: ProduceType? = null
    ) {
        _activePrediction.value = prediction
        _activeImageUri.value = imageUri
        _activeStorageCondition.value = storageCondition
        _activeProduceType.value = produceType ?: ProduceType.fromBackendValue(prediction.foodType)
    }

    fun clearActiveScan() {
        _activePrediction.value = null
        _activeImageUri.value = null
        _activeStorageCondition.value = "ambient"
        _activeProduceType.value = ProduceType.AVOCADO
    }
}
