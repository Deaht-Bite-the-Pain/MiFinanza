package com.udb.mifinanza.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger100
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.White

val FieldShape = RoundedCornerShape(6.dp)

@Composable
fun StatusBarIcons(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(dark) {
        val controller = (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = dark
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = FieldShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent500,
            contentColor = White,
            disabledContainerColor = Accent500,
            disabledContentColor = White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Mensaje de error (rojo suave) para fallos de autenticación o de red. */
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Danger100, RoundedCornerShape(10.dp))
            .border(1.dp, Danger500, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(message, fontFamily = Manrope, fontSize = 12.5.sp, lineHeight = 18.sp, color = Danger500)
    }
}

/** Indicador de carga mientras llegan los datos del usuario desde Firestore. */
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Accent500, strokeWidth = 3.dp)
    }
}

@Composable
fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    borderColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = FieldShape,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun LogoPlaceholder(size: Dp, cornerRadius: Dp, color: Color, textColor: Color = Ink400) {
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "LOGO",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    showToggle: Boolean = isPassword,
    errorText: String? = null,
    enabled: Boolean = true,
    monospace: Boolean = false,
    labelColor: Color = if (enabled) Ink600 else Ink400,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var focused by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val hasError = errorText != null
    val borderColor = when {
        hasError -> Danger500
        focused -> Accent500
        !enabled -> Ink200
        else -> Ink300
    }
    val textStyle = if (monospace) {
        TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Ink950, letterSpacing = 6.sp)
    } else {
        TextStyle(fontFamily = Manrope, fontSize = 14.sp, color = Ink950)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = labelColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(White, FieldShape)
                .border(if (focused || hasError) 1.5.dp else 1.dp, borderColor, FieldShape)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        placeholder,
                        style = if (monospace) textStyle.copy(color = Ink400) else TextStyle(fontFamily = Manrope, fontSize = 14.sp, color = Ink400)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(Accent500),
                    visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                )
            }
            if (isPassword && showToggle) {
                Text(
                    if (visible) "OCULTAR" else "MOSTRAR",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Accent500,
                    modifier = Modifier.clickable { visible = !visible }.padding(start = 8.dp)
                )
            }
        }
        if (errorText != null) {
            Text(errorText, fontFamily = Manrope, fontSize = 11.5.sp, color = Danger500)
        }
    }
}

@Composable
fun SelectField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink600)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(White, FieldShape)
                    .border(1.dp, Ink300, FieldShape)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(selected, fontFamily = Manrope, fontSize = 14.sp, color = Ink950)
                Text("▾", color = Ink500, fontSize = 14.sp)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontFamily = Manrope, fontSize = 14.sp, color = Ink950) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = White,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth().background(backgroundColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Ink950, modifier = Modifier.size(22.dp))
            }
            Text(title, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink950)
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink200))
        }
    }
}

@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(if (checked) Accent500 else White, RoundedCornerShape(4.dp))
                .border(1.5.dp, if (checked) Accent500 else Ink300, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, fontFamily = Manrope, fontSize = 12.5.sp, color = Ink600)
    }
}
