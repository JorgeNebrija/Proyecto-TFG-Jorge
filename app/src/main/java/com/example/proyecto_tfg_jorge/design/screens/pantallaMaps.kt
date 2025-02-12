package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.navegation.NavigationActions

@Composable
fun PantallaMaps(navController: NavHostController) {

    Column {
        // Tu contenido específico de la pantalla Menu
        Text("Contenido de la pantalla Mapas")


        NavigationActions
        // Llamada al componente NavigationLinks para la navegación
        BottomNavigationBar(navController)
    }

}