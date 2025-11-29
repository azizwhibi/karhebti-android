package com.example.karhebti_android.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(application) as T
            }
            modelClass.isAssignableFrom(CarViewModel::class.java) -> {
                CarViewModel(application) as T
            }
            modelClass.isAssignableFrom(MaintenanceViewModel::class.java) -> {
                MaintenanceViewModel(application) as T
            }
            modelClass.isAssignableFrom(GarageViewModel::class.java) -> {
                GarageViewModel(application) as T
            }
            modelClass.isAssignableFrom(DocumentViewModel::class.java) -> {
                DocumentViewModel(application) as T
            }
            modelClass.isAssignableFrom(PartViewModel::class.java) -> {
                PartViewModel(application) as T
            }
            modelClass.isAssignableFrom(AIViewModel::class.java) -> {
                AIViewModel(application) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(application) as T
            }
            modelClass.isAssignableFrom(ServiceViewModel::class.java) -> {
                ServiceViewModel(application) as T
            }
            modelClass.isAssignableFrom(ReservationViewModel::class.java) -> {
                ReservationViewModel(application) as T
            }
            modelClass.isAssignableFrom(OsmViewModel::class.java) -> {  // AJOUTEZ CETTE LIGNE
                OsmViewModel(application) as T
            }
            // ✅ AJOUTEZ CETTE LIGNE
            modelClass.isAssignableFrom(RepairBayViewModel::class.java) -> {
                RepairBayViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}