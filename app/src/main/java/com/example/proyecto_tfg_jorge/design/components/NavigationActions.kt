package com.example.proyecto_tfg_jorge.navegation

import androidx.navigation.NavController

object NavigationActions {

    fun irAPantallaMenu(navController: NavController) {
        navController.navigate("pantallaMenu")
    }

    fun irAPantallaMaps(navController: NavController) {
        navController.navigate("pantallaMaps")
    }

    fun irAPantallaTraining(navController: NavController) {
        navController.navigate("pantallaTraining")
    }

    fun irAPantallaSocialNetwork(navController: NavController) {
        navController.navigate("pantallaSocialNetwork")
    }

    fun irAPantallaProfile(navController: NavController) {
        navController.navigate("pantallaProfile")
    }
}
