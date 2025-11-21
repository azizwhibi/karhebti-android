package com.example.karhebti_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.karhebti_android.data.api.SignupData
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.screens.*
import com.example.karhebti_android.viewmodel.AuthViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import android.app.Application

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object VerifyOtp : Screen("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/$email"
    }
    object ResetPassword : Screen("reset_password/{email}/{otp}") {
        fun createRoute(email: String, otp: String) = "reset_password/$email/$otp"
    }
    object EmailVerification : Screen("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }
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
    object Garages : Screen("garages")
    object Settings : Screen("settings")
    // Marketplace screens
    object MarketplaceBrowse : Screen("marketplace_browse")
    object MyListings : Screen("my_listings")
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object PendingSwipes : Screen("pending_swipes")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    // CRITICAL: Create a SHARED MarketplaceViewModel that persists across navigation
    val marketplaceViewModel: com.example.karhebti_android.viewmodel.MarketplaceViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as Application)
    )

    var pendingSignupPerform by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.observeAsState()

    // When a signup has been triggered after email verification, navigate to Home on success
    LaunchedEffect(authState, pendingSignupPerform) {
        if (pendingSignupPerform && authState is Resource.Success) {
            // Clear pending signup from previous back stack entry if present
            navController.previousBackStackEntry?.savedStateHandle?.remove<SignupData>("pendingSignup")
            // Navigate to home, clear back stack
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
            pendingSignupPerform = false
        }
    }

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
                onSignupInitiated = { signupData: SignupData ->
                    // Store pending signup data so EmailVerification can access it
                    navController.currentBackStackEntry?.savedStateHandle?.set("pendingSignup", signupData)
                    // Initiate signup (sends OTP) then navigate to verification screen on success
                    authViewModel.signupInitiate(signupData.nom, signupData.prenom, signupData.email, signupData.password, signupData.telephone)
                    // Navigate to EmailVerification immediately; EmailVerificationScreen will show resend/cooldown
                    navController.navigate(Screen.EmailVerification.createRoute(signupData.email))
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSignup = { navController.navigate(Screen.SignUp.route) },
                onNavigateToOtpVerification = { email ->
                    navController.navigate(Screen.VerifyOtp.createRoute(email))
                }
            )
        }

        composable(Screen.VerifyOtp.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            requireNotNull(email) { "email parameter wasn't found. Please make sure it's set!" }

            VerifyOtpScreen(
                email = email,
                onBackClick = { navController.popBackStack() },
                onOtpVerified = { verifiedEmail, otp ->
                    navController.navigate(Screen.ResetPassword.createRoute(verifiedEmail, otp))
                }
            )
        }

        composable(Screen.ResetPassword.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            val otp = backStackEntry.arguments?.getString("otp")
            requireNotNull(email) { "email parameter wasn't found. Please make sure it's set!" }
            requireNotNull(otp) { "otp parameter wasn't found. Please make sure it's set!" }
            ResetPasswordScreen(
                email = email,
                otp = otp,
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.EmailVerification.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            requireNotNull(email) { "email parameter wasn't found. Please make sure it's set!" }

            // Check if this is a signup flow (has pending signup data)
            val pendingSignup = navController.previousBackStackEntry?.savedStateHandle?.get<SignupData>("pendingSignup")

            EmailVerificationScreen(
                email = email,
                onBackClick = { navController.popBackStack() },
                onVerificationSuccess = {
                    // After successful email verification, check if there is a pending signup to perform.
                    if (pendingSignup != null) {
                        // This is a signup flow - create the account by verifying signup OTP
                        pendingSignupPerform = true
                        // Remove pending signup from saved state
                        navController.previousBackStackEntry?.savedStateHandle?.remove<SignupData>("pendingSignup")
                        // Note: EmailVerificationScreen will supply the OTP code via the verifyForSignup callback
                        // The actual completion (token save & navigation) will happen when AuthViewModel.authState emits success
                    } else {
                        // No pending signup; just navigate to Home
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                // When in signup flow, EmailVerificationScreen will call this to verify signup OTP
                verifyForSignup = { verifiedEmail, code ->
                    // Mark that we are performing a pending signup so NavGraph listens for authState success
                    pendingSignupPerform = true
                    // Trigger the signup verification which will return AuthResponse and set authState
                    authViewModel.verifySignupOtp(verifiedEmail, code)
                },
                // Provide a resend callback for signup flow that re-initiates the signup using saved pendingSignup
                resendForSignup = { _email ->
                    val saved = navController.previousBackStackEntry?.savedStateHandle?.get<SignupData>("pendingSignup")
                    saved?.let { sd ->
                        authViewModel.signupInitiate(sd.nom, sd.prenom, sd.email, sd.password, sd.telephone)
                    }
                },
                isSignupFlow = pendingSignup != null
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onVehiclesClick = { navController.navigate(Screen.Vehicles.route) },
                onEntretiensClick = { navController.navigate(Screen.Entretiens.route) },
                onDocumentsClick = { navController.navigate(Screen.Documents.route) },
                onGaragesClick = { navController.navigate(Screen.Garages.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onMarketplaceClick = { navController.navigate(Screen.MarketplaceBrowse.route) },
                onMyListingsClick = { navController.navigate(Screen.MyListings.route) },
                onConversationsClick = { navController.navigate(Screen.Conversations.route) },
                onPendingSwipesClick = { navController.navigate(Screen.PendingSwipes.route) }
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
                onBackClick = { navController.popBackStack() },
                onMaintenanceClick = { maintenanceId ->
                    navController.navigate(Screen.MaintenanceDetail.createRoute(maintenanceId))
                }
            )
        }

        composable(Screen.MaintenanceDetail.route) { backStackEntry ->
            val maintenanceId = backStackEntry.arguments?.getString("maintenanceId")
            requireNotNull(maintenanceId) { "maintenanceId parameter wasn't found. Please make sure it's set!" }
            MaintenanceDetailsScreen(
                maintenanceId = maintenanceId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Documents.route) {
            DocumentsScreen(onBackClick = { navController.popBackStack() })
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
                }
            )
        }

        // Marketplace navigation
        composable(Screen.MarketplaceBrowse.route) {
            MarketplaceBrowseScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                }
            )
        }

        composable(Screen.MyListings.route) {
            MyListingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onBackClick = { navController.popBackStack() },
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            requireNotNull(conversationId) { "conversationId parameter wasn't found. Please make sure it's set!" }
            ChatScreen(
                conversationId = conversationId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PendingSwipes.route) {
            PendingSwipesScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                }
            )
        }
    }
}
