package com.freshiq.app.ui.screens.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.data.repository.ScanHistoryRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ComparisonViewModel @Inject constructor(
    private val scanSessionManager: ScanSessionManager,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val gson: Gson
) : ViewModel() {

    // Read active session directly from ScanSessionManager (single source of truth)
    val activePrediction: StateFlow<PredictionResponseDto?> = scanSessionManager.activePrediction
    val activeImageUri: StateFlow<String?> = scanSessionManager.activeImageUri
    val activeStorageCondition: StateFlow<String> = scanSessionManager.activeStorageCondition

    // Recent scans from Room database for empty-state restoration without network calls
    val recentScans: StateFlow<List<ScanHistoryEntity>> = scanHistoryRepository.getScanHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interactive UI state for storage reminder toggle
    private val _reminderSet = MutableStateFlow(false)
    val reminderSet: StateFlow<Boolean> = _reminderSet.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleReminder() {
        _reminderSet.value = !_reminderSet.value
    }

    /**
     * Reopen a previous scan from local Room database without making ANY network calls.
     */
    fun loadHistoricalScan(entity: ScanHistoryEntity) {
        try {
            val prediction = gson.fromJson(entity.predictionJson, PredictionResponseDto::class.java)
            scanSessionManager.setActiveScan(
                prediction = prediction,
                imageUri = entity.thumbnailPath.ifBlank { null },
                storageCondition = entity.storageCondition
            )
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load scan record: ${e.message}"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
