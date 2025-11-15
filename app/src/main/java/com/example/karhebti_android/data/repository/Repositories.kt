package com.example.karhebti_android.data.repository

import com.example.karhebti_android.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

// ==================== AUTH REPOSITORY ====================

class AuthRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun signup(
        nom: String,
        prenom: String,
        email: String,
        motDePasse: String,
        telephone: String
    ): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignupRequest(nom, prenom, email, motDePasse, telephone)
            val response = apiService.signup(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                RetrofitClient.setAuthToken(authResponse.accessToken)
                Resource.Success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur d'inscription: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun login(email: String, motDePasse: String): Resource<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, motDePasse)
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    RetrofitClient.setAuthToken(authResponse.accessToken)
                    Resource.Success(authResponse)
                } else {
                    Resource.Error("Email ou mot de passe incorrect")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.message}")
            }
        }

    suspend fun forgotPassword(email: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = ForgotPasswordRequest(email)
                val response = apiService.forgotPassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Erreur: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.message}")
            }
        }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = apiService.changePassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Erreur: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.message}")
            }
        }

    fun logout() {
        RetrofitClient.setAuthToken(null)
    }
}

// ==================== CAR REPOSITORY ====================

class CarRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getMyCars(): Resource<List<CarResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyCars()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des voitures")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun createCar(
        marque: String,
        modele: String,
        annee: Int,
        immatriculation: String,
        typeCarburant: String
    ): Resource<CarResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateCarRequest(marque, modele, annee, immatriculation, typeCarburant)
            val response = apiService.createCar(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun updateCar(
        id: String,
        marque: String? = null,
        modele: String? = null,
        annee: Int? = null,
        typeCarburant: String? = null,
        kilometrage: Int? = null,
        statut: String? = null,
        prochainEntretien: String? = null,
        joursProchainEntretien: Int? = null,
        imageUrl: String? = null
    ): Resource<CarResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateCarRequest(
                marque, modele, annee, typeCarburant,
                kilometrage, statut, prochainEntretien, joursProchainEntretien, imageUrl
            )
            val response = apiService.updateCar(id, request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la modification")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deleteCar(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteCar(id)

            if (response.isSuccessful) {
                Resource.Success(MessageResponse(message = "Véhicule supprimé avec succès"))
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== MAINTENANCE REPOSITORY ====================

class MaintenanceRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getMaintenances(): Resource<List<MaintenanceResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaintenances()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des entretiens")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getMaintenanceById(id: String): Resource<MaintenanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaintenance(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun createMaintenance(
        type: String,
        date: String,
        cout: Double,
        garage: String,
        voiture: String
    ): Resource<MaintenanceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateMaintenanceRequest(type, date, cout, garage, voiture)
            val response = apiService.createMaintenance(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création de l'entretien")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun updateMaintenance(id: String, request: UpdateMaintenanceRequest): Resource<MaintenanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateMaintenance(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la mise à jour")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deleteMaintenance(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMaintenance(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== GARAGE REPOSITORY ====================

class GarageRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getGarages(): Resource<List<GarageResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGarages()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des garages")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getGarageRecommendations(
        typePanne: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        rayon: Double? = null
    ): Resource<List<GarageRecommendation>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGarageRecommendations(typePanne, latitude, longitude, rayon)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des recommandations")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun createGarage(
        nom: String,
        adresse: String,
        typeService: List<String>,
        telephone: String,
        noteUtilisateur: Double? = null
    ): Resource<GarageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateGarageRequest(nom, adresse, typeService, telephone, noteUtilisateur)
            val response = apiService.createGarage(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création du garage")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== DOCUMENT REPOSITORY ====================

class DocumentRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getDocuments(): Resource<List<DocumentResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocuments()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des documents")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getDocumentById(id: String): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("DocumentRepository", "=== Getting document by ID ===")
            android.util.Log.d("DocumentRepository", "Document ID: $id")

            // WORKAROUND pour erreur 500 du backend
            // Récupérer tous les documents et filtrer celui qu'on veut
            android.util.Log.d("DocumentRepository", "Using workaround: getting all documents and filtering")

            val response = apiService.getDocuments()

            android.util.Log.d("DocumentRepository", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val documents = response.body()!!
                android.util.Log.d("DocumentRepository", "Total documents retrieved: ${documents.size}")

                val document = documents.find { it.id == id }

                if (document != null) {
                    android.util.Log.d("DocumentRepository", "Document found: ${document.type}")
                    Resource.Success(document)
                } else {
                    val errorMsg = "Document avec ID $id non trouvé dans la liste"
                    android.util.Log.e("DocumentRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur ${response.code()}: ${errorBody ?: "Impossible de récupérer les documents"}"
                android.util.Log.e("DocumentRepository", "ERROR: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
        }
    }

    suspend fun createDocument(request: CreateDocumentRequest, filePath: String? = null): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("DocumentRepository", "=== Creating document ===")
            android.util.Log.d("DocumentRepository", "Type: ${request.type}")
            android.util.Log.d("DocumentRepository", "DateEmission: ${request.dateEmission}")
            android.util.Log.d("DocumentRepository", "DateExpiration: ${request.dateExpiration}")
            android.util.Log.d("DocumentRepository", "Voiture: ${request.voiture}")
            android.util.Log.d("DocumentRepository", "Fichier: ${request.fichier}")
            android.util.Log.d("DocumentRepository", "FilePath: $filePath")

            // Pour l'instant, utiliser l'endpoint JSON normal
            // TODO: Utiliser multipart quand le backend sera configuré
            if (filePath.isNullOrBlank()) {
                android.util.Log.d("DocumentRepository", "Creating document without file")
            } else {
                android.util.Log.d("DocumentRepository", "Creating document with file: $filePath (stored locally)")
                val file = File(filePath)
                if (file.exists()) {
                    android.util.Log.d("DocumentRepository", "File size: ${file.length()} bytes")
                } else {
                    android.util.Log.e("DocumentRepository", "File does not exist: $filePath")
                }
            }

            // Utiliser l'endpoint JSON normal avec le chemin du fichier
            val response = apiService.createDocument(request)

            android.util.Log.d("DocumentRepository", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("DocumentRepository", "Document created successfully")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur ${response.code()}: ${errorBody ?: "Inconnue"}"
                android.util.Log.e("DocumentRepository", "ERROR DETAILS: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateDocument(id: String, request: UpdateDocumentRequest, filePath: String? = null): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("DocumentRepository", "=== Updating document ===")
            android.util.Log.d("DocumentRepository", "Document ID: $id")
            android.util.Log.d("DocumentRepository", "Type: ${request.type}")
            android.util.Log.d("DocumentRepository", "DateEmission: ${request.dateEmission}")
            android.util.Log.d("DocumentRepository", "DateExpiration: ${request.dateExpiration}")
            android.util.Log.d("DocumentRepository", "FilePath: $filePath")

            // Utiliser l'endpoint JSON normal (comme pour createDocument)
            // TODO: Utiliser multipart quand le backend sera configuré
            if (!filePath.isNullOrBlank()) {
                val file = File(filePath)
                if (file.exists()) {
                    android.util.Log.d("DocumentRepository", "File size: ${file.length()} bytes (stored locally)")
                } else {
                    android.util.Log.e("DocumentRepository", "File does not exist: $filePath")
                }
            }

            val response = apiService.updateDocument(id, request)

            android.util.Log.d("DocumentRepository", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("DocumentRepository", "Document updated successfully")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur ${response.code()}: ${errorBody ?: "Erreur de mise à jour"}"
                android.util.Log.e("DocumentRepository", "ERROR: $errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteDocument(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteDocument(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression du document")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== PART REPOSITORY ====================

class PartRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getParts(): Resource<List<PartResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getParts()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des pièces")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun createPart(
        nom: String,
        type: String,
        dateInstallation: String,
        kilometrageRecommande: Int,
        voiture: String
    ): Resource<PartResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreatePartRequest(nom, type, dateInstallation, kilometrageRecommande, voiture)
            val response = apiService.createPart(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création de la pièce")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deletePart(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePart(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== AI REPOSITORY ====================

class AIRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun reportRoadIssue(
        latitude: Double,
        longitude: Double,
        typeAnomalie: String,
        description: String
    ): Resource<RoadIssueResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ReportRoadIssueRequest(latitude, longitude, typeAnomalie, description)
            val response = apiService.reportRoadIssue(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors du signalement")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getDangerZones(
        latitude: Double? = null,
        longitude: Double? = null,
        rayon: Double? = null
    ): Resource<List<DangerZone>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDangerZones(latitude, longitude, rayon)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des zones dangereuses")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getMaintenanceRecommendations(
        voitureId: String
    ): Resource<MaintenanceRecommendationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = MaintenanceRecommendationRequest(voitureId)
            val response = apiService.getMaintenanceRecommendations(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des recommandations")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== RECLAMATION REPOSITORY ====================

class ReclamationRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getReclamations(): Resource<List<ReclamationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReclamations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des réclamations")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getReclamationById(id: String): Resource<ReclamationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReclamation(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération de la réclamation")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getMyReclamations(): Resource<List<ReclamationResponse>> = withContext(Dispatchers.IO) {
        try {
            // Le backend filtre automatiquement par utilisateur connecté via JWT
            val response = apiService.getReclamations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération de mes réclamations")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getReclamationsByGarage(garageId: String): Resource<List<ReclamationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReclamationsByGarage(garageId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des réclamations du garage")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getReclamationsByService(serviceId: String): Resource<List<ReclamationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReclamationsByService(serviceId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des réclamations du service")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun createReclamation(
        type: String,
        titre: String,
        message: String,
        garageId: String? = null,
        serviceId: String? = null
    ): Resource<ReclamationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateReclamationRequest(type, titre, message, garageId, serviceId)
            val response = apiService.createReclamation(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création de la réclamation")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun updateReclamation(id: String, titre: String?, message: String?): Resource<ReclamationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateReclamationRequest(titre, message)
            val response = apiService.updateReclamation(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la mise à jour de la réclamation")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deleteReclamation(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteReclamation(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression de la réclamation")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== USER REPOSITORY ====================

class UserRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getAllUsers(): Resource<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des utilisateurs")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getUser(id: String): Resource<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUser(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération de l'utilisateur")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun updateUser(id: String, nom: String?, prenom: String?, telephone: String?): Resource<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateUserRequest(nom, prenom, telephone)
            val response = apiService.updateUser(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la mise à jour de l'utilisateur")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deleteUser(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression de l'utilisateur")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun updateUserRole(id: String, role: String): Resource<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateRoleRequest(role)
            val response = apiService.updateUserRole(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la mise à jour du rôle")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

// ==================== NOTIFICATION REPOSITORY ====================

class NotificationRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getNotifications(): Resource<List<NotificationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des notifications")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getMyNotifications(): Resource<List<NotificationResponse>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("NotificationRepository", "Fetching my notifications...")
            // Utiliser getNotifications() car le backend filtre automatiquement par JWT
            val response = apiService.getNotifications()
            
            android.util.Log.d("NotificationRepository", "Response code: ${response.code()}")
            android.util.Log.d("NotificationRepository", "Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("NotificationRepository", "Successfully fetched ${response.body()!!.size} notifications")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération de mes notifications: ${response.code()} - $errorBody"
                android.util.Log.e("NotificationRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("NotificationRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
        }
    }

    suspend fun getUnreadNotifications(): Resource<List<NotificationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnreadNotifications()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des notifications non lues")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun markNotificationAsRead(id: String): Resource<NotificationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markNotificationAsRead(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors du marquage de la notification")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun markAllNotificationsAsRead(): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllNotificationsAsRead()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors du marquage de toutes les notifications")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun deleteNotification(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteNotification(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression de la notification")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.message}")
        }
    }
}

