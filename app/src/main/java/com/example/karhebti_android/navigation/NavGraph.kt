package com.example.karhebti_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.karhebti_android.data.preferences.TokenManager
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
    object Documents : Screen("documents")
    object Garages : Screen("garages")
    object AddGarage : Screen("add_garage")

    object GarageDetails : Screen("garage_detail/{garageId}") {
        fun createRoute(garageId: String) = "garage_detail/$garageId"
    }
    object Settings : Screen("settings")
    object AddService : Screen("add_service/{garageId}") {
        fun createRoute(garageId: String) = "add_service/$garageId"
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
                        launchSingleTop = true
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
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
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
        composable(Screen.Entretiens.route) {
            EntretiensScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Documents.route) {
            DocumentsScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Garages.route) {
            GaragesScreen(
                onBackClick = { navController.popBackStack() },
                onAddGarageClick = { navController.navigate(Screen.AddGarage.route) }
            )
        }
        composable(Screen.AddGarage.route) {
            AddGarageScreen(
                onBackClick = { navController.popBackStack() },
                onGarageAdded = { navController.popBackStack() }
            )
        }
        composable(Screen.GarageDetails.route) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            GarageDetailsScreen(
                garageId = garageId,
                onBackClick = { navController.popBackStack() },
                userRole = TokenManager.getInstance(navController.context).getUser()?.role ?: "",
                navController = navController // << pass it!
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Garages.route) {
            GaragesScreen(
                onBackClick = { navController.popBackStack() },
                onAddGarageClick = { navController.navigate(Screen.AddGarage.route) },
                onGarageClick = { garageId ->
                    navController.navigate(Screen.GarageDetails.createRoute(garageId))
                }
            )
        }
        composable(Screen.AddService.route) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            AddServiceScreen(
                garageId = garageId,
                onBackClick = { navController.popBackStack() },
                onServiceAdded = { navController.popBackStack() }
            )
        }




    }
}
