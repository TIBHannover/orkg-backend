package org.orkg.auth.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.Unauthorized
import org.orkg.common.exceptions.createProblemURI
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.Base64

private const val GRANT_TYPE = "grant_type"
private const val BASIC_TYPE = "Basic"

@RestController
class LegacyAuthController(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    private val bodyPublisherFactory: (String) -> HttpRequest.BodyPublisher = BodyPublishers::ofString,
    @param:Value("\${orkg.http.user-agent}")
    private val userAgent: String,
    @Value("\${orkg.oauth.legacy-client-id}")
    private val legacyClientId: String,
    @Value("\${orkg.oauth.legacy-client-secret}")
    private val legacyClientSecret: String,
    @Value("\${orkg.oauth.token-endpoint}")
    private val tokenEndpoint: String,
    @Value("\${orkg.oauth.registration-endpoint}")
    private val registrationEndpoint: String,
) {
    @PostMapping("/api/auth/register", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun register(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY.value())
            .location(URI.create(registrationEndpoint))
            .build()

    @PostMapping("/oauth/token")
    fun accessToken(
        @RequestHeader httpHeaders: HttpHeaders,
        @RequestParam request: MutableMap<String, String>,
    ): ResponseEntity<Any> {
        val grantType = request[GRANT_TYPE]
        when {
            grantType.isNullOrEmpty() -> throw OAuth2Exception("invalid_request", "Missing grant type")
            grantType != "password" -> throw OAuth2Exception("unsupported_grant_type", "Unsupported grant type")
        }
        val authorizationToken = extractBasicAuthorizationToken(httpHeaders)
        if (authorizationToken.username != legacyClientId && authorizationToken.secret != legacyClientSecret) {
            throw Unauthorized()
        }
        request["client_id"] = legacyClientId
        val formData = request.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8)}"
        }
        val keycloakRequest = HttpRequest.newBuilder(URI.create(tokenEndpoint))
            .header(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(USER_AGENT, userAgent)
            .POST(bodyPublisherFactory(formData))
            .build()
        try {
            val response = httpClient.send(keycloakRequest, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                return ResponseEntity.status(response.statusCode()).body(response.body())
            }
            return ResponseEntity.ok().body(objectMapper.readValue(response.body(), LegacyTokenResponse::class.java))
        } catch (e: Throwable) {
            throw ServiceUnavailable.create("keycloak", e)
        }
    }

    private fun extractBasicAuthorizationToken(httpHeaders: HttpHeaders): BasicAuthorizationToken {
        httpHeaders[HttpHeaders.AUTHORIZATION]?.forEach { value ->
            if (value.startsWith(BASIC_TYPE, ignoreCase = true)) {
                val token = value.substring(BASIC_TYPE.length).trim()
                val commaIndex = token.indexOf(',')
                if (commaIndex > 0) {
                    return BasicAuthorizationToken.parse(token.substring(0, commaIndex))
                }
                return BasicAuthorizationToken.parse(token)
            }
        }
        throw Unauthorized()
    }

    data class BasicAuthorizationToken(
        val username: String,
        val secret: String,
    ) {
        companion object {
            fun parse(token: String): BasicAuthorizationToken {
                val decoded = String(Base64.getDecoder().decode(token.toByteArray()))
                val colonIndex = decoded.indexOf(":")
                if (colonIndex == -1) {
                    throw Unauthorized()
                }
                return BasicAuthorizationToken(
                    username = decoded.substring(0, colonIndex),
                    secret = decoded.substring(colonIndex + 1)
                )
            }
        }
    }

    class OAuth2Exception(
        errorCode: String,
        override val message: String,
    ) : SimpleMessageException(
            status = BAD_REQUEST,
            message = message,
            properties = mapOf(
                "error" to errorCode,
                "error_description" to message,
            ),
            type = createProblemURI(errorCode)
        )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class LegacyTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("token_type")
        val tokenType: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
        val scope: String,
    )
}
