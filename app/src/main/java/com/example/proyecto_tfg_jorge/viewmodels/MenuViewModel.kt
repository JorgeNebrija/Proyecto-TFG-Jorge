package com.example.proyecto_tfg_jorge.viewmodels

// ui/viewmodels/MenuViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_tfg_jorge.data.actualizarMeGusta
import com.example.proyecto_tfg_jorge.data.eliminarMeGusta
import com.example.proyecto_tfg_jorge.data.obtenerPublicacionesParaTi
import com.example.proyecto_tfg_jorge.data.obtenerTodasLasPublicaciones
import com.example.proyecto_tfg_jorge.data.subirComentario
import com.example.proyecto_tfg_jorge.models.Publicacion
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

    private val _publicaciones = MutableStateFlow<List<Publicacion>>(emptyList())
    val publicaciones: StateFlow<List<Publicacion>> = _publicaciones

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        cargarPublicaciones()
    }

    fun cargarPublicaciones() {
        viewModelScope.launch {
            userId?.let {
                val seguidos = obtenerPublicacionesParaTi(it)
                _publicaciones.value = if (seguidos.isNotEmpty()) seguidos else obtenerTodasLasPublicaciones()
            }
        }
    }

    fun darLike(publicacionId: String) {
        userId?.let {
            actualizarMeGusta(publicacionId, it)
        }
    }

    fun quitarLike(publicacionId: String) {
        viewModelScope.launch {
            userId?.let {
                eliminarMeGusta(publicacionId, it)
            }
        }
    }

    fun eliminarPublicacion(publicacionId: String) {
        viewModelScope.launch {
            eliminarPublicacion(publicacionId)
            cargarPublicaciones()
        }
    }

    fun subirComentario(publicacionId: String, texto: String) {
        viewModelScope.launch {
            userId?.let {
                subirComentario(it, publicacionId, texto)
            }
        }
    }
}
