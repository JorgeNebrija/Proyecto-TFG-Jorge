package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.data.base64ToBitmap
import com.example.proyecto_tfg_jorge.data.eliminarPublicacion
import com.example.proyecto_tfg_jorge.data.obtenerPublicacionesUsuario
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header
import com.example.proyecto_tfg_jorge.models.Publicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaSocialNetwork(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var publicaciones by remember { mutableStateOf(listOf<Publicacion>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        publicaciones = obtenerPublicacionesUsuario(userId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)) // Fondo sólido oscuro
        )
        {
            // Encabezado elegante
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mis Publicaciones",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aquí verás tus rutas y experiencias publicadas",
                        fontSize = 14.sp,
                        color = Color(0xFFBDBDBD)
                    )
                }

                IconButton(
                    onClick = { navController.navigate("pantalla_subir_publicacion") },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF00E676), shape = RoundedCornerShape(50))
                        .border(1.dp, Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Subir publicación",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }


            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Espacio para la barra de navegación
            ) {
                items(publicaciones) { publicacion ->
                    PublicacionItemUsuario(publicacion) { publicacionId ->
                        coroutineScope.launch {
                            eliminarPublicacion(publicacionId)
                            publicaciones = obtenerPublicacionesUsuario(userId) // Actualizar lista
                        }
                    }
                }
            }
        }
    }

    // Barra de navegación inferior
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomNavigationBar(navController)
    }
}
@Composable
fun PublicacionItemUsuario(publicacion: Publicacion, onEliminar: (String) -> Unit) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarDetalles by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { mostrarDetalles = true },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Box(modifier = Modifier.fillMaxWidth()) {
                // Imagen de la publicación
                val imageBitmap = base64ToBitmap(publicacion.imagenBase64)
                imageBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Imagen de la publicación",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // Botón eliminar flotante en la imagen
                IconButton(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color(0x99000000), shape = RoundedCornerShape(50))
                        .size(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.papelera),
                        contentDescription = "Eliminar publicación",
                        modifier = Modifier.size(20.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Red)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = publicacion.titulo,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Descripción
            Text(
                text = publicacion.descripcion,
                fontSize = 16.sp,
                color = Color(0xFFDADADA),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chips de fecha y ubicación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${publicacion.fecha}",
                        color = Color(0xFF00E676),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = " ${publicacion.ubicacion}",
                        color = Color(0xFF00E676),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogo) {
        DialogoConfirmacionEliminar(
            onConfirmar = {
                onEliminar(publicacion.id)
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }
    if (mostrarDetalles) {
        DialogoDetallesPublicacion(publicacion.id) {
            mostrarDetalles = false
        }
    }

}
@Composable
fun DialogoConfirmacionEliminar(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Dialog(onDismissRequest = onCancelar) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B1B1B))
                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Estás seguro de que quieres eliminar esta publicación?", fontSize = 18.sp, color = Color.White)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = onCancelar,
                        colors = ButtonDefaults.buttonColors(Color(0xFF444444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = Color(0xFF00E676) , fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onConfirmar()
                            onCancelar()
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF444444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar", color = Color(0xFF00E676), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun DialogoDetallesPublicacion(publicacionId: String, onDismiss: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var likes by remember { mutableStateOf(listOf<String>()) }
    var comentarios by remember { mutableStateOf(listOf<Triple<String, String, String>>()) }

    LaunchedEffect(publicacionId) {
        val likesSnapshot = db.collection("likes")
            .whereEqualTo("liked", true)
            .get()
            .await()

        likes = likesSnapshot.documents.filter {
            it.id.contains(publicacionId)
        }.mapNotNull {
            val userId = it.id.split("_").firstOrNull()
            if (userId != null) {
                val usuarioDoc = db.collection("users").document(userId).get().await()
                usuarioDoc.getString("name")
            } else null
        }

        val comentariosSnapshot = db.collection("comentarios")
            .whereEqualTo("publicacionId", publicacionId)
            .get().await()

        comentarios = comentariosSnapshot.documents.mapNotNull {
            val texto = it.getString("texto") ?: return@mapNotNull null
            val fecha = it.getTimestamp("fecha")?.toDate()?.toString() ?: "Sin fecha"
            val userId = it.getString("userId") ?: return@mapNotNull null
            val nombre = db.collection("users").document(userId).get().await().getString("name") ?: "Desconocido"
            Triple(nombre, texto, fecha)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B1B1B))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                Text(
                    "Detalles de la publicación",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Me gusta", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                if (likes.isEmpty()) {
                    Text("Nadie ha dado like todavía", color = Color.Gray, fontSize = 14.sp)
                } else {
                    likes.forEach { nombre ->
                        Text("@$nombre", color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Comentarios", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                if (comentarios.isEmpty()) {
                    Text("Sin comentarios", color = Color.Gray, fontSize = 14.sp)
                } else {
                    comentarios.forEach { (nombre, texto, fecha) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("@$nombre", fontWeight = FontWeight.Bold, color = Color(0xFF00E676), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(texto, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(fecha, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
