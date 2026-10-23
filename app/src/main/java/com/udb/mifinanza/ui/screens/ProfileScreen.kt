package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.BottomNavBar
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.NavTab
import com.udb.mifinanza.ui.theme.Accent100
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Danger500
import com.udb.mifinanza.ui.theme.Ink100
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White

import com.udb.mifinanza.ui.components.StatusBarIcons

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onOpenNews: () -> Unit,
    onLogout: () -> Unit,
    onTabSelected: (NavTab) -> Unit,
    errorMessage: String? = null
) {
    StatusBarIcons(dark = true)

    val displayName = profile.name
    val displayEmail = profile.email
    val displayInitials = profile.initials
    val displayPhone = profile.phone.ifEmpty { NOT_SET }
    val displayBirthDate = profile.birthDate.ifEmpty { NOT_SET }
    val displayCurrency = "${profile.currency} (${profile.currencySymbol})"
    val displayInstitution = profile.institution.ifEmpty { NOT_SET }
    val displayCareer = profile.career.ifEmpty { NOT_SET }
    val displaySemester = profile.semester.ifEmpty { NOT_SET }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppTopBar(
            title = "Perfil",
            onBack = onBack,
            backgroundColor = Ink50,
            showDivider = false
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) ErrorBanner(errorMessage)
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Accent100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayInitials,
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Accent500
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            displayName,
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink950
                        )
                        Text(
                            displayEmail,
                            fontFamily = Manrope,
                            fontSize = 12.5.sp,
                            color = Ink600
                        )
                    }
                }
            }
            InfoCard(
                title = "DATOS PERSONALES",
                rows = listOf(
                    "Teléfono" to displayPhone,
                    "Fecha de nacimiento" to displayBirthDate,
                    "Moneda local" to displayCurrency
                )
            )
            InfoCard(
                title = "DATOS ACADÉMICOS",
                rows = listOf(
                    "Institución" to displayInstitution,
                    "Carrera" to displayCareer,
                    "Semestre" to displaySemester
                )
            )
            Card(modifier = Modifier.clickable(onClick = onOpenNews)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Noticias académicas",
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = Ink950
                    )
                    Text("→", color = Ink500, fontSize = 14.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(White, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.5.dp, Danger500), RoundedCornerShape(8.dp))
                    .clickable(onClick = onLogout),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cerrar sesión",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Danger500
                )
            }
        }
        BottomNavBar(active = NavTab.Inicio, onSelect = onTabSelected)
    }
}

/** Texto para los datos que el usuario todavía no ha registrado. */
private const val NOT_SET = "No registrado"

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Ink200, RoundedCornerShape(12.dp))
    ) { content() }
}

@Composable
private fun InfoCard(title: String, rows: List<Pair<String, String>>) {
    Card {
        Column {
            Text(
                title,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                letterSpacing = 0.8.sp,
                color = Ink500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            rows.forEach { (label, value) ->
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink100))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Ink600)
                    Text(
                        value,
                        fontFamily = Manrope,
                        fontWeight = if (value == NOT_SET) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (value == NOT_SET) Ink500 else Ink950
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProfilePreview() {
    MiFinanzaTheme {
        ProfileScreen(
            profile = UserProfile(name = "María Fernanda Ruiz", email = "m.ruiz@correo.com"),
            onBack = {}, onOpenNews = {}, onLogout = {}, onTabSelected = {}
        )
    }
}
