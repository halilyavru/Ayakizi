package com.halilyavru.ayakizi.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.halilyavru.ayakizi.room.FootMarkerDatabase
import com.halilyavru.ayakizi.room.FootMarkerDao
import com.halilyavru.ayakizi.util.PreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLocationDao(database: FootMarkerDatabase): FootMarkerDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): FootMarkerDatabase {
        return FootMarkerDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(@ApplicationContext context: Context): PreferencesHelper {
        return PreferencesHelper(context)
    }
}