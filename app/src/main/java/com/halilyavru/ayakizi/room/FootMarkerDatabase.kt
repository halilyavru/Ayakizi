package com.halilyavru.ayakizi.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.halilyavru.ayakizi.model.FootMarker

@Database(entities = [FootMarker::class], version = 1, exportSchema = false)
abstract class FootMarkerDatabase : RoomDatabase() {
    abstract fun locationDao(): FootMarkerDao

    companion object {
        @Volatile private var instance: FootMarkerDatabase? = null

        fun getInstance(context: Context): FootMarkerDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): FootMarkerDatabase {
            return Room.databaseBuilder(context, FootMarkerDatabase::class.java, "foot_marker_database")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}