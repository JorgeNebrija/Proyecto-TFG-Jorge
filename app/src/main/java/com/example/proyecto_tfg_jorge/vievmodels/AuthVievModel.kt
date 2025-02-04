package com.example.proyecto_tfg_jorge.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto_tfg_jorge.functionality.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val errorMessage = mutableStateOf<String?>(null)
    val isRegistered = mutableStateOf(false) // Track registration state
    val isLoggedIn = mutableStateOf(false) // Track login state

    // SignUp logic
    fun signUp() {
        viewModelScope.launch {
            authRepository.signUp(email.value, password.value) { success, error ->
                if (success) {
                    isRegistered.value = true // Update registration state
                } else {
                    errorMessage.value = error
                }
            }
        }
    }

    // SignIn logic
    fun signIn() {
        viewModelScope.launch {
            authRepository.signIn(email.value, password.value) { success, error ->
                if (success) {
                    isLoggedIn.value = true // Update login state
                } else {
                    errorMessage.value = error
                }
            }
        }
    }

    // Factory to instantiate the ViewModel
    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(AuthRepository(FirebaseAuth.getInstance())) as T
            }
        }
    }
}
