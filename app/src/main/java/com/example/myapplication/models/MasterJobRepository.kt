package com.example.myapplication.models

import com.example.myapplication.models.Master
import com.example.myapplication.models.Job
import com.example.myapplication.models.Review
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import android.util.Log
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MasterJobRepository {
    private val db = FirebaseFirestore.getInstance()
    private val mastersCollection = db.collection("masters")
    private val jobsCollection = db.collection("jobs")
    private val reviewsCollection = db.collection("reviews")

    // 👇 MAJSTORI
    suspend fun getAllMasters(): List<Master> {
        return try {
            val masters = mastersCollection
                .get()
                .await()
                .toObjects(Master::class.java)
            Log.d("MasterJobRepository", "📋 Učitano ${masters.size} majstora")
            masters
        } catch (e: Exception) {
            Log.e("MasterJobRepository", "❌ Greška pri dohvatanju majstora: ${e.message}")
            emptyList()
        }
    }
    suspend fun addMaster(master: Master): String {
        return try {
            val docRef = mastersCollection.document()
            val masterWithId = master.copy(id = docRef.id)
            docRef.set(masterWithId).await()
            Log.d("MasterJobRepository", "✅ Majstor dodat: ${master.name}")
            docRef.id
        } catch (e: Exception) {
            Log.e("MasterJobRepository", "❌ Greška pri dodavanju majstora: ${e.message}")
            ""
        }
    }
    suspend fun updateMasterRating(masterId: String, newRating: Double, newReviewCount: Int) {
        try {
            mastersCollection.document(masterId)
                .update(
                    "rating", newRating,
                    "reviewCount", newReviewCount,
                    "lastUpdated", Date()

                )
                .await()
            Log.d("MasterJobRepository", "⭐ Rating ažuriran za majstora: $masterId")
        } catch (e: Exception) {
            Log.e("MasterJobRepository", "❌ Greška pri ažuriranju ratinga: ${e.message}")
        }
    }
    fun listenToMasters(onUpdate: (List<Master>) -> Unit) {
        mastersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MasterJobRepo", "Error: ${error.message}")
                return@addSnapshotListener
            }
            snapshot?.let {
                val masters = it.toObjects(Master::class.java)
                onUpdate(masters)
            }
        }
    }
    fun listenToJobs(onUpdate: (List<Job>) -> Unit) {
        jobsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MasterJobRepo", "Error: ${error.message}")
                return@addSnapshotListener
            }
            snapshot?.let {
                val jobs = it.toObjects(Job::class.java)
                onUpdate(jobs)
            }
        }
    }
    suspend fun getAllJobs(): List<Job> {
        return try {
            val jobs = jobsCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Job::class.java)
            Log.d("MasterJobRepository", "📋 Učitano ${jobs.size} poslova")
            jobs
        } catch (e: Exception) {
            Log.e("MasterJobRepository", "❌ Greška pri dohvatanju poslova: ${e.message}")
            emptyList()
        }
    }
    suspend fun addJob(job: Job): String {
        return try {
            val docRef = jobsCollection.document()
            val jobWithId = job.copy(id = docRef.id)
            docRef.set(jobWithId).await()
            Log.d("MasterJobRepository", "✅ Posao dodat: ${job.title}")
            docRef.id
        } catch (e: Exception) {
            Log.e("MasterJobRepository", "❌ Greška pri dodavanju posla: ${e.message}")
            ""
        }
    }
    suspend fun getReviewsForMaster(masterId: String): List<Review> {
        return try {
            reviewsCollection
                .whereEqualTo("masterId", masterId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Review::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    private suspend fun updateMasterRatingAfterReview(masterId: String) {
        val reviews = getReviewsForMaster(masterId)
        if (reviews.isNotEmpty()) {
            val averageRating = reviews.map { it.rating }.average()
            val reviewCount = reviews.size
            updateMasterRating(masterId, averageRating, reviewCount)
        }
    }
    suspend fun searchMastersByProfession(profession: String): List<Master> {
    return try {
        val querySnapshot = mastersCollection
            .whereEqualTo("profession", profession.trim().lowercase())
            .whereEqualTo("available", true)
            .get()
            .await()

        Log.d("MasterJobRepository", "📄 Firestore dokumenti: ${querySnapshot.documents.map { it.data }}")

        val masters = querySnapshot.toObjects(Master::class.java)
        Log.d("MasterJobRepository", "🔍 Pronađeno ${masters.size} majstora za profesiju: $profession")
        masters
    } catch (e: Exception) {
        Log.e("MasterJobRepository", "❌ Greška pri pretrazi po profesiji: ${e.message}")
        emptyList()
    }
}
    suspend fun searchMastersByRadius(centerLat: Double, centerLng: Double, radiusInMeters: Double): List<Master> {
    return try {
        val allMasters = mastersCollection
            .whereEqualTo("available", true)
            .get()
            .await()
            .toObjects(Master::class.java)

        val nearby = allMasters.filter {
            val d = calculateDistance(centerLat, centerLng, it.location.latitude, it.location.longitude)
            d <= radiusInMeters
        }

        Log.d("MasterJobRepository", "📍 Pronađeno ${nearby.size} majstora u radiusu od ${radiusInMeters}m")
        nearby
    } catch (e: Exception) {
        Log.e("MasterJobRepository", "❌ Greška pri pretrazi po radiusu: ${e.message}")
        emptyList()
    }
}
    suspend fun searchMastersByProfessionAndRadius(profession: String, centerLat: Double, centerLng: Double, radiusInMeters: Double): List<Master> {
    return try {
        val querySnapshot = mastersCollection
            .whereEqualTo("profession", profession.trim().lowercase())
            .whereEqualTo("available", true)
            .get()
            .await()

        val filtered = querySnapshot.toObjects(Master::class.java).filter {
            val d = calculateDistance(centerLat, centerLng, it.location.latitude, it.location.longitude)
            d <= radiusInMeters
        }

        Log.d("MasterJobRepository", "🎯 Pronađeno ${filtered.size} $profession majstora u radiusu od ${radiusInMeters}m")
        filtered
    } catch (e: Exception) {
        Log.e("MasterJobRepository", "❌ Greška pri kombinovanoj pretrazi: ${e.message}")
        emptyList()
    }
}
    suspend fun searchJobsByProfession(profession: String): List<Job> {
    return try {
        val querySnapshot = jobsCollection
            .whereEqualTo("profession", profession.trim().lowercase())
            .whereEqualTo("status", "Open")
            .get()
            .await()
        querySnapshot.toObjects(Job::class.java)
    } catch (e: Exception) {
        emptyList()
    }
}
    suspend fun searchJobsByRadius(centerLat: Double, centerLng: Double, radiusInMeters: Double): List<Job> {
    return try {
        val allJobs = jobsCollection
            .whereEqualTo("status", "Open")
            .get()
            .await()
            .toObjects(Job::class.java)

        allJobs.filter {
            val d = calculateDistance(centerLat, centerLng, it.location.latitude, it.location.longitude)
            d <= radiusInMeters
        }
    } catch (e: Exception) {
        emptyList()
    }
}
    suspend fun searchJobsByProfessionAndRadius( profession: String, centerLat: Double, centerLng: Double, radiusInMeters: Double): List<Job> {
    return try {
        val querySnapshot = jobsCollection
            .whereEqualTo("profession", profession.trim().lowercase())
            .whereEqualTo("status", "Open")
            .get()
            .await()

        querySnapshot.toObjects(Job::class.java).filter {
            val d = calculateDistance(centerLat, centerLng, it.location.latitude, it.location.longitude)
            d <= radiusInMeters
        }
    } catch (e: Exception) {
        emptyList()
    }
}
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        return 2 * R * atan2(sqrt(a), sqrt(1 - a))
    }
}