package com.example.karhebti_android.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.karhebti_android.data.api.AuthApiService
import com.example.karhebti_android.data.api.LoginRequest
import com.example.karhebti_android.data.api.AuthResponse
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.preferences.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * AuthRepository: Centralizes authentication logic
 */
class AuthRepository(
    private val authApiService: AuthApiService,
    private val context: Context
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    fun login(email: String, motDePasse: String): Flow<Result<AuthResponse>> = flow {
        try {
            Log.d(TAG, "Attempting login for: $email")

            val request = LoginRequest(email = email, motDePasse = motDePasse)
            val response = authApiService.login(request)

            when {
                response.isSuccessful -> {
                    response.body()?.let { body ->
                        // body est de type AuthResponse (voir DTOs.kt)
                        if (body.accessToken.isNotEmpty()) {
                            Log.d(TAG, "✅ Login successful")

                            // Save token to secure storage
                            saveTokenSecurely(body.accessToken)

                            // Save user info
                            body.user.let { user ->
                                TokenManager.getInstance(context).saveUser(
                                    UserData(
                                        id = user.id,
                                        email = user.email,
                                        nom = user.nom,
                                        prenom = user.prenom,
                                        role = user.role,
                                        telephone = user.telephone
                                    )
                                )
                                Log.d(TAG, "✅ User saved: ${user.email}")
                                cacheUserId(user.id ?: "")
                            }

                            emit(Result.success(body))
                        } else {
                            Log.e(TAG, "❌ Login response missing token")
                            emit(Result.failure(Exception("Login failed: empty token")))
                        }
                    } ?: emit(Result.failure(Exception("Empty response body")))
                }
                response.code() == 400 -> {
                    val message = "Validation error"
                    Log.e(TAG, "❌ 400 Bad Request: $message")
                    emit(Result.failure(Exception(message)))
                }
                response.code() == 401 -> {
                    val message = "Invalid email or password"
                    Log.e(TAG, "❌ 401 Unauthorized: $message")
                    emit(Result.failure(Exception(message)))
                }
                else -> {
                    val message = "Login failed with code ${response.code()}"
                    Log.e(TAG, "❌ API Error: $message")
                    emit(Result.failure(Exception(message)))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during login: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    fun logout(): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "Logging out...")
            clearTokenSecurely()
            TokenManager.getInstance(context).clearAll()
            clearUserId()
            Log.d(TAG, "✅ Logout successful")
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during logout: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    fun isLoggedIn(): Boolean {
        return getTokenSecurely() != null && TokenManager.getInstance(context).getUser() != null
    }

    fun getCachedUserId(): String? {
        return try {
            val prefs = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
            prefs.getString("cached_user_id", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached user ID: ${e.message}")
            null
        }
    }

    // ===== PRIVATE HELPERS =====

    private fun saveTokenSecurely(token: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedPrefs.edit().putString("jwt_token", token).apply()
            TokenManager.getInstance(context).saveToken(token)
            Log.d(TAG, "✅ Token saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token: ${e.message}", e)
            TokenManager.getInstance(context).saveToken(token)
        }
    }

    private fun getTokenSecurely(): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedPrefs.getString("jwt_token", null)
                ?: TokenManager.getInstance(context).getToken()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token: ${e.message}")
            TokenManager.getInstance(context).getToken()
        }
    }

    private fun clearTokenSecurely() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedPrefs.edit().remove("jwt_token").apply()
            Log.d(TAG, "✅ Token cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing token: ${e.message}")
        }
    }

    private fun cacheUserId(userId: String) {
        try {
            if (userId.isNotBlank()) {
                val prefs = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
                prefs.edit().putString("cached_user_id", userId).apply()
                Log.d(TAG, "✅ User ID cached: $userId")
            } else {
                Log.w(TAG, "cacheUserId called with blank id - skipping cache")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error caching user ID: ${e.message}")
        }
    }

    private fun clearUserId() {
        try {
            val prefs = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
            prefs.edit().remove("cached_user_id").apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user ID: ${e.message}")
        }
    }
}
