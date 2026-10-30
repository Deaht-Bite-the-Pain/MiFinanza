package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udb.mifinanza.data.BudgetAlert
import com.udb.mifinanza.data.FinanceSummary
import com.udb.mifinanza.data.daysLeftText
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LoadingBox
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.finance.FinanceViewModel
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import kotlin.math.abs

@Composable
fun HomeScreen(
    profile: UserProfile,
    onOpenProfile: () -> Unit,
    onOpenNewMovement: () -> Unit,
    onOpenMovementDetail: (Movement) -> Unit,
    onTabSelected: (NavTab) -> Unit,
    onOpenReports: () -> Unit = {},
    financeViewModel: FinanceViewModel = viewModel()
) {
    StatusBarIcons(dark = true)

    val financeState by financeViewModel.state.collectAsState()
    val summary = financeState.summary
    val symbol = profile.currencySymbol

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeTopBar(
                    // Mientras se carga el perfil no se muestra ningún nombre.
                    name = if (profile.name.isBlank()) "" else profile.firstName,
                    onOpenProfile = onOpenProfile
                )

                financeState.error?.let { ErrorBanner(it, onRetry = financeViewModel::loadSummary) }

                if (!financeState.loaded) {
                    if (financeState.error == null) LoadingBox()
                } else {
                    BalanceCard(summary = summary, symbol = symbol)

                    summary.budgetAlert?.let { BudgetAlertBanner(alert = it, symbol = symbol) }

                    QuickActionsRow(
                        onAddMovement = onOpenNewMovement,
                        onViewReports = onOpenReports
                    )

                    MonthlyExpensesChart(summary = summary)

                    RecentMovementsSection(
                        movements = summary.recentMovements,
                        symbol = symbol,
                        onMovementClick = onOpenMovementDetail,
                        onViewAll = { onTabSelected(NavTab.Historial) }
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            BottomNavBar(active = NavTab.Inicio, onSelect = onTabSelected)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .size(56.dp)
                .background(Accent500, RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenNewMovement),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = White, fontSize = 28.sp, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
private fun HomeTopBar(name: String, onOpenProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Hola,",
                fontFamily = Manrope,
                fontSize = 13.5.sp,
                color = Ink500
            )
            Text(
                name,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Ink950
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                CardOutlineIcon(tint = Ink950)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Perfil",
                    tint = Ink950,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(summary: FinanceSummary, symbol: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00182F), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "SALDO DISPONIBLE",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF7E9BB3)
                )
                Text(
                    (if (summary.availableBalance < 0) "-" else "") + formatMoney(abs(summary.availableBalance), symbol),
                    fontFamily = Manrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = White
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Ingresos del mes",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        color = Color(0xFF7E9BB3)
                    )
                    Text(
                        "+" + formatMoney(summary.monthlyIncome, symbol),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Gastos del mes",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        color = Color(0xFF7E9BB3)
                    )
                    Text(
                        "-" + formatMoney(summary.monthlyExpenses, symbol),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetAlertBanner(alert: BudgetAlert, symbol: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF9EE), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFDCA859), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Alerta",
                tint = Color(0xFF9E6515),
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (alert.isExceeded) "${alert.category} superó el límite (${alert.percentage}%)"
                    else "${alert.category} al ${alert.percentage}% del límite",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF804F0D)
                )
                Text(
                    "${formatMoney(alert.spent, symbol, 0)} de ${formatMoney(alert.limit, symbol, 0)} · " +
                        daysLeftText(alert.daysRemaining, alert.period),
                    fontFamily = Manrope,
                    fontSize = 12.sp,
                    color = Color(0xFF9E6515)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(onAddMovement: () -> Unit, onViewReports: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                .clickable(onClick = onAddMovement)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "+",
                    color = Accent500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Agregar movimiento",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Ink950
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                .clickable(onClick = onViewReports)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChartIcon(tint = Accent500)
                Text(
                    "Ver reportes",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Ink950
                )
            }
        }
    }
}

@Composable
private fun MonthlyExpensesChart(summary: FinanceSummary) {
    val bars = summary.monthlyBars
    val maxAmt = (bars.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Ink200, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Últimos 6 meses",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Ink950
                )
                Text(
                    "GASTOS",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Ink400
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    // Los meses sin gastos se ven como una línea mínima, no como una barra.
                    val fraction = (bar.amount / maxAmt).toFloat().coerceIn(0.03f, 1f)
                    val barHeight = (100 * fraction).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(barHeight)
                                .background(
                                    if (bar.isCurrentMonth) Color(0xFF1461A0) else Color(0xFFD2DBE2),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            bar.monthLabel,
                            fontFamily = Manrope,
                            fontWeight = if (bar.isCurrentMonth) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = if (bar.isCurrentMonth) Ink950 else Ink400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentMovementsSection(
    movements: List<Movement>,
    symbol: String,
    onMovementClick: (Movement) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Movimientos recientes",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Ink950
            )
            Text(
                "VER TODOS",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                letterSpacing = 0.5.sp,
                color = Accent500,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }

        if (movements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay movimientos registrados.", fontFamily = Manrope, fontSize = 13.sp, color = Ink500)
            }
        } else {
            movements.forEach { mov ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White, RoundedCornerShape(12.dp))
                        .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                        .clickable { onMovementClick(mov) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                mov.title,
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Ink950
                            )
                            Text(
                                "${mov.category} · ${mov.formattedDate}",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                color = Ink500
                            )
                        }
                        Text(
                            mov.displayAmount(symbol),
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = if (mov.type == MovementType.INGRESO) Success500 else Danger500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardOutlineIcon(tint: Color) {
    val icon = remember {
        ImageVector.Builder(
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z").toNodes(),
                fill = androidx.compose.ui.graphics.SolidColor(tint)
            )
        }.build()
    }
    Icon(icon, contentDescription = "Tarjeta", tint = tint, modifier = Modifier.size(22.dp))
}

@Composable
private fun ChartIcon(tint: Color) {
    val icon = remember {
        ImageVector.Builder(
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z").toNodes(),
                fill = androidx.compose.ui.graphics.SolidColor(tint)
            )
        }.build()
    }
    Icon(icon, contentDescription = "Reportes", tint = tint, modifier = Modifier.size(20.dp))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomePreview() {
    MiFinanzaTheme {
        HomeScreen(
            profile = UserProfile(name = "María Fernanda Ruiz"),
            onOpenProfile = {},
            onOpenNewMovement = {},
            onOpenMovementDetail = {},
            onTabSelected = {}
        )
    }
}
