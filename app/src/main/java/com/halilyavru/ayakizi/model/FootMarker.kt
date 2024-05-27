package com.halilyavru.ayakizi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foot_marker")
data class FootMarker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)