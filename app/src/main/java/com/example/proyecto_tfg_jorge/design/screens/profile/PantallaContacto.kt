package com.example.proyecto_tfg_jorge.design.screens.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaContacto(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    // Estado para controlar si se ha cargado la info de Firebase
    var cargado by remember { mutableStateOf(false) }
    // Estado para saber si la política ha sido aceptada
    var politicaAceptada by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Obtenemos el usuario actual de FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Referencia al documento del usuario en Firestore
            val docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)

            try {
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    // Obtenemos el valor del campo "politica"
                    val politica = snapshot.getBoolean("politica") ?: false
                    politicaAceptada = politica
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cargado = true
    }

    // Mientras no se haya cargado la información, podrías mostrar un loading o retornar vacío
    if (!cargado) {
        // Aquí puedes poner un composable de "Cargando..." si quieres
        return
    }

    // Si la política NO ha sido aceptada, mostramos la pantalla de política
    if (!politicaAceptada) {
        PoliticaPrivacidadUI {
            scope.launch {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val docRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)

                    // Guardamos "politica = true" en Firestore
                    docRef.set(mapOf("politica" to true), SetOptions.merge())
                    politicaAceptada = true
                } else {
                    // Si no hay usuario, se podría manejar un caso de error o de invitado.
                }
            }
        }
    } else {
        // Si la política ya fue aceptada, mostramos directamente el formulario
        FormularioContacto(navController)
    }
}

@Composable
fun PoliticaPrivacidadUI(
    onAceptar: () -> Unit
) {
    var acepto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Política de Privacidad",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = """
                En Athlo nos tomamos en serio tu privacidad.

                Esta política describe cómo recopilamos, usamos y protegemos tu información personal. 
                Al continuar, aceptas nuestras prácticas conforme al RGPD.

                Esta información se utiliza para mejorar tu experiencia, mantener la seguridad de la plataforma 
                y permitirte conectar con otros deportistas de forma segura.
            """.trimIndent(),
            fontSize = 14.sp,
            color = Color.LightGray,
            style = TextStyle(lineHeight = 20.sp),
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = acepto,
                onCheckedChange = { acepto = it },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "He leído y acepto la política de privacidad",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (acepto) {
                    onAceptar()
                }
            },
            enabled = acepto,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E676),
                disabledContainerColor = Color.DarkGray
            )
        ) {
            Text("Aceptar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun FormularioContacto(navController: NavHostController) {
    val context = LocalContext.current

    var asunto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_tfg2),
            contentDescription = "Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿Tienes dudas o sugerencias?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Rellena el formulario y nos pondremos en contacto contigo.",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = asunto,
            onValueChange = {
                asunto = it
                if (showError) showError = false
            },
            label = { Text("Asunto") },
            isError = showError && asunto.isBlank(),
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color(0xFF00E676),
                focusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.LightGray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = mensaje,
            onValueChange = {
                mensaje = it
                if (showError) showError = false
            },
            label = { Text("Mensaje") },
            isError = showError && mensaje.isBlank(),
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color(0xFF00E676),
                focusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.LightGray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (asunto.isBlank() || mensaje.isBlank()) {
                    showError = true
                    Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("jorgeanton1810@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, asunto)
                        putExtra(Intent.EXTRA_TEXT, mensaje)
                    }
                    context.startActivity(Intent.createChooser(intent, "Enviar email con..."))
                    Toast.makeText(context, "Mensaje preparado para enviar", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
            Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu mensaje será tratado de forma confidencial.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
