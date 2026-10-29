package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.BudgetExceededDialog
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LoadingBox
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.finance.FinanceViewModel
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetScreen(
    currencySymbol: String,
    onTabSelected: (NavTab) -> Unit,
    onAddBudget: () -> Unit = {},
    onEditBudget: (CategoryBudget) -> Unit = {},
    financeViewModel: FinanceViewModel = viewModel()
) {
    StatusBarIcons(dark = true)

    val financeState by financeViewModel.state.collectAsState()
    val summary = financeState.summary
    val budgets = summary.budgets

    var exceededBudgetToShow by remember { mutableStateOf<CategoryBudget?>(null) }
    var hasAutoShownAlert by rememberSaveable { mutableStateOf(false) }

    // El aviso de presupuesto excedido se abre solo, una vez por visita a la pantalla.
    LaunchedEffect(budgets) {
        if (!hasAutoShownAlert && budgets.isNotEmpty()) {
            val firstExceeded = budgets.find { it.isExceeded }
            if (firstExceeded != null) {
                exceededBudgetToShow = firstExceeded
                hasAutoShownAlert = true
            }
        }
    }

    val monthTitle = remember {
        SimpleDateFormat("MMMM  yyyy", Locale("es", "ES")).format(Date()).uppercase()
    }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Presupuestos",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Ink950
                    )
                    Text(
                        monthTitle,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF385773)
                    )
                }

                financeState.error?.let { ErrorBanner(it) }

                if (!financeState.loaded) {
                    if (financeState.error == null) LoadingBox()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White, RoundedCornerShape(12.dp))
                            .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Total presupuestado",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Ink500
                                )
                                Text(
                                    formatMoney(summary.totalBudgeted, currencySymbol),
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Ink950
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Gastado",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Ink500
                                )
                                Text(
                                    formatMoney(summary.totalBudgetSpent, currencySymbol),
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Danger500
                                )
                            }
                        }
                    }

                    if (budgets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White, RoundedCornerShape(12.dp))
                                .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aún no tienes presupuestos. Crea uno con \"+ Nuevo\" para controlar tus gastos por categoría.",
                                fontFamily = Manrope,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Ink500
                            )
                        }
                    }

                    budgets.forEach { budget ->
                        BudgetCategoryCard(
                            budget = budget,
                            currencySymbol = currencySymbol,
                            onClick = {
                                if (budget.isExceeded) {
                                    exceededBudgetToShow = budget
                                } else {
                                    onEditBudget(budget)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(70.dp))
            }

            BottomNavBar(active = NavTab.Presupuestos, onSelect = onTabSelected)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .background(Accent500, RoundedCornerShape(16.dp))
                .clickable {
                    onAddBudget()
                }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "+",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Nuevo",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = White
                )
            }
        }

        exceededBudgetToShow?.let { budget ->
            BudgetExceededDialog(
                budget = budget,
                currencySymbol = currencySymbol,
                onDismiss = { exceededBudgetToShow = null },
                onAdjustLimit = {
                    val toEdit = budget
                    exceededBudgetToShow = null
                    onEditBudget(toEdit)
                }
            )
        }
    }
}

@Composable
private fun BudgetCategoryCard(budget: CategoryBudget, currencySymbol: String, onClick: () -> Unit) {
    val isExceeded = budget.isExceeded
    val isWarning = budget.isNearLimit

    val barColor = when {
        isExceeded -> Danger500
        isWarning -> Color(0xFFB57916)
        else -> Accent500
    }

    val cardBorder = if (isExceeded) {
        BorderStroke(1.5.dp, Danger500)
    } else {
        BorderStroke(1.dp, Ink200)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(cardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        budget.category,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = Ink950
                    )
                    if (budget.period != "Mensual") {
                        Text(
                            budget.period,
                            fontFamily = Manrope,
                            fontSize = 11.5.sp,
                            color = Ink500
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val spentStr = formatMoney(budget.spent, currencySymbol, if (budget.spent % 1.0 == 0.0) 0 else 2)
                    val limitStr = formatMoney(budget.limit, currencySymbol, if (budget.limit % 1.0 == 0.0) 0 else 2)

                    val spentColor = when {
                        isExceeded -> Danger500
                        isWarning -> Color(0xFFB57916)
                        else -> Accent500
                    }

                    Text(
                        spentStr,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = spentColor
                    )
                    Text(
                        "/",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        color = Ink400,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        limitStr,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = if (isExceeded) Danger500 else if (isWarning) Color(0xFFB57916) else Ink950
                    )
                }
            }

            val progressFraction = if (budget.limit > 0) (budget.spent / budget.limit).toFloat().coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Ink100, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(8.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
                )
            }

            if (isExceeded) {
                Text(
                    "Excedido en ${formatMoney(budget.exceededAmount, currencySymbol)}",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Danger500
                )
            } else {
                Text(
                    "${budget.percentage}% usado · quedan ${formatMoney(budget.remaining, currencySymbol)}",
                    fontFamily = Manrope,
                    fontSize = 12.5.sp,
                    color = Ink500
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BudgetPreview() {
    MiFinanzaTheme {
        BudgetScreen(currencySymbol = "$", onTabSelected = {})
    }
}
