package com.udb.mifinanza.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryTotal(
    val category: String,
    val amount: Double,
    /** Porcentaje (0–100) respecto al total de su tipo en el mes. */
    val share: Int
)

/** Resumen de un mes calculado con los movimientos del usuario. */
data class MonthlyReport(
    val monthStart: Long,
    val income: Double,
    val expenses: Double,
    val previousExpenses: Double,
    val movementCount: Int,
    val expensesByCategory: List<CategoryTotal>,
    val incomeByCategory: List<CategoryTotal>,
    val largestExpense: Movement?,
    /** Días del mes que ya transcurrieron (el mes completo si ya terminó). */
    val elapsedDays: Int
) {
    val net: Double get() = income - expenses

    /** Parte del ingreso que no se gastó; null si no hubo ingresos. */
    val savingsRate: Int? get() = if (income > 0) ((net / income) * 100).toInt() else null

    val dailyAverageExpense: Double get() = if (elapsedDays > 0) expenses / elapsedDays else 0.0

    /** Variación del gasto frente al mes anterior; null si el mes anterior no tuvo gastos. */
    val expenseChange: Int? get() =
        if (previousExpenses > 0) (((expenses - previousExpenses) / previousExpenses) * 100).toInt() else null

    val title: String
        get() = SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(Date(monthStart))
            .replaceFirstChar { it.uppercase() }
}

/** Arma el reporte del mes que está [monthsAgo] meses antes del actual (0 = mes en curso). */
fun buildMonthlyReport(movements: List<Movement>, monthsAgo: Int, now: Long = System.currentTimeMillis()): MonthlyReport {
    val reference = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -monthsAgo)
    }.timeInMillis
    val (from, to) = monthRange(reference)
    val (prevFrom, prevTo) = monthRange(Calendar.getInstance().apply {
        timeInMillis = from
        add(Calendar.MONTH, -1)
    }.timeInMillis)

    val inMonth = movements.filter { it.timestamp in from..to }
    val expenses = inMonth.filter { it.type == MovementType.GASTO }
    val income = inMonth.filter { it.type == MovementType.INGRESO }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalIncome = income.sumOf { it.amount }

    val daysInMonth = Calendar.getInstance().apply { timeInMillis = from }.getActualMaximum(Calendar.DAY_OF_MONTH)
    val elapsed = if (now in from..to) Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH)
    else daysInMonth

    return MonthlyReport(
        monthStart = from,
        income = totalIncome,
        expenses = totalExpenses,
        previousExpenses = movements
            .filter { it.type == MovementType.GASTO && it.timestamp in prevFrom..prevTo }
            .sumOf { it.amount },
        movementCount = inMonth.size,
        expensesByCategory = totalsByCategory(expenses, totalExpenses),
        incomeByCategory = totalsByCategory(income, totalIncome),
        largestExpense = expenses.maxByOrNull { it.amount },
        elapsedDays = elapsed
    )
}

private fun totalsByCategory(movements: List<Movement>, total: Double): List<CategoryTotal> =
    movements
        .groupBy { it.category.ifBlank { "Sin categoría" } }
        .map { (category, list) ->
            val amount = list.sumOf { it.amount }
            CategoryTotal(category, amount, if (total > 0) ((amount / total) * 100).toInt() else 0)
        }
        .sortedByDescending { it.amount }
