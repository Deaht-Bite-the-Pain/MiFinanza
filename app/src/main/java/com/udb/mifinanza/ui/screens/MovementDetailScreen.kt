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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.ui.components.BottomNavBar
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

@Composable
fun MovementDetailScreen(
    movement: Movement,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTabSelected: (NavTab) -> Unit
) {
    StatusBarIcons(dark = true)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val displayDate = movement.displayFullDate.ifEmpty { "12/08/2026" }
    val displayCategory = movement.category.ifEmpty { "Alimentación" }
    val displayPayment = movement.paymentMethod.ifEmpty { "Efectivo" }
    val displayDescription = movement.description.ifEmpty {
        "Compra semanal en Supermercado Santa María. Incluye frutas, lácteos y limpieza."
    }
    val displayTags = if (movement.tags.isNotEmpty()) movement.tags else listOf("semanal", "hogar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Barra Superior: Flecha Atrás y Título "Detalle"
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

        // Contenido Scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta 1: Badge Gasto/Ingreso, Monto y Título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Badge GASTO / INGRESO
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

                    // Monto
                    Text(
                        movement.displayAmount,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Ink950
                    )

                    // Nombre / Título
                    Text(
                        movement.title.ifEmpty { "Supermercado Santa María" },
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = Ink950
                    )
                }
            }

            // Tarjeta 2: Tabla de Datos (Categoría, Fecha, Método de Pago, Presupuesto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Ink200, RoundedCornerShape(12.dp))
            ) {
                Column {
                    DetailRow(label = "Categoría", value = displayCategory, isHighlight = false)
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                    DetailRow(label = "Fecha", value = displayDate, isHighlight = false)
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                    DetailRow(label = "Método de pago", value = displayPayment, isHighlight = false)
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                    DetailRow(label = "Presupuesto afectado", value = displayCategory, isHighlight = true)
                }
            }

            // Tarjeta 3: DESCRIPCIÓN y Etiquetas
            Box(
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
                    Text(
                        displayDescription,
                        fontFamily = Manrope,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = Ink950
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        displayTags.forEach { tag ->
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

        // Botones de Acción: Eliminar y Editar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink50)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, Danger500),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
            ) {
                Text(
                    "Eliminar",
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

        // Barra de Navegación Inferior (Pestaña "Movimientos" activa)
        BottomNavBar(active = NavTab.Movimientos, onSelect = onTabSelected)

        // Diálogo de Confirmación para Eliminar
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
