package com.example.myapplication.models

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Master(
    val id: String = "",
    val name: String = "",
    val profession: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val phone: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val lastUpdated: Date = Date(),
    val isAvailable: Boolean = true,
    val email: String = "",
    val description: String = "",
    val services: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Date = Date(),
    val hourlyRate: Double = 0.0,
    val experience: String = ""
)

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val profession: String = "",
    val urgency: String = "Normal", // Hitno, Normal, Nije hitno
    val budget: String = "",
    val createdBy: String = "",
    val createdByEmail: String = "",
    val createdAt: Date = Date(),
    val status: String = "Open", // Open, In Progress, Completed
    val assignedTo: String = "",
    val assignedToName: String = "",
    val contactPhone: String = "",
    val address: String = "",
    val estimatedDuration: String = "",
    val materialsProvided: Boolean = false,
    val userId: String = ""
    )

data class Review(
    val id: String = "",
    val masterId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Date = Date(),
    val jobId: String = ""

)