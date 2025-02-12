package com.example.proyecto_tfg_jorge.navegation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto_tfg_jorge.design.screens.PantallaMaps
import com.example.proyecto_tfg_jorge.design.screens.PantallaMenu
import com.example.proyecto_tfg_jorge.design.screens.PantallaProfile
import com.example.proyecto_tfg_jorge.design.screens.PantallaRegister
import com.example.proyecto_tfg_jorge.design.screens.PantallaSocialNetwork
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaInicio
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaLogin2
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaRegister2

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    context: Context, // Aquí cambiamos "context" por "Context"
    signInLauncher: ActivityResultLauncher<Intent> // Pasamos el launcher aquí
) {
    NavHost(navController = navHostController, startDestination = "pantallaInicio") {
        composable("pantallaInicio") { PantallaInicio(navHostController) }
        composable("pantallaLogin2") {
            PantallaLogin2(navHostController)
        }
        composable("pantallaRegister2") { PantallaRegister2(navHostController) }
        composable("pantallaMenu") { PantallaMenu(navHostController) }
        composable("pantallaMaps") { PantallaMaps(navHostController) }
        composable("pantallaRegister") { PantallaRegister(navHostController) }
        composable("pantallaSocialNetwork") { PantallaSocialNetwork(navHostController) }
        composable("pantallaProfile") { PantallaProfile(navHostController) }
    }
    }






