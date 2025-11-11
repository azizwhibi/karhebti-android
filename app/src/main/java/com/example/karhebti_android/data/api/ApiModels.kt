package com.example.karhebti_android.data.api

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
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

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
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
    val typeCarburant: String? = null,
    val kilometrage: Int? = null,
    val statut: String? = null,
    val prochainEntretien: String? = null,
    val joursProchainEntretien: Int? = null,
    val imageUrl: String? = null
)

data class CarResponse(
    @SerializedName("_id")
    val id: String,
    val marque: String,
    val modele: String,
    val annee: Int,
    val immatriculation: String,
    val typeCarburant: String,
    val kilometrage: Int? = null,
    val statut: String? = null, // "BON", "ATTENTION", "URGENT"
    val prochainEntretien: String? = null,
    val joursProchainEntretien: Int? = null,
    val imageUrl: String? = null,
    @JsonAdapter(FlexibleUserDeserializer::class)
    val user: String? = null, // Can be either user ID string or user object
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
    val cout: Double = 0.0,
    val status: String? = "pending",
    @JsonAdapter(FlexibleGarageDeserializer::class)
    val garage: String? = null, // garage ID or extracted from object
    @JsonAdapter(FlexibleCarDeserializer::class)
    val voiture: String? = null, // car ID or extracted from object
    val user: String? = null, // User ID who created the maintenance
    val createdAt: Date? = null,
    val updatedAt: Date? = null
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
    val fichier: String? = null,
    val description: String? = null,
    val etat: String? = null
)

data class DocumentResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val dateEmission: Date,
    val dateExpiration: Date,
    val fichier: String,
    @JsonAdapter(FlexibleCarDeserializer::class)
    val voiture: String? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val description: String? = null,
    val etat: String? = null
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
    val voiture: String? = null,
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
    val niveauDanger: String
)

data class MaintenanceRecommendationRequest(
    val voitureId: String
)

data class MaintenanceRecommendation(
    val type: String,
    val priorite: String,
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
    @JsonAdapter(FlexibleGarageDeserializer::class)
    val garage: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

// Reclamation (Feedback) DTOs
data class CreateReclamationRequest(
    val type: String,
    val titre: String,
    val message: String,
    val garageId: String? = null,
    val serviceId: String? = null
)

data class UpdateReclamationRequest(
    val titre: String? = null,
    val message: String? = null
)

data class ReclamationResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val titre: String,
    val message: String,
    val user: UserResponse? = null,
    val garage: GarageResponse? = null,
    val service: ServiceResponse? = null,
    val createdAt: Date,
    val updatedAt: Date
)

// Generic Response
data class MessageResponse(
    val message: String
)

// Notification DTOs
data class NotificationResponse(
    @SerializedName("_id")
    val id: String,
    val titre: String,
    val message: String,
    val type: String, // echeance, maintenance, info, alerte
    val user: UserResponse? = null,
    val document: DocumentResponse? = null,
    val lu: Boolean = false,
    val dateEcheance: Date? = null,
    val createdAt: Date,
    val updatedAt: Date
)

data class CreateNotificationRequest(
    val titre: String,
    val message: String,
    val type: String,
    val documentId: String? = null,
    val dateEcheance: String? = null
)

data class UpdateNotificationRequest(
    val lu: Boolean
)

// Error Response
data class ErrorResponse(
    val statusCode: Int,
    val message: List<String>,
    val error: String
)

