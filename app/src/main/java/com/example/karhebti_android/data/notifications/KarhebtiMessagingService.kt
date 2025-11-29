package com.example.karhebti_android.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.karhebti_android.MainActivity
import com.example.karhebti_android.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KarhebtiMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "KarhebtiMessaging"
        private const val CHANNEL_ID = "document_expiration"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "✅ MESSAGE REÇU!")
        Log.d(TAG, "De: ${remoteMessage.from}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")
        Log.d(TAG, "Data: ${remoteMessage.data}")

        // Afficher la notification peu importe la source
        val title = remoteMessage.notification?.title ?: "Karhebti"
        val body = remoteMessage.notification?.body ?: "Notification reçue"

        Log.d(TAG, "Affichage: $title - $body")
        showNotification(title, body, remoteMessage.data)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "✅ Token FCM: $token")
    }

    private fun showNotification(title: String, message: String, data: Map<String, String>) {
        try {
            Log.d(TAG, "🔔 Création de la notification...")

            // Intent pour ouvrir l'app
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("from_notification", true)

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Créer le channel (Android 8+)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Document Expiration Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Alerts for documents expiring soon"
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Channel créé")
            }

            // Créer la notification
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            // Afficher la notification
            notificationManager.notify(1, notification)
            Log.d(TAG, "✅✅✅ NOTIFICATION AFFICHÉE: $title")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
        }
    }
}

