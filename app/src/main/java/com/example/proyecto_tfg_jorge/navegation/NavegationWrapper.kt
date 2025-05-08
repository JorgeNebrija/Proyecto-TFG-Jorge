package com.example.proyecto_tfg_jorge.navegation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyecto_tfg_jorge.design.screens.PantallaMenu
import com.example.proyecto_tfg_jorge.design.screens.PantallaProfile
import com.example.proyecto_tfg_jorge.design.screens.PantallaSocialNetwork
import com.example.proyecto_tfg_jorge.design.screens.PantallaSubirPublicacion
import com.example.proyecto_tfg_jorge.design.screens.PantallaTraining
import com.example.proyecto_tfg_jorge.design.screens.chat.PantallaChat
import com.example.proyecto_tfg_jorge.design.screens.chat.PantallaListaChats
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaInicio
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaLogin
import com.example.proyecto_tfg_jorge.design.screens.login.PantallaRegister
import com.example.proyecto_tfg_jorge.design.screens.profile.PantallaContacto
import com.example.proyecto_tfg_jorge.design.screens.profile.PantallaPoliticaPrivacidad
import com.example.proyecto_tfg_jorge.design.screens.profile.PantallaSeguidores

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = "pantallaInicio") {
        composable("pantallaInicio") { PantallaInicio(navHostController) }
        composable("pantallaLogin") { PantallaLogin(navHostController) }
        composable("pantallaRegister") { PantallaRegister(navHostController) }
        composable("pantallaMenu") { PantallaMenu(navHostController) }
        composable("pantallaTraining") { PantallaTraining(navHostController) }
        composable("pantallaSocialNetwork") { PantallaSocialNetwork(navHostController) }
        composable("pantallaProfile") { PantallaProfile(navHostController) }
        composable("pantalla_subir_publicacion") { PantallaSubirPublicacion(navHostController) }
        composable("seguidores") { PantallaSeguidores(navHostController) }
        composable("politica_privacidad") { PantallaPoliticaPrivacidad(navHostController) }
        composable("contacto") { PantallaContacto(navHostController) }
        composable("mensajes") {
            PantallaListaChats ( navHostController)
        }

        composable(
            "chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            PantallaChat(navController = navHostController, conversationId = conversationId)
        }
    }
    }







