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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class for counters
data class AppCounters(
    val vehicles: Int = 0,
    val entretiens: Int = 0,
    val garages: Int = 0,
    val documents: Int = 0
)

// Auth ViewModel
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val tokenManager = TokenManager.getInstance(application)

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    private val _forgotPasswordState = MutableLiveData<Resource<MessageResponse>>()
    val forgotPasswordState: LiveData<Resource<MessageResponse>> = _forgotPasswordState

    private val _changePasswordState = MutableStateFlow<Resource<MessageResponse>?>(null)
    val changePasswordState: StateFlow<Resource<MessageResponse>?> = _changePasswordState.asStateFlow()

    init {
        tokenManager.initializeToken()
    }

    fun login(email: String, password: String) {
        _authState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Starting login for: $email")
                val result = repository.login(email, password)
                android.util.Log.d("AuthViewModel", "Login result: $result")
                _authState.value = result

                if (result is Resource.Success) {
                    android.util.Log.d("AuthViewModel", "Login successful, saving token...")
                    try {
                        val userData = result.data!!
                        android.util.Log.d("AuthViewModel", "User data received: ${userData.user}")

                        tokenManager.saveToken(userData.accessToken)
                        tokenManager.saveUser(UserData(
                            id = userData.user.id,
                            email = userData.user.email,
                            nom = userData.user.nom,
                            prenom = userData.user.prenom,
                            role = userData.user.role,
                            telephone = userData.user.telephone ?: ""
                        ))
                        android.util.Log.d("AuthViewModel", "Token and user saved successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("AuthViewModel", "Error saving token/user: ${e.message}", e)
                        _authState.value = Resource.Error("Erreur lors de la sauvegarde: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error: ${e.message}", e)
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
                    tokenManager.saveToken(result.data!!.accessToken)
                    tokenManager.saveUser(UserData(
                        id = result.data.user.id,
                        email = result.data.user.email,
                        nom = result.data.user.nom,
                        prenom = result.data.user.prenom,
                        role = result.data.user.role,
                        telephone = result.data.user.telephone ?: ""
                    ))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Signup error: ${e.message}", e)
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

    fun changePassword(currentPassword: String, newPassword: String) {
        _changePasswordState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.changePassword(currentPassword, newPassword)
                _changePasswordState.value = result
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Change password error: ${e.message}", e)
                _changePasswordState.value = Resource.Error("Erreur lors du changement de mot de passe: ${e.localizedMessage}")
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = null
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

    private val _carsStateFlow = MutableStateFlow<Resource<List<CarResponse>>?>(null)
    val carsStateFlow: StateFlow<Resource<List<CarResponse>>?> = _carsStateFlow.asStateFlow()

    private val _carCount = MutableStateFlow(0)
    val carCount: StateFlow<Int> = _carCount.asStateFlow()

    private val _createCarState = MutableLiveData<Resource<CarResponse>>()
    val createCarState: LiveData<Resource<CarResponse>> = _createCarState

    private val _updateCarState = MutableLiveData<Resource<CarResponse>>()
    val updateCarState: LiveData<Resource<CarResponse>> = _updateCarState

    private val _deleteCarState = MutableLiveData<Resource<MessageResponse>>()
    val deleteCarState: LiveData<Resource<MessageResponse>> = _deleteCarState

    fun getMyCars() {
        _carsState.value = Resource.Loading()
        _carsStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getMyCars()
            _carsState.value = result
            _carsStateFlow.value = result
            if (result is Resource.Success) {
                _carCount.value = result.data?.size ?: 0
            }
        }
    }

    fun refresh() = getMyCars()

    fun createCar(marque: String, modele: String, annee: Int, immatriculation: String, typeCarburant: String) {
        _createCarState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createCar(marque, modele, annee, immatriculation, typeCarburant)
            _createCarState.value = result

            if (result is Resource.Success) {
                getMyCars() // Refresh list
            }
        }
    }

    fun updateCar(
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
    ) {
        _updateCarState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateCar(
                id, marque, modele, annee, typeCarburant,
                kilometrage, statut, prochainEntretien, joursProchainEntretien, imageUrl
            )
            _updateCarState.value = result

            if (result is Resource.Success) {
                getMyCars() // Refresh list
            }
        }
    }

    fun deleteCar(id: String) {
        _deleteCarState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.deleteCar(id)
            _deleteCarState.value = result

            if (result is Resource.Success) {
                getMyCars() // Refresh list
            }
        }
    }

    fun resetDeleteState() {
        _deleteCarState.value = Resource.Loading() // Reset to loading instead of null
    }
}

// Maintenance ViewModel
class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MaintenanceRepository()

    private val _maintenancesState = MutableLiveData<Resource<List<MaintenanceResponse>>>()
    val maintenancesState: LiveData<Resource<List<MaintenanceResponse>>> = _maintenancesState

    private val _maintenancesStateFlow = MutableStateFlow<Resource<List<MaintenanceResponse>>?>(null)
    val maintenancesStateFlow: StateFlow<Resource<List<MaintenanceResponse>>?> = _maintenancesStateFlow.asStateFlow()

    private val _maintenanceCount = MutableStateFlow(0)
    val maintenanceCount: StateFlow<Int> = _maintenanceCount.asStateFlow()

    private val _maintenanceState = MutableLiveData<Resource<MaintenanceResponse>>()
    val maintenanceState: LiveData<Resource<MaintenanceResponse>> = _maintenanceState

    private val _createMaintenanceState = MutableLiveData<Resource<MaintenanceResponse>>()
    val createMaintenanceState: LiveData<Resource<MaintenanceResponse>> = _createMaintenanceState

    private val _updateMaintenanceState = MutableLiveData<Resource<MaintenanceResponse>>()
    val updateMaintenanceState: LiveData<Resource<MaintenanceResponse>> = _updateMaintenanceState

    fun getMaintenances() {
        _maintenancesState.value = Resource.Loading()
        _maintenancesStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getMaintenances()
            _maintenancesState.value = result
            _maintenancesStateFlow.value = result
            if (result is Resource.Success) {
                _maintenanceCount.value = result.data?.size ?: 0
            }
        }
    }

    fun refresh() = getMaintenances()

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

    fun getMaintenanceById(id: String) {
        _maintenanceState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getMaintenanceById(id)
            _maintenanceState.value = result
        }
    }

    fun updateMaintenanceDate(id: String, date: String) {
        _updateMaintenanceState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateMaintenance(id, UpdateMaintenanceRequest(date = date))
            _updateMaintenanceState.value = result
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

    private val _garagesStateFlow = MutableStateFlow<Resource<List<GarageResponse>>?>(null)
    val garagesStateFlow: StateFlow<Resource<List<GarageResponse>>?> = _garagesStateFlow.asStateFlow()

    private val _garageCount = MutableStateFlow(0)
    val garageCount: StateFlow<Int> = _garageCount.asStateFlow()

    private val _recommendationsState = MutableLiveData<Resource<List<GarageRecommendation>>>()
    val recommendationsState: LiveData<Resource<List<GarageRecommendation>>> = _recommendationsState

    fun getGarages() {
        _garagesState.value = Resource.Loading()
        _garagesStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getGarages()
            _garagesState.value = result
            _garagesStateFlow.value = result
            if (result is Resource.Success) {
                _garageCount.value = result.data?.size ?: 0
            }
        }
    }

    fun refresh() = getGarages()

    fun getRecommendations(typePanne: String? = null, latitude: Double? = null,
                          longitude: Double? = null, rayon: Double? = null) {
        _recommendationsState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getGarageRecommendations(typePanne, latitude, longitude, rayon)
            _recommendationsState.value = result
        }
    }

    fun createGarage(nom: String, adresse: String, typeService: List<String>, telephone: String, noteUtilisateur: Double? = null) {
        viewModelScope.launch {
            val result = repository.createGarage(nom, adresse, typeService, telephone, noteUtilisateur)
            if (result is Resource.Success) {
                getGarages() // Refresh list
            }
        }
    }
}

// Document ViewModel
class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DocumentRepository()

    // Document States
    private val _documentsState = MutableLiveData<Resource<List<DocumentResponse>>>()
    val documentsState: LiveData<Resource<List<DocumentResponse>>> = _documentsState

    private val _documentDetailState = MutableLiveData<Resource<DocumentResponse>>()
    val documentDetailState: LiveData<Resource<DocumentResponse>> = _documentDetailState

    private val _documentsStateFlow = MutableStateFlow<Resource<List<DocumentResponse>>?>(null)
    val documentsStateFlow: StateFlow<Resource<List<DocumentResponse>>?> = _documentsStateFlow.asStateFlow()

    private val _documentCount = MutableStateFlow(0)
    val documentCount: StateFlow<Int> = _documentCount.asStateFlow()

    private val _createDocumentState = MutableLiveData<Resource<DocumentResponse>>()
    val createDocumentState: LiveData<Resource<DocumentResponse>> = _createDocumentState

    private val _updateDocumentState = MutableLiveData<Resource<DocumentResponse>>()
    val updateDocumentState: LiveData<Resource<DocumentResponse>> = _updateDocumentState

    // Echeance States
    private val _echeancesState = MutableLiveData<Resource<List<EcheanceResponse>>>()
    val echeancesState: LiveData<Resource<List<EcheanceResponse>>> = _echeancesState

    private val _createEcheanceState = MutableLiveData<Resource<EcheanceResponse>>()
    val createEcheanceState: LiveData<Resource<EcheanceResponse>> = _createEcheanceState

    private val _updateEcheanceState = MutableLiveData<Resource<EcheanceResponse>>()
    val updateEcheanceState: LiveData<Resource<EcheanceResponse>> = _updateEcheanceState

    // Document Functions
    fun getDocuments() {
        _documentsState.value = Resource.Loading()
        _documentsStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getDocuments()
            _documentsState.value = result
            _documentsStateFlow.value = result
            if (result is Resource.Success) {
                _documentCount.value = result.data?.size ?: 0
            }
        }
    }

    fun getDocumentById(id: String) {
        _documentDetailState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getDocumentById(id)
            _documentDetailState.value = result
        }
    }

    fun refresh() = getDocuments()

    fun createDocument(request: CreateDocumentRequest) {
        _createDocumentState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createDocument(request)
            _createDocumentState.value = result
            if (result is Resource.Success) {
                getDocuments() // Refresh list
            }
        }
    }

    fun updateDocument(id: String, request: UpdateDocumentRequest) {
        _updateDocumentState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateDocument(id, request)
            _updateDocumentState.value = result
            if (result is Resource.Success) {
                getDocuments() // Refresh list
                getDocumentById(id) // Refresh detail view
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

    // Echeance Functions
    fun getEcheancesForDocument(documentId: String) {
        _echeancesState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getEcheancesForDocument(documentId)
            _echeancesState.value = result
        }
    }

    fun createEcheance(request: CreateEcheanceRequest) {
        _createEcheanceState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createEcheance(request)
            _createEcheanceState.value = result
            if (result is Resource.Success) {
                getEcheancesForDocument(request.documentId)
            }
        }
    }

    fun updateEcheance(id: String, request: UpdateEcheanceRequest, documentId: String) {
        _updateEcheanceState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateEcheance(id, request)
            _updateEcheanceState.value = result
            if (result is Resource.Success) {
                getEcheancesForDocument(documentId)
            }
        }
    }

    fun deleteEcheance(id: String, documentId: String) {
        viewModelScope.launch {
            val result = repository.deleteEcheance(id)
            if (result is Resource.Success) {
                getEcheancesForDocument(documentId) // Refresh list
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
