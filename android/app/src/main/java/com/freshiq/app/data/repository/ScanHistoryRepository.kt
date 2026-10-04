package com.freshiq.app.data.repository

import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    fun getScanHistory(): Flow<List<ScanHistoryEntity>>
    suspend fun getScanById(id: String): ScanHistoryEntity?
    suspend fun saveScan(
        id: String,
        imagePath: String,
        storageCondition: String,
        prediction: PredictionResponseDto
    )
    suspend fun deleteScan(id: String)
    suspend fun clearHistory()
}
