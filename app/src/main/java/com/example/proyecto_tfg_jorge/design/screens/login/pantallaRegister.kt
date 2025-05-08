package com.example.proyecto_tfg_jorge.design.screens.login


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto_tfg_jorge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegister(navHostController: NavHostController) {
    // Instancia de FirebaseAuth para autenticar usuarios
    val auth = FirebaseAuth.getInstance()

    // Estados para almacenar los datos de los campos
    val name = remember { mutableStateOf(TextFieldValue("")) }
    val lastName = remember { mutableStateOf(TextFieldValue("")) }
    val email = remember { mutableStateOf(TextFieldValue("")) }
    val phone = remember { mutableStateOf(TextFieldValue("")) }
    val password = remember { mutableStateOf(TextFieldValue("")) }
    val confirmPassword = remember { mutableStateOf(TextFieldValue("")) }
    val message = remember { mutableStateOf("") } // Mensaje de estado para mostrar errores o confirmaciones

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .padding(horizontal = 10.dp), // Añadir padding lateral
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Alineación vertical al principio
    )  {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver atrás",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .clickable {
                        navHostController.navigate("PantallaLogin") // Navega de vuelta a la pantalla de inicio de sesión
                    },
                tint = Color.White
            )
        }
        Image(
            painter = painterResource(id = R.drawable.logo_tfg2),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White),
            colorFilter = ColorFilter.tint(Color.Black)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Título de la pantalla
        Text(
            text = "Registro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo de la pantalla
        Text(
            text = "¡Crea una cuenta para continuar!",
            fontSize = 14.sp,
            color = Color(0xFF00C853),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campos de entrada para los datos del usuario
        RegisterField("Nombre", name.value, onValueChange = { name.value = it })
        RegisterField("Apellidos", lastName.value, onValueChange = { lastName.value = it })
        RegisterField("Email", email.value, onValueChange = { email.value = it }, keyboardType = KeyboardType.Email)
        RegisterField("Número Telefónico", phone.value, onValueChange = { phone.value = it }, keyboardType = KeyboardType.Phone)
        RegisterField("Establecer contraseña", password.value, onValueChange = { password.value = it }, isPassword = true)
        RegisterField("Repetir contraseña", confirmPassword.value, onValueChange = { confirmPassword.value = it }, isPassword = true)

        Spacer(modifier = Modifier.height(14.dp))

        // Botón para registrar al usuario
        Button(
            onClick = {
                if (password.value.text == confirmPassword.value.text) {
                    // Si las contraseñas coinciden, se registra al usuario
                    registerUser(
                        auth = auth,
                        email = email.value.text,
                        password = password.value.text,
                        message = message,
                        navHostController = navHostController,
                        name = name.value.text,
                        lastName = lastName.value.text,
                        phone = phone.value.text
                    )
                } else {
                    // Si las contraseñas no coinciden, muestra un mensaje de error
                    message.value = "Las contraseñas no coinciden"
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Registro", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mensaje de estado (éxito o error)
        if (message.value.isNotEmpty()) {
            Text(
                text = message.value,
                color = if (message.value.startsWith("Error")) Color.Red else Color(0xFF00C853),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Opción para redirigir al inicio de sesión
        Row {
            Text(
                text = "¿Ya tienes una cuenta? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Iniciar sesión",
                color = Color(0xFF00C853),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navHostController.navigate("pantallaLogin")
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Campo de entrada
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.Black
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility),
                        contentDescription = "Mostrar contraseña",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

    }
}

fun registerUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    message: MutableState<String>,
    navHostController: NavHostController,
    name: String,
    lastName: String,
    phone: String
) {
    if (email.isNotBlank() && password.isNotBlank()) {
        // Autenticación con FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Obtén el ID del usuario
                    val userId = auth.currentUser?.uid
                    val db = FirebaseFirestore.getInstance()

                    // Datos adicionales del usuario
                    val userData = mapOf(
                        "name" to name,
                        "lastName" to lastName,
                        "email" to email,
                        "phone" to phone
                    )

                    // Guarda los datos en Firestore en la colección "users"
                    userId?.let {
                        db.collection("users").document(it).set(userData)
                            .addOnSuccessListener {
                                message.value = "Registro exitoso"
                                navHostController.navigate("pantallaMenu") // Navega al login
                            }
                            .addOnFailureListener { e ->
                                message.value = "Error al guardar datos: ${e.message}"
                            }
                    }
                } else {
                    // Error en el registro
                    message.value = "Error: ${task.exception?.message}"
                }
            }
    } else {
        // Campos vacíos
        message.value = "Por favor, completa todos los campos"
    }
}
