package com.example.myapplication.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AuthViewModel
import com.example.myapplication.Dialog.FilterDialog
import com.example.myapplication.Dialog.MasterRankingDialog
import com.example.myapplication.Dialog.AddMarkerDialog
import com.example.myapplication.R
import com.example.myapplication.models.Master
import com.example.myapplication.models.Job
import com.example.myapplication.models.MasterJobRepository
import com.example.myapplication.maps.GoogleMapComponent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showAddMarkerDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMarkerTable by remember { mutableStateOf(false) }
    var showMasterRankingDialog by remember { mutableStateOf(false) }
    var showUserInfo by remember { mutableStateOf(false) }

    val masterJobRepository = remember { MasterJobRepository() }
    val currentUser by authViewModel.currentUser.observeAsState()
    val userData by authViewModel.userData.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    var masters by remember { mutableStateOf<List<Master>>(emptyList()) }
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUserData()
        userLocation = getCurrentUserLocation(context)
        masterJobRepository.listenToMasters { updatedMasters -> masters = updatedMasters }
        masterJobRepository.listenToJobs { updatedJobs -> jobs = updatedJobs }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMapComponent(
            modifier = Modifier.fillMaxSize(),
            authViewModel = authViewModel,
            onMapClick = { latLng -> selectedLocation = latLng },
            masters = masters,
            jobs = jobs,
            selectedLocation = selectedLocation,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    userData?.get("imageUrl") as? String ?: R.drawable.ic_launcher_foreground
                ),
                contentDescription = "Profilna slika",
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable { showUserInfo = true },
                contentScale = ContentScale.Crop
            )
            FloatingActionButton(
                onClick = { if (selectedLocation != null) showAddMarkerDialog = true },
                modifier = Modifier.size(56.dp),
                containerColor = if (selectedLocation != null) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Dodaj marker") }

            FloatingActionButton(
                onClick = { showMarkerTable = true },
                modifier = Modifier.size(56.dp),
                containerColor = if (selectedLocation != null) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = Color.White
            ) { Icon(Icons.Default.Place, contentDescription = "Lista markera") }

            FloatingActionButton(
                onClick = { showMasterRankingDialog = true },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) { Icon(Icons.Default.List, "Lista majstora") }

            FloatingActionButton(
                onClick = { showFilters = true },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            ) { Icon(Icons.Default.FilterAlt, "Filteri") }

            FloatingActionButton(
                onClick = {
                    authViewModel.signout()
                    navController.navigate("login")
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            ) { Icon(Icons.Default.Logout, "Odjavi se") }


        }
    }

    if (showUserInfo) {
        AlertDialog(
            onDismissRequest = { showUserInfo = false },
            title = { Text("Informacije o korisniku") },
            text = {
                Column {
                    // Profilna slika u dijalogu
                    Image(
                        painter = rememberAsyncImagePainter(userData?.get("imageUrl") as? String),
                        contentDescription = "Profilna slika",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ime: ${userData?.get("ime") as? String ?: ""}")
                    Text("Email: ${userData?.get("prezime") as? String ?: ""}")
                    Text("Telefon: ${userData?.get("email") as? String ?: ""}")
                    Text("Telefon: ${userData?.get("phoneNumber") as? String ?: ""}")
                }
            },
            confirmButton = {
                Button(onClick = { showUserInfo = false }) {
                    Text("Zatvori")
                }
            }
        )
    }

    if (showAddMarkerDialog && selectedLocation != null) {
        AddMarkerDialog(
            onDismiss = { showAddMarkerDialog = false },
            onAddMaster = { masterData ->
                coroutineScope.launch {
                    val newMaster = Master(
                        name = userData?.get("ime") as? String ?: "",
                        profession = masterData["profession"]?.trim()?.lowercase() ?: "",
                        location = GeoPoint(selectedLocation!!.latitude, selectedLocation!!.longitude),
                        phone = userData?.get("phoneNumber") as? String ?: "",
                        email = userData?.get("email") as? String ?: currentUser?.email ?: "",
                        description = masterData["description"] ?: "",
                        createdBy = currentUser?.uid ?: "anonymous",
                        rating = 0.0,
                        reviewCount = 0,
                        isAvailable = true
                    )
                    masterJobRepository.addMaster(newMaster)
                    selectedLocation = null
                    showAddMarkerDialog = false
                }
            },
            onAddJob = { jobData ->
                coroutineScope.launch {
                    val newJob = Job(
                        title = jobData["title"] ?: "",
                        description = jobData["description"] ?: "",
                        location = GeoPoint(selectedLocation!!.latitude, selectedLocation!!.longitude),
                        profession = jobData["profession"] ?: "",
                        urgency = jobData["urgency"] ?: "Normal",
                        budget = jobData["budget"] ?: "",
                        createdBy = currentUser?.uid ?: "anonymous",
                        createdByEmail = currentUser?.email ?: "anonymous",
                        contactPhone = userData?.get("phoneNumber") as? String ?: "030300330",
                        address = jobData["address"] ?: "",
                        status = "Open"
                    )
                    masterJobRepository.addJob(newJob)
                    selectedLocation = null
                    showAddMarkerDialog = false
                }
            }
        )
    }

    if (showMarkerTable) {
        MarkerTableDialog(
            masters = masters,
            jobs = jobs,
            onDismiss = { showMarkerTable = false }
        )
    }

    if (showMasterRankingDialog) {
        MasterRankingDialog(
            masterJobRepository = masterJobRepository,
            onDismiss = { showMasterRankingDialog = false }
        )
    }

    if (showFilters) {
        FilterDialog(
            onDismiss = { showFilters = false },
            onApplyFilters = { filterData ->
                coroutineScope.launch {
                    when (filterData.targetType) {
                        "masters" -> {
                            val filtered = masterJobRepository.applyMasterFilters(masterJobRepository, filterData)
                            masters = filtered
                        }
                        "jobs" -> {
                            val filtered = masterJobRepository.applyJobFilters(masterJobRepository, filterData)
                            jobs = filtered
                        }
                    }
                    showFilters = false
                }
            },
            onResetFilters = {
                coroutineScope.launch {
                    masters = masterJobRepository.getAllMasters()
                    jobs = masterJobRepository.getAllJobs()
                    showFilters = false
                }
            },
            userLocation = userLocation
        )
    }
}
private suspend fun getCurrentUserLocation(context: Context): LatLng? {
    return try {
        if (!hasLocationPermission(context)) return LatLng(43.32, 21.90)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { LatLng(it.latitude, it.longitude) } ?: getNetworkLocation(context) ?: LatLng(43.32, 21.90)
        } catch (e: SecurityException) {
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
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: SecurityException) { null } catch (e: Exception) { null }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
