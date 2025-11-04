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
                Resource.Error("Erreur d'inscription: ${response.message()}")
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
                    RetrofitClient.setAuthToken(authResponse.accessToken)
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

    fun logout() {
        RetrofitClient.setAuthToken(null)
    }
}

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
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
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
                Resource.Error("Erreur lors de la création de la voiture")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun updateCar(
        id: String,
        marque: String? = null,
        modele: String? = null,
        annee: Int? = null,
        typeCarburant: String? = null
    ): Resource<CarResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateCarRequest(marque, modele, annee, typeCarburant)
            val response = apiService.updateCar(id, request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la modification")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteCar(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteCar(id)

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
            val request = ReportRoadIssueRequest(latitude, longitude, typeAnomalie, description)
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
