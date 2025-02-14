package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header

@Composable
fun PantallaSocialNetwork(navController: NavHostController) {

    // Crear un degradado donde el verde esté en el centro y el negro en los extremos
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00C853), // Verde en el centro
            Color.Black // Negro en la parte inferior
        ),
        startY = 0f,
        endY = 1000f // Aseguramos que el negro predomine en los extremos
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient) // Usamos el degradado con verde en el centro y negro en los extremos
    ) {
        // Header con el logo
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
