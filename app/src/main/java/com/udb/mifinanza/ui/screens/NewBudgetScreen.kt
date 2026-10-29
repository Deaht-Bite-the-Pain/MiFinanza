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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.BudgetPeriods
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.ExpenseCategories
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.currentPeriodText
import com.udb.mifinanza.data.filterAmountInput
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.data.spentInPeriod
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink700
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White
import java.util.Locale

@Composable
fun NewBudgetScreen(
    initialBudget: CategoryBudget? = null,
    currencySymbol: String = "$",
    movements: List<Movement> = emptyList(),
    existingBudgetCategories: List<String> = emptyList(),
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onCancel: () -> Unit,
    onSave: (CategoryBudget) -> Unit,
    onDelete: (CategoryBudget) -> Unit = {}
) {
    StatusBarIcons(dark = true)

    var selectedCategory by remember {
        mutableStateOf(
            initialBudget?.category
                ?: ExpenseCategories.firstOrNull { it !in existingBudgetCategories }
                ?: ExpenseCategories.first()
        )
    }
    var limitText by remember {
        mutableStateOf(
            initialBudget?.let { String.format(Locale.US, "%.2f", it.limit) } ?: ""
        )
    }
    var period by remember {
        mutableStateOf(initialBudget?.period ?: "Mensual")
    }
    var alertEnabled by remember {
        mutableStateOf(initialBudget?.alertEnabled ?: true)
    }
    var alertThreshold by remember {
        mutableFloatStateOf(initialBudget?.alertThreshold?.toFloat() ?: 80f)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val spent = remember(selectedCategory, period, movements) {
        spentInPeriod(movements, selectedCategory, period)
    }

    val limitValue = limitText.toDoubleOrNull() ?: 0.0
    val thresholdAmount = limitValue * (alertThreshold / 100.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Ink950,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = if (initialBudget == null) "Nuevo presupuesto" else "Editar presupuesto",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Ink950
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Ink200)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            (formError ?: errorMessage)?.let { ErrorBanner(it) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Categoría",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Ink600
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(White, RoundedCornerShape(10.dp))
                            .border(1.dp, Ink200, RoundedCornerShape(10.dp))
                            .clickable { categoryDropdownExpanded = true }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory,
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Ink950
                        )
                        Text(
                            text = "▾",
                            color = Ink500,
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        ExpenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat,
                                        fontFamily = Manrope,
                                        fontSize = 14.sp,
                                        color = Ink950
                                    )
                                },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Límite ${period.lowercase()}",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Ink600
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(White, RoundedCornerShape(10.dp))
                        .border(1.5.dp, Accent500, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currencySymbol ",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Ink500
                    )
                    BasicTextField(
                        value = limitText,
                        onValueChange = { limitText = filterAmountInput(it, limitText) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Ink950
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(Accent500),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Periodo",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Ink600
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Ink100, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        BudgetPeriods.forEach { p ->
                            val isSelected = period.equals(p, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(
                                        if (isSelected) White else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.dp, Accent500, RoundedCornerShape(8.dp))
                                        } else Modifier
                                    )
                                    .clickable { period = p },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    fontFamily = Manrope,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Accent500 else Ink600
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(14.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(14.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Alerta al acercarse al límite",
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink950
                            )
                            Text(
                                "Aviso en Inicio al llegar al umbral",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                color = Ink500
                            )
                        }

                        Switch(
                            checked = alertEnabled,
                            onCheckedChange = { alertEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Accent500,
                                uncheckedThumbColor = White,
                                uncheckedTrackColor = Ink200,
                                uncheckedBorderColor = Color.Transparent,
                                checkedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Ink100)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Umbral de alerta",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = Ink700
                        )
                        Text(
                            "${alertThreshold.toInt()}%",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Accent500
                        )
                    }

                    Slider(
                        value = alertThreshold,
                        onValueChange = { alertThreshold = it },
                        valueRange = 10f..100f,
                        enabled = alertEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Accent500,
                            activeTrackColor = Accent500,
                            inactiveTrackColor = Ink100,
                            disabledThumbColor = Ink300,
                            disabledActiveTrackColor = Ink200,
                            disabledInactiveTrackColor = Ink100
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEBF3F9), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val spentText = "En $selectedCategory llevas ${formatMoney(spent, currencySymbol)} gastados ${currentPeriodText(period)}."
                val infoText = when {
                    !alertEnabled -> "$spentText La alerta al límite está desactivada."
                    limitValue <= 0.0 -> "$spentText Ingresa un límite para activar el umbral de alerta."
                    else -> "$spentText Con un límite de ${formatMoney(limitValue, currencySymbol)} la alerta se activaría al llegar a ${formatMoney(thresholdAmount, currencySymbol)}."
                }

                Text(
                    text = infoText,
                    fontFamily = Manrope,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Accent700
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Ink200)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val editing = initialBudget != null
                OutlinedButton(
                    onClick = { if (editing) showDeleteDialog = true else onCancel() },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = if (editing) BorderStroke(1.5.dp, Danger500) else BorderStroke(1.dp, Ink300),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
                ) {
                    Text(
                        if (editing) "Eliminar" else "Cancelar",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (editing) Danger500 else Ink950
                    )
                }

                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        if (limit <= 0.0) {
                            formError = "Ingresa un límite válido mayor a cero."
                            return@Button
                        }
                        val takenByOther = initialBudget != null &&
                            !selectedCategory.equals(initialBudget.category, ignoreCase = true) &&
                            existingBudgetCategories.any { it.equals(selectedCategory, ignoreCase = true) }
                        if (takenByOther) {
                            formError = "Ya tienes un presupuesto para $selectedCategory."
                            return@Button
                        }
                        formError = null
                        onSave(
                            CategoryBudget(
                                id = initialBudget?.id ?: "",
                                category = selectedCategory,
                                limit = limit,
                                period = period,
                                alertEnabled = alertEnabled,
                                alertThreshold = alertThreshold.toInt()
                            )
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1.8f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent500)
                ) {
                    Text(
                        if (isSaving) "Guardando..." else "Guardar presupuesto",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = White
                    )
                }
            }
        }
    }

    if (showDeleteDialog && initialBudget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar presupuesto", fontFamily = Manrope, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminará el presupuesto de ${initialBudget.category}. Tus movimientos no se borran.",
                    fontFamily = Manrope
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(initialBudget)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger500)
                ) { Text("Eliminar", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Ink600) }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NewBudgetPreview() {
    MiFinanzaTheme {
        NewBudgetScreen(
            initialBudget = null,
            currencySymbol = "$",
            onCancel = {},
            onSave = {}
        )
    }
}
