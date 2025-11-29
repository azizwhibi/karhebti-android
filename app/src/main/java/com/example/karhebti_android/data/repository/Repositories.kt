package com.example.karhebti_android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.karhebti_android.data.api.*
import com.google.gson.Gson
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
                // Add logging to see what we're receiving

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
                    // Add logging to see what we're receiving

                    android.util.Log.d("AuthRepository", "🔍 Login - Raw ID field: ${authResponse.user.id}")
                    android.util.Log.d("AuthRepository", "🔍 Login - Raw ID type: ${authResponse.user.id?.javaClass?.simpleName}")
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

class RepairBayRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getRepairBaysByGarage(garageId: String): Resource<List<RepairBayResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRepairBaysByGarage(garageId)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error("Erreur: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Resource.Error("Erreur réseau: ${e.localizedMessage}")
            }
        }

    suspend fun getAvailableRepairBays(
        garageId: String,
        date: String,
        heureDebut: String,
        heureFin: String
    ): Resource<List<RepairBayResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableRepairBays(
                garageId = garageId,
                date = date,
                heureDebut = heureDebut,
                heureFin = heureFin
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun createRepairBay(
        garageId: String,
        bayNumber: Int,
        name: String,
        heureOuverture: String,
        heureFermeture: String,
        isActive: Boolean = true
    ): Resource<RepairBayResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateRepairBayRequest(
                garageId = garageId,
                bayNumber = bayNumber,
                name = name,
                heureOuverture = heureOuverture,
                heureFermeture = heureFermeture,
                isActive = isActive
            )
            val response = apiService.createRepairBay(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteRepairBay(bayId: String): Resource<MessageResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteRepairBay(bayId)
                if (response.isSuccessful) {
                    Resource.Success(MessageResponse("Créneau supprimé"))
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
        telephone: String,
        noteUtilisateur: Double = 0.0,
        heureOuverture: String? = null,
        heureFermeture: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        numberOfBays: Int? = null // ✅ NOUVEAU paramètre
    ): Resource<GarageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateGarageRequest(
                nom = nom,
                adresse = adresse,
                telephone = telephone,
                noteUtilisateur = noteUtilisateur,
                heureOuverture = heureOuverture,
                heureFermeture = heureFermeture,
                latitude = latitude,
                longitude = longitude,
                numberOfBays = numberOfBays // ✅ Ajouter
            )
            val response = apiService.createGarage(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }


    suspend fun updateGarage(
        garageId: String,
        nom: String? = null,
        adresse: String? = null,
        telephone: String? = null,
        noteUtilisateur: Double? = null,
        heureOuverture: String? = null,
        heureFermeture: String? = null,
        latitude: Double? = null,    // Ajoutez
        longitude: Double? = null     // Ajoutez
    ): Resource<GarageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateGarageRequest(
                nom = nom,
                adresse = adresse,
                telephone = telephone,
                noteUtilisateur = noteUtilisateur,
                heureOuverture = heureOuverture,
                heureFermeture = heureFermeture,
                latitude = latitude,      // Ajoutez
                longitude = longitude     // Ajoutez
            )
            val response = apiService.updateGarage(garageId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteGarage(garageId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteGarage(garageId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau")
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

class ServiceRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {
    suspend fun createService(
        type: String,
        coutMoyen: Double,
        dureeEstimee: Int,
        garageId: String
    ): Resource<ServiceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateServiceRequest(type, coutMoyen, dureeEstimee, garageId)
            val response = apiService.createService(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun getServicesByGarage(garageId: String): Resource<List<ServiceResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getServicesByGarage(garageId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur API: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun updateService(
        serviceId: String,
        type: String,
        coutMoyen: Double,
        dureeEstimee: Int
    ): Resource<ServiceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateServiceRequest(type, coutMoyen, dureeEstimee)
            val response = apiService.updateService(serviceId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erreur lors de la mise à jour du service")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteService(serviceId: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteService(serviceId)
            if (response.isSuccessful) {
                val body = response.body()
                // Some servers return empty body for 204/200 DELETE, handle both:
                Resource.Success(body ?: MessageResponse("Service supprimé avec succès"))
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur lors de la suppression du service: ${errorBody ?: response.message()}")
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



class ReservationRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun getReservations(): Resource<List<ReservationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReservations()
            if (response.isSuccessful && response.body() != null) {
                // The API returns a ReservationListResponse wrapper
                val body = response.body()!!
                Resource.Success(body.reservations) // Extract the reservations list
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur lors de la récupération des réservations: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }
    suspend fun getMyReservations(): Resource<List<ReservationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyReservations()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(body.reservations) // Extract the reservations list
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur lors de la récupération de vos réservations: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun createReservation(
        garageId: String,
        date: String,
        heureDebut: String,
        heureFin: String,
        status: String = "en_attente",
        services: List<String>? = null,
        commentaires: String? = null
    ): Resource<ReservationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateReservationRequest(
                garageId = garageId,
                date = date,
                heureDebut = heureDebut,
                heureFin = heureFin,
                status = status,
                services = services,
                commentaires = commentaires
            )
            val response = apiService.createReservation(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun updateReservationStatus(
        reservationId: String,
        status: String
    ): Resource<ReservationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateReservationStatusRequest(status)
            val response = apiService.updateReservationStatus(reservationId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun updateReservation(
        reservationId: String,
        date: String? = null,
        heureDebut: String? = null,
        heureFin: String? = null,
        status: String? = null,
        services: List<String>? = null,
        commentaires: String? = null,
        isPaid: Boolean? = null
    ): Resource<ReservationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateReservationRequest(
                date = date,
                heureDebut = heureDebut,
                heureFin = heureFin,
                services = services,
                commentaires = commentaires,
                status = status,
                isPaid = isPaid
            )
            val response = apiService.updateReservation(reservationId, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error("Erreur: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun deleteReservation(id: String): Resource<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteReservation(id)
            if (response.isSuccessful) {
                Resource.Success(MessageResponse("Réservation supprimée"))
            } else {
                Resource.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau")
        }
    }
}

class OsmRepository(private val apiService: KarhebtiApiService = RetrofitClient.apiService) {

    suspend fun searchAddress(query: String): Resource<List<OsmLocationSuggestion>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAddress(query)

            if (response.isSuccessful && response.body() != null) {
                val suggestions = response.body()!!.map { location ->
                    OsmLocationSuggestion(
                        displayName = location.display_name,
                        latitude = location.lat.toDouble(),
                        longitude = location.lon.toDouble(),
                        address = AddressDetails(
                            road = location.address?.road,
                            city = location.address?.city,
                            country = location.address?.country,
                            postcode = location.address?.postcode
                        )
                    )
                }
                Resource.Success(suggestions)
            } else {
                Resource.Error("Erreur lors de la recherche d'adresse")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): Resource<OsmLocationSuggestion> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.reverseGeocode(lat, lon)

            if (response.isSuccessful && response.body() != null) {
                val location = response.body()!!
                val suggestion = OsmLocationSuggestion(
                    displayName = location.display_name,
                    latitude = location.lat.toDouble(),
                    longitude = location.lon.toDouble(),
                    address = AddressDetails(
                        road = location.address?.road,
                        city = location.address?.city,
                        country = location.address?.country,
                        postcode = location.address?.postcode
                    )
                )
                Resource.Success(suggestion)
            } else {
                Resource.Error("Erreur lors du géocodage inverse")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }
}







