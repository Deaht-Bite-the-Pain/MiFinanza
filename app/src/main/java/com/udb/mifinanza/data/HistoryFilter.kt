package com.udb.mifinanza.data

import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TypeFilter(val label: String) {
    GASTOS("Gastos"),
    INGRESOS("Ingresos"),
    AMBOS("Ambos")
}

/** Filtros avanzados del historial. Un valor nulo o vacío significa "sin filtrar por ese criterio". */
data class HistoryFilter(
    val type: TypeFilter = TypeFilter.AMBOS,
    /** Inicio del día (hora local) desde el que se muestran movimientos. */
    val dateFrom: Long? = null,
    /** Último milisegundo del día (hora local) hasta el que se muestran movimientos. */
    val dateTo: Long? = null,
    val categories: Set<String> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    val hasType: Boolean get() = type != TypeFilter.AMBOS
    val hasDates: Boolean get() = dateFrom != null || dateTo != null
    val hasCategories: Boolean get() = categories.isNotEmpty()
    val hasAmounts: Boolean get() = minAmount != null || maxAmount != null

    /** Cuántos criterios hay activos (para el contador "2 filtros"). */
    val activeCount: Int
        get() = listOf(hasType, hasDates, hasCategories, hasAmounts).count { it }

    /** Corrige rangos invertidos (desde > hasta) intercambiando los extremos. */
    fun normalized(): HistoryFilter {
        var f = this
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            f = f.copy(dateFrom = startOfDay(dateTo), dateTo = endOfDay(dateFrom))
        }
        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            f = f.copy(minAmount = maxAmount, maxAmount = minAmount)
        }
        return f
    }

    fun matches(movement: Movement): Boolean {
        when (type) {
            TypeFilter.GASTOS -> if (movement.type != MovementType.GASTO) return false
            TypeFilter.INGRESOS -> if (movement.type != MovementType.INGRESO) return false
            TypeFilter.AMBOS -> Unit
        }
        if (dateFrom != null && movement.timestamp < dateFrom) return false
        if (dateTo != null && movement.timestamp > dateTo) return false
        if (categories.isNotEmpty() && categories.none { it.equals(movement.category, ignoreCase = true) }) return false
        if (minAmount != null && movement.amount < minAmount) return false
        if (maxAmount != null && movement.amount > maxAmount) return false
        return true
    }
}

/** Minúsculas y sin tildes, para que "alimentacion" encuentre "Alimentación". */
private fun String.searchable(): String =
    Normalizer.normalize(this.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

/** Búsqueda por texto en título, descripción, etiquetas y categoría. */
fun Movement.matchesQuery(query: String): Boolean {
    val q = query.trim().removePrefix("#").searchable()
    if (q.isEmpty()) return true
    return title.searchable().contains(q) ||
        description.searchable().contains(q) ||
        category.searchable().contains(q) ||
        tags.any { it.searchable().contains(q) }
}

fun List<Movement>.filterHistory(query: String, filter: HistoryFilter): List<Movement> =
    filter { it.matchesQuery(query) && filter.matches(it) }

fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun endOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = startOfDay(millis)
    add(Calendar.DAY_OF_MONTH, 1)
    add(Calendar.MILLISECOND, -1)
}.timeInMillis

/** Primer y último instante del mes de [millis]. */
fun monthRange(millis: Long): Pair<Long, Long> {
    val first = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis
    val last = Calendar.getInstance().apply {
        timeInMillis = startOfDay(first)
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }.timeInMillis
    return startOfDay(first) to last
}

/** "12/08/2026" */
fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))

/** "12 AGO 2026" (encabezado de cada día del historial). */
fun formatDayHeader(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(Date(millis)).uppercase().replace(".", "")
