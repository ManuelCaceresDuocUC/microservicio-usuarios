package com.barlacteo.usuarios_service.model

import jakarta.persistence.*

@Entity
@Table(name = "usuarios")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nombre: String, // 'var' para poder editarlo

    @Column(nullable = false, unique = true)
    var fono: String,

    @Column(name = "foto_url")
    var fotoUrl: String? = null
)