package com.example.karhebti_android.data.repository

import com.example.karhebti_android.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

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
                val errorMsg = "Erreur d\'inscription: ${response.code()} - $errorBody"
                android.util.Log.e("AuthRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("AuthRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Email ou mot de passe incorrect: ${response.code()} - $errorBody"
                    android.util.Log.e("AuthRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau: ${e.message}"
                android.util.Log.e("AuthRepository", errorMsg, e)
                Resource.Error(errorMsg)
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
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Erreur: ${response.code()} - $errorBody"
                    android.util.Log.e("AuthRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau: ${e.message}"
                android.util.Log.e("AuthRepository", errorMsg, e)
                Resource.Error(errorMsg)
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
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Erreur: ${response.code()} - $errorBody"
                    android.util.Log.e("AuthRepository", errorMsg)
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau: ${e.message}"
                android.util.Log.e("AuthRepository", errorMsg, e)
                Resource.Error(errorMsg)
            }
        }

    fun logout() {
        RetrofitClient.setAuthToken(null)
    }
}

class CarRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getMyCars(): Resource<List<CarResponse>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("CarRepository", "Fetching my cars...")
            val response = apiService.getMyCars()

            android.util.Log.d("CarRepository", "Get cars response code: ${response.code()}")
            android.util.Log.d("CarRepository", "Get cars response successful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CarRepository", "Successfully fetched ${response.body()!!.size} cars")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des voitures: ${response.code()} - $errorBody"
                android.util.Log.e("CarRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("CarRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
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
            android.util.Log.d("CarRepository", "Creating car: $marque $modele $annee $immatriculation $typeCarburant")
            val request = CreateCarRequest(marque, modele, annee, immatriculation, typeCarburant)
            val response = apiService.createCar(request)

            android.util.Log.d("CarRepository", "Response code: ${response.code()}")
            android.util.Log.d("CarRepository", "Response message: ${response.message()}")
            android.util.Log.d("CarRepository", "Response successful: ${response.isSuccessful}")
            android.util.Log.d("CarRepository", "Response body: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CarRepository", "Success: Car created - ${response.body()}")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création: ${response.code()} - $errorBody"
                android.util.Log.e("CarRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("CarRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
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
            android.util.Log.d("CarRepository", "Updating car: $id")
            val request = UpdateCarRequest(
                marque, modele, annee, typeCarburant,
                kilometrage, statut, prochainEntretien, joursProchainEntretien, imageUrl
            )
            val response = apiService.updateCar(id, request)

            android.util.Log.d("CarRepository", "Update response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CarRepository", "Car updated successfully")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la modification: ${response.code()} - $errorBody"
                android.util.Log.e("CarRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("CarRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteCar(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("CarRepository", "Deleting car: $id")
            val response = apiService.deleteCar(id)

            android.util.Log.d("CarRepository", "Delete response code: ${response.code()}")
            
            // Backend returns empty body (Unit/Void), so we just check if successful
            if (response.isSuccessful) {
                android.util.Log.d("CarRepository", "Car deleted successfully")
                // Create a success message for the UI
                val successMessage = MessageResponse(message = "Véhicule supprimé avec succès")
                Resource.Success(successMessage)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression: ${response.code()} - $errorBody"
                android.util.Log.e("CarRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("CarRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}

class MaintenanceRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getMaintenances(): Resource<List<MaintenanceResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaintenances()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des entretiens: ${response.code()} - $errorBody"
                android.util.Log.e("MaintenanceRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("MaintenanceRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun getMaintenanceById(id: String): Resource<MaintenanceResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("MaintenanceRepository", "Fetching maintenance by ID: $id")
            val response = apiService.getMaintenance(id)
            android.util.Log.d("MaintenanceRepository", "Response code: ${response.code()}")
            android.util.Log.d("MaintenanceRepository", "Response body: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("MaintenanceRepository", "Successfully fetched maintenance")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur: ${response.code()} - ${response.message()} - $errorBody"
                android.util.Log.e("MaintenanceRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("MaintenanceRepository", errorMsg, e)
            e.printStackTrace()
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création de l'entretien: ${response.code()} - $errorBody"
                android.util.Log.e("MaintenanceRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("MaintenanceRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateMaintenance(id: String, request: UpdateMaintenanceRequest): Resource<MaintenanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateMaintenance(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour: ${response.code()} - $errorBody"
                android.util.Log.e("MaintenanceRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("MaintenanceRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteMaintenance(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMaintenance(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression: ${response.code()} - $errorBody"
                android.util.Log.e("MaintenanceRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("MaintenanceRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}

class GarageRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getGarages(): Resource<List<GarageResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGarages()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des garages: ${response.code()} - $errorBody"
                android.util.Log.e("GarageRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("GarageRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des recommandations: ${response.code()} - $errorBody"
                android.util.Log.e("GarageRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("GarageRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création du garage: ${response.code()} - $errorBody"
                android.util.Log.e("GarageRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("GarageRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}

class DocumentRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getDocuments(): Resource<List<DocumentResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocuments()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                 val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des documents: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun getDocumentById(id: String): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocument(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération du document: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun createDocument(request: CreateDocumentRequest): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createDocument(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création du document: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateDocument(id: String, request: UpdateDocumentRequest): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateDocument(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour du document: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteDocument(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteDocument(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression du document: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    // Echeance CRUD
    suspend fun getEcheancesForDocument(documentId: String): Resource<List<EcheanceResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEcheancesForDocument(documentId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des échéances: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun createEcheance(request: CreateEcheanceRequest): Resource<EcheanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createEcheance(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création de l\'échéance: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateEcheance(id: String, request: UpdateEcheanceRequest): Resource<EcheanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateEcheance(id, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour de l\'échéance: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteEcheance(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteEcheance(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression de l\'échéance: ${response.code()} - $errorBody"
                android.util.Log.e("DocumentRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("DocumentRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}


class PartRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getParts(): Resource<List<PartResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getParts()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des pièces: ${response.code()} - $errorBody"
                android.util.Log.e("PartRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("PartRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la création de la pièce: ${response.code()} - $errorBody"
                android.util.Log.e("PartRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("PartRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deletePart(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePart(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression: ${response.code()} - $errorBody"
                android.util.Log.e("PartRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("PartRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}

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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors du signalement: ${response.code()} - $errorBody"
                android.util.Log.e("AIRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("AIRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des zones dangereuses: ${response.code()} - $errorBody"
                android.util.Log.e("AIRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("AIRepository", errorMsg, e)
            Resource.Error(errorMsg)
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
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des recommandations: ${response.code()} - $errorBody"
                android.util.Log.e("AIRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("AIRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}

class UserRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getAllUsers(): Resource<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllUsers()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des utilisateurs: ${response.code()} - $errorBody"
                android.util.Log.e("UserRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("UserRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateUser(
        id: String,
        nom: String? = null,
        prenom: String? = null,
        telephone: String? = null
    ): Resource<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateUserRequest(nom, prenom, telephone)
            val response = apiService.updateUser(id, request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour: ${response.code()} - $errorBody"
                android.util.Log.e("UserRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("UserRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun deleteUser(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la suppression: ${response.code()} - $errorBody"
                android.util.Log.e("UserRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("UserRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }

    suspend fun updateUserRole(id: String, role: String): Resource<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateRoleRequest(role)
            val response = apiService.updateUserRole(id, request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour du rôle: ${response.code()} - $errorBody"
                android.util.Log.e("UserRepository", errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur réseau: ${e.message}"
            android.util.Log.e("UserRepository", errorMsg, e)
            Resource.Error(errorMsg)
        }
    }
}
