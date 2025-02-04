package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto_tfg_jorge.R
import kotlinx.coroutines.delay

@Composable
fun PantallaInicio(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3500) // Espera 3 segundos
        navController.navigate("PantallaLogin") // Navega a la pantalla principal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..size.width.toInt() step 100) {
                drawLine(
                    color = Color.Green,
                    start = androidx.compose.ui.geometry.Offset(i.toFloat(), 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height - i.toFloat()),
                    strokeWidth = 10f,
                    alpha = 0.1f
                )
            }
            for (i in 0..size.width.toInt() step 100) {
                drawLine(
                    color = Color.Green,
                    start = androidx.compose.ui.geometry.Offset(size.width, i.toFloat()),
                    end = androidx.compose.ui.geometry.Offset(size.width - i.toFloat(), size.height),
                    strokeWidth = 10f,
                    alpha = 0.1f
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logosinfondo), // Reemplaza con tu recurso de logo
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp) // Increased logo size
            )
            Text(
                text = "Bienvenido a Athlo",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Conecta con personas que comparten tu pasión por el deporte, explora rutas únicas y supera tus propios límites. Cada día es una nueva oportunidad para mejorar tu rendimiento y disfrutar al máximo de lo que te gusta. ¡Empieza hoy mismo!",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}