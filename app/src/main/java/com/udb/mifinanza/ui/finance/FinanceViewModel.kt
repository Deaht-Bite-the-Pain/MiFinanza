package com.udb.mifinanza.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.FinanceRepository
import com.udb.mifinanza.data.FinanceSummary
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    /** true cuando ya se leyeron los datos del usuario al menos una vez. */
    val loaded: Boolean = false,
    val summary: FinanceSummary = FinanceSummary(),
    val error: String? = null
)

class FinanceViewModel(
    private val repository: FinanceRepository = FinanceRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    fun loadSummary() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadSummary()
                .onSuccess { summary -> _state.update { it.copy(isLoading = false, loaded = true, summary = summary) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
        }
    }

    /** Limpia los datos al cerrar sesión para que no se vean los del usuario anterior. */
    fun clear() {
        _state.value = FinanceUiState()
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun saveMovement(movement: Movement, onSuccess: () -> Unit) = runAndReload(onSuccess) {
        repository.saveMovement(movement)
    }

    fun deleteMovement(movementId: String, onSuccess: () -> Unit) = runAndReload(onSuccess) {
        repository.deleteMovement(movementId)
    }

    /** Si ya existe un presupuesto para la categoría se actualiza su límite en vez de duplicarlo. */
    fun saveBudget(budget: CategoryBudget, onSuccess: () -> Unit) {
        val existing = _state.value.summary.budgets.find { it.category.equals(budget.category, ignoreCase = true) }
        runAndReload(onSuccess) { repository.saveBudget(budget.copy(id = existing?.id ?: budget.id)) }
    }

    fun deleteBudget(budgetId: String, onSuccess: () -> Unit) = runAndReload(onSuccess) {
        repository.deleteBudget(budgetId)
    }

    private fun runAndReload(onSuccess: () -> Unit, block: suspend () -> Result<Unit>) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            block()
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    loadSummary()
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.toUserMessage()) } }
        }
    }
}
