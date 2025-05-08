package com.example.proyecto_tfg_jorge.design.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun PantallaPoliticaPrivacidad(navController: NavHostController) {
    var acepto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Política de Privacidad",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Texto de la política
        Text(
            text = """
                En Athlo nos tomamos en serio tu privacidad.

                Esta política describe cómo recopilamos, usamos y protegemos tu información personal. 
                Al continuar, aceptas nuestras prácticas conforme al Reglamento General de Protección de Datos (RGPD).

                Esta información se utiliza para mejorar tu experiencia, mantener la seguridad de la plataforma 
                y permitirte conectar con otros deportistas de forma segura.
            """.trimIndent(),
            fontSize = 14.sp,
            color = Color.LightGray,
            style = TextStyle(lineHeight = 20.sp),
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Aceptar checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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

        // Botón Aceptar
        Button(
            onClick = {
                if (acepto) {
                    navController.popBackStack()
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
