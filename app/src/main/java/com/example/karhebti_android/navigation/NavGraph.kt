package com.example.karhebti_android.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.ui.screens.AddGarageScreen
import com.example.karhebti_android.ui.screens.DocumentsScreen
import com.example.karhebti_android.ui.screens.EntretiensScreen
import com.example.karhebti_android.ui.screens.ForgotPasswordScreen
import com.example.karhebti_android.ui.screens.GarageDetailsScreen
import com.example.karhebti_android.ui.screens.GarageReservationsListScreen
import com.example.karhebti_android.ui.screens.GaragesScreen
import com.example.karhebti_android.ui.screens.HomeScreen
import com.example.karhebti_android.ui.screens.LoginScreen
import com.example.karhebti_android.ui.screens.ReservationScreen
import com.example.karhebti_android.ui.screens.ReservationsListScreen
import com.example.karhebti_android.ui.screens.SettingsScreen
import com.example.karhebti_android.ui.screens.SignUpScreen
import com.example.karhebti_android.ui.screens.UpdateGarageScreen
import com.example.karhebti_android.ui.screens.VehiclesScreen
import com.example.karhebti_android.viewmodel.GarageViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

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
    object EditGarage : Screen("edit_garage/{garageId}") {
        fun createRoute(garageId: String) = "edit_garage/$garageId"
    }
    object Settings : Screen("settings")
    object AddService : Screen("add_service/{garageId}") {
        fun createRoute(garageId: String) = "add_service/$garageId"
    }
    object Reservation : Screen("reservation/{garageId}") {
        fun createRoute(garageId: String) = "reservation/$garageId"
    }
    object ReservationsList : Screen("reservations")
    object GarageReservationsList : Screen("garage_reservations/{garageId}") {
        fun createRoute(garageId: String) = "garage_reservations/$garageId"
    }

}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current
    val garageViewModel: GarageViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as Application)
    )

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
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onAddGarageClick = { navController.navigate(Screen.AddGarage.route) },
                onGarageClick = { garageId ->
                    navController.navigate(Screen.GarageDetails.createRoute(garageId))
                },
                onModifyGarage = { garageId ->
                    navController.navigate(Screen.EditGarage.createRoute(garageId))
                }
                // Remove onDeleteGarage here!
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
                userRole = TokenManager.getInstance(context).getUser()?.role ?: "",
                navController = navController
            )
        }

        composable(
            route = Screen.EditGarage.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            UpdateGarageScreen(
                garageId = garageId,
                onBackClick = { navController.popBackStack() },
                onGarageUpdated = {
                    navController.popBackStack()
                }
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

        // User: Make a reservation for a garage
        composable(
            route = Screen.Reservation.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            ReservationScreen(
                garageId = garageId,
                navController = navController
            )
        }

// User: View their reservations
        composable(Screen.ReservationsList.route) {
            ReservationsListScreen(navController)
        }

// Garage owner: View all reservations for a specific garage
        composable(
            route = Screen.GarageReservationsList.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            GarageReservationsListScreen(
                garageId = garageId,
                navController = navController
            )
        }

    }
}