package com.example.myapplication.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.myapplication.AuthViewModel
import com.example.myapplication.models.Job
import com.example.myapplication.models.Master
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await


@Composable
fun GoogleMapComponent(
    modifier: Modifier = Modifier,
    onMapClick: (LatLng) -> Unit = {},
    masters: List<Master> = emptyList(),
    jobs: List<Job> = emptyList(),
    selectedLocation: LatLng? = null,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        val userLocation = getCurrentUserLocationForCamera(context)
        userLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission(context),
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true
        ),
        onMapClick = onMapClick
    ) {
        // ZUTI MARKER ZA SELEKTOVANU LOKACIJU
        selectedLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Odabrana lokacija",
                snippet = "Klikni 'Dodaj' da dodaš marker ovde",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )
        }

        // MARKERI ZA MAJSTORE (PLAVI)
        masters.forEach { master ->
            val masterLocation = LatLng(master.location.latitude, master.location.longitude)
            Marker(
                state = MarkerState(position = masterLocation),
                title = "Majstor: ${master.name}",
                snippet = "${master.profession} • ⭐${master.rating} • ${master.phone}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }

        // MARKERI ZA POSLOVE (CRVENI)
        jobs.forEach { job ->
            val jobLocation = LatLng(job.location.latitude, job.location.longitude)
            Marker(
                state = MarkerState(position = jobLocation),
                title = "Posao za:  ${job.profession}",
                snippet = "${job.description}  - "  +
                        "${job.contactPhone}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }
    }
}

private suspend fun getCurrentUserLocationForCamera(context: Context): LatLng? {
    return try {
        //  EKSPLICITNO PROVERI DOZVOLE
        if (!hasLocationPermission(context)) {
            return LatLng(43.32,21.90) // Fallback lokacija
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // SecurityException
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { LatLng(it.latitude, it.longitude) }
                ?: getNetworkLocation(context)
                ?: LatLng(43.32,21.90)
        } catch (e: SecurityException) {
            LatLng(43.32,21.90)
        }

    } catch (e: Exception) {
        LatLng(43.32,21.90)
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun getNetworkLocation(context: Context): LatLng? {
    return try {
        // PROVERI DOZVOLE I OVDE
        if (!hasLocationPermission(context)) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkProvider = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        networkProvider?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null
    } catch (e: Exception) {
        null
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}