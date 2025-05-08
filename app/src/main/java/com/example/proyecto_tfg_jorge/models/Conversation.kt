package com.example.proyecto_tfg_jorge.models

import com.google.firebase.Timestamp
import java.util.Date

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Date? = null,
    val lastSenderId: String = "",
    var unreadCount: Int = 0,
    val lastRead: Map<String, Timestamp> = emptyMap()  // ← nuevo
)
