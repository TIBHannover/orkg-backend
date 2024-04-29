package org.orkg.contenttypes.adapter.input.rest

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.internalServerError
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Deprecated("To be removed")
@RestController
@RequestMapping("/api/reproducibility-score")
class ReproducibilityScoreController(private val httpClient: HttpClient) {
    @GetMapping("/url-accessibility")
    fun urlAccessibility(@RequestParam uri: URI): ResponseEntity<Any> =
        try {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .method("HEAD", BodyPublishers.noBody()) // HEAD() is only available for JDK 18+. See: https://bugs.openjdk.org/browse/JDK-8276996
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            status(response.statusCode()).build()
        } catch (e: Exception) {
            internalServerError().build()
        }
}
