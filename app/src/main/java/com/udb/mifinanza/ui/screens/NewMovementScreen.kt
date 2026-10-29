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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.ExpenseCategories
import com.udb.mifinanza.data.IncomeCategories
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.filterAmountInput
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent100
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink700
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val PaymentMethods = listOf(
    "Efectivo",
    "Tarjeta",
    "Transferencia",
    "Cuenta de Ahorros"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewMovementScreen(
    initialMovement: Movement? = null,
    currencySymbol: String,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onCancel: () -> Unit,
    onSave: (Movement) -> Unit
) {
    StatusBarIcons(dark = true)

    var movementType by remember { mutableStateOf(initialMovement?.type ?: MovementType.GASTO) }
    var amountText by remember { mutableStateOf(initialMovement?.let { String.format(Locale.US, "%.2f", it.amount) } ?: "") }
    var title by remember { mutableStateOf(initialMovement?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(initialMovement?.category ?: ExpenseCategories.first()) }
    var timestamp by remember { mutableStateOf(initialMovement?.timestamp ?: System.currentTimeMillis()) }
    var paymentMethod by remember { mutableStateOf(initialMovement?.paymentMethod?.ifEmpty { null } ?: PaymentMethods.first()) }
    var description by remember { mutableStateOf(initialMovement?.description ?: "") }
    var tags by remember { mutableStateOf(initialMovement?.tags ?: emptyList()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val categories = if (movementType == MovementType.INGRESO) IncomeCategories else ExpenseCategories
    // Al cambiar entre Ingreso y Gasto la categoría pasa a la primera de la lista del nuevo tipo.
    fun selectType(type: MovementType) {
        if (type == movementType) return
        movementType = type
        selectedCategory = (if (type == MovementType.INGRESO) IncomeCategories else ExpenseCategories).first()
    }

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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Ink950,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                if (initialMovement == null) "Nuevo movimiento" else "Editar movimiento",
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
            (formError ?: errorMessage)?.let { ErrorBanner(it) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Ink100, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                if (movementType == MovementType.INGRESO) White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .then(
                                if (movementType == MovementType.INGRESO) {
                                    Modifier.border(BorderStroke(1.5.dp, Success500), RoundedCornerShape(6.dp))
                                } else Modifier
                            )
                            .clickable { selectType(MovementType.INGRESO) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingreso",
                            fontFamily = Manrope,
                            fontWeight = if (movementType == MovementType.INGRESO) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (movementType == MovementType.INGRESO) Success500 else Ink600
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                if (movementType == MovementType.GASTO) White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .then(
                                if (movementType == MovementType.GASTO) {
                                    Modifier.border(BorderStroke(1.5.dp, Danger500), RoundedCornerShape(6.dp))
                                } else Modifier
                            )
                            .clickable { selectType(MovementType.GASTO) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Gasto",
                            fontFamily = Manrope,
                            fontWeight = if (movementType == MovementType.GASTO) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (movementType == MovementType.GASTO) Danger500 else Ink600
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Monto",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Ink600
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Accent500, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$currencySymbol ",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Ink600
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = filterAmountInput(it, amountText) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Ink950
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(Accent500),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            LabeledTextField(
                label = "Concepto",
                value = title,
                onValueChange = { title = it },
                placeholder = if (movementType == MovementType.INGRESO) "Ej. Pago de beca, Venta..." else "Ej. Supermercado, Pago de luz..."
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Categoría",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Ink600
                )
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(White, RoundedCornerShape(8.dp))
                            .border(1.dp, Ink300, RoundedCornerShape(8.dp))
                            .clickable { categoryDropdownExpanded = true }
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedCategory,
                            fontFamily = Manrope,
                            fontSize = 14.sp,
                            color = Ink950
                        )
                        Text("▾", color = Ink500, fontSize = 14.sp)
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontFamily = Manrope, fontSize = 14.sp, color = Ink950) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.take(5).forEach { cat ->
                        val isSelected = (cat == selectedCategory)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Accent500 else White,
                                    CircleShape
                                )
                                .then(
                                    if (!isSelected) Modifier.border(1.dp, Ink300, CircleShape) else Modifier
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                cat,
                                fontFamily = Manrope,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = if (isSelected) White else Ink700
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Fecha",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Ink600
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(White, RoundedCornerShape(8.dp))
                            .border(1.dp, Ink300, RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timestamp)),
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            color = Ink950,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "Calendario",
                            tint = Ink500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Método de pago",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Ink600
                    )
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(White, RoundedCornerShape(8.dp))
                                .border(1.dp, Ink300, RoundedCornerShape(8.dp))
                            .clickable { paymentDropdownExpanded = true }
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                paymentMethod,
                                fontFamily = Manrope,
                                fontSize = 13.5.sp,
                                color = Ink950
                            )
                            Text("▾", color = Ink500, fontSize = 14.sp)
                        }

                        DropdownMenu(
                            expanded = paymentDropdownExpanded,
                            onDismissRequest = { paymentDropdownExpanded = false }
                        ) {
                            PaymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method, fontFamily = Manrope, fontSize = 14.sp, color = Ink950) },
                                    onClick = {
                                        paymentMethod = method
                                        paymentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Descripción",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Ink600
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(White, RoundedCornerShape(8.dp))
                        .border(1.dp, Ink300, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    if (description.isEmpty()) {
                        Text("Opcional", fontFamily = Manrope, fontSize = 13.5.sp, color = Ink400)
                    }
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        textStyle = TextStyle(
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            color = Ink950,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Accent500),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Etiquetas",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Ink600
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .background(Accent100, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "#$tag",
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = Accent700
                            )
                            Text(
                                "×",
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Accent700,
                                modifier = Modifier.clickable {
                                    tags = tags.filter { it != tag }
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .drawBehind {
                                drawRoundRect(
                                    color = Ink300,
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                                    )
                                )
                            }
                            .clickable { showTagDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+ agregar",
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            color = Ink600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink50)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Ink300),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
            ) {
                Text(
                    "Cancelar",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Ink950
                )
            }

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    formError = when {
                        amt <= 0.0 -> "Ingresa un monto mayor a cero."
                        title.isBlank() -> "Ingresa el concepto del movimiento."
                        else -> null
                    }
                    if (formError == null) {
                        onSave(
                            Movement(
                                id = initialMovement?.id ?: "",
                                title = title.trim(),
                                category = selectedCategory,
                                amount = amt,
                                type = movementType,
                                timestamp = timestamp,
                                paymentMethod = paymentMethod,
                                description = description.trim(),
                                tags = tags
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .weight(1.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent500)
            ) {
                Text(
                    if (isSaving) "Guardando..." else "Guardar",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = White
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = localToUtcDate(timestamp),
            selectableDates = object : SelectableDates {
                // No se registran movimientos a futuro.
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= localToUtcDate(System.currentTimeMillis())
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { timestamp = utcDateToLocal(it, keepTimeOf = timestamp) }
                    showDatePicker = false
                }) { Text("Aceptar", color = Accent500) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Ink600) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTagDialog) {
        var newTag by remember { mutableStateOf("") }
        val addTag = {
            val clean = newTag.trim().removePrefix("#").replace(" ", "").lowercase()
            if (clean.isNotEmpty() && clean !in tags) tags = tags + clean
            showTagDialog = false
        }
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Nueva etiqueta", fontFamily = Manrope, fontWeight = FontWeight.Bold) },
            text = {
                LabeledTextField(
                    label = "Etiqueta",
                    value = newTag,
                    onValueChange = { newTag = it },
                    placeholder = "Ej. hogar, trabajo..."
                )
            },
            confirmButton = {
                Button(onClick = addTag, colors = ButtonDefaults.buttonColors(containerColor = Accent500)) {
                    Text("Agregar", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) { Text("Cancelar", color = Ink600) }
            }
        )
    }
}

/** El DatePicker trabaja con medianoche UTC; esto convierte una fecha local a ese formato. */
private fun localToUtcDate(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Convierte la fecha elegida (medianoche UTC) a la fecha local, conservando la hora original. */
private fun utcDateToLocal(utcMillis: Long, keepTimeOf: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        timeInMillis = keepTimeOf
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
