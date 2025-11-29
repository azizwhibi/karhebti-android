    package com.example.karhebti_android.data.api

    import com.google.gson.annotations.SerializedName
    import java.util.Date
    import kotlinx.serialization.Serializable

    // --- Auth DTOs ---
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
        @SerializedName("access_token") val accessToken: String,
        val user: UserResponse
    )

    data class ForgotPasswordRequest(val email: String)
    data class ResetPasswordRequest(val token: String, val nouveauMotDePasse: String)


    // --- User DTOs ---
    data class UserResponse(
        @SerializedName("_id") val id: IdWrapper?,
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
    data class UpdateRoleRequest(val role: String)

    data class IdWrapper(@SerializedName("\$oid") val oid: String)

    // --- Car DTOs ---
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
        @SerializedName("_id") val id: IdWrapper,
        val marque: String,
        val modele: String,
        val annee: Int,
        val immatriculation: String,
        val typeCarburant: String,
        val user: IdWrapper? = null,
        val createdAt: Date? = null,
        val updatedAt: Date? = null
    )

    // --- Maintenance DTOs ---
    data class CreateMaintenanceRequest(
        val type: String,
        val date: String,
        val cout: Double,
        val garage: String, // Garage ID as String
        val voiture: String
    )

    data class UpdateMaintenanceRequest(
        val type: String? = null,
        val date: String? = null,
        val cout: Double? = null
    )

    data class MaintenanceResponse(
        @SerializedName("_id") val id: String,
        val type: String,
        val date: Date,
        val cout: Double,
        val garage: GarageResponse? = null,
        val voiture: CarResponse? = null,
        val createdAt: Date,
        val updatedAt: Date
    )

    // --- Document DTOs ---
    data class CreateDocumentRequest(
        val type: String,
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
        @SerializedName("_id") val id: String,
        val type: String,
        val dateEmission: Date,
        val dateExpiration: Date,
        val fichier: String,
        val voiture: CarResponse? = null,
        val createdAt: Date,
        val updatedAt: Date
    )

    // --- Part DTOs ---
    data class CreatePartRequest(
        val nom: String,
        val type: String,
        val dateInstallation: String,
        val kilometrageRecommande: Int,
        val voiture: String
    )
    data class PartResponse(
        @SerializedName("_id") val id: String,
        val nom: String,
        val type: String,
        val dateInstallation: Date,
        val kilometrageRecommande: Int,
        val voiture: CarResponse? = null,
        val createdAt: Date,
        val updatedAt: Date
    )

    // --- AI DTOs ---
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
        @SerializedName("_id") val id: String,
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
    data class MaintenanceRecommendationRequest(val voitureId: String)
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

    data class MessageResponse(val message: String)
    data class ErrorResponse(
        val statusCode: Int,
        val message: List<String>,
        val error: String
    )

    // --- Garage DTOs ---
    data class GarageResponse(
        @SerializedName("_id") val id: String,
        val nom: String,
        val adresse: String,
        val telephone: String,
        val noteUtilisateur: Double,
        val serviceTypes: List<String>? = null,
        val heureOuverture: String? = null,
        val heureFermeture: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        @SerializedName("numberOfBays") val numberOfBays: Int? = null, // ✅ NOUVEAU
        val createdAt: Date? = null,
        val updatedAt: Date? = null
    )

    data class CreateGarageRequest(
        val nom: String,
        val adresse: String,
        val telephone: String,
        val noteUtilisateur: Double?,
        val heureOuverture: String?,
        val heureFermeture: String?,
        val latitude: Double? = null,
        val longitude: Double? = null,
        @SerializedName("numberOfBays") val numberOfBays: Int? = null // ✅ NOUVEAU
    )

    data class UpdateGarageRequest(
        val nom: String? = null,
        val adresse: String? = null,
        val telephone: String? = null,
        val noteUtilisateur: Double? = null,
        val heureOuverture: String? = null,
        val heureFermeture: String? = null,
        val latitude: Double? = null,    // Add this
        val longitude: Double? = null    // Add this
    )

    // --- Service DTOs ---
    data class ServiceResponse(
        @SerializedName("_id") val id: String,
        val type: String,
        val coutMoyen: Double,
        val dureeEstimee: Int,
        val garage: Any?,  // Accept both String or object
        val createdAt: Date? = null,
        val updatedAt: Date? = null
    )
    @Serializable
    data class UpdateServiceRequest(
        val type: String,
        val coutMoyen: Double,
        val dureeEstimee: Int
    )
    data class CreateServiceRequest(
        val type: String,
        val coutMoyen: Double,
        val dureeEstimee: Int,
        val garage: String
    )
    data class GarageServiceRequest(
        val type: String,
        val coutMoyen: Double,
        val dureeEstimee: Int
    )

    // --- Reservation DTOs ---
    data class CreateReservationRequest(
        @SerializedName("userId") val userId: String? = null,
        val email: String? = null,
        @SerializedName("garageId") val garageId: String,
        val date: String,
        @SerializedName("heureDebut") val heureDebut: String,
        @SerializedName("heureFin") val heureFin: String,
        val services: List<String>? = null,
        val status: String = "en_attente",
        val commentaires: String? = null
    )
    data class UpdateReservationRequest(
        // Remove these, do not send in PATCH request:
        // @SerializedName("userId") val userId: String? = null,
        // @SerializedName("garageId") val garageId: String? = null,
        val date: String? = null,
        @SerializedName("heureDebut") val heureDebut: String? = null,
        @SerializedName("heureFin") val heureFin: String? = null,
        val services: List<String>? = null,
        val status: String? = null,
        val commentaires: String? = null,
        val isPaid: Boolean? = null
    )

    data class UpdateReservationStatusRequest(
        val status: String
    )
    // --- Reservation DTOs ---
    data class ReservationResponse(
        @SerializedName("_id") val id: String,
        @SerializedName("userId") val userId: Any?,
        @SerializedName("garageId") val garageId: Any?,
        @SerializedName("repairBayId") val repairBayId: Any? = null, // ✅ NOUVEAU
        val date: Date,
        @SerializedName("heureDebut") val heureDebut: String,
        @SerializedName("heureFin") val heureFin: String,
        val services: List<String>? = null,
        val status: String,
        val commentaires: String? = null,
        @SerializedName("updatedBy") val updatedBy: Any? = null,
        @SerializedName("isPaid") val isPaid: Boolean = false,
        @SerializedName("totalAmount") val totalAmount: Double = 0.0,
        @SerializedName("createdAt") val createdAt: Date? = null,
        @SerializedName("updatedAt") val updatedAt: Date? = null
    ) {
        // Existing helper methods...
        fun getUserId(): String? {
            return when (userId) {
                is String -> userId
                is UserResponse -> userId.id?.oid ?: userId.email
                is Map<*, *> -> (userId["_id"] as? String) ?: (userId["id"] as? String)
                else -> null
            }
        }

        fun getUserName(): String? {
            return when (userId) {
                is UserResponse -> "${userId.prenom} ${userId.nom}"
                is Map<*, *> -> {
                    val prenom = userId["prenom"] as? String ?: ""
                    val nom = userId["nom"] as? String ?: ""
                    "$prenom $nom".trim()
                }
                else -> null
            }
        }

        fun getUserEmail(): String? {
            return when (userId) {
                is UserResponse -> userId.email
                is Map<*, *> -> userId["email"] as? String
                else -> null
            }
        }

        fun getGarageId(): String? {
            return when (garageId) {
                is String -> garageId
                is GarageResponse -> garageId.id
                is Map<*, *> -> (garageId["_id"] as? String) ?: (garageId["id"] as? String)
                else -> null
            }
        }

        fun getGarageName(): String? {
            return when (garageId) {
                is GarageResponse -> garageId.nom
                is Map<*, *> -> garageId["nom"] as? String
                else -> null
            }
        }

        fun getGarageAddress(): String? {
            return when (garageId) {
                is GarageResponse -> garageId.adresse
                is Map<*, *> -> garageId["adresse"] as? String
                else -> null
            }
        }

        fun getGaragePhone(): String? {
            return when (garageId) {
                is GarageResponse -> garageId.telephone
                is Map<*, *> -> garageId["telephone"] as? String
                else -> null
            }
        }

        fun getUpdatedByName(): String? {
            return when (updatedBy) {
                is UserResponse -> "${updatedBy.prenom} ${updatedBy.nom}"
                is Map<*, *> -> {
                    val prenom = updatedBy["prenom"] as? String ?: ""
                    val nom = updatedBy["nom"] as? String ?: ""
                    "$prenom $nom".trim()
                }
                else -> null
            }
        }

        // ✅ NOUVEAUX helper methods pour repair bay
        fun getRepairBayId(): String? {
            return when (repairBayId) {
                is String -> repairBayId
                is RepairBayResponse -> repairBayId.id
                is Map<*, *> -> (repairBayId["_id"] as? String) ?: (repairBayId["id"] as? String)
                else -> null
            }
        }

        fun getRepairBayName(): String? {
            return when (repairBayId) {
                is RepairBayResponse -> repairBayId.name
                is Map<*, *> -> repairBayId["name"] as? String
                else -> null
            }
        }

        fun getRepairBayNumber(): Int? {
            return when (repairBayId) {
                is RepairBayResponse -> repairBayId.bayNumber
                is Map<*, *> -> repairBayId["bayNumber"] as? Int
                else -> null
            }
        }
    }


    data class ReservationListResponse(
        val reservations: List<ReservationResponse>,
        val total: Int? = null,
        val page: Int? = null,
        val limit: Int? = null,
        val totalPages: Int? = null
    )
    data class PopulatedReservationResponse(
        @SerializedName("_id") val id: String,
        val userId: UserResponse?,
        val garageId: GarageResponse?,
        val date: String,
        val heureDebut: String,
        val heureFin: String,
        val status: String,
        val createdAt: Date?,
        val updatedAt: Date?
    )
    data class LocationSuggestion(
        val display_name: String,
        val lat: String,
        val lon: String,
        val address: OsmAddress?
    )

    data class OsmAddress(
        val road: String?,
        val city: String?,
        val country: String?,
        val postcode: String?
    )

    // For frontend usage
    data class OsmLocationSuggestion(
        val displayName: String,
        val latitude: Double,
        val longitude: Double,
        val address: AddressDetails
    )

    data class AddressDetails(
        val road: String?,
        val city: String?,
        val country: String?,
        val postcode: String?
    )

    // --- Repair Bay DTOs ---
    data class RepairBayResponse(
        @SerializedName("_id") val id: String,
        @SerializedName("garageId") val garageId: String,
        @SerializedName("bayNumber") val bayNumber: Int,
        val name: String,
        @SerializedName("heureOuverture") val heureOuverture: String,
        @SerializedName("heureFermeture") val heureFermeture: String,
        @SerializedName("isActive") val isActive: Boolean = true,
        @SerializedName("createdAt") val createdAt: Date? = null,
        @SerializedName("updatedAt") val updatedAt: Date? = null
    )

    data class CreateRepairBayRequest(
        @SerializedName("garageId") val garageId: String,
        @SerializedName("bayNumber") val bayNumber: Int,
        val name: String,
        @SerializedName("heureOuverture") val heureOuverture: String,
        @SerializedName("heureFermeture") val heureFermeture: String,
        @SerializedName("isActive") val isActive: Boolean = true
    )
