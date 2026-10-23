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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.LabeledTextField
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
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White
import java.util.Locale

@Composable
fun BudgetScreen(
    onTabSelected: (NavTab) -> Unit,
    financeViewModel: FinanceViewModel = viewModel()
) {
    StatusBarIcons(dark = true)

    val financeState by financeViewModel.state.collectAsState()
    val summary = financeState.summary
    val budgets = summary.budgets

    var showNewBudgetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Contenido Scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Superior: "Presupuestos" y "AGOSTO 2026"
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
                        "AGOSTO  2026",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF385773)
                    )
                }

                // Tarjeta 1: Total Presupuestado vs Gastado
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
                                String.format(Locale.US, "$%,.2f", summary.totalBudgeted),
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
                                String.format(Locale.US, "$%,.2f", summary.monthlyExpenses),
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Danger500
                            )
                        }
                    }
                }

                // Tarjetas de Presupuesto por Categoría
                budgets.forEach { budget ->
                    BudgetCategoryCard(budget = budget)
                }

                // Espacio inferior para no tapar con el botón flotante "+ Nuevo"
                Spacer(modifier = Modifier.height(70.dp))
            }

            // Barra de Navegación Inferior (Pestaña "Presupuestos" activa)
            BottomNavBar(active = NavTab.Presupuestos, onSelect = onTabSelected)
        }

        // Botón Flotante "+ Nuevo"
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .background(Accent500, RoundedCornerShape(16.dp))
                .clickable { showNewBudgetDialog = true }
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

        // Diálogo para Agregar / Editar Presupuesto
        if (showNewBudgetDialog) {
            AddBudgetDialog(
                onDismiss = { showNewBudgetDialog = false },
                onSave = { newBudget ->
                    financeViewModel.saveBudget(newBudget) {
                        showNewBudgetDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun BudgetCategoryCard(budget: CategoryBudget) {
    val isExceeded = budget.isExceeded
    val isWarning = budget.category == "Alimentación" && budget.percentage >= 90 && !isExceeded

    val barColor = when {
        isExceeded -> Danger500
        isWarning -> Color(0xFFB57916) // Ámbar / dorado
        else -> Accent500 // Azul estándar
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
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Fila de Título y Montos ($gastado / $límite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    budget.category,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = Ink950
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val spentStr = if (budget.spent % 1.0 == 0.0) {
                        String.format(Locale.US, "$%.0f", budget.spent)
                    } else {
                        String.format(Locale.US, "$%.2f", budget.spent)
                    }

                    val limitStr = String.format(Locale.US, "$%.0f", budget.limit)

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

            // Barra de Progreso Personalizada
            val progressFraction = (budget.spent / budget.limit).toFloat().coerceIn(0f, 1f)
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

            // Texto Inferior de Estado
            if (isExceeded) {
                Text(
                    String.format(Locale.US, "Excedido en $%.2f", budget.exceededAmount),
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Danger500
                )
            } else {
                Text(
                    String.format(Locale.US, "%d%% usado · quedan $%.2f", budget.percentage, budget.remaining),
                    fontFamily = Manrope,
                    fontSize = 12.5.sp,
                    color = Ink500
                )
            }
        }
    }
}

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (CategoryBudget) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Presupuesto", fontFamily = Manrope, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledTextField(
                    label = "Categoría",
                    value = category,
                    onValueChange = { category = it },
                    placeholder = "Ej. Educación, Ropa, Gimnasio..."
                )
                LabeledTextField(
                    label = "Límite mensual ($)",
                    value = limitText,
                    onValueChange = { limitText = it },
                    placeholder = "150.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (category.isNotBlank() && limit > 0.0) {
                        onSave(CategoryBudget(category = category.trim(), limit = limit, spent = 0.0))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent500)
            ) {
                Text("Guardar", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Ink600)
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BudgetPreview() {
    MiFinanzaTheme {
        BudgetScreen(onTabSelected = {})
    }
}
