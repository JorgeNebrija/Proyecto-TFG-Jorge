package com.example.proyecto_tfg_jorge.design.notifications

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object FCMSender {
    private const val TAG = "FCMSender"

    fun sendPushNotification(context: Context, notification: PushNotification) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(notification.toUserId)
                    .get(Source.SERVER)
                    .await()


                val notificacionesActivadas = doc.getBoolean("notificationsEnabled") ?: true
                Log.d(TAG, "Valor de 'notificationsEnabled' para ${notification.toUserId}: $notificacionesActivadas")

                if (!notificacionesActivadas) {
                    Log.d(TAG, "Notificaciones desactivadas. No se enviará notificación.")
                    return@launch
                }


                val accessToken = getAccessToken(context)
                val client = OkHttpClient()

                val json = JSONObject()
                json.put("message", JSONObject().apply {
                    put("token", notification.to)
                    put("notification", JSONObject().apply {
                        put("title", notification.data.title)
                        put("body", notification.data.body)
                    })
                })

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/proyecto-tfg-jorge/messages:send")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d(TAG, "Notificación enviada con éxito")
                } else {
                    Log.e(TAG, "Error al enviar notificación: ${response.code} ${response.body?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al enviar notificación: ${e.message}", e)
            }
        }
    }

    private fun getAccessToken(context: Context): String {
        val inputStream = context.resources.openRawResource(
            context.resources.getIdentifier("service_account", "raw", context.packageName)
        )
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }
}
