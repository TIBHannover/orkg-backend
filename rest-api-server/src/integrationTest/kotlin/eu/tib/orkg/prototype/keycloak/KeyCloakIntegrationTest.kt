package eu.tib.orkg.prototype.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.statements.client.OrkgApiClient
import eu.tib.orkg.prototype.testing.KeycloakTestContainersBaseTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation

private const val loginPath = "/realms/orkg-dev/login-actions/registration"
private const val tokenPath = "/realms/orkg-dev/protocol/openid-connect/token"

class KeyCloakIntegrationTest : KeycloakTestContainersBaseTest() {

    private val objectMapper: ObjectMapper = ObjectMapper()

    @Test
    fun test() {
        // TODO: replace with API call and enable transaction management
        val realm = "orkg-dev"

        val keycloak = KeycloakBuilder.builder()
            .grantType(OAuth2Constants.PASSWORD)
            .serverUrl("http://localhost:${container.httpPort}")
            .realm(realm)
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .build()

        val email = "user@example.org"
        val password = "user"

        val response = keycloak
            .realm(realm)
            .users()
            .create(
                UserRepresentation().apply {
                    username = email
                    this.email = email
                    createdTimestamp = System.currentTimeMillis() // TODO: This may need to be in seconds
                    isEnabled = true
                    credentials = listOf(
                        CredentialRepresentation().apply {
                            type = CredentialRepresentation.PASSWORD
                            userLabel = "password"
                            createdDate = System.currentTimeMillis() // TODO: This may need to be in seconds
                            isTemporary = false
                            secretData = objectMapper.writeValueAsString(
                                mapOf(
                                    "value" to password,
                                    "additionalParameters" to emptyMap<String, String>()
                                )
                            )
                            credentialData = objectMapper.writeValueAsString(
                                mapOf(
                                    "additionalParameters" to emptyMap<String, String>()
                                )
                            )
                        }
                    )
                }
            )
        println(response)

        val client = OrkgApiClient(container.httpPort)
        val token = client.getAccessToken(email, password, tokenPath)
        Assertions.assertThat(token).isNotNull()
    }
}
