package com.example.proyecto_tfg_jorge.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.navegation.NavigationActions

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        NavigationItem(
            icon = R.drawable.ic_menu,
            label = "Menú",
            onClick = { NavigationActions.irAPantallaMenu(navController) }
        )
        NavigationItem(
            icon = R.drawable.ic_perfil,
            label = "Perfil",
            onClick = { NavigationActions.irAPantallaProfile(navController) }
        )
        NavigationItem(
            icon = R.drawable.ic_mapa,
            label = "Mapas",
            onClick = { NavigationActions.irAPantallaMaps(navController) }
        )
        NavigationItem(
            icon = R.drawable.ic_social,
            label = "Red Social",
            onClick = { NavigationActions.irAPantallaSocialNetwork(navController) }
        )
        NavigationItem(
            icon = R.drawable.ic_registrar,
            label = "Registrar",
            onClick = { NavigationActions.irAPantallaRegister(navController) }
        )
    }
}

@Composable
fun NavigationItem(icon: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label
        )
        Text(text = label)
    }
}
