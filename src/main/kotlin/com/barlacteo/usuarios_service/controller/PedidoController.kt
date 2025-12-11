package com.barlacteo.usuarios_service.controller

import com.barlacteo.usuarios_service.model.Pedido
import com.barlacteo.usuarios_service.repository.PedidoRepository
import com.barlacteo.usuarios_service.service.TransbankService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class PedidoRequest(val fonoUsuario: String, val total: Int, val items: List<Any>)

@RestController
@RequestMapping("/api/pedidos")
class PedidoController(
    private val pedidoRepository: PedidoRepository,
    private val transbankService: TransbankService
) {

    @PostMapping("/iniciar")
    fun iniciarPago(@RequestBody request: PedidoRequest): ResponseEntity<Any> {
        
        // 1. Generamos una orden de compra única
        val ordenCompra = "ORDER-${UUID.randomUUID().toString().substring(0, 8)}"

        // 2. Pedimos a Transbank que inicie el cobro
        val respuestaTbk = transbankService.iniciarTransaccion(request.total, ordenCompra)

        if (respuestaTbk != null) {
            val urlWebpay = respuestaTbk["url"] as String
            val token = respuestaTbk["token"] as String

            // 3. Guardamos en Base de Datos como PENDIENTE
            val pedido = Pedido(
                usuarioFono = request.fonoUsuario,
                total = request.total,
                tokenWs = token
            )
            pedidoRepository.save(pedido)

            // 4. Devolvemos la URL a Android para que abra el navegador
            return ResponseEntity.ok(mapOf(
                "url" to "$urlWebpay?token_ws=$token",
                "orden" to ordenCompra
            ))
        } else {
            return ResponseEntity.internalServerError().body(mapOf("error" to "Falló conexión con Transbank"))
        }
    }
    // ... código anterior (iniciarPago) ...

    // Este endpoint recibe al usuario cuando vuelve de Webpay
    @GetMapping("/confirmar")
    fun confirmarPago(@RequestParam("token_ws") token: String): ResponseEntity<String> {
        
        // 1. Confirmamos con Transbank
        val respuesta = transbankService.confirmarTransaccion(token)
        
        // 2. Buscamos el pedido en la BD por el token
        // (Asumo que agregaste un método findByTokenWs en tu Repository, si no, usa findAll y filtra)
        val pedido = pedidoRepository.findAll().find { it.tokenWs == token }

        if (pedido != null && respuesta != null) {
            val estadoTbk = respuesta["status"] as String?
            
            if (estadoTbk == "AUTHORIZED") {
                // 3. ACTUALIZAMOS EL ESTADO A PAGADO
                val pedidoActualizado = pedido.copy(estado = "PAGADO")
                pedidoRepository.save(pedidoActualizado)
                
                return ResponseEntity.ok("""
                    <html>
                        <body style="background-color:#f0f0f0; font-family: sans-serif; text-align:center; padding: 50px;">
                            <h1 style="color:green;">¡Pago Exitoso!</h1>
                            <p>Tu pedido por $${pedido.total} ha sido confirmado.</p>
                            <p>Puedes cerrar esta ventana y volver a la App.</p>
                        </body>
                    </html>
                """.trimIndent())
            }
        }

        // Si falló
        return ResponseEntity.ok("""
            <html>
                <body style="text-align:center; padding: 50px;">
                    <h1 style="color:red;">Pago Fallido o Anulado</h1>
                    <p>Inténtalo nuevamente.</p>
                </body>
            </html>
        """.trimIndent())
    }
    @GetMapping("/usuario/{fono}")
    fun obtenerPedidosPorUsuario(@PathVariable fono: String): ResponseEntity<List<Pedido>> {
        val pedidos = pedidoRepository.findByUsuarioFonoOrderByFechaDesc(fono)
        return ResponseEntity.ok(pedidos)
    }
}