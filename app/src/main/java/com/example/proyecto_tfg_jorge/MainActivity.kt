package com.example.proyecto_tfg_jorge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_tfg_jorge.design.components.loadLanguagePreference
import com.example.proyecto_tfg_jorge.design.components.updateLocale
import com.example.proyecto_tfg_jorge.navegation.NavigationWrapper

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar el idioma guardado al iniciar la app
        val languageCode = loadLanguagePreference(this)
        updateLocale(this, languageCode)

        // Establecer la interfaz con Jetpack Compose
        setContent {
            navHostController = rememberNavController()
            // Pasamos el navHostController al composable para la navegación
            NavigationWrapper(navHostController)
        }
    }
}
