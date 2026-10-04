package com.freshiq.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanHistoryEntity::class], version = 1, exportSchema = false)
abstract class FreshIQDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}
