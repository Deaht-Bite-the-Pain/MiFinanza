package com.udb.mifinanza.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tipo de movimiento financiero: Ingreso o Gasto */
enum class MovementType {
    INGRESO,
    GASTO
}

/** Representa un movimiento registrado en Firestore en `users/{uid}/movements/{id}` */
data class Movement(
    val id: String = "",
    val title: String = "",
    val category: String = "Alimentación",
    val amount: Double = 0.0,
    val type: MovementType = MovementType.GASTO,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val fullDateString: String = "",
    val paymentMethod: String = "Efectivo",
    val description: String = "",
    val tags: List<String> = emptyList()
) {
    val formattedDate: String
        get() {
            if (dateString.isNotEmpty()) return dateString
            val sdf = SimpleDateFormat("d MMM", Locale("es", "ES"))
            return sdf.format(Date(timestamp))
        }

    val displayFullDate: String
        get() {
            if (fullDateString.isNotEmpty()) return fullDateString
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            return sdf.format(Date(timestamp))
        }

    val displayAmount: String
        get() {
            val formatted = String.format(Locale.US, "%.2f", amount)
            return if (type == MovementType.INGRESO) "+$$formatted" else "-$$formatted"
        }

    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "category" to category,
        "amount" to amount,
        "type" to type.name,
        "timestamp" to timestamp,
        "dateString" to dateString,
        "fullDateString" to fullDateString,
        "paymentMethod" to paymentMethod,
        "description" to description,
        "tags" to tags
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Movement {
            val typeStr = map["type"] as? String ?: MovementType.GASTO.name
            val type = runCatching { MovementType.valueOf(typeStr) }.getOrDefault(MovementType.GASTO)
            val amt = (map["amount"] as? Number)?.toDouble() ?: 0.0
            val ts = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            @Suppress("UNCHECKED_CAST")
            val tagsList = (map["tags"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return Movement(
                id = id,
                title = map["title"] as? String ?: "",
                category = map["category"] as? String ?: "Alimentación",
                amount = amt,
                type = type,
                timestamp = ts,
                dateString = map["dateString"] as? String ?: "",
                fullDateString = map["fullDateString"] as? String ?: "",
                paymentMethod = map["paymentMethod"] as? String ?: "Efectivo",
                description = map["description"] as? String ?: "",
                tags = tagsList
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

/** Alerta de presupuesto para categorías críticas */
data class BudgetAlert(
    val category: String = "Alimentación",
    val spent: Double = 276.0,
    val limit: Double = 300.0,
    val daysRemaining: Int = 6
) {
    val percentage: Int
        get() = if (limit > 0) ((spent / limit) * 100).toInt() else 0
}

/** Resumen financiero consolidado para la pantalla de inicio */
data class FinanceSummary(
    val availableBalance: Double = 1284.50,
    val monthlyIncome: Double = 1850.00,
    val monthlyExpenses: Double = 565.50,
    val budgetAlert: BudgetAlert = BudgetAlert(),
    val monthlyBars: List<MonthlyExpenseBar> = emptyList(),
    val recentMovements: List<Movement> = emptyList()
)
