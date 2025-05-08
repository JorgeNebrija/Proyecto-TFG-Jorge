package com.example.proyecto_tfg_jorge.design.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.data.base64ToBitmap
import com.example.proyecto_tfg_jorge.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeguidores(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Datos a mostrar
    var seguidores by remember { mutableStateOf(listOf<User>()) }
    var seguidos by remember { mutableStateOf(listOf<User>()) }


    // Buscador
    var searchText by remember { mutableStateOf("") }

    // Pestaña seleccionada: 0 = Seguidores, 1 = Seguidos
    var selectedTab by remember { mutableStateOf(0) }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        // IDs de seguidores => seguidorId
        val segDocs = db.collection("followers")
            .whereEqualTo("seguidoId", userId)
            .get().await()
        val seguidorIds = segDocs.documents.mapNotNull { it.getString("seguidorId") }

        // IDs de seguidos => seguidoId
        val sigDocs = db.collection("followers")
            .whereEqualTo("seguidorId", userId)
            .get().await()
        val seguidoIds = sigDocs.documents.mapNotNull { it.getString("seguidoId") }

        seguidores = obtenerUsers(db, seguidorIds)
        seguidos = obtenerUsers(db, seguidoIds)
    }

    // Filtrar en tiempo real
    val filteredSeguidores = seguidores.filter { it.name.contains(searchText, ignoreCase = true) }
    val filteredSeguidos = seguidos.filter { it.name.contains(searchText, ignoreCase = true) }

    // Para las tabs
    val numSeguidores = seguidores.size
    val numSeguidos = seguidos.size
    val tabs = listOf("Seguidores" to numSeguidores, "Seguidos" to numSeguidos)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("pantallaProfile")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Seguimiento",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Buscador arriba
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(text = "Buscar...", color = Color.LightGray) },
                textStyle = TextStyle(color = Color.White),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color(0xFF00E676)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Pestañas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color(0xFF00E676)
            ) {
                tabs.forEachIndexed { index, (title, count) ->
                    Tab(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) Color(0xFF00E676) else Color.Gray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = count.toString(),
                                    color = if (selectedTab == index) Color(0xFF00E676) else Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }

            // Contenido según la pestaña
            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(filteredSeguidores) { user ->
                        FollowerItem(
                            userBasic   = user,
                            isFollower  = true,
                            currentUser = userId,
                            onRemove    = { uid ->
                                seguidores = seguidores.filter { it.uid != uid }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(filteredSeguidos) { user ->
                        FollowerItem(
                            userBasic   = user,
                            isFollower  = false,
                            currentUser = userId,
                            onRemove    = { uid ->
                                seguidos = seguidos.filter { it.uid != uid }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FollowerItem(
    userBasic: User,
    isFollower: Boolean,
    currentUser: String,
    onRemove: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(userBasic.imageBase64) {
                    userBasic.imageBase64?.let { base64ToBitmap(it) }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = userBasic.name,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        val buttonText = if (isFollower) "Suprimir" else "Dejar de seguir"
        val buttonColor = if (isFollower) Color.Red else Color(0xFF00E676)

        Button(
            onClick = {
                scope.launch {
                    removeFollower(
                        otherUserId   = userBasic.uid,
                        currentUserId = currentUser,
                        isFollower    = isFollower
                    )
                    onRemove(userBasic.uid)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(buttonText, color = Color.Black, fontSize = 12.sp)
        }
    }
}

suspend fun obtenerUsers(db: FirebaseFirestore, ids: List<String>): List<User> {
    val users = mutableListOf<User>() // Lista vacía donde guardaremos los usuarios

    for (id in ids) {
        // Obtenemos el documento del usuario con ese ID
        val documento = db.collection("users").document(id).get().await()

        // Si el documento existe, lo convertimos a un objeto User
        if (documento.exists()) {
            val datos = documento.toObject(User::class.java)
            if (datos != null) {
                // Como el UID es el nombre del documento, lo añadimos manualmente
                users.add(datos.copy(uid = id))
            }
        }
    }

    return users // Devolvemos la lista de usuarios
}



suspend fun removeFollower(
    otherUserId: String,
    currentUserId: String,
    isFollower: Boolean
) {
    val db = FirebaseFirestore.getInstance()
    val query = if (isFollower) {
        db.collection("followers")
            .whereEqualTo("seguidorId", otherUserId)
            .whereEqualTo("seguidoId", currentUserId)
    } else {
        db.collection("followers")
            .whereEqualTo("seguidorId", currentUserId)
            .whereEqualTo("seguidoId", otherUserId)
    }
    val docs = query.get().await()
    for (doc in docs.documents) {
        doc.reference.delete()
    }
}
