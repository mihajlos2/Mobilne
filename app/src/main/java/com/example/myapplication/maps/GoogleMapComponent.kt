package com.example.myapplication.maps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await

@Composable
fun GoogleMapComponent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Uzmi trenutnu lokaciju kada se komponenta prikaže
    LaunchedEffect(Unit) {
        userLocation = getCurrentUserLocation(context)
    }

    val cameraPositionState = rememberCameraPositionState()

    // Kada se dobije lokacija, centriraj mapu na nju
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission(context), // ✅ Ovo prikazuje plavi krug
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true, // ✅ Ovo prikazuje dugme za centriranje na lokaciju
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true
        )
    ) {
        // ✅ NEMA MARKERA - koristimo samo ugrađeni plavi krug
    }
}

private suspend fun getCurrentUserLocation(context: Context): LatLng? {
    return try {
        if (!hasLocationPermission(context)) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()

        if (location != null) {
            LatLng(location.latitude, location.longitude)
        } else {
            LatLng(43.321445, 21.896104) // Fallback lokacija
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        LatLng(43.321445, 21.896104)
    } catch (e: Exception) {
        e.printStackTrace()
        LatLng(43.321445, 21.896104)
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}