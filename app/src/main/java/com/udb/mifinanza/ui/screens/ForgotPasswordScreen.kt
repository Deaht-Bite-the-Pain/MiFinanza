package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.OutlinedAppButton
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.components.SuccessBanner
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink700
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

/**
 * Recuperar contraseña: Firebase envía un enlace al correo y la nueva contraseña se
 * define en la página a la que lleva ese enlace (no hay código de 6 dígitos).
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendLink: (email: String) -> Unit = {},
    onBackToLogin: () -> Unit = onBack,
    isLoading: Boolean = false,
    linkSent: Boolean = false,
    errorMessage: String? = null,
    initialEmail: String = ""
) {
    StatusBarIcons(dark = true)

    var email by remember { mutableStateOf(initialEmail) }

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
                title = "Recuperar contraseña",
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE4EEF6), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFBACFE0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Ingresa el correo de tu cuenta y te enviaremos un enlace para crear una nueva contraseña.",
                    fontFamily = Manrope,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Ink700
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = { email = it },
                placeholder = "correo@ejemplo.com",
                enabled = !linkSent,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                ErrorBanner(errorMessage)
            }

            if (linkSent) {
                Spacer(modifier = Modifier.height(14.dp))
                SuccessBanner(
                    "Listo. Si el correo está registrado, recibirás un enlace para restablecer tu contraseña. " +
                        "Revisa también la carpeta de spam."
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (linkSent) {
                PrimaryButton(text = "Volver a iniciar sesión", onClick = onBackToLogin)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedAppButton(
                    text = "Reenviar enlace",
                    onClick = { onSendLink(email) },
                    borderColor = Accent500,
                    contentColor = Accent500
                )
            } else {
                PrimaryButton(
                    text = "Enviar enlace",
                    loading = isLoading,
                    onClick = { onSendLink(email) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(name = "Formulario", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ForgotPasswordPreview() {
    MiFinanzaTheme { ForgotPasswordScreen(onBack = {}, initialEmail = "m.ruiz@correo.com") }
}

@Preview(name = "Enlace enviado", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ForgotPasswordSentPreview() {
    MiFinanzaTheme { ForgotPasswordScreen(onBack = {}, initialEmail = "m.ruiz@correo.com", linkSent = true) }
}
