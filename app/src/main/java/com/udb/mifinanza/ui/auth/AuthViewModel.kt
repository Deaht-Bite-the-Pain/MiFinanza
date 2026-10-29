package com.udb.mifinanza.ui.auth

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udb.mifinanza.data.AuthRepository
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: UserProfile? = null,
    val resetEmailSent: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val prefs = application.getSharedPreferences("session", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Si el usuario no marcó "Recordarme", la sesión no sobrevive al reinicio de la app.
        if (repository.isSignedIn && !prefs.getBoolean(KEY_KEEP_SESSION, false)) repository.signOut()
    }

    val isSignedIn: Boolean get() = repository.isSignedIn

    fun clearError() = _state.update { it.copy(error = null, resetEmailSent = false) }

    fun login(email: String, password: String, remember: Boolean, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isEmpty()) {
            _state.update { it.copy(error = "Ingresa tu correo y tu contraseña.") }
            return
        }
        run(onSuccess) {
            repository.login(email.trim(), password).onSuccess { setKeepSession(remember) }
        }
    }

    fun register(name: String, email: String, password: String, currency: String, onSuccess: () -> Unit) {
        val error = when {
            name.isBlank() -> "Ingresa tu nombre completo."
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Ingresa un correo válido."
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        run(onSuccess) {
            // El código de moneda es lo que va antes de " — " ("USD — Dólar estadounidense").
            repository.register(name.trim(), email.trim(), password, currency.substringBefore(" ")).onSuccess {
                setKeepSession(true)
            }
        }
    }

    /** Envía el enlace de restablecimiento de Firebase al correo indicado. */
    fun sendPasswordReset(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _state.update { it.copy(error = "Ingresa un correo válido.", resetEmailSent = false) }
            return
        }
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, resetEmailSent = false) }
        viewModelScope.launch {
            repository.sendPasswordReset(email.trim())
                .onSuccess { _state.update { it.copy(isLoading = false, resetEmailSent = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            repository.loadProfile()
                .onSuccess { profile -> _state.update { it.copy(profile = profile, error = null) } }
                .onFailure { e -> _state.update { it.copy(error = e.toUserMessage()) } }
        }
    }

    fun saveProfile(profile: UserProfile, onSuccess: () -> Unit) {
        val error = when {
            profile.name.isBlank() -> "Ingresa tu nombre completo."
            profile.phone.isNotEmpty() && !PHONE.matches(profile.phone) -> "El teléfono solo puede tener números, espacios, + y -."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.saveProfile(profile)
                .onSuccess { saved ->
                    _state.update { it.copy(isLoading = false, profile = saved) }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
        }
    }

    fun signOut() {
        repository.signOut()
        prefs.edit().putBoolean(KEY_KEEP_SESSION, false).apply()
        _state.value = AuthUiState()
    }

    private fun setKeepSession(keep: Boolean) = prefs.edit().putBoolean(KEY_KEEP_SESSION, keep).apply()

    private fun run(onSuccess: () -> Unit, block: suspend () -> Result<*>) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            block()
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
        }
    }

    private companion object {
        const val KEY_KEEP_SESSION = "keep_session"
        val PHONE = Regex("""[+\d][\d\s-]{6,19}""")
    }
}
