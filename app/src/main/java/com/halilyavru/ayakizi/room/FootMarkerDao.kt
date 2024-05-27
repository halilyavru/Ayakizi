package com.halilyavru.ayakizi.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halilyavru.ayakizi.model.FootMarker
import kotlinx.coroutines.flow.Flow

@Dao
interface FootMarkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: FootMarker)

    @Query("SELECT * FROM foot_marker ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<FootMarker>>

    @Query("DELETE FROM foot_marker")
    suspend fun clearAllLocations()
}