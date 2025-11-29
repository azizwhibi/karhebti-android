package com.example.karhebti_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.preferences.TokenManager
import com.example.karhebti_android.data.preferences.UserData
import com.example.karhebti_android.data.repository.*
import kotlinx.coroutines.launch
import com.example.karhebti_android.data.repository.Resource
import com.example.karhebti_android.data.api.ReservationResponse

    // Auth ViewModel
    class AuthViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = AuthRepository()
        private val tokenManager = TokenManager.getInstance(application)

        private val _authState = MutableLiveData<Resource<AuthResponse>>()
        val authState: LiveData<Resource<AuthResponse>> = _authState

        private val _forgotPasswordState = MutableLiveData<Resource<MessageResponse>>()
        val forgotPasswordState: LiveData<Resource<MessageResponse>> = _forgotPasswordState

        init {
            tokenManager.initializeToken()
        }

        fun login(email: String, password: String) {
            _authState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.login(email, password)
                    _authState.value = result

                    if (result is Resource.Success) {
                        try {
                            val userData = result.data!!
                            // FIX: Always use email as user ID for now
                            val userId = userData.user.email // Use email directly

                            tokenManager.saveToken(userData.accessToken)
                            tokenManager.saveUser(
                                UserData(
                                    id = userId, // This will be the email
                                    email = userData.user.email,
                                    nom = userData.user.nom,
                                    prenom = userData.user.prenom,
                                    role = userData.user.role,
                                    telephone = userData.user.telephone ?: ""
                                )
                            )

                            android.util.Log.d("AuthViewModel", "✅ User logged in with EMAIL as ID: $userId")
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "❌ Error saving user: ${e.message}", e)
                            _authState.value = Resource.Error("Erreur lors de la sauvegarde: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    _authState.value = Resource.Error("Erreur de connexion: ${e.localizedMessage}")
                }
            }
        }

        fun signup(nom: String, prenom: String, email: String, password: String, telephone: String) {
            _authState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.signup(nom, prenom, email, password, telephone)
                    _authState.value = result

                    if (result is Resource.Success) {
                        // FIX: Always use email as user ID for now
                        val userId = result.data!!.user.email // Use email directly

                        tokenManager.saveToken(result.data.accessToken)
                        tokenManager.saveUser(
                            UserData(
                                id = userId, // This will be the email
                                email = result.data.user.email,
                                nom = result.data.user.nom,
                                prenom = result.data.user.prenom,
                                role = result.data.user.role,
                                telephone = result.data.user.telephone ?: ""
                            )
                        )

                        android.util.Log.d("AuthViewModel", "✅ User signed up with EMAIL as ID: $userId")
                    }
                } catch (e: Exception) {
                    _authState.value = Resource.Error("Erreur d'inscription: ${e.localizedMessage}")
                }
            }
        }

        fun forgotPassword(email: String) {
            _forgotPasswordState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.forgotPassword(email)
                _forgotPasswordState.value = result
            }
        }

        fun logout() {
            repository.logout()
            tokenManager.clearAll()
        }

        fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
        fun getCurrentUser(): UserData? = tokenManager.getUser()
    }

    // Car ViewModel
    class CarViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = CarRepository()
        private val _carsState = MutableLiveData<Resource<List<CarResponse>>>()
        val carsState: LiveData<Resource<List<CarResponse>>> = _carsState

        private val _createCarState = MutableLiveData<Resource<CarResponse>>()
        val createCarState: LiveData<Resource<CarResponse>> = _createCarState

        private val _deleteCarState = MutableLiveData<Resource<MessageResponse>>()
        val deleteCarState: LiveData<Resource<MessageResponse>> = _deleteCarState

        fun getMyCars() {
            _carsState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getMyCars()
                _carsState.value = result
            }
        }

        fun createCar(marque: String, modele: String, annee: Int, immatriculation: String, typeCarburant: String) {
            _createCarState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createCar(marque, modele, annee, immatriculation, typeCarburant)
                _createCarState.value = result
                if (result is Resource.Success) {
                    getMyCars()
                }
            }
        }

        fun updateCar(id: String, marque: String? = null, modele: String? = null,
                      annee: Int? = null, typeCarburant: String? = null) {
            viewModelScope.launch {
                val result = repository.updateCar(id, marque, modele, annee, typeCarburant)
                if (result is Resource.Success) {
                    getMyCars()
                }
            }
        }

        fun deleteCar(id: String) {
            _deleteCarState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.deleteCar(id)
                _deleteCarState.value = result
                if (result is Resource.Success) {
                    getMyCars()
                }
            }
        }
    }

    // Maintenance ViewModel
    class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = MaintenanceRepository()

        private val _maintenancesState = MutableLiveData<Resource<List<MaintenanceResponse>>>()
        val maintenancesState: LiveData<Resource<List<MaintenanceResponse>>> = _maintenancesState

        private val _createMaintenanceState = MutableLiveData<Resource<MaintenanceResponse>>()
        val createMaintenanceState: LiveData<Resource<MaintenanceResponse>> = _createMaintenanceState

        fun getMaintenances() {
            _maintenancesState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getMaintenances()
                _maintenancesState.value = result
            }
        }

        fun createMaintenance(type: String, date: String, cout: Double, garage: String, voiture: String) {
            _createMaintenanceState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createMaintenance(type, date, cout, garage, voiture)
                _createMaintenanceState.value = result

                if (result is Resource.Success) {
                    getMaintenances() // Refresh list
                }
            }
        }

        fun deleteMaintenance(id: String) {
            viewModelScope.launch {
                val result = repository.deleteMaintenance(id)
                if (result is Resource.Success) {
                    getMaintenances() // Refresh list
                }
            }
        }
    }
    class RepairBayViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = RepairBayRepository()

        private val _repairBaysState = MutableLiveData<Resource<List<RepairBayResponse>>>()
        val repairBaysState: LiveData<Resource<List<RepairBayResponse>>> = _repairBaysState

        private val _availableBaysState = MutableLiveData<Resource<List<RepairBayResponse>>>()
        val availableBaysState: LiveData<Resource<List<RepairBayResponse>>> = _availableBaysState

        fun getRepairBaysByGarage(garageId: String) {
            _repairBaysState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.getRepairBaysByGarage(garageId)
                    _repairBaysState.value = result
                } catch (e: Exception) {
                    _repairBaysState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        fun getAvailableRepairBays(
            garageId: String,
            date: String,
            heureDebut: String,
            heureFin: String
        ) {
            _availableBaysState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.getAvailableRepairBays(
                        garageId = garageId,
                        date = date,
                        heureDebut = heureDebut,
                        heureFin = heureFin
                    )
                    _availableBaysState.value = result
                } catch (e: Exception) {
                    _availableBaysState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        // Réinitialiser l'état des créneaux disponibles
        fun clearAvailableBays() {
            _availableBaysState.value = Resource.Success(emptyList())
        }
    }
    // Garage ViewModel
    // Garage ViewModel
    class GarageViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = GarageRepository()

        private val _garagesState = MutableLiveData<Resource<List<GarageResponse>>>()
        val garagesState: LiveData<Resource<List<GarageResponse>>> = _garagesState

        private val _recommendationsState = MutableLiveData<Resource<List<GarageRecommendation>>>()
        val recommendationsState: LiveData<Resource<List<GarageRecommendation>>> = _recommendationsState

        private val _createGarageState = MutableLiveData<Resource<GarageResponse>>()
        val createGarageState: LiveData<Resource<GarageResponse>> = _createGarageState

        private val _updateGarageState = MutableLiveData<Resource<GarageResponse>>()
        val updateGarageState: LiveData<Resource<GarageResponse>> = _updateGarageState

        private val _deleteGarageState = MutableLiveData<Resource<Unit>>()
        val deleteGarageState: LiveData<Resource<Unit>> = _deleteGarageState

        fun getGarages() {
            _garagesState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.getGarages()
                    _garagesState.value = result
                } catch (e: Exception) {
                    _garagesState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        fun getRecommendations(
            typePanne: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            rayon: Double? = null
        ) {
            _recommendationsState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.getGarageRecommendations(typePanne, latitude, longitude, rayon)
                    _recommendationsState.value = result
                } catch (e: Exception) {
                    _recommendationsState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        fun createGarage(
            nom: String,
            adresse: String,
            telephone: String,
            noteUtilisateur: Double = 0.0,
            heureOuverture: String? = null,
            heureFermeture: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            numberOfBays: Int = 1 // ✅ NOUVEAU paramètre avec valeur par défaut
        ) {
            _createGarageState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.createGarage(
                        nom = nom,
                        adresse = adresse,
                        telephone = telephone,
                        noteUtilisateur = noteUtilisateur,
                        heureOuverture = heureOuverture,
                        heureFermeture = heureFermeture,
                        latitude = latitude,
                        longitude = longitude,
                        numberOfBays = numberOfBays // ✅ Passer le paramètre
                    )
                    _createGarageState.value = result
                    if (result is Resource.Success) {
                        getGarages()
                    }
                } catch (e: Exception) {
                    _createGarageState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        fun updateGarage(
            garageId: String,
            nom: String? = null,
            adresse: String? = null,
            telephone: String? = null,
            noteUtilisateur: Double? = null,
            heureOuverture: String? = null,
            heureFermeture: String? = null,
            latitude: Double? = null,
            longitude: Double? = null
        ) {
            _updateGarageState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.updateGarage(
                        garageId = garageId,
                        nom = nom,
                        adresse = adresse,
                        telephone = telephone,
                        noteUtilisateur = noteUtilisateur,
                        heureOuverture = heureOuverture,
                        heureFermeture = heureFermeture,
                        latitude = latitude,
                        longitude = longitude
                    )
                    _updateGarageState.value = result

                    if (result is Resource.Success) {
                        getGarages()
                    }
                } catch (e: Exception) {
                    _updateGarageState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }

        fun deleteGarage(garageId: String) {
            _deleteGarageState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.deleteGarage(garageId)
                    _deleteGarageState.value = result
                    if (result is Resource.Success) {
                        getGarages()
                    }
                } catch (e: Exception) {
                    _deleteGarageState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }
    }


    // Document ViewModel
    class DocumentViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = DocumentRepository()

        private val _documentsState = MutableLiveData<Resource<List<DocumentResponse>>>()
        val documentsState: LiveData<Resource<List<DocumentResponse>>> = _documentsState

        private val _createDocumentState = MutableLiveData<Resource<DocumentResponse>>()
        val createDocumentState: LiveData<Resource<DocumentResponse>> = _createDocumentState

        fun getDocuments() {
            _documentsState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getDocuments()
                _documentsState.value = result
            }
        }

        fun createDocument(type: String, dateEmission: String, dateExpiration: String, fichier: String, voiture: String) {
            _createDocumentState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createDocument(type, dateEmission, dateExpiration, fichier, voiture)
                _createDocumentState.value = result

                if (result is Resource.Success) {
                    getDocuments() // Refresh list
                }
            }
        }

        fun deleteDocument(id: String) {
            viewModelScope.launch {
                val result = repository.deleteDocument(id)
                if (result is Resource.Success) {
                    getDocuments() // Refresh list
                }
            }
        }
    }

    // Part ViewModel
    class PartViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = PartRepository()

        private val _partsState = MutableLiveData<Resource<List<PartResponse>>>()
        val partsState: LiveData<Resource<List<PartResponse>>> = _partsState

        private val _createPartState = MutableLiveData<Resource<PartResponse>>()
        val createPartState: LiveData<Resource<PartResponse>> = _createPartState

        fun getParts() {
            _partsState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getParts()
                _partsState.value = result
            }
        }

        fun createPart(nom: String, type: String, dateInstallation: String, kilometrageRecommande: Int, voiture: String) {
            _createPartState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createPart(nom, type, dateInstallation, kilometrageRecommande, voiture)
                _createPartState.value = result

                if (result is Resource.Success) {
                    getParts() // Refresh list
                }
            }
        }

        fun deletePart(id: String) {
            viewModelScope.launch {
                val result = repository.deletePart(id)
                if (result is Resource.Success) {
                    getParts() // Refresh list
                }
            }
        }
    }

    // AI ViewModel
    class AIViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = AIRepository()

        private val _roadIssueState = MutableLiveData<Resource<RoadIssueResponse>>()
        val roadIssueState: LiveData<Resource<RoadIssueResponse>> = _roadIssueState

        private val _dangerZonesState = MutableLiveData<Resource<List<DangerZone>>>()
        val dangerZonesState: LiveData<Resource<List<DangerZone>>> = _dangerZonesState

        private val _maintenanceRecommendationsState = MutableLiveData<Resource<MaintenanceRecommendationResponse>>()
        val maintenanceRecommendationsState: LiveData<Resource<MaintenanceRecommendationResponse>> = _maintenanceRecommendationsState

        fun reportRoadIssue(latitude: Double, longitude: Double, typeAnomalie: String, description: String) {
            _roadIssueState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.reportRoadIssue(latitude, longitude, typeAnomalie, description)
                _roadIssueState.value = result
            }
        }

        fun getDangerZones(latitude: Double? = null, longitude: Double? = null, rayon: Double? = null) {
            _dangerZonesState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getDangerZones(latitude, longitude, rayon)
                _dangerZonesState.value = result
            }
        }

        fun getMaintenanceRecommendations(voitureId: String) {
            _maintenanceRecommendationsState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getMaintenanceRecommendations(voitureId)
                _maintenanceRecommendationsState.value = result
            }
        }
    }

    // User ViewModel (for admin)
    class UserViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = UserRepository()

        private val _usersState = MutableLiveData<Resource<List<UserResponse>>>()
        val usersState: LiveData<Resource<List<UserResponse>>> = _usersState

        private val _updateUserState = MutableLiveData<Resource<UserResponse>>()
        val updateUserState: LiveData<Resource<UserResponse>> = _updateUserState

        fun getAllUsers() {
            _usersState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getAllUsers()
                _usersState.value = result
            }
        }

        fun updateUser(id: String, nom: String? = null, prenom: String? = null, telephone: String? = null) {
            _updateUserState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.updateUser(id, nom, prenom, telephone)
                _updateUserState.value = result

                if (result is Resource.Success) {
                    getAllUsers() // Refresh list
                }
            }
        }

        fun updateUserRole(id: String, role: String) {
            viewModelScope.launch {
                val result = repository.updateUserRole(id, role)
                if (result is Resource.Success) {
                    getAllUsers() // Refresh list
                }
            }
        }

        fun deleteUser(id: String) {
            viewModelScope.launch {
                val result = repository.deleteUser(id)
                if (result is Resource.Success) {
                    getAllUsers() // Refresh list
                }
            }
        }
    }

    // Service ViewModel
    class ServiceViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = ServiceRepository()
        private val _createServiceState = MutableLiveData<Resource<ServiceResponse>>()
        val createServiceState: LiveData<Resource<ServiceResponse>> = _createServiceState
        private val _servicesState = MutableLiveData<Resource<List<ServiceResponse>>>()
        val servicesState: LiveData<Resource<List<ServiceResponse>>> = _servicesState
        private val _updateServiceState = MutableLiveData<Resource<ServiceResponse>>()
        val updateServiceState: LiveData<Resource<ServiceResponse>> = _updateServiceState
        private val _deleteServiceState = MutableLiveData<Resource<MessageResponse>>()
        val deleteServiceState: LiveData<Resource<MessageResponse>> = _deleteServiceState

        fun createService(
            type: String,
            coutMoyen: Double,
            dureeEstimee: Int,
            garageId: String
        ) {
            _createServiceState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.createService(type, coutMoyen, dureeEstimee, garageId)
                    _createServiceState.value = result
                } catch (e: Exception) {
                    _createServiceState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }
        fun getServicesByGarage(garageId: String) {
            _servicesState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.getServicesByGarage(garageId)
                    _servicesState.value = result
                } catch (e: Exception) {
                    _servicesState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }
        fun updateService(
            serviceId: String,
            garageId: String,
            type: String,
            coutMoyen: Double,
            dureeEstimee: Int
        ) {
            _updateServiceState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.updateService(serviceId, type, coutMoyen, dureeEstimee)
                    _updateServiceState.value = result
                    if (result is Resource.Success) getServicesByGarage(garageId)
                } catch (e: Exception) {
                    _updateServiceState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }
        fun deleteService(serviceId: String, garageId: String) {
            _deleteServiceState.value = Resource.Loading()
            viewModelScope.launch {
                try {
                    val result = repository.deleteService(serviceId)
                    _deleteServiceState.value = result
                    if (result is Resource.Success) getServicesByGarage(garageId)
                } catch (e: Exception) {
                    _deleteServiceState.value = Resource.Error("Erreur: ${e.message}")
                }
            }
        }
    }

    // Reservation ViewModel
        class ReservationViewModel(application: Application) : AndroidViewModel(application) {
            private val repository = ReservationRepository()

            private val _reservationsState = MutableLiveData<Resource<List<ReservationResponse>>>()
            val reservationsState: LiveData<Resource<List<ReservationResponse>>> = _reservationsState

            private val _createReservationState = MutableLiveData<Resource<ReservationResponse>>()
            val createReservationState: LiveData<Resource<ReservationResponse>> = _createReservationState

            private val _updateReservationState = MutableLiveData<Resource<ReservationResponse>>()
            val updateReservationState: LiveData<Resource<ReservationResponse>> = _updateReservationState

            private val _deleteReservationState = MutableLiveData<Resource<MessageResponse>>()
            val deleteReservationState: LiveData<Resource<MessageResponse>> = _deleteReservationState

            private val _updateStatusState = MutableLiveData<Resource<ReservationResponse>>()
            val updateStatusState: LiveData<Resource<ReservationResponse>> = _updateStatusState

            // For users to get their own reservations
            fun getMyReservations() {
                _reservationsState.value = Resource.Loading()
                viewModelScope.launch {
                    val result = repository.getMyReservations()
                    _reservationsState.value = result
                }
            }

            // For garage owners to get all reservations (with filtering)
            fun getReservations(garageId: String? = null) {
                _reservationsState.value = Resource.Loading()
                viewModelScope.launch {
                    val result = repository.getReservations()
                    _reservationsState.value = result
                }
            }

            fun createReservation(
                garageId: String,
                date: String,
                heureDebut: String,
                heureFin: String,
                status: String = "en_attente",
                services: List<String>? = null,
                commentaires: String? = null
            ) {
                _createReservationState.value = Resource.Loading()
                viewModelScope.launch {
                    val result = repository.createReservation(
                        garageId = garageId,
                        date = date,
                        heureDebut = heureDebut,
                        heureFin = heureFin,
                        status = status,
                        services = services,
                        commentaires = commentaires
                    )
                    _createReservationState.value = result
                    if (result is Resource.Success) {
                        getMyReservations() // Refresh with user's reservations
                    }
                }
            }

            fun updateReservation(
                id: String,
                date: String? = null,
                heureDebut: String? = null,
                heureFin: String? = null,
                status: String? = null,
                services: List<String>? = null,
                commentaires: String? = null,
                isPaid: Boolean? = null
            ) {
                _updateReservationState.value = Resource.Loading()
                viewModelScope.launch {
                    try {
                        val result = repository.updateReservation(
                            reservationId = id,
                            date = date,
                            heureDebut = heureDebut,
                            heureFin = heureFin,
                            status = status,
                            services = services,
                            commentaires = commentaires,
                            isPaid = isPaid
                        )
                        _updateReservationState.value = result
                        if (result is Resource.Success<ReservationResponse>) {
                            getMyReservations() // Refresh with user's reservations
                        }
                    } catch (e: Exception) {
                        _updateReservationState.value = Resource.Error("Erreur: ${e.message}")
                    }
                }
            }

            fun updateReservationStatus(
                id: String,
                status: String
            ) {
                _updateStatusState.value = Resource.Loading()
                viewModelScope.launch {
                    try {
                        val result = repository.updateReservationStatus(id, status)
                        _updateStatusState.value = result
                        if (result is Resource.Success) {
                            getReservations() // Refresh all reservations for garage owner
                        }
                    } catch (e: Exception) {
                        _updateStatusState.value = Resource.Error("Erreur: ${e.message}")
                    }
                }
            }

            fun deleteReservation(id: String) {
                _deleteReservationState.value = Resource.Loading()
                viewModelScope.launch {
                    val result = repository.deleteReservation(id)
                    _deleteReservationState.value = result
                    if (result is Resource.Success) {
                        getMyReservations() // Refresh with user's reservations
                    }
                }
            }
        }


// OSM ViewModel
class OsmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OsmRepository()

    private val _searchResults = MutableLiveData<Resource<List<OsmLocationSuggestion>>>()
    val searchResults: LiveData<Resource<List<OsmLocationSuggestion>>> = _searchResults

    private val _selectedLocation = MutableLiveData<OsmLocationSuggestion?>()
    val selectedLocation: LiveData<OsmLocationSuggestion?> = _selectedLocation

    private val _reverseGeocodeResult = MutableLiveData<Resource<OsmLocationSuggestion>>()
    val reverseGeocodeResult: LiveData<Resource<OsmLocationSuggestion>> = _reverseGeocodeResult

    fun searchAddress(query: String) {
        if (query.length < 3) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }

        _searchResults.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = repository.searchAddress(query)
                _searchResults.value = result
            } catch (e: Exception) {
                _searchResults.value = Resource.Error("Erreur de recherche: ${e.message}")
            }
        }
    }

    fun reverseGeocode(lat: Double, lon: Double) {
        _reverseGeocodeResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = repository.reverseGeocode(lat, lon)
                _reverseGeocodeResult.value = result
            } catch (e: Exception) {
                _reverseGeocodeResult.value = Resource.Error("Erreur de géocodage: ${e.message}")
            }
        }
    }

    fun selectLocation(location: OsmLocationSuggestion) {
        _selectedLocation.value = location
    }

    fun clearSelection() {
        _selectedLocation.value = null
    }

    fun clearSearch() {
        _searchResults.value = Resource.Success(emptyList())
    }
}
