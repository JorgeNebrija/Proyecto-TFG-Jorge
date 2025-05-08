package com.example.proyecto_tfg_jorge.models

data class PublicacionUI(
    val publicacion: Publicacion,
    val autorNombre: String,
    val autorFoto: String?,
    val comentarios: List<ComentarioUI>,
    val yaDioLike: Boolean,
    val estaSiguiendo: Boolean
)
data class ComentarioUI(
    val nombreUsuario: String,
    val texto: String,
    val fecha: String
)