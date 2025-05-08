package com.example.proyecto_tfg_jorge.design.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.data.base64ToBitmap
import com.example.proyecto_tfg_jorge.data.uriToBase64
import com.example.proyecto_tfg_jorge.design.components.Header
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Pantalla principal de Perfil, con todo lo solicitado.
 */
@Composable
fun PantallaProfile(navController: NavHostController) {
    var editarPerfil by remember { mutableStateOf(false) }
    // La variable showResetDialog se usará para el diálogo de cambio de contraseña

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Header(navController)
            ProfileSectionCompleta(
                editarPerfil = editarPerfil,
                onPerfilGuardado = { editarPerfil = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionCompleta(
                onEditarPerfilClick = { editarPerfil = true },
                navController = navController
            )
        }
    }
}

@Composable
fun ProfileSectionCompleta(
    editarPerfil: Boolean,
    onPerfilGuardado: () -> Unit
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()

    // Escucha en tiempo real los cambios del documento del usuario
    DisposableEffect(userId) {
        if (userId == null) {
            onDispose { }
        } else {
            val registration: ListenerRegistration = db.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        name = snapshot.getString("name") ?: ""
                        email = snapshot.getString("email") ?: ""
                        phone = snapshot.getString("phone") ?: ""
                        imageBase64 = snapshot.getString("imageBase64")
                    }
                }
            onDispose {
                registration.remove()
            }
        }
    }

    // Actualizar token FCM automáticamente
    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                db.collection("users").document(userId).update("fcmToken", token)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val base64 = uriToBase64(it, context)
                imageBase64 = base64
                if (userId != null) {
                    db.collection("users").document(userId)
                        .update("imageBase64", base64)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen de perfil
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .clickable {
                    // Seleccionar una nueva imagen
                    imagePickerLauncher.launch("image/*")
                }
                .shadow(10.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(imageBase64) {
                imageBase64?.let { base64ToBitmap(it) }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Seleccionar imagen", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Modo edición o modo lectura
        if (editarPerfil) {
            EditableField(label = "Nombre", value = name) { name = it }
            EditableField(label = "Email", value = email) { email = it }
            EditableField(label = "Teléfono", value = phone) { phone = it }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00E676))
                    .clickable {
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .update(
                                    mapOf(
                                        "name" to name,
                                        "email" to email,
                                        "phone" to phone
                                    )
                                )
                                .addOnSuccessListener {
                                    Toast
                                        .makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT)
                                        .show()
                                    onPerfilGuardado()
                                }
                        }
                    }
                    .padding(vertical = 10.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "Guardar cambios",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        } else {
            // Vista de solo lectura
            Text(
                text = name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "", value = email)
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(label = "", value = phone)
        }
    }
}

@Composable
fun SettingsSectionCompleta(
    onEditarPerfilClick: () -> Unit,
    navController: NavHostController
) {
    // Estado para el switch de notificaciones
    var notificacionesActivas by remember { mutableStateOf(true) }
    // Estados para los diálogos: cambio de contraseña y cierre de sesión
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            val doc = db.collection("users").document(userId).get().await()
            notificacionesActivas = doc.getBoolean("notificationsEnabled") ?: true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón para editar información de perfil
        SettingItem(
            icon = Icons.Default.Settings,
            title = "Editar información de perfil"
        ) {
            onEditarPerfilClick()
        }

        // Switch de notificaciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333), shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notificaciones", fontSize = 16.sp, color = Color(0xFF00C853))
            }
            Switch(
                checked = notificacionesActivas,
                onCheckedChange = {
                    notificacionesActivas = it
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .update("notificationsEnabled", it)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFF00E676),
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }

        // Opción para cambiar contraseña
        SettingItem(
            icon = Icons.Default.Lock,
            title = "Cambio de contraseña"
        ) {
            showResetDialog = true
        }

        // Ir a pantalla de Seguidores/Seguidos
        SettingItem(
            icon = Icons.Default.Person,
            title = "Seguidores"
        ) {
            navController.navigate("seguidores") // Navega a la pantalla correspondiente
        }

        // Contacto
        SettingItem(
            icon = Icons.Default.Email,
            title = "Contáctanos"
        ) {
            navController.navigate("contacto")
        }

        // Opción para cerrar sesión (nueva interfaz de alerta)
        SettingItem(
            icon = Icons.Default.Close,
            title = "Cerrar sesión"
        ) {
            showLogoutDialog = true
        }
    }

    // Diálogo para cambio de contraseña
    if (showResetDialog) {
        CambiarContrasenaDialog(
            onDismiss = { showResetDialog = false },
            navController = navController
        )
    }

    // Diálogo para confirmar el cierre de sesión usando AlertDialog (nueva interfaz)
    if (showLogoutDialog) {
        CerrarSesionDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                showLogoutDialog = false
                // Navega a la pantalla de login y limpia la pila de navegación.
                navController.navigate("pantallaLogin") {
                    popUpTo("pantallaProfile") { inclusive = true }
                }
            }
        )
    }
}

/** Composable para cada ítem de configuración en la pantalla. */
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF333333), shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF00C853),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 16.sp, color = Color(0xFF00C853))
    }
}

/** Campo de texto editable con estilo en modo oscuro. */
@Composable
fun EditableField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        label = {
            Text(text = label, color = Color.White)
        },
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00C853),
            unfocusedBorderColor = Color(0xFF444444),
            cursorColor = Color(0xFF00C853),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.LightGray,
            disabledTextColor = Color.White,
            unfocusedContainerColor = Color(0xFF1A1A1A),
            focusedContainerColor = Color(0xFF1A1A1A)
        )
    )
}

/** Muestra una fila de etiqueta-valor. */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color(0xFF00E676)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = value, fontSize = 16.sp, color = Color.White)
    }
}

/** Diálogo para cambiar contraseña. */
@Composable
fun CambiarContrasenaDialog(
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Cambiar contraseña",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ingresa tu correo para enviarte un enlace de recuperación. Si no lo recibes, revisa tu carpeta de spam.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFF00E676)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    FirebaseAuth.getInstance()
                                        .sendPasswordResetEmail(email)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Correo de recuperación enviado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onDismiss()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Error: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Por favor, ingresa un correo válido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

/** Diálogo para confirmar el cierre de sesión usando AlertDialog (nueva interfaz). */
@Composable
fun CerrarSesionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = false),
        title = {
            Text(
                text = "¿Estás seguro de cerrar sesión?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Perderás tu sesión actual y tendrás que iniciar sesión nuevamente.",
                color = Color.LightGray
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("Cerrar sesión", color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancelar", color = Color(0xFF00E676))
            }
        },
        // Opcional: puedes ajustar los colores del contenedor del AlertDialog
        containerColor = Color(0xFF2A2A2A),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
