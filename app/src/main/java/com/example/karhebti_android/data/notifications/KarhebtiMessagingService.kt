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
        private const val CHANNEL_ID_DOCUMENT = "document_expiration"
        private const val CHANNEL_ID_SOS = "sos_notifications"
        private const val CHANNEL_ID_MESSAGES = "chat_messages"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "✅ MESSAGE REÇU!")
        Log.d(TAG, "De: ${remoteMessage.from}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")
        Log.d(TAG, "Data: ${remoteMessage.data}")

        // Determine notification type from data
        val notificationType = remoteMessage.data["type"] ?: "general"

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Karhebti"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "Notification reçue"

        Log.d(TAG, "Type: $notificationType")
        Log.d(TAG, "Affichage: $title - $body")

        when (notificationType) {
            // Backend envoie "sos_created" quand une nouvelle panne est créée
            "sos_created", "new_breakdown", "sos_request" -> showSOSNotification(title, body, remoteMessage.data)
            // Backend envoie "sos_status_updated" quand le statut change
            "sos_status_updated", "breakdown_status_update" -> showStatusUpdateNotification(title, body, remoteMessage.data)
            "new_message" -> showMessageNotification(title, body, remoteMessage.data)
            else -> showNotification(title, body, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "✅ Nouveau token FCM: $token")
        // TODO: Send token to backend
    }

    private fun showSOSNotification(title: String, message: String, data: Map<String, String>) {
        try {
            Log.d(TAG, "🚨 Création notification SOS...")

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("from_notification", true)
                putExtra("notification_type", "sos")
                data["breakdownId"]?.let { putExtra("breakdownId", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            createNotificationChannel(CHANNEL_ID_SOS, "Demandes SOS", NotificationManager.IMPORTANCE_HIGH)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_SOS)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(0xFFD21313.toInt())
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)

            Log.d(TAG, "✅✅✅ NOTIFICATION SOS AFFICHÉE")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur notification SOS: ${e.message}", e)
        }
    }

    private fun showStatusUpdateNotification(title: String, message: String, data: Map<String, String>) {
        try {
            Log.d(TAG, "📊 Notification de mise à jour de statut...")

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("from_notification", true)
                putExtra("notification_type", "status_update")
                data["breakdownId"]?.let { putExtra("breakdownId", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            createNotificationChannel(CHANNEL_ID_SOS, "Demandes SOS", NotificationManager.IMPORTANCE_HIGH)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_SOS)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFF4CAF50.toInt())
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)

            Log.d(TAG, "✅ Notification de statut affichée")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
        }
    }

    private fun showMessageNotification(title: String, message: String, data: Map<String, String>) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("from_notification", true)
                putExtra("notification_type", "message")
                data["conversationId"]?.let { putExtra("conversationId", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            createNotificationChannel(CHANNEL_ID_MESSAGES, "Messages", NotificationManager.IMPORTANCE_DEFAULT)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_MESSAGES)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
        }
    }

    private fun showNotification(title: String, message: String, data: Map<String, String>) {
        try {
            Log.d(TAG, "🔔 Création de la notification...")

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("from_notification", true)

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            createNotificationChannel(CHANNEL_ID_DOCUMENT, "Document Expiration Alerts", NotificationManager.IMPORTANCE_HIGH)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID_DOCUMENT)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)

            Log.d(TAG, "✅✅✅ NOTIFICATION AFFICHÉE: $title")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = when (channelId) {
                        CHANNEL_ID_SOS -> "Notifications pour les demandes SOS urgentes"
                        CHANNEL_ID_MESSAGES -> "Notifications pour les nouveaux messages"
                        else -> "Alerts for documents expiring soon"
                    }
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Channel créé: $channelId")
            }
        }
    }
}
