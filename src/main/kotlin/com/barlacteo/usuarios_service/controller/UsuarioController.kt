package com.barlacteo.usuarios_service.controller

import com.barlacteo.usuarios_service.dto.*
import com.barlacteo.usuarios_service.model.Usuario
import com.barlacteo.usuarios_service.repository.UsuarioRepository
import com.barlacteo.usuarios_service.service.StorageService
import org.springframework.beans.factory.annotation.Value // <--- IMPORTANTE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioRepository: UsuarioRepository,
    private val storageService: StorageService
) {

    // 1. INYECTAMOS EL NOMBRE DEL BUCKET DESDE application.properties
    @Value("\${aws.s3.bucket}")
    private lateinit var bucketName: String

    // --- AUTENTICACIÓN ---

    @PostMapping("/registro")
    fun registrar(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        if (usuarioRepository.existsByFono(request.fono)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "El fono ya está registrado"))
        }
        val usuario = Usuario(nombre = request.nombre, fono = request.fono)
        usuarioRepository.save(usuario)
        return ResponseEntity.ok(mapOf("mensaje" to "Usuario creado"))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val usuarioOpt = usuarioRepository.findByFono(request.fono)
        if (usuarioOpt.isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Usuario no encontrado"))
        }
        
        val usuario = usuarioOpt.get()
        if (usuario.nombre != request.nombre) {
             return ResponseEntity.status(401).body(mapOf("error" to "Nombre incorrecto"))
        }
        
        return ResponseEntity.ok(
            UsuarioResponse(usuario.id, usuario.nombre, usuario.fono, usuario.fotoUrl)
        )
    }

    // --- PERFIL ---

    @PutMapping("/{id}")
    fun actualizarPerfil(@PathVariable id: Long, @RequestBody request: UpdateProfileRequest): ResponseEntity<Any> {
        val usuarioOpt = usuarioRepository.findById(id)
        if (usuarioOpt.isEmpty) return ResponseEntity.notFound().build()
        
        val usuario = usuarioOpt.get()
        usuario.nombre = request.nombre
        usuario.fono = request.fono
        usuarioRepository.save(usuario)
        
        return ResponseEntity.ok(UsuarioResponse(usuario.id, usuario.nombre, usuario.fono, usuario.fotoUrl))
    }

    @PostMapping("/{id}/foto")
    fun subirFoto(@PathVariable id: Long, @RequestParam("imagen") file: MultipartFile): ResponseEntity<Any> {
        val usuarioOpt = usuarioRepository.findById(id)
        if (usuarioOpt.isEmpty) return ResponseEntity.notFound().build()
        
        try {
            // 1. El servicio ahora devuelve la URL COMPLETA (con https y región)
            // Ya no es solo el 'filename', es la 'urlCompleta'
            val urlCompleta = storageService.store(file)
            
            // 2. CORRECCIÓN: Usamos el valor directo, SIN agregarle nada extra
            val usuario = usuarioOpt.get()
            usuario.fotoUrl = urlCompleta // <--- ASÍ DE SIMPLE
            
            usuarioRepository.save(usuario)
            
            return ResponseEntity.ok(mapOf("url" to urlCompleta))
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.internalServerError().body(mapOf("error" to "Error al subir imagen"))
        }
    }
}