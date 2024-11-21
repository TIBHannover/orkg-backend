package org.orkg.community.adapter.input.keycloak.configuration

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfiguration {
    @Bean
    fun keycloakAdminClient(
        @Value("\${orkg.keycloak.host}") host: String,
        @Value("\${orkg.keycloak.realm}") realm: String,
        @Value("\${orkg.keycloak.client-id}") clientId: String,
        @Value("\${orkg.keycloak.client-secret}") clientSecret: String,
    ): Keycloak =
        KeycloakBuilder.builder()
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .serverUrl(host)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
}
