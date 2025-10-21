package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FirebaseService", "New FCM token: $token")
        // Možete sačuvati token u Firestore za notifikacije
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FirebaseService", "Message received: ${remoteMessage.data}")

        remoteMessage.data.let { data ->
            val title = data["title"] ?: "Majstor u blizini"
            val message = data["message"] ?: "Pronađen je majstor u vašoj okolini"
            val masterId = data["masterId"]

            showNotification(title, message, masterId)
        }
    }

    private fun showNotification(title: String, message: String, masterId: String?) {
        val channelId = "masters_nearby_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kreiraj notification channel za Android O i više
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Majstori u blizini",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Obaveštenja o majstorima u blizini"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}