package com.example.proyecto_tfg_jorge.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.navegation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(navController: NavController) {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.athlo),  // Reemplaza con el nombre de tu imagen
                contentDescription = "Logo de la empresa",
                colorFilter = ColorFilter.tint(Color(0xFF00C853)),  // Aplica el color verde al logo
                modifier = Modifier.size(140.dp)
                    .clickable {
                        // Funcionalidad de navegación cuando se hace clic en la imagen
                        NavigationActions.irAPantallaMenu(navController)
                    }
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Black),
    )
}
