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
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

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
