package com.example.proyecto_tfg_jorge.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.navegation.NavigationActions

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Obtener la ruta actual
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Surface(
        color = Color.Black, // Color de fondo negro
        modifier = Modifier
            .fillMaxWidth()
    ) {
        BottomAppBar(
            containerColor = Color.Black, // Fondo de la barra inferior negro
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black) // Fondo negro
                .padding(0.dp)
        ) {
            // Primer ícono (Menú)
            IconButton(
                onClick = { NavigationActions.irAPantallaMenu(navController) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = if (currentRoute == "pantallaMenu") Color.White else Color(0xFF00C853), // Icono blanco si estamos en el menú
                    modifier = Modifier.size(34.dp)
                )
            }
            // Tercer ícono (Mapas)
            IconButton(
                onClick = { NavigationActions.irAPantallaMaps(navController) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mapa),
                    contentDescription = "Mapas",
                    tint = if (currentRoute == "pantallaMaps") Color.White else Color(0xFF00C853), // Icono blanco si estamos en los mapas
                    modifier = Modifier.size(34.dp)
                )
            }

            // Cuarto ícono (Red Social)
            IconButton(
                onClick = { NavigationActions.irAPantallaSocialNetwork(navController) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_social),
                    contentDescription = "Red Social",
                    tint = if (currentRoute == "pantallaSocialNetwork") Color.White else Color(0xFF00C853), // Icono blanco si estamos en la red social
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(
                onClick = { NavigationActions.irAPantallaTraining(navController) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_registrar),
                    contentDescription = "Registrar",
                    tint = if (currentRoute == "pantallaTraining") Color.White else Color(0xFF00C853), // Icono blanco si estamos en registrar
                    modifier = Modifier.size(34.dp)
                )
            }
            IconButton(
                onClick = { NavigationActions.irAPantallaProfile(navController) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_perfil),
                    contentDescription = "Perfil",
                    tint = if (currentRoute == "pantallaProfile") Color.White else Color(0xFF00C853), // Icono blanco si estamos en el perfil
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}
