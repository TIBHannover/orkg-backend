package org.orkg.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.keycloak.OAuth2Constants.CLIENT_ID
import org.keycloak.OAuth2Constants.GRANT_TYPE
import org.keycloak.OAuth2Constants.PASSWORD
import org.keycloak.OAuth2Constants.USERNAME
import org.keycloak.representations.idm.UserRepresentation
import org.orkg.testing.KEYCLOAK_CLIENT_ID
import org.orkg.testing.KEYCLOAK_CLIENT_SECRET
import org.orkg.testing.KEYCLOAK_REALM
import org.orkg.testing.KeycloakTestContainersBaseTest
import tools.jackson.databind.ObjectMapper
import java.util.Base64

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

        val masterRealmTokenEndpoint = given().`when`().get(container.wellKnownUrl("master"))
            .then().statusCode(200)
            .extract().path<String>("token_endpoint")

        val adminAccessToken = given()
            .log().ifValidationFails()
            .contentType(ContentType.URLENC.withCharset(Charsets.UTF_8))
            .formParam(CLIENT_ID, "admin-cli")
            .formParam(GRANT_TYPE, PASSWORD)
            .formParam(USERNAME, container.adminUsername)
            .formParam(PASSWORD, container.adminPassword)
            .`when`().post(masterRealmTokenEndpoint)
            .then().statusCode(200)
            .extract().path<String>("access_token")

        val user: UserRepresentation = UserRepresentation().apply {
            email = "test@example.org"
            isEnabled = true
            username = "test"
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
            .body(
                UserRepresentation().apply {
                    requiredActions = listOf()
                }
            )
            .auth().oauth2(adminAccessToken)
            .`when`().put(userId)
            .then().statusCode(204)

        val orkgRealmTokenEndpoint = given().`when`().get(container.wellKnownUrl("orkg"))
            .then().statusCode(200)
            .extract().path<String>("token_endpoint")

        val accessToken = given()
            .log().ifValidationFails()
            .contentType(ContentType.URLENC.withCharset(Charsets.UTF_8))
            .formParam("client_id", KEYCLOAK_CLIENT_ID)
            .formParam("client_secret", KEYCLOAK_CLIENT_SECRET)
            .formParam("grant_type", "password")
            .formParam("username", "test")
            .formParam("password", "Pa\$\$w0rd")
            .`when`().post(orkgRealmTokenEndpoint)
            .then().statusCode(200)
            .extract().path<String>("access_token")

        assertThat(accessToken).isNotNull()

        // Decode token
        val (_, tokenPayload) = accessToken
            .split(".")
            .take(2)
            .map { String(Base64.getDecoder().decode(it)) }
        val payload = ObjectMapper().readTree(tokenPayload)

        assertThat(payload.path("azp").stringValue(null)).isEqualTo("orkg-frontend")
        assertThat(payload.path("preferred_username").stringValue(null)).isEqualTo("test")
        assertThat(payload.path("email").stringValue(null)).isEqualTo("test@example.org")
        assertThat(payload.path("display_name").stringValue(null)).isEqualTo("John Doe")
        assertThat(payload.path("iss").stringValue(null)).isEqualTo("${container.authServerUrl}/realms/orkg")
    }
}
