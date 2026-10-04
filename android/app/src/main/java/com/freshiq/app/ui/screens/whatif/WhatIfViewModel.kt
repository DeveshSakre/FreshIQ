package com.freshiq.app.ui.screens.whatif

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

enum class StoragePresetKey(val key: String, val label: String, val sub: String, val temp: String) {
    AMBIENT("ambient", "Ambient Counter", "Warm Countertop (~20–22°C)", "~20–22°C"),
    ROOM_20C("20C", "Controlled Room", "Standard Lab Reference", "20°C"),
    COLD_10C("10C", "Cold Storage (10°C)", "Optimal Vegetable Crisper", "10°C"),
    FRIDGE_4C("4C_refrigerator", "Domestic Refrigerator", "Deep Chilled Shelf (4°C)", "4°C")
}

enum class ContainerFormat(val label: String) {
    OPEN("Open air"),
    CRISPER("Crisper drawer"),
    PAPER("Paper bag")
}

@HiltViewModel
class WhatIfViewModel @Inject constructor(
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

    // Selected thermal regime preset (default to 10°C Cold Storage)
    private val _selectedPreset = MutableStateFlow(StoragePresetKey.COLD_10C)
    val selectedPreset: StateFlow<StoragePresetKey> = _selectedPreset.asStateFlow()

    // Microclimate modifiers
    private val _containerFormat = MutableStateFlow(ContainerFormat.CRISPER)
    val containerFormat: StateFlow<ContainerFormat> = _containerFormat.asStateFlow()

    private val _ethyleneProximity = MutableStateFlow(false)
    val ethyleneProximity: StateFlow<Boolean> = _ethyleneProximity.asStateFlow()

    // Interactive UI action states
    private val _reminderSet = MutableStateFlow(false)
    val reminderSet: StateFlow<Boolean> = _reminderSet.asStateFlow()

    private val _planApplied = MutableStateFlow(false)
    val planApplied: StateFlow<Boolean> = _planApplied.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectPreset(preset: StoragePresetKey) {
        _selectedPreset.value = preset
    }

    fun setContainerFormat(format: ContainerFormat) {
        _containerFormat.value = format
    }

    fun toggleEthyleneProximity() {
        _ethyleneProximity.value = !_ethyleneProximity.value
    }

    fun toggleReminder() {
        _reminderSet.value = !_reminderSet.value
    }

    fun togglePlanApplied() {
        _planApplied.value = !_planApplied.value
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
