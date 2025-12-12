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

    private val region = "us-east-2"

    fun store(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: "imagen.jpg"
        val extension = originalFilename.substringAfterLast('.', "jpg")
        
        val filename = "${UUID.randomUUID()}.$extension"

        val metadata = ObjectMetadata()
        metadata.contentLength = file.size
        metadata.contentType = file.contentType ?: "image/jpeg"

        try {
            s3Client.putObject(bucketName, filename, file.inputStream, metadata)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Fallo al subir a S3: ${e.message}")
        }

     
        return "https://$bucketName.s3.$region.amazonaws.com/$filename"
    }
}