package com.example.karhebti_android.data.repository

import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.preferences.TokenManager
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

class AuthRepository(
    private val apiService: KarhebtiApiService = RetrofitClient.apiService,
    private val tokenManager: TokenManager
) {

    suspend fun signup(
        nom: String,
        prenom: String,
        email: String,
        motDePasse: String,
        telephone: String
    ): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignupRequest(nom, prenom, email, motDePasse, telephone)
            val response = apiService.signup(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur d'inscription: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    // This verifies the OTP for a pending signup and completes account creation.
    suspend fun verifySignupOtp(email: String, otpCode: String): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = VerifySignupOtpRequest(email, otpCode)
            val response = apiService.verifySignupOtp(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur de vérification: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun login(email: String, motDePasse: String): Resource<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, motDePasse)
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Token will be saved by AuthViewModel via tokenManager.saveToken()
                    Resource.Success(authResponse)
                } else {
                    Resource.Error("Email ou mot de passe incorrect")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                    Resource.Error("Erreur: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
            }
        }

    suspend fun verifyOtp(email: String, otp: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = VerifyOtpRequest(email, otp)
                val response = apiService.verifyOtp(request)

                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Code OTP invalide ou expiré")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
            }
        }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = ResetPasswordRequest(email, otp, newPassword)
                val response = apiService.resetPassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Erreur: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
            }
        }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val userId = tokenManager.getUser()?.id ?: tokenManager.getToken()?.let { tokenManager.getUserIdFromToken(it) }
                    ?: return@withContext Resource.Error("Utilisateur non connecté")
                val request = ChangePasswordRequest(userId, currentPassword, newPassword)
                val response = apiService.changePassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Erreur: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
            }
        }

    // Logout is now handled entirely by TokenManager.clearAll()
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
        typeCarburant: String,
        kilometrage: Int? = null
    ): Resource<CarResponse> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("CarRepository", "Creating car: $marque $modele $annee $immatriculation $typeCarburant")
            // Create car without kilometrage first
            val request = CreateCarRequest(marque, modele, annee, immatriculation, typeCarburant)
            val response = apiService.createCar(request)

            android.util.Log.d("CarRepository", "Response code: ${response.code()}")
            android.util.Log.d("CarRepository", "Response message: ${response.message()}")
            android.util.Log.d("CarRepository", "Response successful: ${response.isSuccessful}")
            android.util.Log.d("CarRepository", "Response body: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                val createdCar = response.body()!!
                android.util.Log.d("CarRepository", "Success: Car created - $createdCar")

                // If kilometrage was provided, update the car with it
                if (kilometrage != null && kilometrage > 0) {
                    android.util.Log.d("CarRepository", "Updating car with kilometrage: $kilometrage")
                    val updateRequest = UpdateCarRequest(kilometrage = kilometrage)
                    val updateResponse = apiService.updateCar(createdCar.id, updateRequest)

                    if (updateResponse.isSuccessful && updateResponse.body() != null) {
                        android.util.Log.d("CarRepository", "Success: Car updated with kilometrage")
                        Resource.Success(updateResponse.body()!!)
                    } else {
                        // Car was created but kilometrage update failed - still return success
                        android.util.Log.w("CarRepository", "Car created but kilometrage update failed")
                        Resource.Success(createdCar)
                    }
                } else {
                    Resource.Success(createdCar)
                }
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
                Resource.Error("Erreur lors de la récupération des entretiens")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            val request = CreateMaintenanceRequest(type = type, title = type, date, dueAt = date, cout, garage, voiture)
            val response = apiService.createMaintenance(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création de l'entretien")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la récupération des garages")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la récupération des documents")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun createDocument(
        type: String,
        dateEmission: String,
        dateExpiration: String,
        fichier: String,
        voiture: String
    ): Resource<DocumentResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateDocumentRequest(type, dateEmission, dateExpiration, fichier, voiture)
            val response = apiService.createDocument(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la création du document")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteDocument(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteDocument(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la récupération des pièces")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            // ReportRoadIssueRequest(type, description, latitude, longitude)
            val request = ReportRoadIssueRequest(typeAnomalie, description, latitude, longitude)
            val response = apiService.reportRoadIssue(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors du signalement")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun getMaintenanceRecommendations(
        voitureId: String,
        mileage: Int,
        lastMaintenanceDate: String? = null
    ): Resource<MaintenanceRecommendationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = MaintenanceRecommendationRequest(
                carId = voitureId,
                mileage = mileage,
                lastMaintenanceDate = lastMaintenanceDate
            )
            val response = apiService.getMaintenanceRecommendations(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la récupération des recommandations")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la récupération des utilisateurs")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la mise à jour")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteUser(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(id)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }
}
