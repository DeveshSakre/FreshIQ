package com.freshiq.app.di

import android.content.Context
import androidx.room.Room
import com.freshiq.app.data.local.FreshIQDatabase
import com.freshiq.app.data.local.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FreshIQDatabase {
        return Room.databaseBuilder(
            context,
            FreshIQDatabase::class.java,
            "freshiq_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(database: FreshIQDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }
}
