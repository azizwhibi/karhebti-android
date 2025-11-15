package com.example.karhebti_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.karhebti_android.data.api.*
import com.example.karhebti_android.data.model.Reclamation
import com.example.karhebti_android.data.model.Garage
import com.example.karhebti_android.data.model.Service
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
                _authState.value = Resource.Error("Erreur d\'inscription: ${e.localizedMessage}")
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

    fun getDocuments() {
        _documentsState.value = Resource.Loading()
        _documentsStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getDocuments()
            _documentsState.value = result
            _documentsStateFlow.value = result
            if (result is Resource.Success) {
                _documentCount.value = result.data?.size ?: 0
                // Vérifier les documents expirante et logger les alertes
                checkExpiringDocuments(result.data ?: emptyList())
            }
        }
    }

    private fun checkExpiringDocuments(documents: List<DocumentResponse>) {
        val expirationService = com.example.karhebti_android.data.websocket.DocumentExpirationNotificationService()
        val expiringDocuments = expirationService.getDocumentsExpiringWithinThreeDays(documents)

        if (expiringDocuments.isNotEmpty()) {
            android.util.Log.w("DocumentViewModel", "🚨 ${expiringDocuments.size} document(s) expire(nt) dans 3 jours")
            expiringDocuments.forEach { doc ->
                val alertMessage = expirationService.getAlertMessage(doc)
                android.util.Log.w("DocumentViewModel", alertMessage)
            }
        }
    }

    fun getDocumentById(id: String) {
        android.util.Log.d("DocumentViewModel", "getDocumentById called with ID: $id")
        _documentDetailState.value = Resource.Loading()
        viewModelScope.launch {
            android.util.Log.d("DocumentViewModel", "Fetching document from repository...")
            val result = repository.getDocumentById(id)
            android.util.Log.d("DocumentViewModel", "Result type: ${result::class.simpleName}")
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

    fun createDocument(request: CreateDocumentRequest, filePath: String? = null) {
        _createDocumentState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createDocument(request, filePath)
            _createDocumentState.value = result
            if (result is Resource.Success) {
                getDocuments() // Refresh list
            }
        }
    }

    fun updateDocument(id: String, request: UpdateDocumentRequest, filePath: String? = null) {
        _updateDocumentState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateDocument(id, request, filePath)
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
}

// Reclamation (Feedback) ViewModel
class ReclamationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReclamationRepository()

    private val _reclamationsState = MutableLiveData<Resource<List<Reclamation>>>()
    val reclamationsState: LiveData<Resource<List<Reclamation>>> = _reclamationsState

    private val _reclamationDetailState = MutableLiveData<Resource<Reclamation>>()
    val reclamationDetailState: LiveData<Resource<Reclamation>> = _reclamationDetailState

    private val _myReclamationsState = MutableLiveData<Resource<List<Reclamation>>>()
    val myReclamationsState: LiveData<Resource<List<Reclamation>>> = _myReclamationsState

    private val _createReclamationState = MutableLiveData<Resource<ReclamationResponse>>()
    val createReclamationState: LiveData<Resource<ReclamationResponse>> = _createReclamationState

    private val _updateReclamationState = MutableLiveData<Resource<ReclamationResponse>>()
    val updateReclamationState: LiveData<Resource<ReclamationResponse>> = _updateReclamationState

    private val _deleteReclamationState = MutableLiveData<Resource<MessageResponse>>()
    val deleteReclamationState: LiveData<Resource<MessageResponse>> = _deleteReclamationState

    private val _reclamationsStateFlow = MutableStateFlow<Resource<List<Reclamation>>?>(null)
    val reclamationsStateFlow: StateFlow<Resource<List<Reclamation>>?> = _reclamationsStateFlow.asStateFlow()

    private fun mapToReclamation(response: ReclamationResponse): Reclamation {
        return Reclamation(
            id = response.id,
            titre = response.titre,
            message = response.message,
            type = response.type,
            garage = response.garage?.let {
                Garage(
                    id = it.id,
                    nom = it.nom,
                    adresse = it.adresse,
                    latitude = 0.0,
                    longitude = 0.0,
                    distance = 0.0,
                    rating = 0.0f,
                    reviewCount = 0,
                    phoneNumber = it.telephone ?: "",
                    isOpen = false,
                    openUntil = null,
                    services = emptyList(),
                    imageUrl = null
                )
            },
            service = response.service?.let { Service(it.id, it.type) },
            createdAt = response.createdAt
        )
    }

    fun getAllReclamations() {
        _reclamationsState.value = Resource.Loading()
        _reclamationsStateFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getReclamations()
            @Suppress("UNCHECKED_CAST")
            val mappedResult: Resource<List<Reclamation>> = when (result) {
                is Resource.Success -> Resource.Success(result.data?.map { mapToReclamation(it) } ?: emptyList())
                is Resource.Error -> result as Resource<List<Reclamation>>
                is Resource.Loading -> result as Resource<List<Reclamation>>
            }
            _reclamationsState.value = mappedResult
            _reclamationsStateFlow.value = mappedResult
        }
    }

    fun getReclamationById(id: String) {
        _reclamationDetailState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getReclamationById(id)
                android.util.Log.d("ReclamationViewModel", "getReclamationById result: $result")
                @Suppress("UNCHECKED_CAST")
                val mappedResult: Resource<Reclamation> = when (result) {
                    is Resource.Success -> result.data?.let { Resource.Success(mapToReclamation(it)) } as? Resource<Reclamation> ?: Resource.Error("No data")
                    is Resource.Error -> result as Resource<Reclamation>
                    is Resource.Loading -> result as Resource<Reclamation>
                }
                _reclamationDetailState.value = mappedResult
            } catch (e: Exception) {
                android.util.Log.e("ReclamationViewModel", "Error in getReclamationById: ${e.message}", e)
                _reclamationDetailState.value = Resource.Error("Erreur: ${e.message}")
            }
        }
    }

    fun getMyReclamations() {
        _myReclamationsState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getMyReclamations()
                android.util.Log.d("ReclamationViewModel", "getMyReclamations result: $result")
                @Suppress("UNCHECKED_CAST")
                val mappedResult: Resource<List<Reclamation>> = when (result) {
                    is Resource.Success -> Resource.Success(result.data?.map { mapToReclamation(it) } ?: emptyList())
                    is Resource.Error -> result as Resource<List<Reclamation>>
                    is Resource.Loading -> result as Resource<List<Reclamation>>
                }
                _myReclamationsState.value = mappedResult
            } catch (e: Exception) {
                android.util.Log.e("ReclamationViewModel", "Error in getMyReclamations: ${e.message}", e)
                _myReclamationsState.value = Resource.Error("Erreur: ${e.message}")
            }
        }
    }

    fun getReclamationsByGarage(garageId: String) {
        _reclamationsState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getReclamationsByGarage(garageId)
            @Suppress("UNCHECKED_CAST")
            val mappedResult: Resource<List<Reclamation>> = when (result) {
                is Resource.Success -> Resource.Success(result.data?.map { mapToReclamation(it) } ?: emptyList())
                is Resource.Error -> result as Resource<List<Reclamation>>
                is Resource.Loading -> result as Resource<List<Reclamation>>
            }
            _reclamationsState.value = mappedResult
        }
    }

    fun getReclamationsByService(serviceId: String) {
        _reclamationsState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getReclamationsByService(serviceId)
            @Suppress("UNCHECKED_CAST")
            val mappedResult: Resource<List<Reclamation>> = when (result) {
                is Resource.Success -> Resource.Success(result.data?.map { mapToReclamation(it) } ?: emptyList())
                is Resource.Error -> result as Resource<List<Reclamation>>
                is Resource.Loading -> result as Resource<List<Reclamation>>
            }
            _reclamationsState.value = mappedResult
        }
    }

    fun createReclamation(
        type: String,
        titre: String,
        message: String,
        garageId: String? = null,
        serviceId: String? = null
    ) {
        _createReclamationState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createReclamation(
                type = type,
                titre = titre,
                message = message,
                garageId = garageId,
                serviceId = serviceId
            )
            _createReclamationState.value = result

            if (result is Resource.Success) {
                getMyReclamations() // Refresh user's reclamations
            }
        }
    }

    fun updateReclamation(id: String, titre: String? = null, message: String? = null) {
        _updateReclamationState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateReclamation(id, titre, message)
            _updateReclamationState.value = result

            if (result is Resource.Success) {
                getMyReclamations() // Refresh list
            }
        }
    }

    fun deleteReclamation(id: String) {
        _deleteReclamationState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.deleteReclamation(id)
            _deleteReclamationState.value = result

            if (result is Resource.Success) {
                getMyReclamations() // Refresh list
            }
        }
    }

    fun refresh() {
        getMyReclamations()
    }
}

// Notification ViewModel
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotificationRepository()

    private val _notificationsState = MutableLiveData<Resource<List<NotificationResponse>>>()
    val notificationsState: LiveData<Resource<List<NotificationResponse>>> = _notificationsState

    private val _unreadNotificationsState = MutableLiveData<Resource<List<NotificationResponse>>>()
    val unreadNotificationsState: LiveData<Resource<List<NotificationResponse>>> = _unreadNotificationsState

    private val _markAsReadState = MutableLiveData<Resource<NotificationResponse>>()
    val markAsReadState: LiveData<Resource<NotificationResponse>> = _markAsReadState

    private val _deleteNotificationState = MutableLiveData<Resource<MessageResponse>>()
    val deleteNotificationState: LiveData<Resource<MessageResponse>> = _deleteNotificationState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun getMyNotifications() {
        _notificationsState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getMyNotifications()
                _notificationsState.value = result

                // Update unread count
                if (result is Resource.Success) {
                    _unreadCount.value = result.data?.count { !it.lu } ?: 0
                } else if (result is Resource.Error) {
                    // Si erreur 500, afficher liste vide avec message
                    android.util.Log.e("NotificationViewModel", "Erreur: ${result.message}")
                    _notificationsState.value = Resource.Success(emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Exception: ${e.message}", e)
                _notificationsState.value = Resource.Success(emptyList())
            }
        }
    }

    fun getUnreadNotifications() {
        _unreadNotificationsState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getUnreadNotifications()
                _unreadNotificationsState.value = result

                // Update unread count
                if (result is Resource.Success) {
                    _unreadCount.value = result.data?.size ?: 0
                } else if (result is Resource.Error) {
                    // Si erreur 500, afficher liste vide
                    android.util.Log.e("NotificationViewModel", "Erreur: ${result.message}")
                    _unreadNotificationsState.value = Resource.Success(emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Exception: ${e.message}", e)
                _unreadNotificationsState.value = Resource.Success(emptyList())
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        _markAsReadState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.markNotificationAsRead(id)
            _markAsReadState.value = result

            if (result is Resource.Success) {
                getMyNotifications() // Refresh list
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            val result = repository.markAllNotificationsAsRead()
            if (result is Resource.Success) {
                getMyNotifications() // Refresh list
            }
        }
    }

    fun deleteNotification(id: String) {
        _deleteNotificationState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.deleteNotification(id)
            _deleteNotificationState.value = result

            if (result is Resource.Success) {
                getMyNotifications() // Refresh list
            }
        }
    }

    fun refresh() {
        getMyNotifications()
    }
}
