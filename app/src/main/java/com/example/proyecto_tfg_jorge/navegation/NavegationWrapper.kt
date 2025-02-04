package com.example.proyecto_tfg_jorge.navegation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto_tfg_jorge.design.screens.PantallaInicio
import com.example.proyecto_tfg_jorge.design.screens.PantallaMenu
import com.example.proyecto_tfg_jorge.functionality.auth.PantallaLogin
import com.example.proyecto_tfg_jorge.functionality.auth.PantallaLoginFormScreen

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    context: Context, // Aquí cambiamos "context" por "Context"
    signInLauncher: ActivityResultLauncher<Intent> // Pasamos el launcher aquí
) {
    NavHost(navController = navHostController, startDestination = "pantallaInicio") {
        composable("pantallaInicio") { PantallaInicio(navHostController) }
        composable("pantallaLogin") {
            PantallaLogin(
                navController = navHostController,
                signInLauncher = signInLauncher, // Pasamos el launcher a PantallaLogin
                context = context // Pasamos el contexto correctamente
            )
        }
        composable("pantallaLoginForm") { PantallaLoginFormScreen(navHostController) }
        composable("pantallaMenu") { PantallaMenu() }
    }
}
