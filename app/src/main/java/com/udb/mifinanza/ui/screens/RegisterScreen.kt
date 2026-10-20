package com.udb.mifinanza.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.CheckboxRow
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.components.SelectField
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

val CurrencyOptions = listOf(
    "USD — Dólar estadounidense",
    "EUR — Euro",
    "MXN — Peso mexicano",
    "COP — Peso colombiano",
    "ARS — Peso argentino",
    "CLP — Peso chileno",
    "PEN — Sol peruano",
    "GTQ — Quetzal guatemalteco",
    "HNL — Lempira hondureño",
    "NIO — Córdoba nicaragüense",
    "CRC — Colón costarricense",
    "DOP — Peso dominicano"
)

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegister: (name: String, email: String, password: String, currency: String) -> Unit,
    onLogin: () -> Unit,
    initialFullName: String = "",
    initialEmail: String = "",
    initialPassword: String = "",
    initialConfirmPassword: String = "",
    initialCurrency: String = "USD — Dólar estadounidense",
    initialTermsAccepted: Boolean = false
) {
    // Configurar iconos oscuros en la barra de estado (fondo blanco de la AppTopBar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val controller = (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
            val previous = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = true
            onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
        }
    }

    var fullName by remember { mutableStateOf(initialFullName) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf(initialPassword) }
    var confirmPassword by remember { mutableStateOf(initialConfirmPassword) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var termsAccepted by remember { mutableStateOf(initialTermsAccepted) }

    val passwordsDoNotMatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordErrorText = if (passwordsDoNotMatch) "Las contraseñas no coinciden" else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Encabezado superior blanco con barra de estado y divisor
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .statusBarsPadding()
        ) {
            AppTopBar(
                title = "Crear cuenta",
                onBack = onBack
            )
        }

        // Contenido con soporte de desplazamiento y distribución hacia abajo
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Formulario de campos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LabeledTextField(
                        label = "Nombre completo",
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "María Fernanda Ruiz",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    LabeledTextField(
                        label = "Correo electrónico",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "m.ruiz@correo.com",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    LabeledTextField(
                        label = "Contraseña",
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••••",
                        isPassword = true,
                        showToggle = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )

                    LabeledTextField(
                        label = "Confirmar contraseña",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "••••••",
                        isPassword = true,
                        showToggle = false,
                        errorText = passwordErrorText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    SelectField(
                        label = "Moneda local",
                        options = CurrencyOptions,
                        selected = selectedCurrency,
                        onSelected = { selectedCurrency = it }
                    )

                    CheckboxRow(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        label = "Acepto los términos de uso y la política de privacidad",
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Botón Registrarme y Enlace a Iniciar sesión
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        text = "Registrarme",
                        onClick = {
                            if (!passwordsDoNotMatch) {
                                onRegister(fullName, email, password, selectedCurrency)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? ",
                            fontFamily = Manrope,
                            fontSize = 12.5.sp,
                            color = Ink600
                        )
                        Text(
                            text = "Inicia sesión",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Accent500,
                            modifier = Modifier.clickable(onClick = onLogin)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Diseño Idéntico (Referencia)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterMockupPreview() {
    MiFinanzaTheme {
        RegisterScreen(
            onBack = {},
            onRegister = { _, _, _, _ -> },
            onLogin = {},
            initialFullName = "María Fernanda Ruiz",
            initialEmail = "m.ruiz@correo.com",
            initialPassword = "password123",
            initialConfirmPassword = "pass",
            initialCurrency = "USD — Dólar estadounidense",
            initialTermsAccepted = true
        )
    }
}

@Preview(name = "Formulario Vacío", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterEmptyPreview() {
    MiFinanzaTheme {
        RegisterScreen(
            onBack = {},
            onRegister = { _, _, _, _ -> },
            onLogin = {}
        )
    }
}
