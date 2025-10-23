package com.example.myapplication.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.myapplication.AuthViewModel
import com.example.myapplication.models.Job
import com.example.myapplication.models.Master
import com.example.myapplication.models.MasterJobRepository
import com.example.myapplication.Info.MarkerInfoCard
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
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
    val repository = remember { MasterJobRepository() }

    var selectedMarkerData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val currentUser = authViewModel.currentUser.observeAsState()
    val currentUserId = currentUser.value?.uid

    // Za BottomSheet
    var showSheet by remember { mutableStateOf(false) }

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
        // Žuti marker za selektovanu lokaciju
        selectedLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Odabrana lokacija",
                snippet = "Klikni 'Dodaj' da dodaš marker ovde",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )
        }

        // Plavi markeri - majstori
        masters.forEach { master ->
            val masterLocation = LatLng(master.location.latitude, master.location.longitude)
            Marker(
                state = MarkerState(position = masterLocation),
                title = "Majstor: ${master.name}",
                snippet = "${master.profession} • ⭐${master.rating} • ${master.phone}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                onClick = {
                    selectedMarkerData = mapOf(
                        "id" to master.id,
                        "type" to "master",
                        "name" to master.name,
                        "profession" to master.profession,
                        "phone" to master.phone,
                        "rating" to master.rating,
                        "createdBy" to master.createdBy,
                        "createdAt" to master.createdAt
                    )
                    showSheet = true
                    true
                }
            )
        }

        // Crveni markeri - poslovi
        jobs.forEach { job ->
            val jobLocation = LatLng(job.location.latitude, job.location.longitude)
            Marker(
                state = MarkerState(position = jobLocation),
                title = "Posao: ${job.profession}",
                snippet = "${job.description} - ${job.contactPhone}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = {
                    selectedMarkerData = mapOf(
                        "id" to job.id,
                        "type" to "job",
                        "profession" to job.profession,
                        "description" to job.description,
                        "phone" to job.contactPhone,
                        "createdBy" to job.createdBy,
                        "createdAt" to job.createdAt
                    )
                    showSheet = true
                    true
                }
            )
        }
    }

    // BottomSheet sa informacijama o markeru
    if (showSheet && selectedMarkerData != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            MarkerInfoCard(
                markerData = selectedMarkerData!!,
                currentUserId = currentUserId,
                onDelete = {
                    val data = selectedMarkerData!!
                    val type = data["type"] as String
                    val id = data["id"] as String
                    repository.deleteDocument(
                        if (type == "master") "masters" else "jobs",
                        id
                    ) { success ->
                        showSheet = false
                    }
                }
            )
        }
    }
}

// --- Lokacija korisnika ---
private suspend fun getCurrentUserLocationForCamera(context: Context): LatLng? {
    return try {
        // Explicitna provera permisije
        if (!hasLocationPermission(context)) {
            return LatLng(43.32, 21.90) // fallback lokacija
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { LatLng(it.latitude, it.longitude) } ?: getNetworkLocation(context)
        } catch (e: SecurityException) {
            // Ako permisija nije data, vrati fallback
            LatLng(43.32, 21.90)
        }
    } catch (e: Exception) {
        LatLng(43.32, 21.90)
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun getNetworkLocation(context: Context): LatLng? {
    return try {
        if (!hasLocationPermission(context)) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                LatLng(it.latitude, it.longitude)
            }
        } catch (e: SecurityException) {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

