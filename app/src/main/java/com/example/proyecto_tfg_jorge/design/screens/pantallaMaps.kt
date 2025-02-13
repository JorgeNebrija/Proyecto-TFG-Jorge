package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header

@Composable
fun PantallaMaps(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Llamada al componente Header con la navegación incluida
        Header(navController)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Llamada al componente NavigationLinks para la navegación
        BottomNavigationBar(navController)
    }

}