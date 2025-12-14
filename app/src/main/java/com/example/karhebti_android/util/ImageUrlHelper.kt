package com.example.karhebti_android.util

/**
 * Utility for handling image URLs and paths
 */
object ImageUrlHelper {
    // ⚙️ CONFIGURATION: Change this to match ApiConfig
    private const val USE_LOCAL_SERVER = false // Set to false to use Render backend (production)

    // Backend URLs
    private const val PRODUCTION_URL = "https://karhebti-backend.onrender.com"
    private const val LOCAL_URL = "http://10.0.2.2:3000" // For Android Emulator

    private val BASE_URL = if (USE_LOCAL_SERVER) LOCAL_URL else PRODUCTION_URL

    /**
     * Convert relative image path to absolute URL
     * @param imagePath Relative path like "/uploads/cars/filename.jpg" or absolute URL
     * @return Full absolute URL
     */
    fun getFullImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrEmpty()) {
            android.util.Log.w("ImageUrlHelper", "Image path is null or empty")
            return null
        }

        android.util.Log.d("ImageUrlHelper", "Input imagePath: $imagePath")

        // If it's already an absolute URL, return as is
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            android.util.Log.d("ImageUrlHelper", "Already absolute URL: $imagePath")
            return imagePath
        }

        // If it's a relative path, prepend base URL
        val fullUrl = if (imagePath.startsWith("/")) {
            "$BASE_URL$imagePath"
        } else {
            // Otherwise, assume it needs a leading slash
            "$BASE_URL/$imagePath"
        }

        android.util.Log.d("ImageUrlHelper", "Constructed full URL: $fullUrl")
        android.util.Log.d("ImageUrlHelper", "Using BASE_URL: $BASE_URL")
        return fullUrl
    }

    /**
     * Check if an image URL is valid and can be loaded
     */
    fun isValidImageUrl(imageUrl: String?): Boolean {
        return !imageUrl.isNullOrEmpty() &&
               (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/"))
    }

    /**
     * Get diagnostic information about image loading issues
     */
    fun getDiagnosticInfo(imagePath: String?): String {
        return buildString {
            appendLine("=== Image Loading Diagnostic ===")
            appendLine("Environment: ${if (USE_LOCAL_SERVER) "LOCAL" else "PRODUCTION"}")
            appendLine("Base URL: $BASE_URL")
            appendLine("Original Path: $imagePath")
            appendLine("Full URL: ${getFullImageUrl(imagePath)}")
            appendLine("Is Valid: ${isValidImageUrl(imagePath)}")
            appendLine("================================")
        }
    }
}
