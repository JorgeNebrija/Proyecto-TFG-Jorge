package com.example.proyecto_tfg_jorge.models

data class Publicacion(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",   // Agregamos el título
    val imagenUrl: String = "",
    val descripcion: String = "",
    val ubicacion: String = "", // Agregamos la ubicación
    val fecha: String = "",    // Agregamos la fecha
    var meGusta: Int = 0,
    val imagenBase64: String = "" // Si lo vas a guardar como Base64 en lugar de Storage
)
