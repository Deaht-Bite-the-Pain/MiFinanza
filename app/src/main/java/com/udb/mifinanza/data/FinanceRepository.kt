package com.udb.mifinanza.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FinanceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userCollection(name: String): CollectionReference {
        val userId = auth.currentUser?.uid ?: error("Sin sesión de usuario activa")
        return db.collection(USERS).document(userId).collection(name)
    }

    /** Lee todos los movimientos y presupuestos del usuario y calcula el resumen. */
    suspend fun loadSummary(): Result<FinanceSummary> = runCatching {
        val movements = userCollection(MOVEMENTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .documents.map { Movement.fromMap(it.id, it.data ?: emptyMap()) }
        val budgets = userCollection(BUDGETS)
            .get().await()
            .documents.map { CategoryBudget.fromMap(it.id, it.data ?: emptyMap()) }
        calculateSummary(movements, budgets)
    }

    suspend fun saveMovement(movement: Movement): Result<Unit> = runCatching {
        val coll = userCollection(MOVEMENTS)
        if (movement.id.isEmpty()) coll.add(movement.toMap()).await()
        else coll.document(movement.id).set(movement.toMap()).await()
        Unit
    }

    suspend fun deleteMovement(movementId: String): Result<Unit> = runCatching {
        userCollection(MOVEMENTS).document(movementId).delete().await()
        Unit
    }

    suspend fun saveBudget(budget: CategoryBudget): Result<Unit> = runCatching {
        val coll = userCollection(BUDGETS)
        if (budget.id.isEmpty()) coll.add(budget.toMap()).await()
        else coll.document(budget.id).set(budget.toMap()).await()
        Unit
    }

    private fun calculateSummary(movements: List<Movement>, storedBudgets: List<CategoryBudget>): FinanceSummary {
        val now = Calendar.getInstance()
        val currentKey = monthKey(now)

        val balance = movements.sumOf { if (it.type == MovementType.INGRESO) it.amount else -it.amount }
        val thisMonth = movements.filter { monthKey(it.timestamp) == currentKey }
        val monthIncome = thisMonth.filter { it.type == MovementType.INGRESO }.sumOf { it.amount }
        val monthExpensesList = thisMonth.filter { it.type == MovementType.GASTO }
        val monthExpenses = monthExpensesList.sumOf { it.amount }

        // Gastos reales de cada uno de los últimos 6 meses (el actual al final).
        val expensesByMonth = movements
            .filter { it.type == MovementType.GASTO }
            .groupBy { monthKey(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val bars = (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -monthsAgo)
            }
            MonthlyExpenseBar(
                monthLabel = MONTH_LABELS[cal.get(Calendar.MONTH)],
                amount = expensesByMonth[monthKey(cal)] ?: 0.0,
                isCurrentMonth = monthsAgo == 0
            )
        }

        // Lo gastado en cada presupuesto sale de los gastos del mes en esa categoría.
        val budgets = storedBudgets
            .map { budget ->
                budget.copy(
                    spent = monthExpensesList
                        .filter { it.category.equals(budget.category, ignoreCase = true) }
                        .sumOf { it.amount }
                )
            }
            .sortedBy { it.category }

        val daysRemaining = now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH)
        val alert = budgets
            .filter { it.limit > 0 && it.percentage >= ALERT_PERCENTAGE }
            .maxByOrNull { it.spent / it.limit }
            ?.let { BudgetAlert(it.category, it.spent, it.limit, daysRemaining) }

        return FinanceSummary(
            availableBalance = balance,
            monthlyIncome = monthIncome,
            monthlyExpenses = monthExpenses,
            totalBudgeted = budgets.sumOf { it.limit },
            totalBudgetSpent = budgets.sumOf { it.spent },
            budgetAlert = alert,
            monthlyBars = bars,
            recentMovements = movements.take(RECENT_COUNT),
            budgets = budgets
        )
    }

    private fun monthKey(timestamp: Long): Int =
        monthKey(Calendar.getInstance().apply { timeInMillis = timestamp })

    private fun monthKey(cal: Calendar): Int = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)

    private companion object {
        const val USERS = "users"
        const val MOVEMENTS = "movements"
        const val BUDGETS = "budgets"
        const val RECENT_COUNT = 5
        const val ALERT_PERCENTAGE = 80
        val MONTH_LABELS = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
    }
}
