package org.orkg.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler
import org.springframework.security.web.firewall.RequestRejectedHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Custom CORS configuration.
 *
 * See also the documentation on CORS for [Spring MVC](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web.html#mvc-cors)
 * and [Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#cors).
 */
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        val configuration = CorsConfiguration()
            .applyPermitDefaultValues()
            .apply {
                allowedMethods = listOf("OPTIONS", "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                // Define safe header fields for clients. Safe-listed headers do not need to be included, see
                // https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_response_header.
                // (The defaults in Spring differ, see the documentation.)
                exposedHeaders = listOf(
                    "Accept",
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Allow-Headers",
                    "Access-Control-Expose-Headers",
                    "Access-Control-Allow-Credentials",
                    "Access-Control-Max-Age",
                    "Access-Control-Request-Headers",
                    "Access-Control-Request-Method",
                    "Authorization",
                    "Location",
                    "Origin",
                    "Vary",
                )
            }
        registerCorsConfiguration("/**", configuration)
    }
}

@Configuration
class RequestRejectedHandlerConfiguration {
    @Bean
    fun requestRejectedHandler(): RequestRejectedHandler = HttpStatusRequestRejectedHandler()
}
