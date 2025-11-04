package com.example.karhebti_android.data.api

import com.google.gson.annotations.SerializedName
import java.util.Date

// Auth DTOs
data class SignupRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val motDePasse: String,
    val telephone: String
)

data class LoginRequest(
    val email: String,
    val motDePasse: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    val user: UserResponse
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val nouveauMotDePasse: String
)

// User DTOs
data class UserResponse(
    @SerializedName("_id")
    val id: String?,
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String?,
    val role: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

data class UpdateUserRequest(
    val nom: String? = null,
    val prenom: String? = null,
    val telephone: String? = null
)

data class UpdateRoleRequest(
    val role: String
)

// Car DTOs
data class CreateCarRequest(
    val marque: String,
    val modele: String,
    val annee: Int,
    val immatriculation: String,
    val typeCarburant: String
)

data class UpdateCarRequest(
    val marque: String? = null,
    val modele: String? = null,
    val annee: Int? = null,
    val typeCarburant: String? = null
)

data class CarResponse(
    @SerializedName("_id")
    val id: String,
    val marque: String,
    val modele: String,
    val annee: Int,
    val immatriculation: String,
    val typeCarburant: String,
    val user: UserResponse? = null,
    val createdAt: Date,
    val updatedAt: Date
)

// Maintenance DTOs
data class CreateMaintenanceRequest(
    val type: String, // vidange, révision, réparation
    val date: String, // ISO 8601 format
    val cout: Double,
    val garage: String, // Garage ID
    val voiture: String // Car ID
)

data class UpdateMaintenanceRequest(
    val type: String? = null,
    val date: String? = null,
    val cout: Double? = null
)

data class MaintenanceResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val date: Date,
    val cout: Double,
    val garage: GarageResponse? = null,
    val voiture: CarResponse? = null,
    val createdAt: Date,
    val updatedAt: Date
)

// Garage DTOs
data class CreateGarageRequest(
    val nom: String,
    val adresse: String,
    val typeService: List<String>,
    val telephone: String,
    val noteUtilisateur: Double? = null
)

data class UpdateGarageRequest(
    val nom: String? = null,
    val adresse: String? = null,
    val typeService: List<String>? = null,
    val telephone: String? = null,
    val noteUtilisateur: Double? = null
)

data class GarageResponse(
    @SerializedName("_id")
    val id: String,
    val nom: String,
    val adresse: String,
    val typeService: List<String>,
    val telephone: String,
    val noteUtilisateur: Double,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

// Document DTOs
data class CreateDocumentRequest(
    val type: String, // assurance, carte grise, contrôle technique
    val dateEmission: String,
    val dateExpiration: String,
    val fichier: String,
    val voiture: String
)

data class UpdateDocumentRequest(
    val type: String? = null,
    val dateEmission: String? = null,
    val dateExpiration: String? = null,
    val fichier: String? = null
)

data class DocumentResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val dateEmission: Date,
    val dateExpiration: Date,
    val fichier: String,
    val voiture: CarResponse? = null,
    val createdAt: Date,
    val updatedAt: Date
)

// Part DTOs
data class CreatePartRequest(
    val nom: String,
    val type: String,
    val dateInstallation: String,
    val kilometrageRecommande: Int,
    val voiture: String
)

data class PartResponse(
    @SerializedName("_id")
    val id: String,
    val nom: String,
    val type: String,
    val dateInstallation: Date,
    val kilometrageRecommande: Int,
    val voiture: CarResponse? = null,
    val createdAt: Date,
    val updatedAt: Date
)

// AI DTOs
data class ReportRoadIssueRequest(
    val latitude: Double,
    val longitude: Double,
    val typeAnomalie: String,
    val description: String
)

data class RoadIssueResponse(
    val message: String,
    val roadIssue: RoadIssue
)

data class RoadIssue(
    @SerializedName("_id")
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val typeAnomalie: String,
    val description: String,
    val signalements: Int,
    val createdAt: Date
)

data class DangerZone(
    val id: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val signalements: Int,
    val niveauDanger: String // faible, moyen, élevé, très élevé
)

data class MaintenanceRecommendationRequest(
    val voitureId: String
)

data class MaintenanceRecommendation(
    val type: String,
    val priorite: String, // faible, moyenne, haute
    val raison: String,
    val estimationCout: Double,
    val delaiRecommande: String
)

data class MaintenanceRecommendationResponse(
    val voiture: VoitureInfo,
    val recommandations: List<MaintenanceRecommendation>,
    val scoreEntretien: Int
)

data class VoitureInfo(
    val marque: String,
    val modele: String,
    val annee: Int,
    val age: Int
)

data class GarageRecommendation(
    val id: String,
    val nom: String,
    val adresse: String,
    val telephone: String,
    val note: Double,
    val services: List<String>,
    val distanceEstimee: String,
    val recommande: Boolean
)

// Service DTOs
data class CreateServiceRequest(
    val type: String,
    val coutMoyen: Double,
    val dureeEstimee: Int,
    val garage: String
)

data class ServiceResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val coutMoyen: Double,
    val dureeEstimee: Int,
    val garage: GarageResponse? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

// Generic Response
data class MessageResponse(
    val message: String
)

// Error Response
data class ErrorResponse(
    val statusCode: Int,
    val message: List<String>,
    val error: String
)
