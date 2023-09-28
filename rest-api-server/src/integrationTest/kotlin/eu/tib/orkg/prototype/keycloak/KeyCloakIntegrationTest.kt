package eu.tib.orkg.prototype.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import eu.tib.orkg.prototype.statements.client.OrkgApiClient
import eu.tib.orkg.prototype.testing.KEYCLOAK_TEST_REALM
import eu.tib.orkg.prototype.testing.KeycloakTestContainersBaseTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

private const val loginPath = "/realms/orkg-dev/login-actions/registration"
private const val tokenPath = "/realms/orkg-dev/protocol/openid-connect/token"

private fun KeycloakContainer.wellKnownUrl(realm: String): String =
    "$authServerUrl/realms/$realm/.well-known/openid-configuration"

class KeyCloakIntegrationTest : KeycloakTestContainersBaseTest() {

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
    fun `obtain access token for regular user`() {
        assert(container.isRunning)

        val tokenEndpoint = given().`when`().get(container.wellKnownUrl("master"))
            .then().statusCode(200)
            .extract().path<String>("token_endpoint")

        // TODO: might be better to use client credentials grant.
        val adminAccessToken = given()
            .contentType(ContentType.URLENC.withCharset(Charsets.UTF_8))
            // TODO: use OAuth2Constants?
            .formParam("client_id", "admin-cli")
            .formParam("grant_type", "password")
            .formParam("username", container.adminUsername)
            .formParam("password", container.adminPassword)
            .`when`().post(tokenEndpoint)
            .then().statusCode(200)
            .extract().path<String>("access_token")

        // Create the user
        val userId = given()
            .contentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            .body(
                mapOf(
                    "firstName" to "John",
                    "lastName" to "Doe",
                    "email" to "user@example.org",
                    "enabled" to "true",
                    "username" to "user",
                    "emailVerified" to "true",
                )
            )
            .auth().oauth2(adminAccessToken)
            .`when`().post("${container.authServerUrl}/admin/realms/$KEYCLOAK_TEST_REALM/users")
            .then().statusCode(201)
            .extract().header("Location")

        // Set the password
        given()
            .contentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            .body(mapOf("type" to "password", "temporary" to false, "value" to "password"))
            .auth().oauth2(adminAccessToken)
            .`when`().put("$userId/reset-password")
            .then().statusCode(204)

        val client = OrkgApiClient(container.httpPort)
        val token = client.getAccessToken(username = "user", password = "password", path = tokenPath)
        val tokens = client.getTokens(username = "user", password = "password")
        assertThat(token).isNotNull()
        assertThat(tokens.accessToken).isNotNull()
    }
}
