package com.example.karhebti_android.utils

import java.util.Locale

/**
 * Utility functions for GPS distance calculations
 */
object DistanceUtils {

    /**
     * Calculate real distance between two GPS coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Format distance for display
     * @param distanceKm Distance in kilometers
     * @return Formatted string (e.g., "1.5 km" or "850 m")
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 0.01 -> "< 10 m"
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            else -> String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    /**
     * Estimate time of arrival based on distance
     * @param distanceKm Distance in kilometers
     * @param speedKmh Average speed in km/h (default 40 km/h in city)
     * @return Formatted ETA string
     */
    fun estimateETA(distanceKm: Double, speedKmh: Double = 40.0): String {
        val hours = distanceKm / speedKmh
        val minutes = (hours * 60).toInt()

        return when {
            minutes < 1 -> "< 1 min"
            minutes < 60 -> "$minutes min"
            else -> {
                val h = minutes / 60
                val m = minutes % 60
                if (m == 0) "$h h" else "$h h $m min"
            }
        }
    }
}

