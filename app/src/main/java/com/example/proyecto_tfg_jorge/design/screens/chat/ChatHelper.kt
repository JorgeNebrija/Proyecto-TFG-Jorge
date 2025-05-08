package com.example.proyecto_tfg_jorge.design.screens.chat

import android.content.Context
import com.example.proyecto_tfg_jorge.design.notifications.FCMSender
import com.example.proyecto_tfg_jorge.design.notifications.NotificationData
import com.example.proyecto_tfg_jorge.design.notifications.PushNotification
import com.example.proyecto_tfg_jorge.models.Conversation
import com.example.proyecto_tfg_jorge.models.Message
import com.example.proyecto_tfg_jorge.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Helper para manejar chats: envío, recepción, notificaciones y funciones de conversación.
 */
object ChatHelper {
    // Instancia única de Firestore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /** Obtiene el UID del usuario actualmente autenticado. */
    fun obtenerUsuarioActual(): String? = FirebaseAuth.getInstance().currentUser?.uid

    /** Carga un mapa de usuarios por sus UIDs. */
    suspend fun cargarUsuariosPorIds(ids: List<String>): Map<String, User> {
        val usuarios = mutableMapOf<String, User>()
        for (uid in ids) {
            try {
                val doc = db.collection("users").document(uid).get().await()
                doc.toObject(User::class.java)?.let { user ->
                    usuarios[uid] = user.copy(uid = uid)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return usuarios
    }


    /** Obtiene el usuario receptor en una conversación dada. */
    suspend fun obtenerReceptor(conversationId: String, miId: String): User? {
        val doc = db.collection("conversations").document(conversationId).get().await()
        val participants = doc.get("participants") as? List<String> ?: return null
        val otroId = participants.firstOrNull { it != miId } ?: return null
        val snap = db.collection("users").document(otroId).get().await()
        return snap.toObject(User::class.java)?.copy(uid = otroId)
    }


    /** Envía un mensaje y actualiza la conversación, además dispara notificación push. */
    suspend fun enviarMensaje(
        conversationId: String,
        senderId: String,
        texto: String,
        context: Context
    ) {
        val timestamp = Timestamp.now()
        // 1) Crear mensaje
        val data = mapOf(
            "conversationId" to conversationId,
            "senderId" to senderId,
            "text" to texto,
            "timestamp" to timestamp
        )
        db.collection("messages").add(data).await()
        // 2) Actualizar metadatos conversación
        db.collection("conversations").document(conversationId)
            .update(
                mapOf(
                    "lastMessage" to texto,
                    "lastMessageTime" to timestamp,
                    "lastSenderId" to senderId
                )
            ).await()
        // 3) Notificación push al receptor
        val conv = db.collection("conversations").document(conversationId).get().await()
        val participants = conv.get("participants") as? List<String> ?: return
        val receptorId = participants.firstOrNull { it != senderId } ?: return
        val receptorDoc = db.collection("users").document(receptorId).get().await()
        val fcmToken = receptorDoc.getString("fcmToken") ?: return
        val senderDoc = db.collection("users").document(senderId).get().await()
        val senderName = senderDoc.getString("name") ?: "Nuevo mensaje"
        val notification = PushNotification(
            data = NotificationData(title = senderName, body = texto),
            to = fcmToken,
            toUserId = receptorId
        )
        FCMSender.sendPushNotification(context, notification)
    }

    /** Escucha los mensajes en tiempo real para una conversación. */
    fun escucharMensajes(
        conversationId: String,
        onResultado: (List<Message>) -> Unit
    ): ListenerRegistration {
        return db.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    err?.printStackTrace()
                    return@addSnapshotListener
                }
                val msgs = snap.documents.mapNotNull { it.toObject(Message::class.java) }
                onResultado(msgs)
            }
    }


    /** Elimina todos los mensajes de la conversación y reinicia su último mensaje. */
    suspend fun eliminarMensajes(conversationId: String) {
        val batch = db.batch()
        val snapshot = db.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .get().await()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        val convRef = db.collection("conversations").document(conversationId)
        batch.update(
            convRef, mapOf(
                "lastMessage" to "",
                "lastMessageTime" to null
            )
        )
        batch.commit().await()
    }

    // Guarda el fondo **del usuario actual** en la conversación
    suspend fun guardarFondo(conversationId: String, imageBase64: String) {
        val miId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // actualizamos sólo la clave dentro del map
        db.collection("conversations")
            .document(conversationId)
            .update("backgroundImages.$miId", imageBase64)
            .await()
    }

    // Recupera el fondo **del usuario actual**
    suspend fun obtenerFondo(conversationId: String): String? {
        val miId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val doc = db.collection("conversations")
            .document(conversationId)
            .get().await()
        val mapa = doc.get("backgroundImages") as? Map<*, *>
        return mapa?.get(miId) as? String
    }

    // dentro de object ChatHelper
    suspend fun obtenerUsuarioPorId(uid: String): User? {
        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get().await()
                .toObject(User::class.java)
                ?.copy(uid = uid)
        } catch (e: Exception) {
            null
        }
    }

    /** Marca la conversación como leída para el usuario actual */
    suspend fun marcarConversacionComoLeida(convId: String) {
        val uid = obtenerUsuarioActual() ?: return
        val now = Timestamp.now()
        db.collection("conversations")
            .document(convId)
            .update("lastRead.$uid", now)
            .await()
    }

    /**
     * Escucha conversaciones y calcula unreadCount para cada una.
     * onResultado recibe ya la lista con unreadCount calculado.
     */
    fun escucharConversaciones(
        userId: String,
        onResultado: (List<Conversation>, Map<String, User>) -> Unit
    ) {
        db.collection("conversations")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (snap == null || err != null) return@addSnapshotListener

                // 1) Parseamos conversaciones con el campo lastRead
                val convs = snap.documents.mapNotNull { doc ->
                    val parts = doc.get("participants") as? List<String> ?: return@mapNotNull null
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val lastTime = doc.getTimestamp("lastMessageTime")?.toDate()
                    val lastSender = doc.getString("lastSenderId") ?: ""
                    val lr = doc.get("lastRead") as? Map<String, Timestamp> ?: emptyMap()
                    Conversation(
                        id = doc.id,
                        participants = parts,
                        lastMessage = lastMsg,
                        lastMessageTime = lastTime,
                        lastSenderId = lastSender,
                        lastRead = lr
                    )
                }

                // 2) En background cargamos usuarios y contamos no-leídos
                CoroutineScope(Dispatchers.IO).launch {
                    val users = cargarUsuariosPorIds(convs.flatMap { it.participants }.distinct())
                    val withUnread = convs.map { conv ->
                        val myReadTs = conv.lastRead[userId] ?: Timestamp(Date(0))
                        val countSnap = db.collection("messages")
                            .whereEqualTo("conversationId", conv.id)
                            .whereGreaterThan("timestamp", myReadTs)
                            .get()
                            .await()
                        conv.apply { unreadCount = countSnap.size() }
                    }
                    withContext(Dispatchers.Main) {
                        onResultado(withUnread, users)
                    }
                }
            }
    }

    suspend fun obtenerSeguidosSinConversacion(usuarioId: String): List<User> {
        val db = FirebaseFirestore.getInstance()

        // 1. Obtener conversaciones actuales
        val conversacionesSnapshot = db.collection("conversations")
            .whereArrayContains("participants", usuarioId)
            .get().await()

        val idsConConversacion = conversacionesSnapshot.documents
            .flatMap { it.get("participants") as? List<String> ?: emptyList() }
            .filter { it != usuarioId }
            .toSet()

        // 2. Obtener usuarios a los que sigo
        val seguidoresSnapshot = db.collection("followers")
            .whereEqualTo("seguidorId", usuarioId)
            .get().await()

        val seguidosIds = seguidoresSnapshot.documents
            .mapNotNull { it.getString("seguidoId") }
            .filter { it !in idsConConversacion }

        // 3. Obtener sus datos
        val usuarios = mutableListOf<User>()
        for (uid in seguidosIds) {
            val snap = db.collection("users").document(uid).get().await()
            snap.toObject(User::class.java)?.let {
                usuarios.add(it.copy(uid = uid))
            }
        }

        return usuarios
    }
    suspend fun iniciarConversacionSiNoExiste(usuarioActual: String, otroUsuarioId: String): String {
        val db = FirebaseFirestore.getInstance()
        val conversacionExistente = db.collection("conversations")
            .whereEqualTo("participants", listOf(usuarioActual, otroUsuarioId).sorted())
            .get()
            .await()
            .documents
            .firstOrNull()

        return if (conversacionExistente != null) {
            conversacionExistente.id
        } else {
            // Crea una nueva conversación
            val nuevaConv = mapOf(
                "participants" to listOf(usuarioActual, otroUsuarioId).sorted(),
                "lastMessage" to "",
                "lastMessageTime" to null,
                "lastSenderId" to "",
                "lastRead" to mapOf<String, Any>(),
                "backgroundImages" to mapOf<String, Any>()
            )
            val nuevaDoc = db.collection("conversations").add(nuevaConv).await()
            nuevaDoc.id
        }
    }

}
