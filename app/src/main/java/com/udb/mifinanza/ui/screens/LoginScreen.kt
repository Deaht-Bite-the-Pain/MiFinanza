package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.ui.components.CheckboxRow
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.AppLogo
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String, remember: Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 56.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLogo(size = 56.dp, cornerRadius = 16.dp)
                Text(
                    "Bienvenida de nuevo",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Ink950
                )
                Text(
                    "Ingresa para continuar con tus finanzas.",
                    fontFamily = Manrope,
                    fontSize = 13.5.sp,
                    color = Ink600
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LabeledTextField(
                    label = "Correo electrónico",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "correo@ejemplo.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                LabeledTextField(
                    label = "Contraseña",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Tu contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckboxRow(checked = rememberMe, onCheckedChange = { rememberMe = it }, label = "Recordarme")
                    Text(
                        "¿Olvidaste tu contraseña?",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Accent500,
                        modifier = Modifier.clickable(onClick = onForgotPassword)
                    )
                }
                if (errorMessage != null) ErrorBanner(errorMessage)
                PrimaryButton(
                    text = "Iniciar sesión",
                    loading = isLoading,
                    onClick = { onLogin(email, password, rememberMe) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿No tienes cuenta? ", fontFamily = Manrope, fontSize = 12.5.sp, color = Ink600)
            Text(
                "Regístrate",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = Accent500,
                modifier = Modifier.clickable(onClick = onRegister)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() {
    MiFinanzaTheme { LoginScreen({ _, _, _ -> }, {}, {}) }
}
