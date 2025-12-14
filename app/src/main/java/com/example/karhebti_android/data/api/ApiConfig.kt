package com.example.karhebti_android.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.network.BreakdownsApi
import java.util.concurrent.TimeUnit
import okhttp3.Dns

object ApiConfig {
    // ⚙️ CONFIGURATION: Change this to switch between environments
    private const val USE_LOCAL_SERVER = false // Set to false to use Render backend (production)

    // Backend URLs
    private const val PRODUCTION_URL = "https://karhebti-backend.onrender.com/"
    private const val LOCAL_URL = "http://10.0.2.2:3000/" // For Android Emulator
    // If using physical device, use your computer's IP: "http://192.168.1.x:3000/"

    val BASE_URL: String
        get() = if (USE_LOCAL_SERVER) LOCAL_URL else PRODUCTION_URL

    const val MONGODB_URL = "mongodb://192.168.1.190:27017/karhebti"
}

object RetrofitClient {
    // Use the configured BASE_URL from ApiConfig
    private val BASE_URL get() = ApiConfig.BASE_URL

    private var context: Context? = null
    private var tokenManager: TokenManager? = null
    private var retrofit: Retrofit? = null
    private var _apiService: KarhebtiApiService? = null

    /**
     * Initialize RetrofitClient with application context
     * Must be called from Application.onCreate() or MainActivity.onCreate()
     */
    fun initialize(appContext: Context) {
        synchronized(this) {
            if (context == null) {
                context = appContext
                tokenManager = TokenManager.getInstance(appContext)
                buildRetrofit()
            }
        }
    }

    private fun buildRetrofit() {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            android.util.Log.d("AuthInterceptor", "=== Processing request to: ${chain.request().url} ===")
            android.util.Log.d("AuthInterceptor", "TokenManager instance: ${tokenManager != null}")

            // Get token from TokenManager - it will be fetched from SharedPreferences at request time
            val token = tokenManager?.getToken()

            android.util.Log.d("AuthInterceptor", "Token retrieved: ${token != null}")
            if (token != null) {
                android.util.Log.d("AuthInterceptor", "Token value (first 20 chars): ${token.take(20)}...")
                android.util.Log.d("AuthInterceptor", "Token length: ${token.length}")
            }

            if (token != null && token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                android.util.Log.d("AuthInterceptor", "✓ Authorization header added successfully")
            } else {
                android.util.Log.e("AuthInterceptor", "✗ NO TOKEN AVAILABLE - Request will be unauthorized!")
            }

            requestBuilder.addHeader("Content-Type", "application/json")

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS) // Increased to 120s for Render.com cold start
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // Enable automatic retry
            .dns(Dns.SYSTEM) // Use system DNS
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setLenient()
            .registerTypeAdapter(NotificationItemResponse::class.java, NotificationItemDeserializer())
            .registerTypeAdapter(NotificationsResponse::class.java, NotificationsResponseDeserializer())
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        _apiService = retrofit!!.create(KarhebtiApiService::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {
        val appContext = context ?: throw IllegalStateException(
            "RetrofitClient not initialized. Call RetrofitClient.initialize(context) first"
        )

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(appContext))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setLenient()
                .registerTypeAdapter(NotificationItemResponse::class.java, NotificationItemDeserializer())
                .registerTypeAdapter(NotificationsResponse::class.java, NotificationsResponseDeserializer())
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    val apiService: KarhebtiApiService
        get() {
            if (_apiService == null) {
                throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first in Application.onCreate()")
            }
            return _apiService!!
        }

    val notificationApiService: NotificationApiService
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first in Application.onCreate()")
            }
            return retrofit!!.create(NotificationApiService::class.java)
        }

    val authApiService: AuthApiService
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first in Application.onCreate()")
            }
            return retrofit!!.create(AuthApiService::class.java)
        }

    val breakdownsApiService: BreakdownsApi
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first in Application.onCreate()")
            }
            return retrofit!!.create(BreakdownsApi::class.java)
        }

    // Legacy compatibility
    @Deprecated("Use AuthInterceptor instead")
    fun setAuthToken(token: String?) {
        // This is handled by AuthInterceptor now
    }
}
