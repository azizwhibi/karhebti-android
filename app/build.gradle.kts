plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.karhebti_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.karhebti_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)

    // LiveData integration for Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")

    // Lifecycle-aware state collection for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")

    // Firebase Cloud Messaging (Push Notifications)
    implementation("com.google.firebase:firebase-messaging:23.2.1")
    implementation("com.google.firebase:firebase-analytics:21.3.0")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")

    // Android Security - Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // OpenStreetMap (osmdroid) - Alternative gratuite et open source à Google Maps
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Play Services Location toujours nécessaire pour obtenir la position GPS
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.retrofit.converter.gson)
    // NOTE: the native Jitsi Meet SDK line below caused Gradle resolution failures
    // (missing transitive artifact com.yqritc:android-scalablevideoview:1.0.4).
    // We are using a lightweight Custom Tab fallback to open meet.jit.si, so the
    // SDK dependency is removed to keep the build stable. If you want the native
    // SDK integration later, I can add the correct repos/versions and resolve
    // transitive artifacts.
    // implementation("org.jitsi.react:jitsi-meet-sdk:4.0.0")
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}