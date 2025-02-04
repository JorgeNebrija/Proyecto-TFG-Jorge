package com.example.proyecto_tfg_jorge.functionality.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    // Método para registrar al usuario
    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null) // Registro exitoso
                } else {
                    callback(false, task.exception?.message) // Error en el registro
                }
            }
    }

    // Método para iniciar sesión
    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null) // Inicio de sesión exitoso
                } else {
                    callback(false, task.exception?.message) // Error en el inicio de sesión
                }
            }
    }
}
