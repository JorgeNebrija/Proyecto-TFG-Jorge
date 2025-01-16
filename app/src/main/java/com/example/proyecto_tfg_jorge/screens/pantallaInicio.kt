package com.example.proyecto_tfg_jorge.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.proyecto_tfg_jorge.R
import kotlinx.coroutines.delay

@Composable
fun PantallaInicio(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000) // Espera 3 segundos
        navController.navigate("PantallaMenu") // Navega a la pantalla principal
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_tfg), // Reemplaza con tu recurso de logo
            contentDescription = "Logo"
        )
    }
}