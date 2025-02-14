package com.example.proyecto_tfg_jorge.navegation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto_tfg_jorge.design.screens.PantallaMaps
import com.example.proyecto_tfg_jorge.design.screens.PantallaMenu
import com.example.proyecto_tfg_jorge.design.screens.PantallaProfile
import com.example.proyecto_tfg_jorge.design.screens.PantallaSocialNetwork
import com.example.proyecto_tfg_jorge.design.screens.PantallaTraining
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaInicio
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaLogin
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaRegister

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = "pantallaProfile") {
        composable("pantallaInicio") { PantallaInicio(navHostController) }
        composable("pantallaLogin") { PantallaLogin(navHostController) }
        composable("pantallaRegister") { PantallaRegister(navHostController) }
        composable("pantallaMenu") { PantallaMenu(navHostController) }
        composable("pantallaMaps") { PantallaMaps(navHostController) }
        composable("pantallaTraining") { PantallaTraining(navHostController) }
        composable("pantallaSocialNetwork") { PantallaSocialNetwork(navHostController) }
        composable("pantallaProfile") { PantallaProfile(navHostController) }
    }
    }






