package com.example.proyecto_tfg_jorge.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val imageBase64: String? = null,
    // Otros campos opcionales, según se guarden en Firestore:
    val lastName: String? = null,
    val phone: String? = null,
    val notificationsEnabled: Boolean? = null,
    val politica: Boolean = false, // CAMBIO A BOOLEANO
    var fcmToken: String = "", // <- Esto debe coincidir con Firestore
    var meSigue: Boolean? = null,
    var teSigo: Boolean? = null

)
