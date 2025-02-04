package com.example.proyecto_tfg_jorge.functionality.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_tfg_jorge.design.screens.PantallaLoginForm
import com.example.proyecto_tfg_jorge.viewmodels.AuthViewModel

@Composable
fun PantallaLoginFormScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory())

    LaunchedEffect(authViewModel.isLoggedIn.value) {
        if (authViewModel.isLoggedIn.value) {
            navController.navigate("pantallaMenu")
        }
    }

    PantallaLoginForm(
        email = authViewModel.email.value,
        password = authViewModel.password.value,
        onEmailChange = { authViewModel.email.value = it },
        onPasswordChange = { authViewModel.password.value = it },
        onLoginClick = { authViewModel.signIn() },
        errorMessage = authViewModel.errorMessage.value
    )
}