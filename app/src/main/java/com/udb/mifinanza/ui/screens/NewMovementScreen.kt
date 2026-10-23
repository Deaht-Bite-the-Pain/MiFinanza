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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent100
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
import com.udb.mifinanza.ui.theme.Success500
import com.udb.mifinanza.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val AvailableCategories = listOf(
    "Alimentación",
    "Transporte",
    "Ocio",
    "Servicios",
    "Salud",
    "Educación",
    "Hogar",
    "Otros"
)

val PaymentMethods = listOf(
    "Efectivo",
    "Tarjeta",
    "Transferencia",
    "Cuenta de Ahorros"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewMovementScreen(
    initialMovement: Movement? = null,
    onCancel: () -> Unit,
    onSave: (Movement) -> Unit
) {
    StatusBarIcons(dark = true)

    val todayStr = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        sdf.format(Date())
    }

    var movementType by remember { mutableStateOf(initialMovement?.type ?: MovementType.GASTO) }
    var amountText by remember { mutableStateOf(initialMovement?.let { String.format(Locale.US, "%.2f", it.amount) } ?: "42.80") }
    var selectedCategory by remember { mutableStateOf(initialMovement?.category ?: "Alimentación") }
    var dateText by remember { mutableStateOf(initialMovement?.fullDateString?.ifEmpty { todayStr } ?: "12/08/2026") }
    var paymentMethod by remember { mutableStateOf(initialMovement?.paymentMethod ?: "Efectivo") }
    var description by remember { mutableStateOf(initialMovement?.description ?: "Compra semanal en Supermercado Santa María") }
    var tags by remember { mutableStateOf(initialMovement?.tags?.takeIf { it.isNotEmpty() } ?: listOf("semanal", "hogar")) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Barra Superior con Botón "X" y Título
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
                "Nuevo movimiento",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink950
            )
        }

        // Formulario Scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector Ingreso / Gasto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Ink100, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Ingreso
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
                            .clickable { movementType = MovementType.INGRESO },
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

                    // Gasto
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
                            .clickable { movementType = MovementType.GASTO },
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

            // Campo Monto
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
                        "$ ",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Ink600
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
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

            // Campo Categoría con Dropdown
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
                        AvailableCategories.forEach { cat ->
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

                // Chips de Categorías rápidas
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Alimentación", "Transporte", "Ocio", "Servicios", "Salud").forEach { cat ->
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

            // Dos Columnas: Fecha y Método de Pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fecha
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
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = dateText,
                            onValueChange = { dateText = it },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = Manrope, fontSize = 13.5.sp, color = Ink950),
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

                // Método de Pago
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

            // Campo Descripción
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

            // Campo Etiquetas
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Etiquetas",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Ink600
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
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

                    // Botón "+ agregar" con borde punteado
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
                            .clickable {
                                // Agregar etiqueta genérica si se desea
                                val nextTag = "extra${tags.size + 1}"
                                tags = tags + nextTag
                            }
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

        // Barra Inferior Fija: Cancelar y Guardar
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
                    if (amt > 0.0) {
                        // Extraer título sugerido si está vacío
                        val resolvedTitle = if (description.contains("Supermercado", ignoreCase = true)) {
                            "Supermercado Santa María"
                        } else if (description.isNotBlank()) {
                            description.take(30)
                        } else {
                            "$selectedCategory ${if (movementType == MovementType.INGRESO) "Ingreso" else "Gasto"}"
                        }

                        val movement = Movement(
                            id = initialMovement?.id ?: "",
                            title = resolvedTitle,
                            category = selectedCategory,
                            amount = amt,
                            type = movementType,
                            timestamp = initialMovement?.timestamp ?: System.currentTimeMillis(),
                            dateString = dateText.take(6),
                            fullDateString = dateText,
                            paymentMethod = paymentMethod,
                            description = description,
                            tags = tags
                        )
                        onSave(movement)
                    }
                },
                modifier = Modifier
                    .weight(1.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent500)
            ) {
                Text(
                    "Guardar",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = White
                )
            }
        }
    }
}
