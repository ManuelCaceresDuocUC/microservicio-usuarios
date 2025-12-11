package com.barlacteo.usuarios_service.repository

import com.barlacteo.usuarios_service.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByFono(fono: String): Optional<Usuario>
    fun existsByFono(fono: String): Boolean
}