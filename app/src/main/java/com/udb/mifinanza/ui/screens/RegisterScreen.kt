package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.CheckboxRow
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.components.SelectField
import com.udb.mifinanza.ui.components.StatusBarIcons
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
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    StatusBarIcons(dark = true)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(CurrencyOptions.first()) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsError by remember { mutableStateOf(false) }

    val passwordsDoNotMatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordErrorText = if (passwordsDoNotMatch) "Las contraseñas no coinciden" else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .navigationBarsPadding()
            .imePadding()
    ) {
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) ErrorBanner(errorMessage)
                    if (showTermsError) ErrorBanner("Debes aceptar los términos de uso para continuar.")
                    PrimaryButton(
                        text = "Registrarme",
                        loading = isLoading,
                        onClick = {
                            showTermsError = !termsAccepted
                            if (!passwordsDoNotMatch && termsAccepted) {
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterPreview() {
    MiFinanzaTheme { RegisterScreen({}, { _, _, _, _ -> }, {}) }
}
