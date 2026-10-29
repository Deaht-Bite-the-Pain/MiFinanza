package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.HistoryFilter
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.TypeFilter
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
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import kotlin.math.abs

/** Ícono del botón de filtros: tres barras de largo decreciente. */
@Composable
fun FilterBarsIcon(modifier: Modifier = Modifier, tint: Color = Accent500) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = 2.dp.toPx()

        drawLine(
            color = tint,
            start = Offset(w * 0.20f, h * 0.32f),
            end = Offset(w * 0.80f, h * 0.32f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = tint,
            start = Offset(w * 0.32f, h * 0.50f),
            end = Offset(w * 0.68f, h * 0.50f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = tint,
            start = Offset(w * 0.44f, h * 0.68f),
            end = Offset(w * 0.56f, h * 0.68f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
    }
}

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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                !loaded && error == null -> LoadingBox()
                !loaded -> Column(
                    modifier = Modifier.padding(16.dp),
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
                    message = "Registra tu primer ingreso o gasto para empezar a ver tu historial.",
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (error != null) item { ErrorBanner(error) }

                    groups.forEach { (day, list) ->
                        item(key = "day-$day") {
                            val net = list.sumOf { if (it.type == MovementType.INGRESO) it.amount else -it.amount }
                            val sign = if (net >= 0) "+" else "-"
                            Text(
                                text = "${formatDayHeader(day)}  ·  $sign${formatMoney(abs(net), currencySymbol)}",
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF385773),
                                modifier = Modifier.padding(top = 14.dp, bottom = 4.dp, start = 2.dp)
                            )
                        }

                        items(list, key = { it.id }) { movement ->
                            MovementCard(movement, currencySymbol) { onMovementClick(movement) }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
    ) {
        Column(
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Ink950
                )
                Text(
                    text = if (count == 1) "1 MOVIMIENTO" else "$count MOVIMIENTOS",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF385773)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(White, RoundedCornerShape(8.dp))
                        .border(1.dp, Ink200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = Ink500,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                "Buscar por descripción o etiqueta",
                                fontFamily = Manrope,
                                fontSize = 13.5.sp,
                                color = Ink400,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = Manrope,
                                fontSize = 13.5.sp,
                                color = Ink950
                            ),
                            cursorBrush = SolidColor(Accent500),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onQueryChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "×",
                                fontSize = 18.sp,
                                color = Ink500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Accent500, RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenFilters),
                    contentAlignment = Alignment.Center
                ) {
                    FilterBarsIcon(
                        modifier = Modifier.size(20.dp),
                        tint = Accent500
                    )
                }
            }

            ActiveFilterChips(
                filter = filter,
                symbol = currencySymbol,
                onOpenFilters = onOpenFilters,
                onChange = onFilterChange
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Ink200)
        )
    }
}

@Composable
private fun ActiveFilterChips(
    filter: HistoryFilter,
    symbol: String,
    onOpenFilters: () -> Unit,
    onChange: (HistoryFilter) -> Unit
) {
    if (filter.activeCount == 0) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (filter.hasDates) {
            val (monthStart, monthEnd) = monthRange(System.currentTimeMillis())
            val label = if (filter.dateFrom == monthStart && filter.dateTo == monthEnd) "Este mes"
            else "${filter.dateFrom?.let(::formatDate) ?: "…"} – ${filter.dateTo?.let(::formatDate) ?: "…"}"
            RemovableFilterChip(
                label = label,
                solid = true,
                onRemove = { onChange(filter.copy(dateFrom = null, dateTo = null)) }
            )
        }
        if (filter.hasType) {
            RemovableFilterChip(
                label = filter.type.label,
                solid = false,
                onRemove = { onChange(filter.copy(type = TypeFilter.AMBOS)) }
            )
        }
        if (filter.hasCategories) {
            val label = if (filter.categories.size == 1) filter.categories.first() else "${filter.categories.size} categorías"
            RemovableFilterChip(
                label = label,
                solid = false,
                onRemove = { onChange(filter.copy(categories = emptySet())) }
            )
        }
        if (filter.hasAmounts) {
            val min = filter.minAmount?.let { formatMoney(it, symbol, 0) }
            val max = filter.maxAmount?.let { formatMoney(it, symbol, 0) }
            val label = when {
                min != null && max != null -> "$min – $max"
                min != null -> "≥ $min"
                else -> "≤ $max"
            }
            RemovableFilterChip(
                label = label,
                solid = false,
                onRemove = { onChange(filter.copy(minAmount = null, maxAmount = null)) }
            )
        }
        if (filter.activeCount > 1) {
            Box(
                modifier = Modifier
                    .background(White, CircleShape)
                    .border(1.dp, Ink200, CircleShape)
                    .clickable(onClick = onOpenFilters)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${filter.activeCount} filtros",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = Ink600
                )
            }
        }
    }
}

@Composable
private fun RemovableFilterChip(
    label: String,
    solid: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (solid) Accent500 else Color(0xFFE2ECF5),
                CircleShape
            )
            .clickable(onClick = onRemove)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = if (solid) White else Accent700
            )
            Text(
                text = "×",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (solid) White else Accent700
            )
        }
    }
}

@Composable
private fun MovementCard(movement: Movement, symbol: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, Ink200), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = movement.title,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Ink950,
                maxLines = 1
            )
            Text(
                text = listOf(movement.category, movement.paymentMethod).filter { it.isNotBlank() }.joinToString(" · "),
                fontFamily = Manrope,
                fontSize = 12.5.sp,
                color = Ink500,
                maxLines = 1
            )
        }

        val amountColor = if (movement.type == MovementType.INGRESO) Success500 else Danger500
        Text(
            text = movement.displayAmount(symbol),
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = amountColor
        )
    }
}

private val PreviewMovements = listOf(
    Movement(id = "1", title = "Supermercado Santa María", category = "Alimentación", amount = 42.80, type = MovementType.GASTO, paymentMethod = "Efectivo", timestamp = 1786521600000L),
    Movement(id = "2", title = "Cine Multiplaza", category = "Ocio", amount = 18.00, type = MovementType.GASTO, paymentMethod = "Tarjeta", timestamp = 1786435200000L),
    Movement(id = "3", title = "Bus urbano", category = "Transporte", amount = 13.20, type = MovementType.GASTO, paymentMethod = "Efectivo", timestamp = 1786435200000L),
    Movement(id = "4", title = "Salario agosto", category = "Salario", amount = 1850.00, type = MovementType.INGRESO, paymentMethod = "Transferencia", timestamp = 1786348800000L),
    Movement(id = "5", title = "Internet fibra 100MB", category = "Servicios", amount = 45.00, type = MovementType.GASTO, paymentMethod = "Cuenta de Ahorros", timestamp = 1786176000000L)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HistoryPreview() {
    MiFinanzaTheme {
        HistoryScreen(PreviewMovements, loaded = true, error = null, currencySymbol = "$", {}, {}, {}, {})
    }
}
