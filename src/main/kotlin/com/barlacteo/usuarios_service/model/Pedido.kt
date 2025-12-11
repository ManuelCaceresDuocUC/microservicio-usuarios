package com.barlacteo.usuarios_service.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "PedidosKotlin") // <--- ¡AQUÍ EVITAMOS EL CONFLICTO!
data class Pedido(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val usuarioFono: String, // Para saber de quién es
    val total: Int,
    val estado: String = "PENDIENTE", // PENDIENTE, PAGADO, RECHAZADO
    val fecha: LocalDateTime = LocalDateTime.now(),
    
    // Guardamos el token que nos da Transbank para rastrearlo
    val tokenWs: String? = null
)