package com.udb.mifinanza.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.ui.auth.AuthViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udb.mifinanza.ui.screens.ForgotPasswordScreen
import com.udb.mifinanza.ui.screens.LoginScreen
import com.udb.mifinanza.ui.screens.ProfileScreen
import com.udb.mifinanza.ui.screens.RegisterScreen
import com.udb.mifinanza.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val auth by authViewModel.state.collectAsState()
    // Con sesión guardada la app abre directo en el perfil (luego será el inicio).
    val startDestination = remember { if (authViewModel.isSignedIn) Routes.PROFILE else Routes.WELCOME }

    fun goHome() = navController.navigate(Routes.PROFILE) { popUpTo(0) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCreateAccount = { navController.navigate(Routes.REGISTER) },
                onHaveAccount = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LaunchedEffect(Unit) { authViewModel.clearError() }
            LoginScreen(
                onLogin = { email, password, remember ->
                    authViewModel.login(email, password, remember, onSuccess = ::goHome)
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onRegister = { navController.navigate(Routes.REGISTER) },
                isLoading = auth.isLoading,
                errorMessage = auth.error
            )
        }
        composable(Routes.REGISTER) {
            LaunchedEffect(Unit) { authViewModel.clearError() }
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegister = { name, email, password, currency ->
                    authViewModel.register(name, email, password, currency, onSuccess = ::goHome)
                },
                onLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME)
                    }
                },
                isLoading = auth.isLoading,
                errorMessage = auth.error
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onPasswordResetSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PROFILE) {
            LaunchedEffect(Unit) { authViewModel.loadProfile() }
            ProfileScreen(
                profile = auth.profile ?: UserProfile(),
                onBack = { navController.popBackStack() },
                onOpenNews = { /* TODO: noticias académicas */ },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.WELCOME) { popUpTo(0) }
                },
                onTabSelected = { /* TODO: pestañas aún no implementadas */ },
                errorMessage = auth.error
            )
        }
    }
}
