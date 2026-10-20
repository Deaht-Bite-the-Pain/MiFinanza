package com.udb.mifinanza.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.ui.components.LogoPlaceholder
import com.udb.mifinanza.ui.components.OutlinedAppButton
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

@Composable
fun WelcomeScreen(onCreateAccount: () -> Unit, onHaveAccount: () -> Unit) {
    // Fondo oscuro: iconos claros en la barra de estado mientras esta pantalla está visible.
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val controller = (view.context as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
            val previous = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = false
            onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            LogoPlaceholder(size = 88.dp, cornerRadius = 24.dp, color = Ink500)
            Text(
                "MiFinanza",
                fontFamily = Manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                lineHeight = 33.sp,
                color = White
            )
            Text(
                "Controla tus ingresos, gastos y presupuestos en un solo lugar.",
                fontFamily = Manrope,
                fontSize = 14.sp,
                lineHeight = 22.4.sp,
                color = Ink300,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(text = "Crear cuenta", onClick = onCreateAccount)
            OutlinedAppButton(
                text = "Ya tengo cuenta",
                onClick = onHaveAccount,
                borderColor = Ink600,
                contentColor = White
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun WelcomePreview() {
    MiFinanzaTheme { WelcomeScreen({}, {}) }
}
