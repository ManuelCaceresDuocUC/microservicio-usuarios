package com.barlacteo.usuarios_service.repository

import com.barlacteo.usuarios_service.model.Pedido
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PedidoRepository : JpaRepository<Pedido, Long> {
    // Spring Boot crea la consulta SQL solo con leer el nombre de la función:
    fun findByUsuarioFonoOrderByFechaDesc(usuarioFono: String): List<Pedido>
}
