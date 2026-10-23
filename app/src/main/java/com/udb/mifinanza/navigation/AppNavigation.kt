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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.finance.FinanceViewModel
import com.udb.mifinanza.ui.screens.ForgotPasswordScreen
import com.udb.mifinanza.ui.screens.HomeScreen
import com.udb.mifinanza.ui.screens.LoginScreen
import com.udb.mifinanza.ui.screens.MovementDetailScreen
import com.udb.mifinanza.ui.screens.NewMovementScreen
import com.udb.mifinanza.ui.screens.ProfileScreen
import com.udb.mifinanza.ui.screens.RegisterScreen
import com.udb.mifinanza.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val NEW_MOVEMENT = "new_movement"
    const val MOVEMENT_DETAIL = "movement_detail"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val financeViewModel: FinanceViewModel = viewModel()
    val auth by authViewModel.state.collectAsState()
    val startDestination = remember { if (authViewModel.isSignedIn) Routes.HOME else Routes.WELCOME }

    var selectedMovement by remember { mutableStateOf<Movement?>(null) }

    fun goHome() = navController.navigate(Routes.HOME) { popUpTo(0) }

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
        composable(Routes.HOME) {
            LaunchedEffect(Unit) {
                authViewModel.loadProfile()
                financeViewModel.loadSummary()
            }
            HomeScreen(
                profile = auth.profile ?: UserProfile(),
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenNewMovement = {
                    selectedMovement = null
                    navController.navigate(Routes.NEW_MOVEMENT)
                },
                onOpenMovementDetail = { movement ->
                    selectedMovement = movement
                    navController.navigate(Routes.MOVEMENT_DETAIL)
                },
                onTabSelected = { tab ->
                    if (tab == NavTab.Inicio) {
                        // Ya estamos en Inicio
                    }
                },
                financeViewModel = financeViewModel
            )
        }
        composable(Routes.NEW_MOVEMENT) {
            NewMovementScreen(
                initialMovement = selectedMovement,
                onCancel = { navController.popBackStack() },
                onSave = { movement ->
                    financeViewModel.saveMovement(movement) {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(Routes.MOVEMENT_DETAIL) {
            val mov = selectedMovement ?: Movement(
                title = "Supermercado Santa María",
                category = "Alimentación",
                amount = 42.80,
                type = MovementType.GASTO,
                fullDateString = "12/08/2026",
                paymentMethod = "Efectivo",
                description = "Compra semanal en Supermercado Santa María. Incluye frutas, lácteos y limpieza.",
                tags = listOf("semanal", "hogar")
            )
            MovementDetailScreen(
                movement = mov,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate(Routes.NEW_MOVEMENT)
                },
                onDelete = {
                    if (mov.id.isNotEmpty()) {
                        financeViewModel.deleteMovement(mov.id) {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onTabSelected = { tab ->
                    if (tab == NavTab.Inicio) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
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
                onTabSelected = { tab ->
                    if (tab == NavTab.Inicio) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                errorMessage = auth.error
            )
        }
    }
}
