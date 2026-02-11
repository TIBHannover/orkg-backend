package org.orkg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess

@SpringBootApplication
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder) =
        builder.sources(Application::class.java)
}

fun main(args: Array<String>) {
    if ("--healthcheck" in args) {
        performHealthCheckByCallingActuatorAndExit()
    }
    runApplication<Application>(*args)
}

/**
 * Calls the actuator health endpoint, and exists the application based on the status.
 *
 * This check is required for Docker, because the Distroless images used do not contain additional tools like `curl` or
 * `sh` to do the job.
 */
private fun performHealthCheckByCallingActuatorAndExit() {
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(URI("http://localhost:8080/actuator/health"))
        .version(HTTP_1_1)
        .GET()
        .build()
    val response: HttpResponse<String> = try {
        client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (_: Exception) {
        // Might be a ConnectionException or something else, but we will just assume we are not ready.
        exitProcess(1)
    }
    if (response.statusCode() != 200) {
        // It is enough to check the status code here, because the status code is in the 500 to 599 range for all
        // but the "UP" state.
        exitProcess(1)
    }
    exitProcess(0)
}
