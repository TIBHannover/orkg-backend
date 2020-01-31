package eu.tib.orkg.prototype

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@SpringBootApplication
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)

    @Value("\${orkg.cors.origins:*}")
    val origins: List<String>? = null

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = origins
            allowedMethods = listOf("OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
