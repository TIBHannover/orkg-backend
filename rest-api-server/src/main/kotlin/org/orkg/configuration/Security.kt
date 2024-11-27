package org.orkg.configuration

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler
import org.springframework.security.web.firewall.RequestRejectedHandler
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Class to provide an [AuthenticationManager] so OAuth2 can use the `password` grant type.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    private val jwtAuthenticationConverterImpl: JwtAuthenticationConverter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            cors {
                withDefaults<HttpSecurity>()
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            csrf {
                disable()
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = jwtAuthenticationConverterImpl
                }
            }
        }
        return http.build()
    }
}

@Component
class JwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = jwtGrantedAuthoritiesConverter.convert(jwt)!!.flatMap { extractRoles(jwt) }
        val name = jwt.getClaim<String>(JwtClaimNames.SUB) // user id
        return JwtAuthenticationToken(jwt, authorities, name)
    }

    fun extractRoles(jwt: Jwt): Collection<GrantedAuthority> {
        val resourceAccess = jwt.getClaim<Map<String, Any?>>("realm_access")
        val roles = resourceAccess["roles"] as? List<*> ?: emptyList<Any?>()
        return roles.map { SimpleGrantedAuthority("ROLE_${it.toString().uppercase()}") }
    }
}

// TODO: can most likely be removed
/**
 * An authentication entry point that will not redirect to login, but return `401 Unauthorized` instead.
 */
@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // TODO: Send WWW-Authenticate header? What does the standard say?
        response.sendError(SC_UNAUTHORIZED, "Unauthorized")
    }
}

/**
 * Custom CORS configuration.
 *
 * See also the documentation on CORS for [Spring MVC](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web.html#mvc-cors)
 * and [Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#cors).
 */
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        return UrlBasedCorsConfigurationSource().apply {
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
}

@Configuration
class RequestRejectedHandlerConfiguration {
    @Bean
    fun requestRejectedHandler(): RequestRejectedHandler = HttpStatusRequestRejectedHandler()
}
