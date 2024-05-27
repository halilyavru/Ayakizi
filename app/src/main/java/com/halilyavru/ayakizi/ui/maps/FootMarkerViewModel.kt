package com.halilyavru.ayakizi.ui.maps

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.halilyavru.ayakizi.model.FootMarker
import com.halilyavru.ayakizi.repository.LocationRepository
import com.halilyavru.ayakizi.services.LocationService
import com.halilyavru.ayakizi.util.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
@HiltViewModel
class FootMarkerViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val preferencesHelper: PreferencesHelper
)  : ViewModel()  {


    private val _locations = MutableStateFlow<List<FootMarker>>(emptyList())
    val locations: StateFlow<List<FootMarker>> get() = _locations

    init {
        viewModelScope.launch (Dispatchers.IO) {
            locationRepository.getAllLocations().collect { locations ->
                _locations.value = locations
            }
        }
    }

    private val _isServiceRunning = MutableLiveData<Boolean>()
    val isServiceRunning: LiveData<Boolean> get() = _isServiceRunning

    fun startLocationService(context: Context) {
        _isServiceRunning.value = true
        preferencesHelper.setServiceStarted(true)
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopLocationService(context: Context) {
        _isServiceRunning.value = false
        preferencesHelper.setServiceStarted(false)
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }

    fun clearLocations() {
        viewModelScope.launch {
            locationRepository.clearAllLocations()
        }
    }

    fun isServiceStarted(): Boolean {
        return preferencesHelper.isServiceStarted()
    }
}