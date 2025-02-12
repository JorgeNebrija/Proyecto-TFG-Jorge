package com.example.proyecto_tfg_jorge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_tfg_jorge.navegation.NavigationWrapper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var navHostController: NavHostController

    // Declaramos el ActivityResultLauncher en la Activity
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    // Variable para almacenar el cliente de GoogleSignIn, ahora correctamente tipada como GoogleSignInClient
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Inicializa Firebase
        enableEdgeToEdge()

        // Inicializa GoogleSignInClient dentro de onCreate
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("769654967618-s8e9fu0lq81tpuljh5821gfvb709p0vt.apps.googleusercontent.com") // Reemplaza con tu ID de cliente web de Firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializa el launcher para el inicio de sesión
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Lógica cuando la actividad de inicio de sesión es exitosa
            }
        }

        setContent {
            navHostController = rememberNavController()
            // Pasamos el signInLauncher al composable
            NavigationWrapper(navHostController, this, signInLauncher)
        }
    }
}
