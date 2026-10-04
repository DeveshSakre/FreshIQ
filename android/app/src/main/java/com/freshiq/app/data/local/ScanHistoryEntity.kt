package com.freshiq.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailPath: String = "",
    val storageCondition: String = "ambient",
    val predictionJson: String
)
