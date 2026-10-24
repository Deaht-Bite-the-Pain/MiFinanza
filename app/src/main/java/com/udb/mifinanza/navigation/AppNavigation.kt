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
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.finance.FinanceViewModel
import com.udb.mifinanza.ui.screens.BudgetScreen
import com.udb.mifinanza.ui.screens.ForgotPasswordScreen
import com.udb.mifinanza.ui.screens.HistoryScreen
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
    const val BUDGETS = "budgets"
    const val HISTORY = "history"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val financeViewModel: FinanceViewModel = viewModel()
    val auth by authViewModel.state.collectAsState()
    val finance by financeViewModel.state.collectAsState()
    val currencySymbol = (auth.profile ?: UserProfile()).currencySymbol
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
            LaunchedEffect(Unit) { authViewModel.clearError() }
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onSendLink = { email -> authViewModel.sendPasswordReset(email) },
                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                isLoading = auth.isLoading,
                linkSent = auth.resetEmailSent,
                errorMessage = auth.error
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
                    when (tab) {
                        NavTab.Inicio -> { /* Ya estamos en Inicio */ }
                        NavTab.Presupuestos -> navController.navigate(Routes.BUDGETS)
                        NavTab.Movimientos -> { /* Vista de movimientos */ }
                        NavTab.Historial -> navController.navigate(Routes.HISTORY)
                    }
                },
                financeViewModel = financeViewModel
            )
        }
        composable(Routes.NEW_MOVEMENT) {
            NewMovementScreen(
                initialMovement = selectedMovement,
                currencySymbol = currencySymbol,
                isSaving = finance.isLoading,
                errorMessage = finance.error,
                onCancel = { navController.popBackStack() },
                onSave = { movement ->
                    financeViewModel.saveMovement(movement) {
                        // Al editar se vuelve a Inicio para no mostrar el detalle con datos viejos.
                        if (movement.id.isEmpty()) navController.popBackStack()
                        else navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                }
            )
        }
        composable(Routes.MOVEMENT_DETAIL) {
            // Sin movimiento seleccionado (p. ej. tras recrear la actividad) no hay nada que mostrar.
            val mov = selectedMovement
            if (mov == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.HOME, inclusive = false) }
                return@composable
            }
            MovementDetailScreen(
                movement = mov,
                currencySymbol = currencySymbol,
                budget = finance.summary.budgets.find { it.category.equals(mov.category, ignoreCase = true) },
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate(Routes.NEW_MOVEMENT)
                },
                onDelete = {
                    financeViewModel.deleteMovement(mov.id) {
                        navController.popBackStack()
                    }
                },
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Inicio -> navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        NavTab.Presupuestos -> navController.navigate(Routes.BUDGETS)
                        NavTab.Movimientos -> { /* Ya en movimientos */ }
                        NavTab.Historial -> navController.navigate(Routes.HISTORY)
                    }
                }
            )
        }
        composable(Routes.BUDGETS) {
            LaunchedEffect(Unit) {
                financeViewModel.loadSummary()
            }
            BudgetScreen(
                currencySymbol = currencySymbol,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Inicio -> navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        NavTab.Presupuestos -> { /* Ya en Presupuestos */ }
                        NavTab.Movimientos -> navController.navigate(Routes.HOME)
                        NavTab.Historial -> navController.navigate(Routes.HISTORY)
                    }
                },
                financeViewModel = financeViewModel
            )
        }
        composable(Routes.HISTORY) {
            LaunchedEffect(Unit) { financeViewModel.loadSummary() }
            HistoryScreen(
                movements = finance.summary.allMovements,
                loaded = finance.loaded,
                error = finance.error,
                currencySymbol = currencySymbol,
                onMovementClick = { movement ->
                    selectedMovement = movement
                    navController.navigate(Routes.MOVEMENT_DETAIL)
                },
                onAddMovement = {
                    selectedMovement = null
                    navController.navigate(Routes.NEW_MOVEMENT)
                },
                onRetry = { financeViewModel.loadSummary() },
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Inicio -> navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        NavTab.Presupuestos -> navController.navigate(Routes.BUDGETS)
                        NavTab.Movimientos -> navController.navigate(Routes.HOME)
                        NavTab.Historial -> { /* Ya en Historial */ }
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
                    financeViewModel.clear()
                    navController.navigate(Routes.WELCOME) { popUpTo(0) }
                },
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Inicio -> navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        NavTab.Presupuestos -> navController.navigate(Routes.BUDGETS)
                        NavTab.Movimientos -> navController.navigate(Routes.HOME)
                        NavTab.Historial -> navController.navigate(Routes.HISTORY)
                    }
                },
                errorMessage = auth.error
            )
        }
    }
}
