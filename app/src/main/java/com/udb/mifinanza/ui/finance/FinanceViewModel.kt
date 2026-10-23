package com.udb.mifinanza.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udb.mifinanza.data.FinanceRepository
import com.udb.mifinanza.data.FinanceSummary
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceUiState(
    val isLoading: Boolean = false,
    val summary: FinanceSummary = FinanceSummary(),
    val error: String? = null,
    val showAddDialog: Boolean = false
)

class FinanceViewModel(
    private val repository: FinanceRepository = FinanceRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadSummary()
                .onSuccess { summary ->
                    _state.update { it.copy(isLoading = false, summary = summary) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
        }
    }

    fun openAddDialog() = _state.update { it.copy(showAddDialog = true) }
    fun closeAddDialog() = _state.update { it.copy(showAddDialog = false) }

    fun getMovementById(id: String): Movement? {
        return _state.value.summary.recentMovements.find { it.id == id }
    }

    fun saveMovement(movement: Movement, onSuccess: () -> Unit) {
        if (movement.amount <= 0.0) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.saveMovement(movement)
                .onSuccess {
                    loadSummary()
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
        }
    }

    fun deleteMovement(movementId: String, onSuccess: () -> Unit) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.deleteMovement(movementId)
                .onSuccess {
                    loadSummary()
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
        }
    }

    fun addMovement(title: String, category: String, amount: Double, isIncome: Boolean) {
        if (title.isBlank() || amount <= 0.0) return
        val type = if (isIncome) MovementType.INGRESO else MovementType.GASTO
        viewModelScope.launch {
            repository.addMovement(title.trim(), category.trim(), amount, type)
                .onSuccess {
                    closeAddDialog()
                    loadSummary()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.localizedMessage) }
                }
        }
    }
}
