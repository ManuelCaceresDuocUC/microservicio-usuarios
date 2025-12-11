package com.barlacteo.usuarios_service.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class StorageService(
    private val s3Client: AmazonS3
) {

    @Value("\${aws.s3.bucket}")
    private lateinit var bucketName: String

    fun store(file: MultipartFile): String {
        // 1. Obtenemos la extensión original (jpg, png, etc.)
        val originalFilename = file.originalFilename ?: "imagen.jpg"
        val extension = originalFilename.substringAfterLast('.', "jpg")
        
        // 2. Generamos nombre único
        val filename = "${UUID.randomUUID()}.$extension"

        // 3. Preparamos los metadatos para S3
        // Esto es CRÍTICO para que el navegador sepa que es una imagen y no un archivo genérico
        val metadata = ObjectMetadata()
        metadata.contentLength = file.size
        metadata.contentType = file.contentType ?: "image/jpeg" // "image/png", etc.

        // 4. Subimos el archivo ORIGINAL directamente (Sin conversión que pueda fallar)
        try {
            s3Client.putObject(bucketName, filename, file.inputStream, metadata)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Fallo al subir a S3: ${e.message}")
        }

        // 5. Retornamos el nombre
        return filename
    }
}