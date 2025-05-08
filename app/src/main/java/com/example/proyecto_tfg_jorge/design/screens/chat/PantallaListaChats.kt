package com.example.proyecto_tfg_jorge.design.screens.chat

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.design.components.BottomNavigationBar
import com.example.proyecto_tfg_jorge.design.components.Header
import com.example.proyecto_tfg_jorge.models.Conversation
import com.example.proyecto_tfg_jorge.models.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaChats(navController: NavHostController) {
    val usuarioId = ChatHelper.obtenerUsuarioActual() ?: return
    var conversaciones by remember { mutableStateOf(listOf<Conversation>()) }
    var usuariosMap by remember { mutableStateOf(mapOf<String, User>()) }
    var seguidosSinConversacion by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(usuarioId) {
        ChatHelper.escucharConversaciones(usuarioId) { lista, mapa ->
            conversaciones = lista.sortedByDescending { it.lastMessageTime }
            usuariosMap = mapa
        }
        seguidosSinConversacion = ChatHelper.obtenerSeguidosSinConversacion(usuarioId)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(top = 16.dp)
            ) {
                Header(navController)
                Text(
                    text = "Conversaciones",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 8.dp)
                )
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            }
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = Color.Black
    ) { padding ->

        val chatsFiltrados = conversaciones.filter { conv ->
            val otherId = conv.participants.firstOrNull { it != usuarioId } ?: ""
            val user = usuariosMap[otherId]
            val nameMatch = user?.name?.contains(searchQuery, ignoreCase = true) == true
            val msgMatch = conv.lastMessage.contains(searchQuery, ignoreCase = true)
            searchQuery.isBlank() || nameMatch || msgMatch
        }

        val idsConversacion = conversaciones.flatMap { it.participants }.toSet()
        val seguidosFiltrados = seguidosSinConversacion.filter {
            it.name.contains(searchQuery, ignoreCase = true) && !idsConversacion.contains(it.uid)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatsFiltrados) { conv ->
                val otherId = conv.participants.first { it != usuarioId }
                val user = usuariosMap[otherId] ?: return@items
                val unread = conv.unreadCount > 0 && conv.lastSenderId != usuarioId

                ConversationCardCompact(
                    avatarBase64 = user.imageBase64,
                    name = user.name,
                    lastMessage = conv.lastMessage,
                    lastTime = conv.lastMessageTime,
                    unread = unread
                ) {
                    scope.launch {
                        ChatHelper.marcarConversacionComoLeida(conv.id)
                        navController.navigate("chat/${conv.id}")
                    }
                }
            }

            item {
                if (seguidosFiltrados.isNotEmpty()) {
                    Text(
                        "Personas que sigues",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            items(seguidosFiltrados) { user ->
                ConversationCardCompact(
                    avatarBase64 = user.imageBase64,
                    name = user.name,
                    lastMessage = "Toca para iniciar conversación",
                    lastTime = null,
                    unread = false
                ) {
                    scope.launch {
                        val conversationId = ChatHelper.iniciarConversacionSiNoExiste(usuarioId, user.uid)
                        navController.navigate("chat/$conversationId")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar...", color = Color(0xFFB0B0B0)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB0B0B0))
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(color = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color(0xFF00E676),
            containerColor = Color(0xFF1F1F1F),
            cursorColor = Color(0xFF00E676),
        )
    )
}

@Composable
fun ConversationCardCompact(
    avatarBase64: String?,
    name: String,
    lastMessage: String?,
    lastTime: Date?,
    unread: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("HH:mm", Locale("es", "ES")).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    val timeText = lastTime?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(avatarBase64, size = 44.dp)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
                Text(
                    text = lastMessage ?: "",
                    color = if (unread) Color(0xFF00E676) else Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (unread) {
                    Text(
                        text = "Tienes mensajes no leídos",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            if (lastTime != null) {
                Text(timeText, color = Color(0xFF888888), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ProfileImage(imageBase64: String?, size: Dp = 44.dp) {
    val bitmap = remember(imageBase64) {
        imageBase64?.let {
            val data = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(data, 0, data.size)
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.DarkGray)
        )
    }
}
