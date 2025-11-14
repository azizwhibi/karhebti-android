package com.example.karhebti_android

import android.app.Application
import com.example.karhebti_android.data.api.RetrofitClient
import com.example.karhebti_android.data.preferences.TokenManager

class KarhebtiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RetrofitClient with context
        RetrofitClient.initialize(this)
        // Initialize TokenManager on app start
        TokenManager.getInstance(this).initializeToken()
    }
}
