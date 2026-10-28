package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.ExpenseCategories
import com.udb.mifinanza.data.HistoryFilter
import com.udb.mifinanza.data.IncomeCategories
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.TypeFilter
import com.udb.mifinanza.data.endOfDay
import com.udb.mifinanza.data.filterAmountInput
import com.udb.mifinanza.data.filterHistory
import com.udb.mifinanza.data.formatDate
import com.udb.mifinanza.data.startOfDay
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.White
import java.util.Calendar
import java.util.TimeZone

private val AllCategories = listOf(
    "Alimentación",
    "Transporte",
    "Ocio",
    "Servicios",
    "Salud",
    "Salario",
    "Educación",
    "Hogar",
    "Ventas",
    "Beca",
    "Otros"
)

/** Hoja inferior con los filtros avanzados del historial */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersSheet(
    initial: HistoryFilter,
    movements: List<Movement>,
    query: String,
    currencySymbol: String,
    onApply: (HistoryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(initial) }
    var minText by remember { mutableStateOf(initial.minAmount?.let(::amountToText).orEmpty()) }
    var maxText by remember { mutableStateOf(initial.maxAmount?.let(::amountToText).orEmpty()) }
    var pickingDate by remember { mutableStateOf<DateField?>(null) }

    // El contador de resultados se calcula en vivo según lo que el usuario va modificando
    val resultCount = remember(draft, movements, query) {
        movements.filterHistory(query, draft.normalized()).size
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(Ink200, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Encabezado: "Filtros" y "LIMPIAR TODO"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink950
                )
                Text(
                    text = "LIMPIAR TODO",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    letterSpacing = 0.5.sp,
                    color = Accent500,
                    modifier = Modifier.clickable {
                        draft = HistoryFilter()
                        minText = ""
                        maxText = ""
                    }
                )
            }

            // Sección 1: TIPO
            Section("TIPO") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeFilter.entries.forEach { type ->
                        SelectableChip(
                            label = type.label,
                            selected = draft.type == type,
                            onClick = { draft = draft.copy(type = type) }
                        )
                    }
                }
            }

            // Sección 2: RANGO DE FECHAS
            Section("RANGO DE FECHAS") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateBox(
                        value = draft.dateFrom,
                        placeholder = "Desde",
                        modifier = Modifier.weight(1f),
                        onClick = { pickingDate = DateField.From }
                    )
                    DateBox(
                        value = draft.dateTo,
                        placeholder = "Hasta",
                        modifier = Modifier.weight(1f),
                        onClick = { pickingDate = DateField.To }
                    )
                }
            }

            // Sección 3: CATEGORÍAS
            Section("CATEGORÍAS") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AllCategories.forEach { category ->
                        val selected = category in draft.categories
                        SelectableChip(
                            label = category,
                            selected = selected,
                            onClick = {
                                draft = draft.copy(
                                    categories = if (selected) draft.categories - category else draft.categories + category
                                )
                            }
                        )
                    }
                }
            }

            // Sección 4: MONTO
            Section("MONTO") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AmountBox(
                        symbol = currencySymbol,
                        value = minText,
                        placeholder = "10.00",
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            minText = filterAmountInput(it, minText)
                            draft = draft.copy(minAmount = minText.toDoubleOrNull())
                        }
                    )
                    Text(
                        text = "a",
                        fontFamily = Manrope,
                        fontSize = 13.5.sp,
                        color = Ink500
                    )
                    AmountBox(
                        symbol = currencySymbol,
                        value = maxText,
                        placeholder = "500.00",
                        modifier = Modifier.weight(1f),
                        onValueChange = {
                            maxText = filterAmountInput(it, maxText)
                            draft = draft.copy(maxAmount = maxText.toDoubleOrNull())
                        }
                    )
                }
            }

            // Botón Principal: Aplicar filtros (X resultados)
            Button(
                onClick = { onApply(draft.normalized()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent500,
                    contentColor = White
                )
            ) {
                Text(
                    text = "Aplicar filtros ($resultCount ${if (resultCount == 1) "resultado" else "resultados"})",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = White
                )
            }
        }
    }

    // Selector de Fecha emergente
    pickingDate?.let { field ->
        val current = if (field == DateField.From) draft.dateFrom else draft.dateTo
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = localToUtcDay(current ?: System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        val local = utcDayToLocal(utc)
                        draft = if (field == DateField.From) {
                            draft.copy(dateFrom = startOfDay(local))
                        } else {
                            draft.copy(dateTo = endOfDay(local))
                        }
                    }
                    pickingDate = null
                }) { Text("Aceptar", color = Accent500) }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = null }) {
                    Text("Cancelar", color = Ink600)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private enum class DateField { From, To }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 0.8.sp,
            color = Color(0xFF385773)
        )
        content()
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Accent500 else White,
                CircleShape
            )
            .then(
                if (!selected) {
                    Modifier.border(1.dp, Ink200, CircleShape)
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = if (selected) White else Color(0xFF24425F)
        )
    }
}

@Composable
private fun DateBox(
    value: Long?,
    placeholder: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(White, RoundedCornerShape(8.dp))
            .border(1.dp, Ink200, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value?.let(::formatDate) ?: placeholder,
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (value != null) Ink950 else Ink400
        )
    }
}

@Composable
private fun AmountBox(
    symbol: String,
    value: String,
    placeholder: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(White, RoundedCornerShape(8.dp))
            .border(1.dp, Ink200, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$symbol ",
            fontFamily = Manrope,
            fontSize = 14.sp,
            color = Ink950
        )
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = Manrope,
                    fontSize = 14.sp,
                    color = Ink400,
                    maxLines = 1
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Manrope,
                    fontSize = 14.sp,
                    color = Ink950
                ),
                cursorBrush = SolidColor(Accent500),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun amountToText(amount: Double): String =
    if (amount % 1.0 == 0.0) String.format(java.util.Locale.US, "%.2f", amount) else amount.toString()

/** El DatePicker trabaja con medianoche UTC; convierte una fecha local a ese formato. */
private fun localToUtcDay(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Convierte la fecha elegida en el DatePicker (medianoche UTC) a esa misma fecha en hora local. */
private fun utcDayToLocal(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
