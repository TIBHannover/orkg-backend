package eu.tib.orkg.prototype.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import dasniko.testcontainers.keycloak.KeycloakContainer
import eu.tib.orkg.prototype.statements.client.OrkgApiClient
import eu.tib.orkg.prototype.testing.KEYCLOAK_TEST_REALM
import eu.tib.orkg.prototype.testing.KeycloakTestContainersBaseTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation

private const val loginPath = "/realms/orkg-dev/login-actions/registration"
private const val tokenPath = "/realms/orkg-dev/protocol/openid-connect/token"

private fun KeycloakContainer.wellKnownUrl(realm: String): String =
    "$authServerUrl/realms/$realm/.well-known/openid-configuration"

class KeyCloakIntegrationTest : KeycloakTestContainersBaseTest() {

    private val objectMapper: ObjectMapper = ObjectMapper()

    @Test
    fun `obtains the account service information and successfully connects to it`() {
        val accountService = given().`when`().get("${container.authServerUrl}/realms/$KEYCLOAK_TEST_REALM")
            .then().statusCode(200).body("realm", equalTo(KEYCLOAK_TEST_REALM))
            .extract().path<String>("account-service")

        given().`when`().get(accountService).then().statusCode(200)
    }

    @Test
    fun `use OIDC discovery to verify that the password grant is supported`() {
        val supportedGrantTypes = given().`when`().get(container.wellKnownUrl(KEYCLOAK_TEST_REALM))
            .then().statusCode(200)
            .extract().body().jsonPath().getList<String>("grant_types_supported")

        assertThat(supportedGrantTypes).contains("password")
    }

    @Test
    fun test() {
        assert(container.isRunning)

        val realm = "orkg-dev"

        val keycloak = container.keycloakAdminClient

        /*
        val keycloak = KeycloakBuilder.builder()
            .grantType(OAuth2Constants.PASSWORD)
            .serverUrl("http://localhost:${container.httpPort}")
            .realm(realm)
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .build()
        */

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
