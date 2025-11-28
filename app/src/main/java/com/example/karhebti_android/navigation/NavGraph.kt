package com.example.karhebti_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.karhebti_android.ui.screens.*
import com.example.karhebti_android.ui.screens.BreakdownSOSScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Vehicles : Screen("vehicles")
    object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: String) = "vehicle_detail/$vehicleId"
    }
    object Entretiens : Screen("entretiens")
    object MaintenanceDetail : Screen("maintenance_detail/{maintenanceId}") {
        fun createRoute(maintenanceId: String) = "maintenance_detail/$maintenanceId"
    }
    object Documents : Screen("documents")
    object DocumentDetail : Screen("document_detail/{documentId}") {
        fun createRoute(documentId: String) = "document_detail/$documentId"
    }
    object AddDocument : Screen("add_document")
    object EditDocument : Screen("edit_document/{documentId}") {
        fun createRoute(documentId: String) = "edit_document/$documentId"
    }
    object Garages : Screen("garages")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Reclamations : Screen("reclamations")
    object AddReclamation : Screen("add_reclamation")
    object ReclamationDetail : Screen("reclamation_detail/{reclamationId}") {
        fun createRoute(reclamationId: String) = "reclamation_detail/$reclamationId"
    }
    object EditReclamation : Screen("edit_reclamation/{reclamationId}") {
        fun createRoute(reclamationId: String) = "edit_reclamation/$reclamationId"
    }
    object AddDocumentChoice : Screen("add_document_choice")
    object OCRDocumentScan : Screen("ocr_document_scan")
    object SOS : Screen("sos")
    object SOSStatus : Screen("sos_status/{breakdownId}/{type}/{latitude}/{longitude}") {
        fun createRoute(breakdownId: String?, type: String, latitude: Double, longitude: Double) = 
            "sos_status/${breakdownId ?: "null"}/$type/$latitude/$longitude"
    }
    object SOSHistory : Screen("sos_history")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onVehiclesClick = { navController.navigate(Screen.Vehicles.route) },
                onEntretiensClick = { navController.navigate(Screen.Entretiens.route) },
                onDocumentsClick = { navController.navigate(Screen.Documents.route) },
                onGaragesClick = { navController.navigate(Screen.Garages.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Vehicles.route) {
            VehiclesScreen(
                onBackClick = { navController.popBackStack() },
                onVehicleClick = { vehicleId ->
                    navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                }
            )
        }

        composable(Screen.VehicleDetail.route) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")
            requireNotNull(vehicleId) { "vehicleId parameter wasn't found. Please make sure it's set!" }
            VehicleDetailScreen(
                vehicleId = vehicleId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Entretiens.route) {
            EntretiensScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Documents.route) {
            DocumentsScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                },
                onAddDocumentClick = { navController.navigate(Screen.AddDocumentChoice.route) }
            )
        }

        composable(Screen.DocumentDetail.route) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            requireNotNull(documentId) { "documentId parameter wasn't found. Please make sure it's set!" }
            DocumentDetailScreen(
                documentId = documentId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { docId -> navController.navigate(Screen.EditDocument.createRoute(docId)) }
            )
        }

        composable(Screen.AddDocument.route) {
            AddDocumentScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.EditDocument.route) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            requireNotNull(documentId) { "documentId parameter wasn't found. Please make sure it's set!" }
            AddDocumentScreen(
                documentId = documentId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Garages.route) {
            GaragesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onReclamationsClick = { navController.navigate(Screen.Reclamations.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onSOSClick = { navController.navigate(Screen.SOS.route) } // <-- navigation SOS
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Reclamations.route) {
            ReclamationsScreen(
                onBackClick = { navController.popBackStack() },
                onAddReclamationClick = { navController.navigate(Screen.AddReclamation.route) },
                onReclamationClick = { reclamationId ->
                    navController.navigate(Screen.ReclamationDetail.createRoute(reclamationId))
                }
            )
        }

        composable(Screen.AddReclamation.route) {
            AddReclamationScreen(
                onBackClick = { navController.popBackStack() },
                onReclamationCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.ReclamationDetail.route) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId")
            requireNotNull(reclamationId) { "reclamationId parameter wasn't found. Please make sure it's set!" }
            ReclamationDetailScreen(
                reclamationId = reclamationId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.EditReclamation.createRoute(id))
                }
            )
        }

        composable(Screen.EditReclamation.route) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId")
            requireNotNull(reclamationId) { "reclamationId parameter wasn't found. Please make sure it's set!" }
            EditReclamationScreen(
                reclamationId = reclamationId,
                onBackClick = { navController.popBackStack() },
                onReclamationUpdated = { navController.popBackStack() }
            )
        }

        composable(Screen.AddDocumentChoice.route) {
            AddDocumentChoiceScreen(
                onBackClick = { navController.popBackStack() },
                onOcrClick = { navController.navigate(Screen.OCRDocumentScan.route) },
                onManualEntryClick = { navController.navigate(Screen.AddDocument.route) }
            )
        }

        composable(Screen.OCRDocumentScan.route) {
            OCRDocumentScanScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SOS.route) {
            BreakdownSOSScreen(
                onBackClick = { navController.popBackStack() },
                onHistoryClick = { navController.navigate(Screen.SOSHistory.route) },
                onSOSSuccess = { breakdownId, type, lat, lon ->
                    navController.navigate(Screen.SOSStatus.createRoute(breakdownId, type, lat, lon)) {
                        popUpTo(Screen.SOS.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SOSHistory.route) {
            // Create a local Retrofit + Repo + ViewModelFactory so we can instantiate BreakdownViewModel
            val context = androidx.compose.ui.platform.LocalContext.current
            val retrofitLocal = androidx.compose.runtime.remember {
                val logging = okhttp3.logging.HttpLoggingInterceptor().apply { level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY }
                val client = okhttp3.OkHttpClient.Builder()
                    .addInterceptor(com.example.karhebti_android.data.api.AuthInterceptor(context))
                    .addInterceptor(logging)
                    .build()
                retrofit2.Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:3000/")
                    .client(client)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
            }
            val apiLocal = retrofitLocal.create(com.example.karhebti_android.network.BreakdownsApi::class.java)
            val repoLocal = com.example.karhebti_android.repository.BreakdownsRepository(apiLocal)
            val factoryLocal = com.example.karhebti_android.viewmodel.BreakdownViewModelFactory(repoLocal)
            val viewModel: com.example.karhebti_android.viewmodel.BreakdownViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factoryLocal)

             // Collect UI state
             val uiState by viewModel.uiState.collectAsState(initial = com.example.karhebti_android.viewmodel.BreakdownUiState.Idle)

             // Map BreakdownResponse data to HistoryItem list for the screen
             val items: List<HistoryItem> = when (uiState) {
                is com.example.karhebti_android.viewmodel.BreakdownUiState.Success -> {
                    val data = (uiState as com.example.karhebti_android.viewmodel.BreakdownUiState.Success).data
                    if (data is List<*>) {
                        data.filterIsInstance<com.example.karhebti_android.data.BreakdownResponse>().map { b ->
                            HistoryItem(
                                id = b.id,
                                type = b.type,
                                status = b.status,
                                date = b.createdAt ?: "-",
                                latitude = b.latitude,
                                longitude = b.longitude
                            )
                        }
                    } else emptyList()
                }
                else -> emptyList()
            }

            val callContext = context // capture LocalContext.current once

            BreakdownHistoryScreen(
                items = items,
                isLoading = uiState is com.example.karhebti_android.viewmodel.BreakdownUiState.Loading,
                onRefresh = { viewModel.fetchAllBreakdowns() },
                onBackClick = { navController.popBackStack() },
                onCall = { roomId ->
                    // Start the JitsiCallActivity using captured context
                    val intent = com.example.karhebti_android.jitsi.JitsiCallActivity.createIntent(callContext, roomId)
                    callContext.startActivity(intent)
                }
            )

            // Trigger initial load (fetch user breakdowns). If you have a cached userId, pass it here.
            // For simplicity we'll try to fetch all breakdowns (server must filter by auth). Use viewModel.fetchUserBreakdowns(userId) if you have an id.
            androidx.compose.runtime.LaunchedEffect(Unit) {
                // Fetch all breakdowns (backend should return only user's breakdowns when authenticated)
                viewModel.fetchAllBreakdowns()
            }
        }

        composable(Screen.SOSStatus.route) { backStackEntry ->
            val breakdownId = backStackEntry.arguments?.getString("breakdownId")?.takeIf { it != "null" }
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            
            SOSStatusScreen(
                breakdownId = breakdownId,
                type = type,
                latitude = latitude,
                longitude = longitude,
                onBackClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
