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
                            // Extract userId as a string (from IdWrapper)
                            val userId = userData.user.id?.oid ?: userData.user.email
                            tokenManager.saveToken(userData.accessToken)
                            tokenManager.saveUser(
                                UserData(
                                    id = userId,
                                    email = userData.user.email,
                                    nom = userData.user.nom,
                                    prenom = userData.user.prenom,
                                    role = userData.user.role,
                                    telephone = userData.user.telephone ?: ""
                                )
                            )
                        } catch (e: Exception) {
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
                        val userId = result.data!!.user.id?.oid ?: result.data.user.email
                        tokenManager.saveToken(result.data.accessToken)
                        tokenManager.saveUser(
                            UserData(
                                id = userId,
                                email = result.data.user.email,
                                nom = result.data.user.nom,
                                prenom = result.data.user.prenom,
                                role = result.data.user.role,
                                telephone = result.data.user.telephone ?: ""
                            )
                        )
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

    // Garage ViewModel
    class GarageViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = GarageRepository()

        private val _garagesState = MutableLiveData<Resource<List<GarageResponse>>>()
        val garagesState: LiveData<Resource<List<GarageResponse>>> = _garagesState

        private val _recommendationsState = MutableLiveData<Resource<List<GarageRecommendation>>>()
        val recommendationsState: LiveData<Resource<List<GarageRecommendation>>> = _recommendationsState

        // Add this for garage creation state
        private val _createGarageState = MutableLiveData<Resource<GarageResponse>>()
        val createGarageState: LiveData<Resource<GarageResponse>> = _createGarageState

        fun getGarages() {
            _garagesState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getGarages()
                _garagesState.value = result
            }
        }

        fun getRecommendations(typePanne: String? = null, latitude: Double? = null,
                               longitude: Double? = null, rayon: Double? = null) {
            _recommendationsState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getGarageRecommendations(typePanne, latitude, longitude, rayon)
                _recommendationsState.value = result
            }
        }

        // REACTIVE GARAGE CREATION
        fun createGarage(nom: String, adresse: String, typeService: List<String>, telephone: String, noteUtilisateur: Double? = null) {
            _createGarageState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createGarage(nom, adresse, typeService, telephone, noteUtilisateur)
                _createGarageState.value = result
                if (result is Resource.Success) {
                    getGarages() // Refresh list after success
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

        fun createService(
            type: String,
            coutMoyen: Double,
            dureeEstimee: Int,
            garageId: String,
            carId: String
        ) {
            _createServiceState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.createService(type, coutMoyen, dureeEstimee, garageId, carId)
                _createServiceState.value = result
            }
        }

        private val _servicesState = MutableLiveData<Resource<List<ServiceResponse>>>()
        val servicesState: LiveData<Resource<List<ServiceResponse>>> = _servicesState

        fun getServicesByGarage(garageId: String) {
            _servicesState.value = Resource.Loading()
            viewModelScope.launch {
                val result = repository.getServicesByGarage(garageId)
                _servicesState.value = result
            }
        }
    }




