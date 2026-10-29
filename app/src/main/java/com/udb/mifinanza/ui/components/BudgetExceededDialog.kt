package com.udb.mifinanza.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.daysLeftInPeriod
import com.udb.mifinanza.data.daysLeftText
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

@Composable
fun BudgetExceededDialog(
    budget: CategoryBudget,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onAdjustLimit: () -> Unit
) {
    val daysText = daysLeftText(daysLeftInPeriod(budget.period), budget.period)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFFDECEB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val w = size.width
                        val h = size.height

                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.12f)
                            lineTo(w * 0.92f, h * 0.88f)
                            lineTo(w * 0.08f, h * 0.88f)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = Danger500,
                            style = Stroke(
                                width = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        drawLine(
                            color = Danger500,
                            start = Offset(w * 0.5f, h * 0.40f),
                            end = Offset(w * 0.5f, h * 0.62f),
                            strokeWidth = 2.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = Danger500,
                            radius = 1.4.dp.toPx(),
                            center = Offset(w * 0.5f, h * 0.74f)
                        )
                    }
                }

                Text(
                    text = "Presupuesto de ${budget.category}\nexcedido",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = Ink950
                )

                Text(
                    text = "Gastaste ${formatMoney(budget.spent, currencySymbol)} de un límite de ${formatMoney(budget.limit, currencySymbol)}. El exceso es de ${formatMoney(budget.exceededAmount, currencySymbol)} y $daysText.",
                    fontFamily = Manrope,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Ink600
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .background(Danger500, RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Ink300),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = White)
                    ) {
                        Text(
                            "Entendido",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink950
                        )
                    }

                    Button(
                        onClick = onAdjustLimit,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent500)
                    ) {
                        Text(
                            "Ajustar límite",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = White
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BudgetExceededDialogPreview() {
    MiFinanzaTheme {
        BudgetExceededDialog(
            budget = CategoryBudget(
                category = "Ocio",
                spent = 132.0,
                limit = 120.0
            ),
            currencySymbol = "$",
            onDismiss = {},
            onAdjustLimit = {}
        )
    }
}
