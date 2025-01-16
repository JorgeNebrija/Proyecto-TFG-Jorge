package com.example.ejerciciobd_crud

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.proyecto_tfg_jorge.screens.PantallaInicio


@Composable
fun NavigationWrapper (navHostController: NavHostController) {

    NavHost(navController = navHostController, startDestination = "pantallaInicio") {

        composable ("pantallaInicio") {PantallaInicio(navHostController)}

    }
}




