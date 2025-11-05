package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.AuthViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Observe forgot password state
    val forgotPasswordState by authViewModel.forgotPasswordState.observeAsState()
    var showConfirmation by remember { mutableStateOf(false) }

    // Handle success response
    LaunchedEffect(forgotPasswordState) {
        if (forgotPasswordState is Resource.Success) {
            showConfirmation = true
            delay(3000)
            showConfirmation = false
        }
    }

    // Validation function
    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "L'email est requis"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email invalide"
            else -> null
        }
        return emailError == null
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message
    LaunchedEffect(forgotPasswordState) {
        if (forgotPasswordState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = (forgotPasswordState as Resource.Error).message ?: "Erreur",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftWhite
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftWhite)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = "Entrez votre email pour réinitialiser votre mot de passe",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
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
                enabled = forgotPasswordState !is Resource.Loading
            )

            // Send Button
            Button(
                onClick = {
                    if (validateEmail()) {
                        authViewModel.forgotPassword(email)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepPurple
                ),
                enabled = forgotPasswordState !is Resource.Loading
            ) {
                if (forgotPasswordState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Envoyer instructions",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            // Confirmation Chip
            if (showConfirmation) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                        Text(
                            text = "Instructions envoyées avec succès !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    KarhebtiandroidTheme {
        ForgotPasswordScreen()
    }
}
