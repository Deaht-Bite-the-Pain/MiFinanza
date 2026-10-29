package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.CategoryTotal
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.buildMonthlyReport
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.EmptyState
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LoadingBox
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import java.util.Calendar
import kotlin.math.abs

/** Reporte mensual: totales, ahorro, comparación con el mes anterior y gasto por categoría. */
@Composable
fun ReportsScreen(
    movements: List<Movement>,
    loaded: Boolean,
    error: String?,
    currencySymbol: String,
    onBack: () -> Unit
) {
    StatusBarIcons(dark = true)

    var monthsAgo by rememberSaveable { mutableIntStateOf(0) }
    val report = remember(movements, monthsAgo) { buildMonthlyReport(movements, monthsAgo) }
    // No se puede retroceder más allá del mes del movimiento más antiguo.
    val oldestMonthsAgo = remember(movements) {
        val oldest = movements.minOfOrNull { it.timestamp } ?: return@remember 0
        val then = Calendar.getInstance().apply { timeInMillis = oldest }
        val now = Calendar.getInstance()
        val diff = (now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH)) -
            (then.get(Calendar.YEAR) * 12 + then.get(Calendar.MONTH))
        diff.coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppTopBar(title = "Reportes", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonthArrow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                description = "Mes anterior",
                enabled = monthsAgo < oldestMonthsAgo,
                onClick = { monthsAgo++ }
            )
            Text(report.title, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink950)
            MonthArrow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                description = "Mes siguiente",
                enabled = monthsAgo > 0,
                onClick = { monthsAgo-- }
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink200))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            error?.let { ErrorBanner(it) }

            when {
                !loaded -> if (error == null) LoadingBox()
                report.movementCount == 0 -> EmptyState(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "Sin movimientos este mes",
                    message = "Cuando registres ingresos o gastos en ${report.title.lowercase()} verás aquí su resumen."
                )
                else -> {
                    ReportCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Amount("Ingresos", "+" + formatMoney(report.income, currencySymbol), Success500)
                            Amount("Gastos", "-" + formatMoney(report.expenses, currencySymbol), Danger500, Alignment.End)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Amount(
                                "Balance del mes",
                                (if (report.net < 0) "-" else "+") + formatMoney(abs(report.net), currencySymbol),
                                if (report.net < 0) Danger500 else Ink950
                            )
                            Amount(
                                "Ahorro",
                                report.savingsRate?.let { "$it%" } ?: "—",
                                Ink950,
                                Alignment.End
                            )
                        }
                    }

                    ReportCard {
                        SectionTitle("RESUMEN")
                        StatRow("Movimientos registrados", report.movementCount.toString())
                        StatRow("Gasto promedio por día", formatMoney(report.dailyAverageExpense, currencySymbol))
                        StatRow(
                            "Frente al mes anterior",
                            report.expenseChange?.let { change ->
                                when {
                                    change > 0 -> "+$change% en gastos"
                                    change < 0 -> "$change% en gastos"
                                    else -> "Igual"
                                }
                            } ?: "Sin datos",
                            when {
                                (report.expenseChange ?: 0) > 0 -> Danger500
                                (report.expenseChange ?: 0) < 0 -> Success500
                                else -> Ink950
                            }
                        )
                        report.largestExpense?.let {
                            StatRow("Gasto más alto", "${it.title} · ${formatMoney(it.amount, currencySymbol)}")
                        }
                    }

                    if (report.expensesByCategory.isNotEmpty()) {
                        CategoryBreakdown("GASTOS POR CATEGORÍA", report.expensesByCategory, currencySymbol, Accent500)
                    }
                    if (report.incomeByCategory.isNotEmpty()) {
                        CategoryBreakdown("INGRESOS POR CATEGORÍA", report.incomeByCategory, currencySymbol, Success500)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) Ink950 else Ink300, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ReportCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Ink200, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.8.sp, color = Ink500)
}

@Composable
private fun Amount(label: String, value: String, color: Color, align: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = align, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontFamily = Manrope, fontSize = 12.5.sp, color = Ink500)
        Text(value, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = Ink950) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Ink600, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = valueColor)
    }
}

@Composable
private fun CategoryBreakdown(title: String, totals: List<CategoryTotal>, symbol: String, barColor: Color) {
    ReportCard {
        SectionTitle(title)
        totals.forEach { total ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(total.category, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Ink950)
                    Text(
                        "${formatMoney(total.amount, symbol)} · ${total.share}%",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Ink600
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Ink100, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((total.share / 100f).coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .background(barColor, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReportsPreview() {
    val now = System.currentTimeMillis()
    MiFinanzaTheme {
        ReportsScreen(
            movements = listOf(
                Movement(id = "1", title = "Salario", category = "Salario", amount = 900.0, type = MovementType.INGRESO, timestamp = now),
                Movement(id = "2", title = "Supermercado", category = "Alimentación", amount = 120.0, timestamp = now),
                Movement(id = "3", title = "Bus", category = "Transporte", amount = 30.0, timestamp = now)
            ),
            loaded = true,
            error = null,
            currencySymbol = "$",
            onBack = {}
        )
    }
}
