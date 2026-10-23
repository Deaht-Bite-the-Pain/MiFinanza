package com.udb.mifinanza.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tipo de movimiento financiero: Ingreso o Gasto */
enum class MovementType {
    INGRESO,
    GASTO
}

/** Categorías con las que se registran los gastos (y sobre las que se definen presupuestos). */
val ExpenseCategories = listOf(
    "Alimentación",
    "Transporte",
    "Ocio",
    "Servicios",
    "Salud",
    "Educación",
    "Hogar",
    "Otros"
)

/** Categorías con las que se registran los ingresos. */
val IncomeCategories = listOf(
    "Salario",
    "Beca",
    "Ventas",
    "Regalo",
    "Otros ingresos"
)

/** Formatea un monto con el símbolo de la moneda del usuario: "$1,284.50". */
fun formatMoney(amount: Double, symbol: String, decimals: Int = 2): String =
    symbol + String.format(Locale.US, "%,.${decimals}f", amount)

private val AmountInput = Regex("""\d{0,9}(\.\d{0,2})?""")

/** Filtra lo que se escribe en un campo de monto: solo dígitos, un punto y hasta 2 decimales. */
fun filterAmountInput(input: String, current: String): String {
    val normalized = input.replace(',', '.')
    return if (AmountInput.matches(normalized)) normalized else current
}

/** Representa un movimiento registrado en Firestore en `users/{uid}/movements/{id}` */
data class Movement(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val type: MovementType = MovementType.GASTO,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Efectivo",
    val description: String = "",
    val tags: List<String> = emptyList()
) {
    /** Fecha corta para listas: "12 ago". */
    val formattedDate: String
        get() = SimpleDateFormat("d MMM", Locale("es", "ES")).format(Date(timestamp)).lowercase()

    /** Fecha completa: "12/08/2026". */
    val displayFullDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timestamp))

    fun displayAmount(symbol: String): String =
        (if (type == MovementType.INGRESO) "+" else "-") + formatMoney(amount, symbol)

    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "category" to category,
        "amount" to amount,
        "type" to type.name,
        "timestamp" to timestamp,
        "paymentMethod" to paymentMethod,
        "description" to description,
        "tags" to tags
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Movement {
            val typeStr = map["type"] as? String ?: MovementType.GASTO.name
            val type = runCatching { MovementType.valueOf(typeStr) }.getOrDefault(MovementType.GASTO)
            return Movement(
                id = id,
                title = map["title"] as? String ?: "",
                category = map["category"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                type = type,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                paymentMethod = map["paymentMethod"] as? String ?: "",
                description = map["description"] as? String ?: "",
                tags = (map["tags"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            )
        }
    }
}

/** Información para el gráfico de barras de los últimos 6 meses */
data class MonthlyExpenseBar(
    val monthLabel: String,
    val amount: Double,
    val isCurrentMonth: Boolean = false
)

/** Alerta del presupuesto más comprometido del mes (80 % o más de su límite). */
data class BudgetAlert(
    val category: String,
    val spent: Double,
    val limit: Double,
    val daysRemaining: Int
) {
    val percentage: Int
        get() = if (limit > 0) ((spent / limit) * 100).toInt() else 0
    val isExceeded: Boolean get() = spent > limit
}

/**
 * Presupuesto mensual de una categoría, guardado en `users/{uid}/budgets/{id}`.
 * Solo se guarda el límite; [spent] se calcula con los gastos del mes en curso.
 */
data class CategoryBudget(
    val id: String = "",
    val category: String = "",
    val spent: Double = 0.0,
    val limit: Double = 0.0
) {
    val isExceeded: Boolean get() = spent > limit
    val percentage: Int get() = if (limit > 0) ((spent / limit) * 100).toInt() else 0
    val remaining: Double get() = (limit - spent).coerceAtLeast(0.0)
    val exceededAmount: Double get() = (spent - limit).coerceAtLeast(0.0)

    fun toMap(): Map<String, Any> = mapOf(
        "category" to category,
        "limit" to limit
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): CategoryBudget = CategoryBudget(
            id = id,
            category = map["category"] as? String ?: "",
            limit = (map["limit"] as? Number)?.toDouble() ?: 0.0
        )
    }
}

/** Resumen financiero del usuario, calculado a partir de sus movimientos y presupuestos. */
data class FinanceSummary(
    val availableBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val totalBudgeted: Double = 0.0,
    val totalBudgetSpent: Double = 0.0,
    val budgetAlert: BudgetAlert? = null,
    val monthlyBars: List<MonthlyExpenseBar> = emptyList(),
    val recentMovements: List<Movement> = emptyList(),
    val budgets: List<CategoryBudget> = emptyList()
)
