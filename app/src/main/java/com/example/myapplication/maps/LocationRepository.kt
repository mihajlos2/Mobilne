package com.example.myapplication.maps

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

data class Master(
    val id: String = "",
    val name: String = "",
    val profession: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val phone: String = "",
    val rating: Double = 0.0,
    val lastUpdated: Date = Date(),
    val isAvailable: Boolean = true
)

class LocationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userLocationsCollection = db.collection("user_locations")

    // Šalji trenutnu lokaciju korisnika na server
    suspend fun sendUserLocation(userId: String, location: LatLng) {
        try {
            val userLocation = hashMapOf(
                "userId" to userId,
                "location" to GeoPoint(location.latitude, location.longitude),
                "timestamp" to Date()
            )

            userLocationsCollection
                .document(userId)
                .set(userLocation, SetOptions.merge())
                .await()

        } catch (e: Exception) {
            Log.e("LocationRepository", "Greška pri slanju lokacije: ${e.message}")
        }
    }





}