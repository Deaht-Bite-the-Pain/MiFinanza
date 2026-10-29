package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.ErrorBanner
import com.udb.mifinanza.ui.components.FieldShape
import com.udb.mifinanza.ui.components.LabeledTextField
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.components.SelectField
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink400
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val SemesterOptions = listOf(NO_SEMESTER) + (1..12).map { it.toString() }
private const val NO_SEMESTER = "Sin especificar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: UserProfile,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    StatusBarIcons(dark = true)

    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var birthDate by remember { mutableStateOf(profile.birthDate) }
    var currency by remember {
        mutableStateOf(CurrencyOptions.firstOrNull { it.startsWith(profile.currency + " ") } ?: CurrencyOptions.first())
    }
    var institution by remember { mutableStateOf(profile.institution) }
    var career by remember { mutableStateOf(profile.career) }
    var semester by remember { mutableStateOf(profile.semester.ifBlank { NO_SEMESTER }) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        AppTopBar(title = "Editar perfil", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            errorMessage?.let { ErrorBanner(it) }

            SectionLabel("DATOS PERSONALES")
            LabeledTextField(
                label = "Nombre completo",
                value = name,
                onValueChange = { name = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            LabeledTextField(label = "Correo electrónico", value = profile.email, onValueChange = {}, enabled = false)
            LabeledTextField(
                label = "Teléfono",
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() || c in "+- " }.take(20) },
                placeholder = "7000-0000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Fecha de nacimiento", fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink600)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(White, FieldShape)
                        .border(1.dp, Ink300, FieldShape)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        birthDate.ifEmpty { "Seleccionar" },
                        fontFamily = Manrope,
                        fontSize = 14.sp,
                        color = if (birthDate.isEmpty()) Ink400 else Ink950,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Outlined.DateRange, contentDescription = "Calendario", tint = Ink500, modifier = Modifier.size(18.dp))
                }
            }
            SelectField(label = "Moneda local", options = CurrencyOptions, selected = currency, onSelected = { currency = it })
            Text(
                "Cambiar la moneda solo cambia el símbolo; los montos guardados no se convierten.",
                fontFamily = Manrope,
                fontSize = 11.5.sp,
                color = Ink500
            )

            SectionLabel("DATOS ACADÉMICOS")
            LabeledTextField(
                label = "Institución",
                value = institution,
                onValueChange = { institution = it },
                placeholder = "Universidad Don Bosco",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            LabeledTextField(
                label = "Carrera",
                value = career,
                onValueChange = { career = it },
                placeholder = "Ingeniería en Ciencias de la Computación",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            SelectField(label = "Semestre", options = SemesterOptions, selected = semester, onSelected = { semester = it })
        }

        Column(modifier = Modifier.background(White).padding(horizontal = 20.dp, vertical = 14.dp)) {
            PrimaryButton(
                text = "Guardar cambios",
                loading = isSaving,
                onClick = {
                    onSave(
                        profile.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            birthDate = birthDate,
                            currency = currency.substringBefore(" "),
                            institution = institution.trim(),
                            career = career.trim(),
                            semester = if (semester == NO_SEMESTER) "" else semester
                        )
                    )
                }
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseBirthDate(birthDate),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { birthDate = formatBirthDate(it) }
                    showDatePicker = false
                }) { Text("Aceptar", color = Accent500) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Ink600) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        color = Ink500,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// El DatePicker usa medianoche UTC, así que la fecha se convierte en UTC para no correrse un día.
private fun birthDateFormat() = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun formatBirthDate(utcMillis: Long): String = birthDateFormat().format(utcMillis)

private fun parseBirthDate(text: String): Long? =
    if (text.isBlank()) null
    else runCatching { birthDateFormat().parse(text)?.time }.getOrNull()

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EditProfilePreview() {
    MiFinanzaTheme {
        EditProfileScreen(
            profile = UserProfile(name = "María Fernanda Ruiz", email = "m.ruiz@correo.com"),
            isSaving = false,
            errorMessage = null,
            onBack = {},
            onSave = {}
        )
    }
}
