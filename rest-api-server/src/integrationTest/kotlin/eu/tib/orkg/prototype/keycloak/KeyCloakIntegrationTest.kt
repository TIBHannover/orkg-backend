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
                    createdTimestamp = System.currentTimeMillis()
                    isEnabled = true
                    isEmailVerified = true
                    credentials = listOf(
                        CredentialRepresentation().apply {
                            type = CredentialRepresentation.PASSWORD
                            userLabel = "password"
                            createdDate = System.currentTimeMillis()
                            isTemporary = false
                            secretData = objectMapper.writeValueAsString(
                                mapOf(
                                    "value" to "yiEmC+ZPHpcF5AL70YEJ/jUcsD3RVJbD9A2AO/iNp4Y=",
                                    "salt" to "c9/klVHsyTa/hnZRBktBkw==",
                                    "additionalParameters" to emptyMap<String, String>()
                                )
                            )
                            credentialData = objectMapper.writeValueAsString(
                                mapOf(
                                    "hashIterations" to 27500,
                                    "algorithm" to "pbkdf2-sha256",
                                    "additionalParameters" to emptyMap<String, String>()
                                )
                            )
                        }
                    )
                    realmRoles = listOf("default-roles-orkg-dev")
                }
            )
        println(response)

        val client = OrkgApiClient(container.httpPort)
        val token = client.getAccessToken(email, password, tokenPath)
        Assertions.assertThat(token).isNotNull()
    }
}
