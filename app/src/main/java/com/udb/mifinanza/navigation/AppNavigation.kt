package com.udb.mifinanza.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.ui.auth.AuthViewModel
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.finance.FinanceViewModel
import com.udb.mifinanza.ui.news.NewsViewModel
import com.udb.mifinanza.ui.screens.BudgetScreen
import com.udb.mifinanza.ui.screens.EditProfileScreen
import com.udb.mifinanza.ui.screens.ForgotPasswordScreen
import com.udb.mifinanza.ui.screens.HistoryScreen
import com.udb.mifinanza.ui.screens.HomeScreen
import com.udb.mifinanza.ui.screens.LoginScreen
import com.udb.mifinanza.ui.screens.MovementDetailScreen
import com.udb.mifinanza.ui.screens.NewBudgetScreen
import com.udb.mifinanza.ui.screens.NewMovementScreen
import com.udb.mifinanza.ui.screens.NewsScreen
import com.udb.mifinanza.ui.screens.ProfileScreen
import com.udb.mifinanza.ui.screens.RegisterScreen
import com.udb.mifinanza.ui.screens.ReportsScreen
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
    const val NEW_BUDGET = "new_budget"
    const val HISTORY = "history"
    const val REPORTS = "reports"
    const val NEWS = "news"
    const val EDIT_PROFILE = "edit_profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val financeViewModel: FinanceViewModel = viewModel()
    val newsViewModel: NewsViewModel = viewModel()
    val auth by authViewModel.state.collectAsState()
    val finance by financeViewModel.state.collectAsState()
    val currencySymbol = (auth.profile ?: UserProfile()).currencySymbol
    val startDestination = remember { if (authViewModel.isSignedIn) Routes.HOME else Routes.WELCOME }

    var selectedMovement by remember { mutableStateOf<Movement?>(null) }
    var selectedBudget by remember { mutableStateOf<CategoryBudget?>(null) }

    fun goHome() = navController.navigate(Routes.HOME) { popUpTo(0) }

    // Las pestañas no se apilan: desde cualquiera, "atrás" regresa a Inicio.
    fun openTab(tab: NavTab) {
        val route = when (tab) {
            NavTab.Inicio -> Routes.HOME
            NavTab.Presupuestos -> Routes.BUDGETS
            NavTab.Movimientos, NavTab.Historial -> Routes.HISTORY
        }
        if (navController.currentDestination?.route == route) return
        navController.navigate(route) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
        }
    }

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
                onTabSelected = ::openTab,
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                financeViewModel = financeViewModel
            )
        }
        composable(Routes.REPORTS) {
            LaunchedEffect(Unit) { financeViewModel.loadSummary() }
            ReportsScreen(
                movements = finance.summary.allMovements,
                loaded = finance.loaded,
                error = finance.error,
                currencySymbol = currencySymbol,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.NEW_MOVEMENT) {
            LaunchedEffect(Unit) { financeViewModel.clearError() }
            NewMovementScreen(
                initialMovement = selectedMovement,
                currencySymbol = currencySymbol,
                isSaving = finance.isSaving,
                errorMessage = finance.error,
                onCancel = { navController.popBackStack() },
                onSave = { movement ->
                    financeViewModel.saveMovement(movement) {
                        // Al editar, el detalle vuelve a mostrarse con los datos recién guardados.
                        if (movement.id.isNotEmpty()) selectedMovement = movement
                        navController.popBackStack()
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
            LaunchedEffect(Unit) { financeViewModel.clearError() }
            MovementDetailScreen(
                movement = mov,
                currencySymbol = currencySymbol,
                budget = finance.summary.budgets.find { it.category.equals(mov.category, ignoreCase = true) },
                isDeleting = finance.isSaving,
                errorMessage = finance.error,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.NEW_MOVEMENT) },
                onDelete = {
                    financeViewModel.deleteMovement(mov.id) {
                        navController.popBackStack()
                    }
                },
                onTabSelected = ::openTab
            )
        }
        composable(Routes.BUDGETS) {
            LaunchedEffect(Unit) { financeViewModel.loadSummary() }
            BudgetScreen(
                currencySymbol = currencySymbol,
                onTabSelected = ::openTab,
                onAddBudget = {
                    selectedBudget = null
                    navController.navigate(Routes.NEW_BUDGET)
                },
                onEditBudget = { budget ->
                    selectedBudget = budget
                    navController.navigate(Routes.NEW_BUDGET)
                },
                financeViewModel = financeViewModel
            )
        }
        composable(Routes.NEW_BUDGET) {
            LaunchedEffect(Unit) { financeViewModel.clearError() }
            NewBudgetScreen(
                initialBudget = selectedBudget,
                currencySymbol = currencySymbol,
                movements = finance.summary.allMovements,
                existingBudgetCategories = finance.summary.budgets.map { it.category },
                isSaving = finance.isSaving,
                errorMessage = finance.error,
                onCancel = { navController.popBackStack() },
                onSave = { budget ->
                    financeViewModel.saveBudget(budget) {
                        navController.popBackStack()
                    }
                },
                onDelete = { budget ->
                    financeViewModel.deleteBudget(budget.id) {
                        navController.popBackStack()
                    }
                }
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
                onTabSelected = ::openTab
            )
        }
        composable(Routes.PROFILE) {
            LaunchedEffect(Unit) { authViewModel.loadProfile() }
            ProfileScreen(
                profile = auth.profile ?: UserProfile(),
                onBack = { navController.popBackStack() },
                onOpenNews = { navController.navigate(Routes.NEWS) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onLogout = {
                    authViewModel.signOut()
                    financeViewModel.clear()
                    navController.navigate(Routes.WELCOME) { popUpTo(0) }
                },
                onTabSelected = ::openTab,
                errorMessage = auth.error
            )
        }
        composable(Routes.EDIT_PROFILE) {
            LaunchedEffect(Unit) { authViewModel.clearError() }
            EditProfileScreen(
                profile = auth.profile ?: UserProfile(),
                isSaving = auth.isLoading,
                errorMessage = auth.error,
                onBack = { navController.popBackStack() },
                onSave = { profile ->
                    authViewModel.saveProfile(profile) { navController.popBackStack() }
                }
            )
        }
        composable(Routes.NEWS) {
            val news by newsViewModel.state.collectAsState()
            LaunchedEffect(Unit) { if (!news.loaded) newsViewModel.load() }
            NewsScreen(
                items = news.items,
                loaded = news.loaded,
                isLoading = news.isLoading,
                error = news.error,
                onRetry = newsViewModel::load,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
