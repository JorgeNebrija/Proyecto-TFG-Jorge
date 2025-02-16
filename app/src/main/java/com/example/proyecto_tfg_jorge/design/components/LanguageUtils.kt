package com.example.proyecto_tfg_jorge.design.components

import android.content.Context
import java.util.*

// Función para guardar el idioma seleccionado
fun saveLanguagePreference(context: Context, languageCode: String) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("language", languageCode).apply()
}

// Función para cargar el idioma guardado
fun loadLanguagePreference(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("language", "es") ?: "es"  // Valor por defecto: español
}

// Función para actualizar la localización de la aplicación
fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
