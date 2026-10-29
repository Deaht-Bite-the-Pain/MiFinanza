package com.udb.mifinanza.data

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

/** Convierte excepciones de Firebase en mensajes para mostrar al usuario. */
fun Throwable.toUserMessage(): String = when (this) {
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_INVALID_EMAIL" -> "El correo no es válido."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo."
        "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil. Usa al menos 8 caracteres."
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
            "Correo o contraseña incorrectos."
        "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada."
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento e inténtalo otra vez."
        else -> "No se pudo completar la operación ($errorCode)."
    }
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) "No tienes permiso para acceder a estos datos."
        else "Error al acceder a la base de datos."
    is FirebaseException -> "No se pudo conectar con el servidor. Inténtalo de nuevo más tarde."
    is IOException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
    else ->"Ocurrió un error inesperado."
}
