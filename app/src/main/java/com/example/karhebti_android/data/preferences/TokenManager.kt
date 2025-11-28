package com.example.karhebti_android.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.karhebti_android.data.api.RetrofitClient
import com.google.gson.Gson
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "karhebti_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_data"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        RetrofitClient.setAuthToken(token)
        // Also attempt to save into EncryptedSharedPreferences under "jwt_token" so
        // other readers (AuthInterceptor / readAnyToken) can find it.
        try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                appContext,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encryptedPrefs.edit().putString("jwt_token", token).apply()
        } catch (e: Exception) {
            // Not fatal: some devices/emulator configs might not support encrypted prefs at runtime
            android.util.Log.w("TokenManager", "Could not write to EncryptedSharedPreferences: ${e.message}")
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: UserData) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): UserData? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UserData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        RetrofitClient.setAuthToken(null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null && getUser() != null
    }

    fun isAdmin(): Boolean {
        return getUser()?.role == "admin"
    }

    // Initialize token on app start
    fun initializeToken() {
        val token = getToken()
        if (token != null) {
            RetrofitClient.setAuthToken(token)
        }
    }
}

data class UserData(
    val id: String?, // Changed to nullable to match backend response
    val email: String,
    val nom: String,
    val prenom: String,
    val role: String,
    val telephone: String? = null
)
