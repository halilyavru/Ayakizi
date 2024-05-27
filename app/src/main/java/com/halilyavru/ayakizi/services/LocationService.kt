package com.halilyavru.ayakizi.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.halilyavru.ayakizi.MainActivity
import com.halilyavru.ayakizi.R
import com.halilyavru.ayakizi.model.FootMarker
import com.halilyavru.ayakizi.repository.LocationRepository
import com.halilyavru.ayakizi.util.PreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : LifecycleService() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private val channelId = "LocationServiceChannel"
    private val notificationId = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, createNotification(), FOREGROUND_SERVICE_TYPE_LOCATION)
        }else{
            startForeground(notificationId, createNotification())
        }
        startLocationUpdates()
    }

    private fun createNotificationChannel() {
        val name = "Location Service Channel"
        val descriptionText = "Channel for Location Service"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(5000)
            .setMinUpdateDistanceMeters(100f)
            .setWaitForAccurateLocation(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        saveLocationToDatabase(location)
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    private fun saveLocationToDatabase(location: Location) {
        val timestamp = System.currentTimeMillis()
        lifecycleScope.launch {
            locationRepository.insertLocation(
                FootMarker(latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = timestamp
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(object : LocationCallback() {})
        preferencesHelper.setServiceStarted(false)
    }
}