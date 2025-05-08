// app/src/main/java/com/example/proyecto_tfg_jorge/design/screens/chat/PantallaChat.kt
package com.example.proyecto_tfg_jorge.design.screens.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.example.proyecto_tfg_jorge.models.Message
import com.example.proyecto_tfg_jorge.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(
    navController: NavHostController,
    conversationId: String
) {
    // 1) IDs y estados
    val usuarioActualId = ChatHelper.obtenerUsuarioActual() ?: return
    var usuarioActualObj by remember { mutableStateOf<User?>(null) }
    var receptor by remember { mutableStateOf<User?>(null) }
    var mensajes by remember { mutableStateOf<List<Message>>(emptyList()) }
    var texto by remember { mutableStateOf("") }
    var fondoBase64 by remember { mutableStateOf<String?>(null) }
    var lastMsgTime   by remember { mutableStateOf<String?>(null) }
    var showEmojiPanel by remember { mutableStateOf(false) }
// Generamos la lista de emojis (puedes añadir más rangos)
    val emojiList = remember {
        val blocks = listOf(
            0x1F600..0x1F64F, // Emoticons
            0x1F300..0x1F5FF, // Misc Symbols & Pictographs
            0x1F680..0x1F6FF, // Transport & Map
            0x2600..0x26FF,   // Misc symbols
            0x2700..0x27BF    // Dingbats
        )
        blocks.flatMap { range ->
            range.mapNotNull { code ->
                runCatching { String(Character.toChars(code)) }.getOrNull()
            }
        }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 2) Cargar usuario actual (para su avatar pequeño)
    LaunchedEffect(usuarioActualId) {
        usuarioActualObj = ChatHelper.obtenerUsuarioPorId(usuarioActualId)
    }
    // 3) Cargar receptor y fondo al arrancar
    LaunchedEffect(conversationId) {
        receptor = ChatHelper.obtenerReceptor(conversationId, usuarioActualId)
        fondoBase64 = ChatHelper.obtenerFondo(conversationId)
            val doc = FirebaseFirestore
            .getInstance()
              .collection("conversations")
              .document(conversationId)
              .get()
              .await()
            lastMsgTime = doc.getTimestamp("lastMessageTime")
              ?.toDate()
              ?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
    }
    // 4) Escuchar mensajes en tiempo real
    DisposableEffect(conversationId) {
        val listener = ChatHelper.escucharMensajes(conversationId) { nuevos ->
            mensajes = nuevos
        }
        onDispose { listener.remove() }
    }

    // 5) Launcher para elegir fondo (galería)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            val baos = ByteArrayOutputStream().apply {
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
            }
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            scope.launch {
                ChatHelper.guardarFondo(conversationId, b64)
                fondoBase64 = b64
                Toast.makeText(context, "Fondo actualizado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // --- Fondo personalizado si existe ---
        fondoBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Column(
                            modifier = Modifier
                            .padding(start = 4.dp),
                        horizontalAlignment = Alignment.Start
                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // avatar receptor
                                receptor?.imageBase64?.let { b64 ->
                                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                } ?: Icon(
                                    Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    text = receptor?.name.takeIf { !it.isNullOrBlank() }
                                        ?: receptor?.email.orEmpty(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    maxLines = 1
                                )

                            }
                        }
                    },
                    actions = {
                        var menuAbierto by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuAbierto = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "Más",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(text = { Text("Eliminar chat") }, onClick = {
                                menuAbierto = false
                                scope.launch {
                                    ChatHelper.eliminarMensajes(conversationId)
                                    Toast.makeText(context, "Chat eliminado", Toast.LENGTH_SHORT).show()
                                }
                            })
                            DropdownMenuItem(text = { Text("Cambiar fondo") }, onClick = {
                                menuAbierto = false
                                launcher.launch("image/*")
                            })
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                )

            },
            bottomBar = {
                Surface(color = Color(0xFF1A1A1A), tonalElevation = 4.dp) {
                    Column {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { texto = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe un mensaje…", color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = Color(0xFF2C2C2C),
                                cursorColor = Color(0xFF00E676),
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showEmojiPanel = !showEmojiPanel }) {
                            Icon(
                                painter = painterResource(R.drawable.emoji),
                                contentDescription = "Emoji",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (texto.isNotBlank()) {
                                val textoEnviado = texto.trim() // guarda el texto antes de borrarlo
                                texto = "" // borra el campo para el usuario al instante
                                scope.launch {
                                    ChatHelper.enviarMensaje(
                                        conversationId = conversationId,
                                        senderId = usuarioActualId,
                                        texto = textoEnviado,
                                        context = context
                                    )
                                }
                            }
                        }) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                    }
                        AnimatedVisibility(
                            visible = showEmojiPanel,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF1A1A1A))
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(6),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    items(emojiList) { emoji ->
                                        Text(
                                            text = emoji,
                                            fontSize = 22.sp,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable { texto += emoji }
                                                .background(Color.Transparent),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }


                }


    },

            containerColor = Color.Transparent,
            content = { innerPadding ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Transparent),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mensajes) { msg ->
                        BurbujaMensajeConHora(
                            message = msg,
                            esMio = msg.senderId == usuarioActualId,
                            receptor = receptor,
                            usuarioActual = usuarioActualObj
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        )

    }

}

@Composable
private fun BurbujaMensajeConHora(
    message: Message,
    esMio: Boolean,
    receptor: User?,
    usuarioActual: User?
) {
    // 1) color único verde para todas las burbujas
    val colorBurbuja = Color(0xFF00E676)
    val colorTexto = Color.Black

    val sdf = SimpleDateFormat("HH:mm", Locale("es", "ES")).apply {
        timeZone = TimeZone.getDefault() // Usa la misma zona que en ListaChats
    }

    val hora = message.timestamp
        ?.let { sdf.format(it.toDate()) }
        .orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        // 3) avatar pequeño (izq o dch según sender)
        val b64 = if (esMio) usuarioActual?.imageBase64 else receptor?.imageBase64
        if (b64 != null) {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom)
            )
            Spacer(Modifier.width(4.dp))
        }

        // 4) la burbuja en sí
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colorBurbuja,
            modifier = Modifier.widthIn(min = 60.dp, max = 280.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                Text(text = message.text, color = colorTexto)
                Spacer(Modifier.height(2.dp))
                Text(text = hora, color = colorTexto, fontSize = 10.sp)
            }
        }
    }
}
