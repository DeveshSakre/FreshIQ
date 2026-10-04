package com.freshiq.app.data.repository

import com.freshiq.app.data.local.ScanHistoryDao
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanHistoryRepositoryImpl @Inject constructor(
    private val dao: ScanHistoryDao,
    private val gson: Gson
) : ScanHistoryRepository {

    override fun getScanHistory(): Flow<List<ScanHistoryEntity>> = dao.getAllScans()

    override suspend fun getScanById(id: String): ScanHistoryEntity? = dao.getScanById(id)

    override suspend fun saveScan(
        id: String,
        imagePath: String,
        storageCondition: String,
        prediction: PredictionResponseDto
    ) {
        val json = gson.toJson(prediction)
        val entity = ScanHistoryEntity(
            id = id,
            timestamp = System.currentTimeMillis(),
            thumbnailPath = imagePath,
            storageCondition = storageCondition,
            predictionJson = json
        )
        dao.insertScan(entity)
    }

    override suspend fun deleteScan(id: String) {
        dao.deleteScanById(id)
    }

    override suspend fun clearHistory() {
        dao.clearAllScans()
    }
}
