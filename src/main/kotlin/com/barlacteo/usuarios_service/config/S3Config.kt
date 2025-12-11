package com.barlacteo.usuarios_service.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config {

    @Value("\${aws.access.key}")
    private lateinit var accessKey: String

    @Value("\${aws.secret.key}")
    private lateinit var secretKey: String

    @Value("\${aws.region}")
    private lateinit var region: String

    @Bean
    fun s3Client(): AmazonS3 {
        val creds = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(creds))
            .withRegion(region)
            .build()
    }
}