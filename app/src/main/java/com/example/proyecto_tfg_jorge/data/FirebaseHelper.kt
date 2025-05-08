package com.example.proyecto_tfg_jorge.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.proyecto_tfg_jorge.models.Publicacion
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Función para obtener publicaciones de un usuario específico
suspend fun obtenerPublicacionesUsuario(userId: String): List<Publicacion> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("publicaciones")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        snapshot.documents.map { doc ->
            Publicacion(
                id = doc.id,
                userId = userId,
                titulo = doc.getString("titulo") ?: "",
                imagenUrl = doc.getString("imagenUrl") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                ubicacion = doc.getString("ubicacion") ?: "Ubicación no disponible",
                fecha = doc.getString("fecha") ?: "Fecha no disponible",
                meGusta = doc.getLong("meGusta")?.toInt() ?: 0,
                imagenBase64 = doc.getString("imagenBase64") ?: ""
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// Función para obtener todas las publicaciones
suspend fun obtenerTodasLasPublicaciones(): List<Publicacion> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("publicaciones").orderBy("timestamp").get().await()
        snapshot.documents.map { doc ->
            Publicacion(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                titulo = doc.getString("titulo") ?: "",
                imagenUrl = doc.getString("imagenUrl") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                ubicacion = doc.getString("ubicacion") ?: "Ubicación no disponible",
                fecha = doc.getString("fecha") ?: "Fecha no disponible",
                meGusta = doc.getLong("meGusta")?.toInt() ?: 0,
                imagenBase64 = doc.getString("imagenBase64") ?: ""
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// Función para subir una publicación a Firestore
suspend fun subirPublicacionFirestore(
    userId: String,
    titulo: String,
    descripcion: String,
    imagenUri: Uri?,
    ubicacion: String,
    fecha: String,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()

    // Convertir imagen a Base64
    val imagenBase64 = imagenUri?.let { uriToBase64(it, context) } ?: ""

    val nuevaPublicacion = hashMapOf(
        "userId" to userId,
        "titulo" to titulo,
        "descripcion" to descripcion,
        "imagenBase64" to imagenBase64, // Se guarda la imagen en Base64
        "ubicacion" to ubicacion, // Se guarda la ubicación
        "fecha" to fecha, // Se guarda la fecha
        "meGusta" to 0,
        "timestamp" to FieldValue.serverTimestamp() // Fecha y hora del servidor
    )

    try {
        db.collection("publicaciones").add(nuevaPublicacion).await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Función para convertir una imagen URI a Base64
fun uriToBase64(uri: Uri, context: Context): String {
    val inputStream = try {
        context.contentResolver.openInputStream(uri)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    if (inputStream == null) {
        throw IOException("No se pudo abrir el stream de la imagen")
    }
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream) // Compresión para optimizar almacenamiento
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Función para convertir una imagen Base64 a Bitmap para mostrarla en la UI
fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

// Función para actualizar los "me gusta", asegurando que cada usuario solo pueda dar uno
fun actualizarMeGusta(idPublicacion: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val likeRef = db.collection("likes").document("${userId}_${idPublicacion}")

    likeRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            // El usuario no ha dado like antes, se incrementa el contador
            db.collection("publicaciones").document(idPublicacion)
                .update("meGusta", FieldValue.increment(1))

            // Se registra el like en la colección 'likes'
            likeRef.set(mapOf("liked" to true))
        }
    }
}
suspend fun eliminarPublicacion(publicacionId: String) {
    val db = FirebaseFirestore.getInstance()
    try {
        db.collection("publicaciones").document(publicacionId).delete().await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// FirebaseHelper.kt o donde gestiones Firestore
suspend fun eliminarMeGusta(idPublicacion: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val likeRef = db.collection("likes").document("${userId}_$idPublicacion")

    try {
        // Eliminar el documento de like
        likeRef.delete().await()

        // Decrementar el contador de me gusta
        val publicacionRef = db.collection("publicaciones").document(idPublicacion)
        publicacionRef.update("meGusta", FieldValue.increment(-1)).await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun formatearFecha(fecha: Date): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    formato.timeZone = TimeZone.getTimeZone("Europe/Madrid")
    return formato.format(fecha)
}

suspend fun subirComentario(userId: String, publicacionId: String, texto: String) {
    val db = FirebaseFirestore.getInstance()
    val nuevoComentario = hashMapOf(
        "userId" to userId,
        "publicacionId" to publicacionId,
        "texto" to texto,
        "fecha" to FieldValue.serverTimestamp()
    )

    try {
        db.collection("comentarios").add(nuevoComentario).await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
suspend fun obtenerPublicacionesParaTi(userId: String): List<Publicacion> {
    val db = FirebaseFirestore.getInstance()
    val seguidoresSnapshot = db.collection("followers")
        .whereEqualTo("seguidorId", userId)
        .get()
        .await()

    val seguidosIds = seguidoresSnapshot.documents.mapNotNull { it.getString("seguidoId") }

    if (seguidosIds.isEmpty()) return emptyList()

    val publicacionesSnapshot = db.collection("publicaciones")
        .whereIn("userId", seguidosIds)
        .get()
        .await()

    return publicacionesSnapshot.documents.mapNotNull { it.toObject(Publicacion::class.java)?.copy(id = it.id) }
}

