package com.freshiq.app.di

import com.freshiq.app.data.repository.FreshIQRepository
import com.freshiq.app.data.repository.FreshIQRepositoryImpl
import com.freshiq.app.data.repository.ScanHistoryRepository
import com.freshiq.app.data.repository.ScanHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFreshIQRepository(
        impl: FreshIQRepositoryImpl
    ): FreshIQRepository

    @Binds
    @Singleton
    abstract fun bindScanHistoryRepository(
        impl: ScanHistoryRepositoryImpl
    ): ScanHistoryRepository
}
