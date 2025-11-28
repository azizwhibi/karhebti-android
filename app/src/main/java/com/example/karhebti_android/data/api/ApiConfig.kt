package com.example.karhebti_android.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object ApiConfig {
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
}
