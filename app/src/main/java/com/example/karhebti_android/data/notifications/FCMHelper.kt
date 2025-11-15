package com.example.karhebti_android.data.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper pour gérer Firebase Cloud Messaging et les tokens FCM
 */
class FCMHelper(private val context: Context) {

    companion object {
        private const val TAG = "FCMHelper"
    }

    /**
     * Obtenir le token FCM actuel
     */
    fun getFCMToken(callback: (token: String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Erreur lors de la récupération du token", task.exception)
                callback("")
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "Token FCM obtenu: $token")
            callback(token)
        }
    }

    /**
     * S'abonner à un topic de notifications
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Abonné au topic: $topic")
                } else {
                    Log.e(TAG, "❌ Erreur abonnement au topic: $topic")
                }
            }
    }

    /**
     * Se désabonner d'un topic
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Désabonné du topic: $topic")
                } else {
                    Log.e(TAG, "❌ Erreur désabonnement du topic: $topic")
                }
            }
    }

    /**
     * Activer les notifications push
     */
    fun enableNotifications() {
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        Log.d(TAG, "✅ Notifications push activées")
    }

    /**
     * Désactiver les notifications push
     */
    fun disableNotifications() {
        FirebaseMessaging.getInstance().isAutoInitEnabled = false
        Log.d(TAG, "❌ Notifications push désactivées")
    }
}

