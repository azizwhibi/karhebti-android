package com.example.karhebti_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.karhebti_android.data.notifications.FCMTokenService
import com.example.karhebti_android.navigation.NavGraph
import com.example.karhebti_android.navigation.Screen
import com.example.karhebti_android.ui.theme.KarhebtiandroidTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Permission de notification accordée")
        } else {
            Log.w("MainActivity", "❌ Permission de notification refusée")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate started")

        // Demander la permission de notification pour Android 13+
        askNotificationPermission()

        try {
            // Initialize RetrofitClient with application context
            com.example.karhebti_android.data.api.RetrofitClient.initialize(this.applicationContext)

            // Initialiser FCM et envoyer le token au backend
            initializeFCM()

            enableEdgeToEdge()
            setContent {
                KarhebtiandroidTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        // Gérer la navigation depuis une notification
                        LaunchedEffect(Unit) {
                            handleNotificationIntent(intent, navController)
                        }

                        NavGraph(navController = navController)
                    }
                }
            }
            Log.d("MainActivity", "setContent completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("MainActivity", "onNewIntent called")
    }

    /**
     * Gérer la navigation depuis une notification
     */
    private fun handleNotificationIntent(
        intent: Intent,
        navController: androidx.navigation.NavHostController
    ) {
        try {
            val fromNotification = intent.getBooleanExtra("from_notification", false)
            if (!fromNotification) {
                Log.d("MainActivity", "Pas de notification, navigation normale")
                return
            }

            val notificationType = intent.getStringExtra("notification_type")
            Log.d("MainActivity", "📱 Navigation depuis notification: $notificationType")

            when (notificationType) {
                // Backend envoie "sos_created" pour les nouvelles pannes
                "sos", "new_breakdown", "sos_request", "sos_created" -> {
                    val breakdownId = intent.getStringExtra("breakdownId")
                    if (breakdownId != null) {
                        Log.d("MainActivity", "🚨 Navigation vers BreakdownDetail: $breakdownId")
                        navController.navigate(Screen.BreakdownDetail.createRoute(breakdownId))
                    }
                }
                // Backend envoie "sos_status_updated" pour les changements de statut
                "status_update", "breakdown_status_update", "sos_status_updated" -> {
                    val breakdownId = intent.getStringExtra("breakdownId")
                    if (breakdownId != null) {
                        Log.d("MainActivity", "📊 Navigation vers tracking: $breakdownId")
                        navController.navigate(Screen.BreakdownTracking.createRoute(breakdownId))
                    }
                }
                "message" -> {
                    val conversationId = intent.getStringExtra("conversationId")
                    if (conversationId != null) {
                        Log.d("MainActivity", "💬 Navigation vers chat: $conversationId")
                        navController.navigate(Screen.Chat.createRoute(conversationId))
                    }
                }
                else -> {
                    Log.d("MainActivity", "Type de notification non géré: $notificationType")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Erreur lors de la navigation depuis notification: ${e.message}", e)
        }
    }

    private fun askNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Initialiser Firebase Cloud Messaging
     */
    private fun initializeFCM() {
        try {
            val fcmTokenService = FCMTokenService(this)

            // Enregistrer le token device au backend
            fcmTokenService.registerDeviceToken()

            // S'abonner aux topics
            fcmTokenService.subscribeToTopics()

            Log.d("MainActivity", "✅ FCM initialisé avec succès")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Erreur lors de l'initialisation FCM: ${e.message}", e)
        }
    }
}
