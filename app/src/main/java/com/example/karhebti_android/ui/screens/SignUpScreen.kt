package com.example.karhebti_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.ui.theme.*
import com.example.karhebti_android.viewmodel.AuthViewModel
import com.example.karhebti_android.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Observe auth state
    val authState by authViewModel.authState.observeAsState()

    // Handle auth response
    LaunchedEffect(authState) {
        when (authState) {
            is Resource.Success -> {
                onSignUpSuccess()
            }
            else -> {}
        }
    }

    // Validation functions
    fun validateName(): Boolean {
        nameError = when {
            name.isBlank() -> "Le nom est requis"
            name.length < 2 -> "Le nom doit contenir au moins 2 caractères"
            else -> null
        }
        return nameError == null
    }

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

    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Veuillez confirmer le mot de passe"
            confirmPassword != password -> "Les mots de passe ne correspondent pas"
            else -> null
        }
        return confirmPasswordError == null
    }

    fun validateAll(): Boolean {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        return isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message
    LaunchedEffect(authState) {
        if (authState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = (authState as Resource.Error).message ?: "Erreur d'inscription",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un compte") },
                navigationIcon = {
                    IconButton(onClick = onLoginClick) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) validateName()
                },
                label = { Text("Nom complet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputBackground,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = if (nameError != null) AlertRed else InputBorder,
                    focusedBorderColor = if (nameError != null) AlertRed else InputBorderFocused,
                    unfocusedTextColor = InputText,
                    focusedTextColor = InputText,
                    cursorColor = DeepPurple,
                    unfocusedLabelColor = if (nameError != null) AlertRed else TextSecondary,
                    focusedLabelColor = if (nameError != null) AlertRed else DeepPurple,
                    errorBorderColor = AlertRed,
                    errorLabelColor = AlertRed
                ),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = AlertRed) } },
                singleLine = true,
                enabled = authState !is Resource.Loading
            )

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

            // Telephone TextField (Optional)
            OutlinedTextField(
                value = telephone,
                onValueChange = { telephone = it },
                label = { Text("Téléphone (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputBackground,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = InputBorder,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedTextColor = InputText,
                    focusedTextColor = InputText,
                    cursorColor = DeepPurple,
                    unfocusedLabelColor = TextSecondary,
                    focusedLabelColor = DeepPurple
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = authState !is Resource.Loading
            )

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) validatePassword()
                    if (confirmPassword.isNotEmpty()) validateConfirmPassword()
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
                supportingText = passwordError?.let { { Text(it, color = AlertRed) } }
                    ?: { Text("Au moins 6 caractères", color = TextSecondary) },
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

            // Confirm Password TextField
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError != null) validateConfirmPassword()
                },
                label = { Text("Confirmer mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputBackground,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = if (confirmPasswordError != null) AlertRed else InputBorder,
                    focusedBorderColor = if (confirmPasswordError != null) AlertRed else InputBorderFocused,
                    unfocusedTextColor = InputText,
                    focusedTextColor = InputText,
                    cursorColor = DeepPurple,
                    unfocusedLabelColor = if (confirmPasswordError != null) AlertRed else TextSecondary,
                    focusedLabelColor = if (confirmPasswordError != null) AlertRed else DeepPurple,
                    errorBorderColor = AlertRed,
                    errorLabelColor = AlertRed
                ),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it, color = AlertRed) } },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Cacher" else "Afficher",
                            tint = TextSecondary
                        )
                    }
                },
                singleLine = true,
                enabled = authState !is Resource.Loading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sign Up Button
            Button(
                onClick = {
                    if (validateAll()) {
                        // Split name into nom and prenom (simple split by space)
                        val nameParts = name.trim().split(" ", limit = 2)
                        val nom = nameParts.getOrNull(0) ?: ""
                        val prenom = nameParts.getOrNull(1) ?: nameParts.getOrNull(0) ?: ""

                        authViewModel.signup(nom, prenom, email, password, telephone)
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
                        text = "S'inscrire",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Déjà membre ? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepPurple,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    KarhebtiandroidTheme {
        SignUpScreen()
    }
}
