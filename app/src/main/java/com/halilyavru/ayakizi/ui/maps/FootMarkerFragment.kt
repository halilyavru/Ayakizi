package com.halilyavru.ayakizi.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.halilyavru.ayakizi.R
import com.halilyavru.ayakizi.databinding.FragmentFootMarkerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import com.halilyavru.ayakizi.model.FootMarker as FootMarker

@AndroidEntryPoint
class FootMarkerFragment : Fragment() {

    private val footMarkerViewModel: FootMarkerViewModel by viewModels()
    private var _binding: FragmentFootMarkerBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private val addedMarkers = mutableMapOf<Int, Marker>()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkAndRequestLocationPermission()
            } else {
                if(!approvedPermission(Manifest.permission.POST_NOTIFICATIONS)){
                    showPermissionDeniedDialog()
                }
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkAndRequestBackGroundLocationPermission()
                }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                    if (footMarkerViewModel.isServiceStarted()) {
                        footMarkerViewModel.startLocationService(requireContext())
                    }
                }

            } else {
                if(!approvedPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                    showPermissionDeniedDialog()
                }
            }
        }

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (footMarkerViewModel.isServiceStarted()) {
                    footMarkerViewModel.startLocationService(requireContext())
                }
            } else {
                if(!approvedPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                    showPermissionDeniedDialog()
                }
            }
        }

    private val callback = OnMapReadyCallback { googleMap ->
        this@FootMarkerFragment.googleMap = googleMap
        this@FootMarkerFragment.googleMap.setOnMarkerClickListener(markerClickListener)
        enableMyLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFootMarkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMap()
        initializeButtons()
        observeLocationUpdates()
        checkAllPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun checkAllPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkAndRequestNotificationPermission()
        }else{
            checkAndRequestLocationPermission()
        }

    }

    private fun checkPermission(permission : String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun approvedPermission(permission : String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            permission
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndRequestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                checkPermission(permission) -> {
                    checkAndRequestLocationPermission()
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(permission)
                }
            }
        }else{
            checkAndRequestLocationPermission()
        }
    }

    private fun checkAndRequestLocationPermission(){
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        when {
            checkPermission(permission) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkAndRequestBackGroundLocationPermission()
                }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                    if (footMarkerViewModel.isServiceStarted()) {
                        footMarkerViewModel.startLocationService(requireContext())
                    }
                }

            }
            else -> {
                requestLocationPermissionLauncher.launch(permission)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestBackGroundLocationPermission(){
        val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        when {
            checkPermission(permission) -> {
                if (footMarkerViewModel.isServiceStarted()) {
                    footMarkerViewModel.startLocationService(requireContext())
                }
            }
            else -> {
                requestBackgroundLocationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_permission_title)
            .setMessage(R.string.dialog_permission_message)
            .setPositiveButton(R.string.dialog_permissions_open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.dialog_permissions_later) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun initializeButtons() {
        binding.startServiceButton.setOnClickListener {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkPermission(Manifest.permission.POST_NOTIFICATIONS)) || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        footMarkerViewModel.startLocationService(requireContext())
                    }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                        footMarkerViewModel.startLocationService(requireContext())
                    }else{
                        checkAndRequestBackGroundLocationPermission()
                    }
                }else{
                    checkAndRequestLocationPermission()
                }
            }else{
                checkAndRequestNotificationPermission()
            }
        }

        binding.stopServiceButton.setOnClickListener {
            footMarkerViewModel.stopLocationService(requireContext())
        }

        binding.clearPinsButton.setOnClickListener {
            clearPins()
            footMarkerViewModel.clearLocations()
        }
        footMarkerViewModel.isServiceRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning) {
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.green)
                binding.startServiceButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
                binding.startServiceButton.setTextColor(Color.WHITE)
            }
        }
    }

    private fun observeLocationUpdates() {
        lifecycleScope.launch {
            footMarkerViewModel.locations.collectLatest { locations ->
                val existingMarkerIds = addedMarkers.keys
                locations.forEach { location ->
                    if (location.id !in existingMarkerIds) {
                        val marker = addPin(location)
                        if (marker != null) {
                            addedMarkers[location.id] = marker
                        }
                    }
                }
            }
        }

    }

    private fun addPin(location: FootMarker) : Marker? {
        val position = LatLng(location.latitude, location.longitude)
        return googleMap.addMarker(MarkerOptions().position(position))
    }

    private fun clearPins() {
        googleMap.clear()
    }

    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName = ""
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                if(list[0].countryName != null)
                    addressName += list[0].countryName
                if(list[0].locality != null)
                    addressName += " "+list[0].locality
                if(list[0].subLocality != null)
                    addressName += " "+list[0].subLocality
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressName
    }

    private val markerClickListener = GoogleMap.OnMarkerClickListener { marker ->
        marker.title = getAddressName(
            lat = marker.position.latitude,
            lon = marker.position.longitude
        )
        false
    }

}