package com.example.proyecto_tfg_jorge.design.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.data.actualizarMeGusta
import com.example.proyecto_tfg_jorge.data.base64ToBitmap
import com.example.proyecto_tfg_jorge.data.eliminarMeGusta
import com.example.proyecto_tfg_jorge.data.formatearFecha
import com.example.proyecto_tfg_jorge.data.obtenerPublicacionesParaTi
import com.example.proyecto_tfg_jorge.data.obtenerTodasLasPublicaciones
import com.example.proyecto_tfg_jorge.data.subirComentario
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header
import com.example.proyecto_tfg_jorge.design.notifications.FCMSender
import com.example.proyecto_tfg_jorge.design.notifications.NotificationData
import com.example.proyecto_tfg_jorge.design.notifications.PushNotification
import com.example.proyecto_tfg_jorge.models.Publicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun PantallaMenu(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        val opciones = listOf("Para ti", "Explorar")
        var seleccion by remember { mutableStateOf("Para ti") }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        var publicaciones by remember { mutableStateOf(listOf<Publicacion>()) }

        LaunchedEffect(seleccion) {
            publicaciones = if (seleccion == "Para ti") {
                obtenerPublicacionesParaTi(userId)
            } else {
                obtenerTodasLasPublicaciones().shuffled()
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Header(navController)

            // Selector "Para ti / Explorar"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                opciones.forEach { opcion ->
                    val seleccionado = seleccion == opcion
                    Text(
                        text = opcion,
                        color = if (seleccionado) Color.White else Color.Gray,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (seleccionado) Color(0xFF00C853) else Color.Transparent)
                            .clickable { seleccion = opcion }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                item {
                    BannerInspiracional()
                }

                items(publicaciones) { publicacion ->
                    PublicacionItem(publicacion, userId, navController)
                }
            }
        }
    }
}
    @Composable
fun PublicacionItem(publicacion: Publicacion, userId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var meGusta by remember { mutableStateOf(publicacion.meGusta.coerceAtLeast(0)) }
    var yaDioLike by remember { mutableStateOf(false) }
    var estaSiguiendo by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    var fotoPerfil by remember { mutableStateOf<String?>(null) }
    var comentario by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf(listOf<Triple<String, String, String>>()) }
    var verComentarios by remember { mutableStateOf(false) }
    var mostrarInputComentario by remember { mutableStateOf(false) }
    val cantidadComentarios = comentarios.size
    val context = LocalContext.current
    var imagenAmpliada by remember { mutableStateOf(false) }


    val scale = remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale.value,
        animationSpec = tween(durationMillis = 300),
        label = "scaleAnimation"
    )

    LaunchedEffect(publicacion.id) {
        val userDoc = db.collection("users").document(publicacion.userId).get().await()
        nombreUsuario = userDoc.getString("name") ?: publicacion.userId
        fotoPerfil = userDoc.getString("imageBase64")

        val followDoc = db.collection("followers")
            .whereEqualTo("seguidorId", userId)
            .whereEqualTo("seguidoId", publicacion.userId)
            .get().await()
        estaSiguiendo = !followDoc.isEmpty

        val likeDoc = db.collection("likes").document("${userId}_${publicacion.id}").get().await()
        yaDioLike = likeDoc.exists()

        val coms = db.collection("comentarios")
            .whereEqualTo("publicacionId", publicacion.id)
            .get().await()
        comentarios = coms.documents
            .filter { !it.getString("texto").isNullOrBlank() }
            .map {
                val uid = it.getString("userId") ?: ""
                val texto = it.getString("texto") ?: ""
                val fecha = it.getTimestamp("fecha")?.toDate()?.let { date -> formatearFecha(date) } ?: "Sin fecha"
                val nombreComentador = db.collection("users").document(uid).get().await()
                    .getString("name") ?: uid

                Triple(nombreComentador, texto, fecha)
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header con foto, nombre y botón seguir
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarBitmap = fotoPerfil?.let { base64ToBitmap(it) }

                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_defecto),
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text("@$nombreUsuario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(publicacion.ubicacion, color = Color.Gray, fontSize = 12.sp)
                    }
                }

                if (publicacion.userId != userId && !estaSiguiendo) {
                    Text(
                        text = "Seguir",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color(0xFF00C853), shape = RoundedCornerShape(10.dp))
                            .clickable {
                                val follow = hashMapOf("seguidorId" to userId, "seguidoId" to publicacion.userId)
                                scope.launch {
                                    db.collection("followers").add(follow)
                                    estaSiguiendo = true
                                    db.collection("users").document(publicacion.userId).get()
                                        .addOnSuccessListener { document ->
                                            val token = document.getString("fcmToken")
                                            val nombreSeguidor = FirebaseAuth.getInstance().currentUser?.displayName ?: "Nuevo seguidor"
                                            if (!token.isNullOrEmpty()) {
                                                val notification = PushNotification(
                                                    NotificationData("¡Tienes un nuevo seguidor!", "$nombreSeguidor ha empezado a seguirte"),
                                                    to = token,
                                                    toUserId = publicacion.userId


                                                )
                                                FCMSender.sendPushNotification(context, notification)
                                            }
                                        }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            base64ToBitmap(publicacion.imagenBase64)?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { imagenAmpliada = true }
                )
            }
            if (imagenAmpliada) {
                Dialog(onDismissRequest = { imagenAmpliada = false }) {
                    base64ToBitmap(publicacion.imagenBase64)?.let {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .width(300.dp) // Puedes ajustar el tamaño aquí
                                    .clickable { imagenAmpliada = false }
                            )
                        }
                    }
                }


        }




            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(publicacion.titulo, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(publicacion.descripcion, color = Color(0xFFDDDDDD), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(publicacion.fecha, color = Color.Gray, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (yaDioLike) Color.Red else Color.Gray,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
                            .clickable {
                                scope.launch {
                                    if (!yaDioLike) {
                                        scale.value = 1.3f
                                        delay(100)
                                        scale.value = 1f
                                        meGusta++
                                        actualizarMeGusta(publicacion.id, userId)
                                        yaDioLike = true

                                        if (publicacion.userId != userId) {
                                            val autorDoc = db.collection("users").document(publicacion.userId).get().await()
                                            val token = autorDoc.getString("fcmToken")
                                            val nombreUsuario = FirebaseAuth.getInstance().currentUser?.displayName ?: "Alguien"

                                            if (!token.isNullOrEmpty()) {
                                                val notification = PushNotification(
                                                    NotificationData(
                                                        title = "¡Han dado like a tu publicación!",
                                                        body = "$nombreUsuario ha indicado que le gusta tu publicación"
                                                    ),
                                                    to = token,
                                                    toUserId = publicacion.userId


                                                )
                                                FCMSender.sendPushNotification(context, notification)
                                            }
                                        }
                                    } else {
                                        if (meGusta > 0) {
                                            meGusta--
                                            eliminarMeGusta(publicacion.id, userId)
                                            yaDioLike = false
                                        }
                                    }
                                }
                            }
                    )
                    if (meGusta > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$meGusta Me gusta", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cantidadComentarios > 0) {
                    Text(
                        text = if (verComentarios) "Ocultar comentarios ($cantidadComentarios)" else "Ver comentarios ($cantidadComentarios)",
                        color = Color(0xFF00E676),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { verComentarios = !verComentarios }
                    )
                }
                Text(
                    text = if (mostrarInputComentario) "Cancelar" else "Añadir comentario",
                    color = Color(0xFF00E676),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { mostrarInputComentario = !mostrarInputComentario }
                )
            }

            AnimatedVisibility(
                visible = verComentarios,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    comentarios.forEach { (usuario, texto, fecha) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("@$usuario", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(texto, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fecha, color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (mostrarInputComentario) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    placeholder = { Text("Escribe tu comentario...", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color.DarkGray,
                        cursorColor = Color.White
                    )
                )
                Text(
                    text = "Publicar",
                    color = Color(0xFF00E676),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                        .clickable {
                            if (comentario.isNotBlank()) {
                                scope.launch {
                                    subirComentario(userId, publicacion.id, comentario)
                                    comentario = ""
                                    mostrarInputComentario = false

                                    // Notificación comentario
                                    val token = db.collection("users").document(publicacion.userId).get().await()
                                        .getString("fcmToken")
                                    val nombreComentador = db.collection("users").document(userId).get().await()
                                        .getString("name") ?: "Alguien"
                                    if (!token.isNullOrEmpty() && publicacion.userId != userId) {
                                        val notification = PushNotification(
                                            NotificationData("Nueva actividad", "$nombreComentador dejó un comentario"),
                                            to = token,
                                            toUserId = publicacion.userId

                                        )
                                        FCMSender.sendPushNotification(context, notification)
                                    }

                                    val nuevos = db.collection("comentarios")
                                        .whereEqualTo("publicacionId", publicacion.id)
                                        .get().await()

                                    comentarios = nuevos.documents
                                        .filter { !it.getString("texto").isNullOrBlank() }
                                        .map {
                                            val uid = it.getString("userId") ?: ""
                                            val texto = it.getString("texto") ?: ""
                                            val fecha = it.getTimestamp("fecha")?.toDate()
                                                ?.let { date -> formatearFecha(date) } ?: "Sin fecha"
                                            val nombreComentador = db.collection("users").document(uid).get().await()
                                                .getString("name") ?: uid
                                            Triple(nombreComentador, texto, fecha)
                                        }
                                    verComentarios = true
                                }
                            }
                        }
                )
            }
        }
    }
}


@Composable
fun BannerInspiracional() {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(20.dp)) // borde igual que las publicaciones
            .height(180.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.montana3),
            contentDescription = "Banner motivacional",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible.value,
                enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -40 }),
            ) {
                Text(
                    text = "Comparte tu pasión por la aventura",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}
