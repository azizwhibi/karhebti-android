package com.example.karhebti_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.karhebti_android.ui.screens.*

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
                onAddDocumentClick = { navController.navigate(Screen.AddDocument.route) }
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
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
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
    }
}

