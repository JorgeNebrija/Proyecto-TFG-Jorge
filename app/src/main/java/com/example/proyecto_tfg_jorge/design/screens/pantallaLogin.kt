package com.example.proyecto_tfg_jorge.functionality.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_tfg_jorge.design.screens.PantallaLoginDesign
import com.example.proyecto_tfg_jorge.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

@Composable
fun PantallaLogin(navController: NavController, signInLauncher: ActivityResultLauncher<Intent>, context: Context) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory())

    LaunchedEffect(authViewModel.isRegistered.value) {
        if (authViewModel.isRegistered.value) {
            navController.navigate("pantallaMenu") // Navegar a la pantalla de menú cuando el usuario esté registrado
        }
    }

    LaunchedEffect(authViewModel.isLoggedIn.value) {
        if (authViewModel.isLoggedIn.value) {
            navController.navigate("pantallaMenu") // Navegar a la pantalla de menú cuando el usuario haya iniciado sesión
        }
    }

    PantallaLoginDesign(
        email = authViewModel.email.value,
        password = authViewModel.password.value,
        onEmailChange = { authViewModel.email.value = it },
        onPasswordChange = { authViewModel.password.value = it },
        onContinueClick = { authViewModel.signUp() },
        onLoginClick = { navController.navigate("pantallaLoginForm") },
        onGoogleClick = { onGoogleSignIn(context, signInLauncher) },
        onGithubClick = { onGithubSignIn(context, navController) } // Aquí pasamos el navController
    )
}

// Lógica de inicio de sesión con Google
fun onGoogleSignIn(activity: Context, signInLauncher: ActivityResultLauncher<Intent>) {
    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("YOUR_WEB_CLIENT_ID") // Reemplaza con tu Web Client ID
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions)
    val signInIntent = googleSignInClient.signInIntent
    signInLauncher.launch(signInIntent) // Llamamos al launcher para iniciar la actividad de Google Sign-In
}

// Lógica de inicio de sesión con GitHub
fun onGithubSignIn(activity: Context, navController: NavController) {
    // Aseguramos que el 'activity' sea de tipo 'Activity' para evitar el error de tipo
    val activityCast = activity as Activity

    val provider = OAuthProvider.newBuilder("github.com").build()

    FirebaseAuth.getInstance()
        .startActivityForSignInWithProvider(activityCast, provider)
        .addOnSuccessListener { authResult ->
            // Usuario autenticado correctamente
            // Navegar a la pantalla principal después de la autenticación
            navController.navigate("pantallaMenu") // Usamos navController para la navegación
        }
        .addOnFailureListener { e ->
            // Error en la autenticación
            // Maneja el error aquí
        }
}
