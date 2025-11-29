package com.example.karhebti_android.data.api

import android.content.Context
<<<<<<< HEAD
import com.example.karhebti_android.data.preferences.TokenManager
import okhttp3.Interceptor
=======
>>>>>>> origin/documents1
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object ApiConfig {
<<<<<<< HEAD
    const val BASE_URL = "http://192.168.1.190:27017/" // MongoDB URL
    const val MONGODB_URL = "mongodb://192.168.1.190:27017/karhebti"
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.190:3000/" // Backend API URL

    private var context: Context? = null
    private var retrofit: Retrofit? = null
    private var _apiService: KarhebtiApiService? = null
    private var tokenManager: TokenManager? = null

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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setLenient()
            // Note: FlexibleUserObjectDeserializer and FlexibleCarObjectDeserializer are applied
            // via @JsonAdapter annotations on specific fields in ConversationResponse, not globally
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        _apiService = retrofit!!.create(KarhebtiApiService::class.java)
    }

    val apiService: KarhebtiApiService
        get() {
            if (_apiService == null) {
                throw IllegalStateException("RetrofitClient not initialized. Call initialize(context) first in Application.onCreate()")
            }
            return _apiService!!
        }
=======
    const val BASE_URL = "http://10.0.2.2:27017/" // For Android Emulator
    const val MONGODB_URL = "mongodb://localhost:27017/karhebti"
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/" // For Android Emulator
    private var context: Context? = null
    private var retrofit: Retrofit? = null

    /**
     * Initialize RetrofitClient with application context
     * Must be called from Application.onCreate() or MainActivity.onCreate()
     */
    fun initialize(appContext: Context) {
        context = appContext
        retrofit = null // Force rebuild with context
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun getOkHttpClient(): OkHttpClient {
        val appContext = context ?: throw IllegalStateException(
            "RetrofitClient not initialized. Call RetrofitClient.initialize(context) first"
        )

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(appContext))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setLenient()
        .registerTypeAdapter(NotificationItemResponse::class.java, NotificationItemDeserializer())
        .registerTypeAdapter(NotificationsResponse::class.java, NotificationsResponseDeserializer())
        .create()

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    val apiService: KarhebtiApiService
        get() = getRetrofit().create(KarhebtiApiService::class.java)

    val notificationApiService: NotificationApiService
        get() = getRetrofit().create(NotificationApiService::class.java)

    val authApiService: AuthApiService
        get() = getRetrofit().create(AuthApiService::class.java)

    // Legacy compatibility
    @Deprecated("Use AuthInterceptor instead")
    fun setAuthToken(token: String?) {
        // This is handled by AuthInterceptor now
    }
>>>>>>> origin/documents1
}
