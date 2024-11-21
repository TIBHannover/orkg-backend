package org.orkg.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.keycloak.OAuth2Constants.CLIENT_ID
import org.keycloak.OAuth2Constants.GRANT_TYPE
import org.keycloak.OAuth2Constants.PASSWORD
import org.keycloak.OAuth2Constants.USERNAME
import org.keycloak.representations.idm.UserRepresentation
import org.orkg.auth.client.OrkgApiClient
import org.orkg.testing.KEYCLOAK_CLIENT_ID
import org.orkg.testing.KEYCLOAK_REALM
import org.orkg.testing.KeycloakTestContainersBaseTest

private fun KeycloakContainer.wellKnownUrl(realm: String): String =
    "$authServerUrl/realms/$realm/.well-known/openid-configuration"

internal class KeyCloakIntegrationTest : KeycloakTestContainersBaseTest() {

    @Test
    fun `obtains the account service information and successfully connects to it`() {
        val accountService = given().`when`().get("${container.authServerUrl}/realms/$KEYCLOAK_REALM")
            .then().statusCode(200).body("realm", equalTo(KEYCLOAK_REALM))
            .extract().path<String>("account-service")

        given().`when`().get(accountService).then().statusCode(200)
    }

    @Test
    fun `use OIDC discovery to verify that the password grant is supported`() {
        val supportedGrantTypes = given().`when`().get(container.wellKnownUrl(KEYCLOAK_REALM))
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

        val adminAccessToken = given()
            .log().ifValidationFails()
            .contentType(ContentType.URLENC.withCharset(Charsets.UTF_8))
            .formParam(CLIENT_ID, "admin-cli")
            .formParam(GRANT_TYPE, PASSWORD)
            .formParam(USERNAME, container.adminUsername)
            .formParam(PASSWORD, container.adminPassword)
            .`when`().post(tokenEndpoint)
            .then().statusCode(200)
            .extract().path<String>("access_token")

        val user: UserRepresentation = UserRepresentation().apply {
            email = "user@example.org"
            isEnabled = true
            username = "user"
            isEmailVerified = true
            attributes = mapOf(
                "displayName" to listOf("John Doe"),
            )
        }

        // Create the user
        val userId = given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            .body(user)
            .auth().oauth2(adminAccessToken)
            .`when`().post("${container.authServerUrl}/admin/realms/$KEYCLOAK_REALM/users")
            .then().statusCode(201)
            .extract().header("Location")

        // Set the password
        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            .body(mapOf("type" to "password", "temporary" to false, "value" to "Pa\$\$w0rd"))
            .auth().oauth2(adminAccessToken)
            .`when`().put("$userId/reset-password")
            .then().statusCode(204)

        // Remove required actions
        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            .body(UserRepresentation().apply {
                requiredActions = listOf()
            })
            .auth().oauth2(adminAccessToken)
            .`when`().put(userId)
            .then().statusCode(204)

        val client = OrkgApiClient(KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, container.host, container.httpPort)
        val tokens = client.tokensFor(username = "user", password = "Pa\$\$w0rd")
        assertThat(tokens.accessToken).isNotNull()

        // Decode token
        val (_, tokenPayload) = tokens.accessToken!!
            .split(".")
            .take(2)
            .map { String(Base64.getDecoder().decode(it)) }
        val payload = ObjectMapper().readTree(tokenPayload)

        assertThat(payload.path("azp").textValue()).isEqualTo("orkg-frontend")
        assertThat(payload.path("preferred_username").textValue()).isEqualTo("user")
        assertThat(payload.path("email").textValue()).isEqualTo("user@example.org")
        assertThat(payload.path("display_name").textValue()).isEqualTo("John Doe")
        assertThat(payload.path("iss").textValue()).isEqualTo("${container.authServerUrl}/realms/orkg")
    }
}
