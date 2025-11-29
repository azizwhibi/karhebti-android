package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.AuthViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Observe auth state
    val authState by authViewModel.authState.observeAsState()

    // Handle auth response with detailed logging
    LaunchedEffect(authState) {
        android.util.Log.d("LoginScreen", "Auth State Changed: $authState")
        when (val state = authState) {
            is Resource.Success -> {
                android.util.Log.d("LoginScreen", "Login Success - User: ${state.data?.user?.email}")
                try {
                    onLoginSuccess()
                    android.util.Log.d("LoginScreen", "Navigation triggered successfully")
                } catch (e: Exception) {
                    android.util.Log.e("LoginScreen", "Navigation error: ${e.message}", e)
                }
            }
            is Resource.Error -> {
                android.util.Log.e("LoginScreen", "Login Error: ${state.message}")
            }
            is Resource.Loading -> {
                android.util.Log.d("LoginScreen", "Login Loading...")
            }
            else -> {
                android.util.Log.d("LoginScreen", "Auth state is null or initial")
            }
        }
    }

    // Validation functions
    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "L'email est requis"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email invalide"
            else -> null
        }
        return emailError == null
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "Le mot de passe est requis"
            password.length < 6 -> "Le mot de passe doit contenir au moins 6 caractères"
            else -> null
        }
        return passwordError == null
    }

    fun validateAll(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message
    LaunchedEffect(authState) {
        if (authState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = (authState as Resource.Error).message ?: "Erreur de connexion",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DeepPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Headline
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email TextField
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        if (emailError != null) validateEmail()
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = InputBackground,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = if (emailError != null) AlertRed else InputBorder,
                        focusedBorderColor = if (emailError != null) AlertRed else InputBorderFocused,
                        unfocusedTextColor = InputText,
                        focusedTextColor = InputText,
                        cursorColor = DeepPurple,
                        unfocusedLabelColor = if (emailError != null) AlertRed else TextSecondary,
                        focusedLabelColor = if (emailError != null) AlertRed else DeepPurple,
                        errorBorderColor = AlertRed,
                        errorLabelColor = AlertRed
                    ),
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = AlertRed) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = authState !is Resource.Loading
                )

                // Password TextField
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) validatePassword()
                    },
                    label = { Text("Mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = InputBackground,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = if (passwordError != null) AlertRed else InputBorder,
                        focusedBorderColor = if (passwordError != null) AlertRed else InputBorderFocused,
                        unfocusedTextColor = InputText,
                        focusedTextColor = InputText,
                        cursorColor = DeepPurple,
                        unfocusedLabelColor = if (passwordError != null) AlertRed else TextSecondary,
                        focusedLabelColor = if (passwordError != null) AlertRed else DeepPurple,
                        errorBorderColor = AlertRed,
                        errorLabelColor = AlertRed
                    ),
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it, color = AlertRed) } },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Cacher" else "Afficher",
                                tint = TextSecondary
                            )
                        }
                    },
                    singleLine = true,
                    enabled = authState !is Resource.Loading
                )

                // Login Button
                Button(
                    onClick = {
                        if (validateAll()) {
                            authViewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepPurple
                    ),
                    enabled = authState !is Resource.Loading
                ) {
                    if (authState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Se connecter",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }

                // Forgot Password Link
                Text(
                    text = "Mot de passe oublié ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepPurple,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            // Sign Up Navigation
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Pas encore membre ? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "S'inscrire",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepPurple,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    KarhebtiandroidTheme {
        LoginScreen()
    }
}
