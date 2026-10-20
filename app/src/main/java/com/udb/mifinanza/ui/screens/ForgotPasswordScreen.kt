package com.udb.mifinanza.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.FieldShape
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink700
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendCode: (email: String) -> Unit = {},
    onResetPassword: (code: String, newPassword: String) -> Unit = { _, _ -> },
    onPasswordResetSuccess: () -> Unit = onBack,
    initialEmail: String = "",
    initialCodeSent: Boolean = false
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val controller = (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
            val previous = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = true
            onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
        }
    }

    var email by remember { mutableStateOf(initialEmail) }
    var codeSent by remember { mutableStateOf(initialCodeSent) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val passwordsDoNotMatch = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
    val canSave = codeSent && code.isNotBlank() && newPassword.length >= 8 && newPassword == confirmNewPassword

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
                    text = "Paso 1 de 2 · Enviaremos un código de 6 dígitos al correo registrado.",
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryButton(
                text = "Enviar código",
                onClick = {
                    codeSent = true
                    onSendCode(email)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Ink200)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Paso 2 · Nueva contraseña",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Ink950
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledTextField(
                label = "Código de verificación",
                value = code,
                onValueChange = { code = it },
                placeholder = "—  —  —  —  —  —",
                enabled = codeSent,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledTextField(
                label = "Nueva contraseña",
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "Mínimo 8 caracteres",
                isPassword = true,
                showToggle = false,
                enabled = codeSent,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledTextField(
                label = "Confirmar nueva contraseña",
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                placeholder = "Repite la contraseña",
                isPassword = true,
                showToggle = false,
                enabled = codeSent,
                errorText = if (passwordsDoNotMatch) "Las contraseñas no coinciden" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (canSave) {
                PrimaryButton(
                    text = "Guardar contraseña",
                    onClick = {
                        onResetPassword(code, newPassword)
                        onPasswordResetSuccess()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFE1E7EC), FieldShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Guardar contraseña",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Ink400
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(name = "Diseño Idéntico (Referencia)", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ForgotPasswordMockupPreview() {
    MiFinanzaTheme {
        ForgotPasswordScreen(
            onBack = {},
            initialEmail = "m.ruiz@correo.com",
            initialCodeSent = false
        )
    }
}

@Preview(name = "Paso 2 Activo", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ForgotPasswordActivePreview() {
    MiFinanzaTheme {
        ForgotPasswordScreen(
            onBack = {},
            initialEmail = "m.ruiz@correo.com",
            initialCodeSent = true
        )
    }
}
