package org.orkg.auth.client

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.internal.mapping.Jackson2Mapper
import java.net.URI
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

class OrkgApiClient(
    private val realm: String,
    private val clientId: String,
    private val host: String,
    private val port: Int = 80
) {
    private val oidc: OIDCDiscovery by lazy {
        given()
            .log().ifValidationFails()
            .`when`().get(
                URI("http", null, host, port, "/realms/$realm/.well-known/openid-configuration", null, null)
            )
            .then().statusCode(200)
            .extract().`as`(OIDCDiscovery::class.java, objectMapper)
    }

    /**
     * Obtain tokens from via the token endpoint.
     */
    fun tokensFor(username: String, password: String): TokenResponse =
        given()
            .log().ifValidationFails()
            .contentType(ContentType.URLENC.withCharset(Charsets.UTF_8))
            .formParam("client_id", clientId)
            .formParam("grant_type", "password")
            .formParam("username", username)
            .formParam("password", password)
            .`when`().post(oidc.tokenEndpoint)
            .then().statusCode(200)
            .extract().`as`(TokenResponse::class.java, objectMapper)

    fun registerUser(username: String, password: String, path: String) {
        val uri = URI("http", null, host, port, path, "client_id=orkg-frontend&response_type=code", null)
        val url = uri.toString()

        val params = LinkedMultiValueMap<String, String>().apply {
            add("username", username)
            add("new-password", password)
            add("password-confirm", password)
            add("firstName", "User")
            add("lastName", "ORKG")
            add("email", username)
        }
        val headers = HttpHeaders().apply {
            contentType = APPLICATION_FORM_URLENCODED
        }
        val request = HttpEntity(params, headers)

        val restTemplate = RestTemplate().apply {
            messageConverters.add(StringHttpMessageConverter())
        }
        restTemplate.postForObject<String?>(url, request)
    }

    /**
     * Provides access to the elements in the OIDC token response.
     */
    data class TokenResponse(
        // TODO: extend as needed
        val accessToken: String?,
        val tokenType: String?,
        val expiresIn: String?,
        val scope: String?
    )

    private data class OIDCDiscovery(
        // TODO: extend as needed
        val tokenEndpoint: String
    )

    private val objectMapper = Jackson2Mapper { _, _ ->
        ObjectMapper().findAndRegisterModules().apply {
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            setPropertyNamingStrategy(SNAKE_CASE)
        }
    }
}
