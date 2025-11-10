package com.example.karhebti_android.data.model

import java.util.Date

data class Echeance(
    val id: String, // id_ech
    val documentId: String, // Clé étrangère (id_doc_fk)
    val dateEcheance: Date,
    val description: String,
    val estTerminee: Boolean
)
