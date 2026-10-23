package com.udb.mifinanza.data

/** Perfil del usuario, guardado en Firestore en `users/{uid}`. */
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val currency: String = "USD",
    val phone: String = "",
    val birthDate: String = "",
    val institution: String = "",
    val career: String = "",
    val semester: String = ""
) {
    val firstName: String
        get() {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return "Usuario"
            val parts = trimmed.split("\\s+".toRegex())
            return if (parts.size >= 2) "${parts[0]} ${parts[1]}" else parts[0]
        }

    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts.first().first()}${parts.last().first()}".uppercase()
            }
        }

    /** Símbolo de la moneda local elegida al registrarse. */
    val currencySymbol: String
        get() = CURRENCY_SYMBOLS[currency] ?: "$"

    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "email" to email,
        "currency" to currency,
        "phone" to phone,
        "birthDate" to birthDate,
        "institution" to institution,
        "career" to career,
        "semester" to semester
    )

    companion object {
        private val CURRENCY_SYMBOLS = mapOf(
            "USD" to "$", "EUR" to "€", "MXN" to "$", "COP" to "$", "ARS" to "$", "CLP" to "$",
            "PEN" to "S/", "GTQ" to "Q", "HNL" to "L", "NIO" to "C$", "CRC" to "₡", "DOP" to "RD$"
        )

        fun fromMap(data: Map<String, Any?>): UserProfile = UserProfile(
            name = data["name"] as? String ?: "",
            email = data["email"] as? String ?: "",
            currency = data["currency"] as? String ?: "USD",
            phone = data["phone"] as? String ?: "",
            birthDate = data["birthDate"] as? String ?: "",
            institution = data["institution"] as? String ?: "",
            career = data["career"] as? String ?: "",
            semester = data["semester"] as? String ?: ""
        )
    }
}
