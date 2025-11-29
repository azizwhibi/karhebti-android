package com.example.karhebti_android.data

import com.google.gson.annotations.SerializedName

// Data class représentant une panne (breakdown)
// Alignée avec la réponse du backend (MongoDB ObjectId -> String)
data class BreakdownResponse(
    @SerializedName("_id")
    val id: String,

    @SerializedName("userId")
    val userId: String?,

    @SerializedName("vehicleId")
    val vehicleId: String?,

    val type: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,

    @SerializedName("assignedTo")
    val assignedTo: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)
