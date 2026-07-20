package com.streamflow.app.di

import com.streamflow.app.data.repository.AuthRepository
import com.streamflow.app.data.repository.CatalogRepository
import com.streamflow.app.data.repository.DownloadRepository
import com.streamflow.app.data.repository.FirebaseAuthRepository
import com.streamflow.app.data.repository.FirestoreCatalogRepository
import com.streamflow.app.data.repository.MockDownloadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.streamflow.app.data.repository.NeonCatalogRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        impl: NeonCatalogRepository
    ): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        impl: MockDownloadRepository
    ): DownloadRepository
}

