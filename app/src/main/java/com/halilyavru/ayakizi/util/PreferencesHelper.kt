package com.halilyavru.ayakizi.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val SERVICE_STARTED = "service_started"
    }

    fun isServiceStarted(): Boolean {
        return preferences.getBoolean(SERVICE_STARTED, false)
    }

    fun setServiceStarted(started: Boolean) {
        preferences.edit().putBoolean(SERVICE_STARTED, started).apply()
    }
}