package com.example.proyecto_tfg_jorge.design.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.proyecto_tfg_jorge.data.subirPublicacionFirestore
import com.example.proyecto_tfg_jorge.ui.theme.textFieldColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSubirPublicacion(navController: NavHostController) {
    val contexto = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var titulo by remember { mutableStateOf(TextFieldValue("")) }
    var descripcion by remember { mutableStateOf(TextFieldValue("")) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var ubicacion by remember { mutableStateOf("Ubicación no seleccionada") }
    var fechaSeleccionada by remember { mutableStateOf("Fecha no seleccionada") }
    var mostrarDialogoUbicacion by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imagenUri = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()) // Habilitar scroll vertical
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Nueva Publicación",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Título
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = if (titulo.text.isNotBlank()) Color(0xFF00C853) else Color(0xFF444444),
                focusedLabelColor = Color(0xFF00C853),
                unfocusedLabelColor = if (titulo.text.isNotBlank()) Color(0xFF00C853) else Color.White,
                cursorColor = Color.White
            )
        )

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = if (descripcion.text.isNotBlank()) Color(0xFF00C853) else Color(0xFF444444),
                focusedLabelColor = Color(0xFF00C853),
                unfocusedLabelColor = if (descripcion.text.isNotBlank()) Color(0xFF00C853) else Color.White,
                cursorColor = Color.White
            )
        )

        // Imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = if (imagenUri != null) Color(0xFF00C853) else Color(0xFF444444),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imagenUri != null) {
                AsyncImage(
                    model = imagenUri,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Toca para añadir una foto", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { mostrarDialogoUbicacion = true },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853)),
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", tint = Color.White)
            }

            IconButton(
                onClick = {
                    val calendario = Calendar.getInstance()
                    DatePickerDialog(contexto, { _, year, month, day ->
                        fechaSeleccionada = "$day/${month + 1}/$year"
                    }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853)),
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Fecha", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (ubicacion != "Ubicación no seleccionada") Color(0xFF00C853) else Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ubicacion != "Ubicación no seleccionada") ubicacion else "Ubicación no seleccionada",
                        color = if (ubicacion != "Ubicación no seleccionada") Color.White else Color.Gray,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = if (fechaSeleccionada != "Fecha no seleccionada") Color(0xFF00C853) else Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (fechaSeleccionada != "Fecha no seleccionada") fechaSeleccionada else "Fecha no seleccionada",
                        color = if (fechaSeleccionada != "Fecha no seleccionada") Color.White else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Publicar
        Button(
            onClick = {
                if (titulo.text.isBlank() || descripcion.text.isBlank() || imagenUri == null) {
                    errorMensaje = "Todos los campos son obligatorios"
                } else {
                    errorMensaje = ""
                    coroutineScope.launch {
                        try {
                            subirPublicacionFirestore(
                                userId,
                                titulo.text,
                                descripcion.text,
                                imagenUri,
                                ubicacion,
                                fechaSeleccionada,
                                contexto
                            )
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMensaje = "Error al subir imagen. Intenta otra o reinicia la app."
                            e.printStackTrace()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            enabled = titulo.text.isNotBlank() && descripcion.text.isNotBlank() && imagenUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Publicar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (errorMensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(errorMensaje, color = Color.Red, fontSize = 14.sp)
        }
    }

    if (mostrarDialogoUbicacion) {
        DialogoUbicacion(
            onUbicacionConfirmada = { ubicacion = it },
            onDismiss = { mostrarDialogoUbicacion = false }
        )
    }
}
@Composable
fun DialogoUbicacion(
    onUbicacionConfirmada: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var ubicacionTexto by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B1B1B))
                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seleccionar Ubicación", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ubicacionTexto,
                    onValueChange = { ubicacionTexto = it },
                    label = { Text("Introduce la dirección", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(Color(0xFF444444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = Color.White, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (ubicacionTexto.isNotBlank()) {
                                onUbicacionConfirmada(ubicacionTexto)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF00C853)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}