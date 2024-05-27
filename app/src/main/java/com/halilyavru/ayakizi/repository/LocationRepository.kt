package com.halilyavru.ayakizi.repository

import com.halilyavru.ayakizi.model.FootMarker
import com.halilyavru.ayakizi.room.FootMarkerDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val footMarkerDao: FootMarkerDao
) {
    suspend fun insertLocation(location: FootMarker) {
        footMarkerDao.insertLocation(location)
    }

    fun getAllLocations(): Flow<List<FootMarker>> {
        return footMarkerDao.getAllLocations()
    }

    suspend fun clearAllLocations() {
        footMarkerDao.clearAllLocations()
    }
}