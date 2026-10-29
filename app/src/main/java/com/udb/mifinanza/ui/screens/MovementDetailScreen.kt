package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.budgetPeriodRange
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent100
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Danger100
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.Success100
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovementDetailScreen(
    movement: Movement,
    currencySymbol: String,
    budget: CategoryBudget?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTabSelected: (NavTab) -> Unit,
    isDeleting: Boolean = false,
    errorMessage: String? = null
) {
    StatusBarIcons(dark = true)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isExpense = movement.type == MovementType.GASTO
    // Un gasto solo afecta un presupuesto si el usuario creó uno para esa categoría.
    val budgetText = when {
        budget == null -> "Sin presupuesto"
        movement.timestamp !in budgetPeriodRange(budget.period).let { it.first..it.second } -> "Periodo anterior"
        else -> "${budget.category} · ${budget.percentage}%"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Ink950,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "Detalle",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink950
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { ErrorBanner(it) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (movement.type == MovementType.GASTO) Danger100 else Success100,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (movement.type == MovementType.GASTO) "GASTO" else "INGRESO",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (movement.type == MovementType.GASTO) Danger500 else Success500
                        )
                    }

                    Text(
                        movement.displayAmount(currencySymbol),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Ink950
                    )

                    Text(
                        movement.title,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = Ink950
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
            ) {
                Column {
                    DetailRow(label = "Categoría", value = movement.category, isHighlight = false)
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                    DetailRow(label = "Fecha", value = movement.displayFullDate, isHighlight = false)
                    if (movement.paymentMethod.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                        DetailRow(label = "Método de pago", value = movement.paymentMethod, isHighlight = false)
                    }
                    if (isExpense) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                        DetailRow(label = "Presupuesto afectado", value = budgetText, isHighlight = budget != null)
                    }
                }
            }

            if (movement.description.isNotEmpty() || movement.tags.isNotEmpty()) Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "DESCRIPCIÓN",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Ink500
                    )
                    if (movement.description.isNotEmpty()) {
                        Text(
                            movement.description,
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = Ink950
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        movement.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Accent100, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "#$tag",
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Accent700
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink50)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                enabled = !isDeleting,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, Danger500),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
            ) {
                Text(
                    if (isDeleting) "Eliminando..." else "Eliminar",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Danger500
                )
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent500)
            ) {
                Text(
                    "Editar",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = White
                )
            }
        }

        BottomNavBar(active = NavTab.Movimientos, onSelect = onTabSelected)

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Eliminar movimiento", fontFamily = Manrope, fontWeight = FontWeight.Bold) },
                text = { Text("¿Estás seguro de que deseas eliminar este movimiento? Esta acción no se puede deshacer.", fontFamily = Manrope) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Danger500)
                    ) {
                        Text("Eliminar", color = White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancelar", color = Ink600)
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isHighlight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Ink600
        )
        Text(
            value,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = if (isHighlight) Accent500 else Ink950
        )
    }
}
