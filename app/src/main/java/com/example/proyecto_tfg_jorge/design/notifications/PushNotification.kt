package com.example.proyecto_tfg_jorge.design.notifications

data class PushNotification(
    val data: NotificationData,
    val to: String,           // token FCM
    val toUserId: String      // ID del usuario receptor
)
