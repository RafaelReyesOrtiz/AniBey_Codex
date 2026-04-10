package com.example.anibey_codex_tfg.domain.model

/**
 * Representa el perfil de un usuario en el sistema.
 * Nota: Firebase requiere que todos los campos tengan valores por defecto 
 * para poder generar un constructor vacío durante la deserialización.
 */
data class UserProfile(
    val email: String = "",
    val username: String = "",
    val photoUrl: String? = null
)