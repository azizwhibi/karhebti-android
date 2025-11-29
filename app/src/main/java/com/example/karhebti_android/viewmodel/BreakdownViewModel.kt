package com.example.karhebti_android.viewmodel

// ViewModel pour la gestion des pannes (déclaration, historique, suivi)
// Utilise StateFlow pour la gestion d'état UI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karhebti_android.data.BreakdownResponse
import com.example.karhebti_android.data.CreateBreakdownRequest
import com.example.karhebti_android.repository.BreakdownsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BreakdownUiState {
    object Idle : BreakdownUiState()
    object Loading : BreakdownUiState()
    data class Success(val data: Any) : BreakdownUiState()
    data class Error(val message: String) : BreakdownUiState()
}

class BreakdownViewModel(private val repo: BreakdownsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<BreakdownUiState>(BreakdownUiState.Idle)
    val uiState: StateFlow<BreakdownUiState> = _uiState

    // userIdFallback: optional string to retry if backend requires userId
    fun declareBreakdown(request: CreateBreakdownRequest) {
        _uiState.value = BreakdownUiState.Loading

        viewModelScope.launch {
            repo.createBreakdown(request).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { BreakdownUiState.Success(it) },
                    onFailure = {
                        // Affiner le message si c'est un HTTP 400/403 par exemple
                        val raw = it.message ?: "Erreur inconnue"
                        val userMessage = when {
                            raw.startsWith("HTTP 400") -> "Données invalides : vérifiez le type et la description. Détail: ${raw.removePrefix("HTTP 400: ")}"
                            raw.startsWith("HTTP 403") -> "Non autorisé : votre session peut avoir expiré. Veuillez vous reconnecter."
                            raw.startsWith("HTTP 401") -> "Non authentifié : veuillez vous reconnecter."
                            else -> raw
                        }
                        BreakdownUiState.Error(userMessage)
                    }
                )
            }
        }
    }

    fun fetchUserBreakdowns(userId: Int) {
        _uiState.value = BreakdownUiState.Loading
        viewModelScope.launch {
            repo.getUserBreakdowns(userId).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { BreakdownUiState.Success(it) },
                    onFailure = { BreakdownUiState.Error(it.message ?: "Erreur") }
                )
            }
        }
    }

    /**
     * Fetch all breakdowns (backend should filter by authenticated user when needed).
     */
    fun fetchAllBreakdowns(status: String? = null) {
        _uiState.value = BreakdownUiState.Loading
        viewModelScope.launch {
            repo.getAllBreakdowns(status, null).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { BreakdownUiState.Success(it) },
                    onFailure = { BreakdownUiState.Error(it.message ?: "Erreur") }
                )
            }
        }
    }

    fun fetchBreakdown(id: Int) {
        _uiState.value = BreakdownUiState.Loading
        viewModelScope.launch {
            repo.getBreakdown(id).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { BreakdownUiState.Success(it) },
                    onFailure = { BreakdownUiState.Error(it.message ?: "Erreur") }
                )
            }
        }
    }
}
