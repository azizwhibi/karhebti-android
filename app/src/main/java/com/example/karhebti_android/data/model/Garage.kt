package com.example.karhebti_android.data.model

data class GarageServiceForm(
    val type: String,            // e.g. "vidange"
    val coutMoyen: Double,
    val dureeEstimee: Int
)

data class NewGarageRequest(
    val nom: String,
    val adresse: String,
    val telephone: String,
    val noteUtilisateur: Double,
    val services: List<GarageServiceForm>,
    val heureOuverture: String,   // Added opening hours (HH:mm format)
    val heureFermeture: String    // Added closing hours (HH:mm format)
)
