package org.orkg.community.adapter.input.keycloak.configuration

import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.ClientRequestContext
import jakarta.ws.rs.client.ClientRequestFilter
import jakarta.ws.rs.core.HttpHeaders
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class KeycloakConfiguration {
    @Bean
    fun keycloakAdminClient(
        @Value("\${orkg.keycloak.host}") host: String,
        @Value("\${orkg.keycloak.realm}") realm: String,
        @Value("\${orkg.keycloak.client-id}") clientId: String,
        @Value("\${orkg.keycloak.client-secret}") clientSecret: String,
        @Value("\${orkg.http.user-agent}") userAgent: String,
    ): Keycloak =
        KeycloakBuilder.builder()
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .resteasyClient(
                ClientBuilder.newBuilder()
                    .register(object : ClientRequestFilter {
                        override fun filter(requestContext: ClientRequestContext) =
                            requestContext.headers.set(HttpHeaders.USER_AGENT, listOf(userAgent))
                    })
                    .readTimeout(5, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()
            )
            .serverUrl(host)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
}
