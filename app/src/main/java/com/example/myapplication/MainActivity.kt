package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.models.MasterJobRepository
import com.example.myapplication.maps.LocationRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val repository = MasterJobRepository()
    private val authViewModel: AuthViewModel by viewModels()
    private val locationRepository = LocationRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStop() {
        super.onStop()
        val currentUser = authViewModel.auth.currentUser
        if (currentUser != null && hasLocationPermission()) {
            lifecycleScope.launch {
                // Pošalji trenutnu lokaciju
                sendCurrentUserLocation()

                while (true) {
                    val location = getLastKnownLocation() ?: LatLng(43.32, 21.90) // default lokacija
                    val nearbyMasters = repository.getMastersNearLocation(location.latitude, location.longitude)
                    val nearbyJobs = repository.getJobsNearLocation(location.latitude, location.longitude)
                    val totalMarkers = nearbyMasters.size + nearbyJobs.size ?: 0

                    showAppMinimizedNotification(totalMarkers)


                    delay(10 * 60 * 1000) // svaka 5 minuta
                }
            }
        }
    }

    // --- LOKACIJA ---
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun sendCurrentUserLocation() {
        val currentUser = authViewModel.auth.currentUser ?: return
        val location = try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) { null }

        location?.let {
            locationRepository.sendUserLocation(
                currentUser.uid,
                LatLng(it.latitude, it.longitude)
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getLastKnownLocation(): LatLng? {
        return try {
            fusedLocationClient.lastLocation.await()?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // --- NOTIFIKACIJA ---
    private fun showAppMinimizedNotification(markerCount: Int) {
        val channelId = "app_minimized_channel"
        val notificationId = 1001
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App minimizovana",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Obaveštenje kada se aplikacija minimizuje"
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Novi marker u blizini!")
            .setContentText("Broj markera u krugu od 2 km: $markerCount")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
