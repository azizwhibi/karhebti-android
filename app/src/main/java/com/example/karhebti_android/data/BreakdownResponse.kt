package com.example.karhebti_android.data

import com.google.gson.annotations.SerializedName

// Data class représentant une panne (breakdown)
// Alignée avec la réponse du backend (MongoDB ObjectId -> String)
data class BreakdownResponse(
    @SerializedName("_id")
    val id: String,

    val userId: String?,

    val vehicleId: String?,

    val type: String = "",
    val status: String = "pending",
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,

    val assignedTo: String?,

    // Informations du garage assigné (si populé par le backend)
    val assignedToDetails: AssignedGarageDetails? = null,

    val createdAt: String?,

    val updatedAt: String?
)

// Détails du garage/utilisateur assigné
data class AssignedGarageDetails(
    @SerializedName("_id")
    val id: String?,
    val nom: String?,
    val prenom: String?,
    val telephone: String?,
    val latitude: Double?,
    val longitude: Double?
)

// Wrapper for breakdowns list response
data class BreakdownsListResponse(
    val breakdowns: List<BreakdownResponse>? = null,
    val data: List<BreakdownResponse>? = null
) {
    fun toList(): List<BreakdownResponse> {
        return breakdowns ?: data ?: emptyList()
    }
}
