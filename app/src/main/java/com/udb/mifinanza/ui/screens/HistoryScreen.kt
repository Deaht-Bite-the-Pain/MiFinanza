package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.HistoryFilter
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.filterHistory
import com.udb.mifinanza.data.formatDate
import com.udb.mifinanza.data.formatDayHeader
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.data.monthRange
import com.udb.mifinanza.data.startOfDay
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.EmptyState
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LoadingBox
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent100
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import kotlin.math.abs

private fun strokeIcon(vararg paths: String): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()

/** Ícono de embudo del botón de filtros. */
internal val FilterIcon: ImageVector by lazy { strokeIcon("M3 5h18l-7 8v6l-4-2v-4z") }

@Composable
fun HistoryScreen(
    movements: List<Movement>,
    loaded: Boolean,
    error: String?,
    currencySymbol: String,
    onMovementClick: (Movement) -> Unit,
    onAddMovement: () -> Unit,
    onRetry: () -> Unit,
    onTabSelected: (NavTab) -> Unit
) {
    StatusBarIcons(dark = true)

    var query by rememberSaveable { mutableStateOf("") }
    var filter by remember { mutableStateOf(HistoryFilter()) }
    var showFilters by remember { mutableStateOf(false) }

    val results = remember(movements, query, filter) { movements.filterHistory(query, filter) }
    val groups = remember(results) {
        results.groupBy { startOfDay(it.timestamp) }.toList().sortedByDescending { it.first }
    }
    val hasCriteria = query.isNotBlank() || filter.activeCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HistoryHeader(
            count = results.size,
            query = query,
            onQueryChange = { query = it },
            filter = filter,
            currencySymbol = currencySymbol,
            onOpenFilters = { showFilters = true },
            onFilterChange = { filter = it }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                !loaded && error == null -> LoadingBox()
                !loaded -> Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ErrorBanner(error ?: "")
                    Text(
                        "REINTENTAR",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Accent500,
                        modifier = Modifier.clickable(onClick = onRetry)
                    )
                }
                movements.isEmpty() -> EmptyState(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "Sin movimientos aún",
                    message = "Registra tu primer ingreso o gasto para empezar a ver tu historial y tus reportes.",
                    actionText = "Agregar movimiento",
                    onAction = onAddMovement
                )
                results.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = "Sin resultados",
                    message = "No encontramos movimientos con esa búsqueda o esos filtros.",
                    actionText = if (hasCriteria) "Limpiar filtros" else null,
                    onAction = {
                        query = ""
                        filter = HistoryFilter()
                    }
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (error != null) item { ErrorBanner(error) }
                    groups.forEach { (day, list) ->
                        item(key = "day-$day") {
                            val net = list.sumOf { if (it.type == MovementType.INGRESO) it.amount else -it.amount }
                            val sign = if (net >= 0) "+" else "-"
                            Text(
                                "${formatDayHeader(day)} · $sign${formatMoney(abs(net), currencySymbol)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.6.sp,
                                color = Ink500,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(list, key = { it.id }) { movement ->
                            MovementRow(movement, currencySymbol) { onMovementClick(movement) }
                        }
                    }
                }
            }
        }

        BottomNavBar(active = NavTab.Historial, onSelect = onTabSelected)
    }

    if (showFilters) {
        FiltersSheet(
            initial = filter,
            movements = movements,
            query = query,
            currencySymbol = currencySymbol,
            onApply = {
                filter = it
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun HistoryHeader(
    count: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: HistoryFilter,
    currencySymbol: String,
    onOpenFilters: () -> Unit,
    onFilterChange: (HistoryFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(White)) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Historial", fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                Text(
                    if (count == 1) "1 MOVIMIENTO" else "$count MOVIMIENTOS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    color = Ink500
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Ink50, RoundedCornerShape(6.dp))
                        .border(1.dp, Ink200, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Ink500, modifier = Modifier.size(18.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Buscar por descripción o etiqueta", fontFamily = Manrope, fontSize = 13.5.sp, color = Ink500, maxLines = 1)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = Manrope, fontSize = 13.5.sp, color = Ink950),
                            cursorBrush = SolidColor(Accent500),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Text("×", fontSize = 18.sp, color = Ink500, modifier = Modifier.clickable { onQueryChange("") })
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, Accent500, RoundedCornerShape(6.dp))
                        .background(if (filter.activeCount > 0) Accent100 else White, RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenFilters),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FilterIcon, contentDescription = "Filtros", tint = Accent500, modifier = Modifier.size(20.dp))
                }
            }
            ActiveFilterChips(filter, currencySymbol, onFilterChange)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink200))
    }
}

@Composable
private fun ActiveFilterChips(filter: HistoryFilter, symbol: String, onChange: (HistoryFilter) -> Unit) {
    if (filter.activeCount == 0) return
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (filter.hasDates) {
            val (monthStart, monthEnd) = monthRange(System.currentTimeMillis())
            val label = if (filter.dateFrom == monthStart && filter.dateTo == monthEnd) "Este mes"
            else "${filter.dateFrom?.let(::formatDate) ?: "…"} – ${filter.dateTo?.let(::formatDate) ?: "…"}"
            RemovableChip(label, solid = true) { onChange(filter.copy(dateFrom = null, dateTo = null)) }
        }
        if (filter.hasType) {
            RemovableChip(filter.type.label, solid = false) { onChange(filter.copy(type = com.udb.mifinanza.data.TypeFilter.AMBOS)) }
        }
        if (filter.hasCategories) {
            val label = if (filter.categories.size == 1) filter.categories.first() else "${filter.categories.size} categorías"
            RemovableChip(label, solid = false) { onChange(filter.copy(categories = emptySet())) }
        }
        if (filter.hasAmounts) {
            val min = filter.minAmount?.let { formatMoney(it, symbol, 0) }
            val max = filter.maxAmount?.let { formatMoney(it, symbol, 0) }
            val label = when {
                min != null && max != null -> "$min – $max"
                min != null -> "≥ $min"
                else -> "≤ $max"
            }
            RemovableChip(label, solid = false) { onChange(filter.copy(minAmount = null, maxAmount = null)) }
        }
        if (filter.activeCount > 1) {
            Text(
                "${filter.activeCount} filtros",
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = Ink500,
                modifier = Modifier
                    .border(1.dp, Ink200, CircleShape)
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun RemovableChip(label: String, solid: Boolean, onRemove: () -> Unit) {
    Text(
        "$label ×",
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        color = if (solid) White else Accent700,
        maxLines = 1,
        modifier = Modifier
            .background(if (solid) Accent500 else Accent100, CircleShape)
            .clickable(onClick = onRemove)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    )
}

@Composable
private fun MovementRow(movement: Movement, symbol: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, Ink200), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                movement.title,
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = Ink950,
                maxLines = 1
            )
            Text(
                listOf(movement.category, movement.paymentMethod).filter { it.isNotBlank() }.joinToString(" · "),
                fontFamily = Manrope,
                fontSize = 11.5.sp,
                color = Ink500,
                maxLines = 1
            )
        }
        Text(
            movement.displayAmount(symbol),
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (movement.type == MovementType.INGRESO) Success500 else Danger500
        )
    }
}

private val PreviewMovements = listOf(
    Movement(id = "1", title = "Supermercado Santa María", category = "Alimentación", amount = 42.80, paymentMethod = "Efectivo"),
    Movement(id = "2", title = "Salario agosto", category = "Salario", amount = 1850.0, type = MovementType.INGRESO, paymentMethod = "Transferencia")
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HistoryPreview() {
    MiFinanzaTheme {
        HistoryScreen(PreviewMovements, loaded = true, error = null, currencySymbol = "$", {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HistoryEmptyPreview() {
    MiFinanzaTheme {
        HistoryScreen(emptyList(), loaded = true, error = null, currencySymbol = "$", {}, {}, {}, {})
    }
}
