package com.example.proyecto_tfg_jorge.models

data class Message(
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    // Se usa Timestamp para evitar problemas de deserialización
    val timestamp: com.google.firebase.Timestamp? = null
)
