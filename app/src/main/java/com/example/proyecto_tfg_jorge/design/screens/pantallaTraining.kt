package com.example.proyecto_tfg_jorge.design.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun PantallaTraining(navController: NavHostController) {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Variables
    var isTracking = remember { false }
    val startTime = remember { System.currentTimeMillis() }
    var coordinates = remember { mutableListOf<Location>() } // Lista para almacenar las coordenadas
    var distance = remember { 0f } // Distancia total

    // Verificar permisos
    val permissionGranted = ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // LocationListener para actualizaciones de ubicación
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (isTracking) {
                // Agregar la ubicación a la lista
                coordinates.add(location)

                // Si hay más de una coordenada, calculamos la distancia
                if (coordinates.size > 1) {
                    val lastLocation = coordinates[coordinates.size - 2]
                    distance += lastLocation.distanceTo(location) // Calcular distancia entre puntos consecutivos
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    LaunchedEffect(key1 = permissionGranted) {
        if (permissionGranted) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 10f, locationListener
            )
        }
    }

    // Función para guardar la ruta en Firestore
    fun saveRoute() {
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime) / 1000  // Duración en segundos

        val firestore = FirebaseFirestore.getInstance()

        // Crear un mapa con los datos a guardar
        val routeData = hashMapOf(
            "startTime" to startTime,
            "endTime" to endTime,
            "duration" to duration,
            "distance" to distance,
            "coordinates" to coordinates.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
        )

        // Guardar los datos en Firestore
        firestore.collection("rutas")
            .add(routeData)
            .addOnSuccessListener { documentReference ->
                println("Ruta guardada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error al guardar la ruta: $e")
            }
    }

    // Mostrar UI
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF00C853))) {
        // Cabecera
        Header(navController = rememberNavController())

        // Contenedor para el mapa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)  // Ajusta la altura del mapa
                .padding(8.dp)
        ) {
            // Mapa interactivo
            val mapView = remember { MapView(context) }
            mapView.setMultiTouchControls(true)

            val locationOverlay = MyLocationNewOverlay(mapView)
            locationOverlay.enableMyLocation()
            mapView.overlays.add(locationOverlay)

            AndroidView(factory = { mapView })
        }

        // Espaciado entre mapa y contenido
        Spacer(modifier = Modifier.height(16.dp))

        // Sección de control de entrenamiento
        Column(modifier = Modifier.padding(16.dp)) {
            // Botón para iniciar/detener el entrenamiento
            Button(
                onClick = {
                    isTracking = !isTracking // Iniciar o detener el seguimiento
                    if (isTracking) {
                        coordinates.clear() // Limpiar las coordenadas si se empieza de nuevo
                        distance = 0f // Reiniciar la distancia
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text(text = if (isTracking) "Detener Entrenamiento" else "Iniciar Entrenamiento")
            }

            // Mostrar distancia y duración
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Distancia: $distance m",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
            Text(
                text = "Duración: ${(System.currentTimeMillis() - startTime) / 1000} s",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )

            // Botón para guardar la ruta en Firestore
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { saveRoute() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text(text = "Guardar Ruta")
            }
        }

        // BottomNavigationBar
        BottomNavigationBar(navController = rememberNavController())
    }
}
