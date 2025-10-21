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
    private val mastersCollection = db.collection("masters")
    private val userLocationsCollection = db.collection("user_locations")
    private val locationLogsCollection = db.collection("location_logs")

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
                .set(userLocation, SetOptions.merge()) // ✅ update ako postoji, create ako ne
                .await()

            Log.d(
                "LocationRepository",
                "✅ Lokacija ažurirana/poslata za user: $userId - ${location.latitude}, ${location.longitude}"
            )

            // Proveri da li ima majstora u blizini
            checkForNearbyMasters(location, userId)

        } catch (e: Exception) {
            Log.e("LocationRepository", "❌ Greška pri slanju lokacije: ${e.message}")
        }
    }

    // Proveri majstore u blizini
    private suspend fun checkForNearbyMasters(userLocation: LatLng, userId: String) {
        try {
            // Radius od 2km
            val radiusInM = 2000

            val masters = mastersCollection
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
                .toObjects(Master::class.java)

            val nearbyMasters = masters.filter { master ->
                val distance = calculateDistance(
                    userLocation.latitude,
                    userLocation.longitude,
                    master.location.latitude,
                    master.location.longitude
                )
                distance <= radiusInM
            }


            if (nearbyMasters.isNotEmpty()) {
                Log.d("LocationRepository", "📍 Pronađeno ${nearbyMasters.size} majstora u blizini za user: $userId")
            } else {
                Log.d("LocationRepository", "❌ Nema majstora u blizini za user: $userId")
            }

        } catch (e: Exception) {
            Log.e("LocationRepository", "❌ Greška pri proveri majstora u blizini: ${e.message}")
        }
    }
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 2000 // metara

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
    // Dohvati sve majstore
    suspend fun getAllMasters(): List<Master> {
        return try {
            val masters = mastersCollection
                .get()
                .await()
                .toObjects(Master::class.java)
            Log.d("LocationRepository", "📋 Učitano ${masters.size} majstora iz baze")
            masters
        } catch (e: Exception) {
            Log.e("LocationRepository", "❌ Greška pri dohvatanju majstora: ${e.message}")
            emptyList()
        }
    }


}