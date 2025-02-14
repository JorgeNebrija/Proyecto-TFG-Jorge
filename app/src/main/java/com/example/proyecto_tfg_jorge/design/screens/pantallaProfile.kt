package com.example.proyecto_tfg_jorge.design.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.proyecto_tfg_jorge.design.components.Header
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaProfile(navController: NavHostController) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF00C853), Color.Black)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Header(navController)
            ProfileSection()
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection()
        }
    }
}

@Composable
fun ProfileSection() {
    var name by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf("Cargando...") }
    var phone by remember { mutableStateOf("Cargando...") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Recuperar datos del usuario
    LaunchedEffect(userId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(userId).get().await()
            name = userDoc.getString("name") ?: "Sin nombre"
            email = userDoc.getString("email") ?: "Sin email"
            phone = userDoc.getString("phone") ?: "Sin teléfono"
            imageUrl = userDoc.getString("imageUrl") // URL de la imagen
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                uploadImageToFirebase(it, userId) { downloadUrl ->
                    imageUrl = downloadUrl
                    FirebaseFirestore.getInstance().collection("users").document(userId!!)
                        .update("imageUrl", downloadUrl)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable {
                    imagePickerLauncher.launch("image/*")
                }
        ) {
            if (imageUrl != null) {
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Imagen de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Insertar imagen",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("$email", fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("$phone", fontSize = 14.sp, color = Color.White)
    }
}

private fun uploadImageToFirebase(uri: Uri, userId: String?, onComplete: (String) -> Unit) {
    if (userId == null) return
    val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onComplete(downloadUri.toString())
        }
    }
}

@Composable
fun SettingsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingItem("Editar información de perfil")
        SettingItem("Notificaciones", "ON")
        SettingItem("Idioma", "Español")
        SettingItem("Tema", "Modo claro")
        SettingItem("Contáctanos")
        SettingItem("Política de privacidad")
    }
    }

@Composable
fun SettingItem(title: String, value: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF333333), shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = Color(0xFF00C853))
        if (value != null) {
            Text(value, fontSize = 14.sp, color = Color.Gray)
        }
    }
}
