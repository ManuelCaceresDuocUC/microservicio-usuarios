package com.barlacteo.usuarios_service.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.UUID

@Service
class TransbankService {

    // Credenciales OFICIALES de Integración (Testing) de Transbank
    private val commerceCode = "597055555532"
    private val apiKey = "579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C"
    private val urlBase = "https://webpay3gint.transbank.cl/rswebpaytransaction/api/webpay/v1.2"

    fun iniciarTransaccion(monto: Int, ordenCompra: String): Map<String, Any>? {
        val restTemplate = RestTemplate()
        val url = "$urlBase/transactions"

        // Headers obligatorios
        val headers = HttpHeaders()
        headers.add("Tbk-Api-Key-Id", commerceCode)
        headers.add("Tbk-Api-Key-Secret", apiKey)
        headers.contentType = MediaType.APPLICATION_JSON

        // Cuerpo de la petición (JSON)
        val body = mapOf(
            "buy_order" to ordenCompra,
            "session_id" to UUID.randomUUID().toString(),
            "amount" to monto,
            // Url de retorno (aunque en app móvil usaremos la respuesta directa)
            "return_url" to "http://10.0.2.2:8081/api/pedidos/confirmar"      )

        val request = HttpEntity(body, headers)

        return try {
            // Enviamos POST a Transbank
            val response = restTemplate.postForObject(url, request, Map::class.java)
            // Transbank responde con { "token": "...", "url": "..." }
            response as Map<String, Any>?
        } catch (e: Exception) {
            println("Error Transbank: ${e.message}")
            null
        }
    }

    fun confirmarTransaccion(token: String): Map<String, Any>? {
        val restTemplate = RestTemplate()
        // URL para confirmar (commit)
        val url = "$urlBase/transactions/$token"

        val headers = HttpHeaders()
        headers.add("Tbk-Api-Key-Id", commerceCode)
        headers.add("Tbk-Api-Key-Secret", apiKey)
        headers.contentType = MediaType.APPLICATION_JSON

        val request = HttpEntity<Any>(headers)

        return try {
            // Hacemos un PUT para confirmar la transacción
            val response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, request, Map::class.java)
            response.body as Map<String, Any>?
        } catch (e: Exception) {
            println("Error al confirmar: ${e.message}")
            null
        }
    }

}