package com.barlacteo.usuarios_service.dto

// Lo que recibimos al registrar
data class RegisterRequest(
    val nombre: String,
    val fono: String
)

// Lo que recibimos al loguear
data class LoginRequest(
    val nombre: String,
    val fono: String
)

// Lo que recibimos al editar perfil
data class UpdateProfileRequest(
    val nombre: String,
    val fono: String
)

// Lo que devolvemos al Android
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val fono: String,
    val fotoUrl: String?
)