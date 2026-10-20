package com.udb.mifinanza.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udb.mifinanza.ui.screens.LoginScreen
import com.udb.mifinanza.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCreateAccount = { /* TODO: pantalla de registro */ },
                onHaveAccount = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { _, _, _ -> /* TODO: Firebase Auth */ },
                onForgotPassword = { /* TODO: recuperar contraseña */ },
                onRegister = { /* TODO: pantalla de registro */ }
            )
        }
    }
}
