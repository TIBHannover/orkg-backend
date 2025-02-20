package org.orkg.auth.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.testing.fixtures.TestBodyPublisher
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

@Import(SecurityTestConfiguration::class, LegacyAuthControllerUnitTest.LegacyAuthControllerTestConfiguration::class)
@TestPropertySource(
    properties = [
        "orkg.oauth.legacy-client-id=orkg-client",
        "orkg.oauth.legacy-client-secret=secret",
        "orkg.oauth.token-endpoint=http://localhost:1234/path/to/token/endpoint",
        "orkg.oauth.registration-endpoint=http://localhost:1234/path/to/registration/endpoint",
    ]
)
@ContextConfiguration(classes = [LegacyAuthController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LegacyAuthController::class])
internal class LegacyAuthControllerUnitTest : MockMvcBaseTest("legacy-auth") {
    @Autowired
    private lateinit var httpClient: HttpClient

    @Value("\${orkg.oauth.legacy-client-id}")
    private lateinit var legacyClientId: String

    @Value("\${orkg.oauth.legacy-client-secret}")
    private lateinit var legacyClientSecret: String

    @Value("\${orkg.oauth.token-endpoint}")
    private lateinit var tokenEndpoint: String

    @Value("\${orkg.oauth.registration-endpoint}")
    private lateinit var registrationEndpoint: String

    @Test
    fun `Given a user registration request, then status is 301 MOVED PERMANENTLY`() {
        post("/api/auth/register")
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isMovedPermanently)
            .andExpect(header().string("Location", registrationEndpoint))
    }

    @Test
    fun `Given a token request, when valid, then status is 200 OK and token is returned`() {
        val response = mockk<HttpResponse<String>>()

        val username = "test_user"
        val password = "secret"
        val grantType = "password"
        val clientId = "orkg-client"

        val accessToken = "SecretAccessToken"
        val tokenType = "Bearer"
        val expiresIn = 1800
        val scope = "email profile"

        val keycloakResponse = objectMapper.writeValueAsString(
            mapOf(
                "access_token" to accessToken,
                "expires_in" to expiresIn,
                "refresh_expires_in" to 1800,
                "refresh_token" to "SecretRefreshToken",
                "token_type" to tokenType,
                "not-before-policy" to 0,
                "session_state" to "StateUUID",
                "scope" to scope
            )
        )

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns keycloakResponse

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("password", password)
            .param("client_id", clientId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.access_token").value(accessToken))
            .andExpect(jsonPath("$.token_type").value(tokenType))
            .andExpect(jsonPath("$.expires_in").value(expiresIn))
            .andExpect(jsonPath("$.scope").value(scope))

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        HttpHeaders.CONTENT_TYPE to listOf(APPLICATION_FORM_URLENCODED_VALUE)
                    )
                    request.uri() shouldBe URI.create(tokenEndpoint)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            val requestParameters = decodeFormUrlEncodedParameters(it.content)
                            requestParameters shouldContainExactly mapOf(
                                "username" to username,
                                "password" to password,
                                "grant_type" to grantType,
                                "client_id" to clientId
                            )
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given a token request, when content-type is invalid, then status is 400 BAD REQUEST`() {
        val username = "test_user"
        val password = "secret"
        val grantType = "password"

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_JSON)
            .content(
                mapOf(
                    "grant_type" to grantType,
                    "username" to username,
                    "password" to password,
                    "client_id" to "orkg-client",
                )
            )
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("invalid_request"))
            .andExpect(jsonPath("$.error_description").value("Missing grant type"))
    }

    @Test
    fun `Given a token request, when authorization header is missing, then status is 401 UNAUTHORIZED`() {
        val username = "test_user"
        val password = "secret"
        val grantType = "password"

        post("/oauth/token")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("password", password)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status", `is`(401)))
            .andExpect(jsonPath("$.error", `is`("Unauthorized")))
            .andExpect(jsonPath("$.path", `is`("/oauth/token")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
            .andExpect(jsonPath("$.message", `is`("Unauthorized.")))
    }

    @Test
    fun `Given a token request, when credentials are wrong, then status is 401 UNAUTHORIZED`() {
        val response = mockk<HttpResponse<String>>()

        val username = "test_user"
        val password = "secret"
        val grantType = "password"
        val clientId = "orkg-client"

        val error = "invalid_grant"
        val errorDescription = "Invalid user credentials"

        val keycloakResponse = objectMapper.writeValueAsString(
            mapOf(
                "error" to error,
                "error_description" to errorDescription
            )
        )

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 401
        every { response.body() } returns keycloakResponse

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("password", password)
            .param("client_id", clientId)
            .perform()
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value(error))
            .andExpect(jsonPath("$.error_description").value(errorDescription))

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        HttpHeaders.CONTENT_TYPE to listOf(APPLICATION_FORM_URLENCODED_VALUE)
                    )
                    request.uri() shouldBe URI.create(tokenEndpoint)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            val requestParameters = decodeFormUrlEncodedParameters(it.content)
                            requestParameters shouldContainExactly mapOf(
                                "username" to username,
                                "password" to password,
                                "grant_type" to grantType,
                                "client_id" to clientId
                            )
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given a token request, when username is missing, then status is 401 UNAUTHORIZED`() {
        val response = mockk<HttpResponse<String>>()

        val password = "secret"
        val grantType = "password"
        val clientId = "orkg-client"

        val error = "invalid_request"
        val errorDescription = "Missing parameter: username"

        val keycloakResponse = objectMapper.writeValueAsString(
            mapOf(
                "error" to error,
                "error_description" to errorDescription
            )
        )

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 401
        every { response.body() } returns keycloakResponse

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("password", password)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value(error))
            .andExpect(jsonPath("$.error_description").value(errorDescription))

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        HttpHeaders.CONTENT_TYPE to listOf(APPLICATION_FORM_URLENCODED_VALUE)
                    )
                    request.uri() shouldBe URI.create(tokenEndpoint)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            val requestParameters = decodeFormUrlEncodedParameters(it.content)
                            requestParameters shouldContainExactly mapOf(
                                "password" to password,
                                "grant_type" to grantType,
                                "client_id" to clientId
                            )
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given a token request, when password is missing, then status is 401 UNAUTHORIZED`() {
        val response = mockk<HttpResponse<String>>()

        val username = "test_user"
        val grantType = "password"
        val clientId = "orkg-client"

        val error = "invalid_request"
        val errorDescription = "Invalid user credentials"

        val keycloakResponse = objectMapper.writeValueAsString(
            mapOf(
                "error" to error,
                "error_description" to errorDescription
            )
        )

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 401
        every { response.body() } returns keycloakResponse

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value(error))
            .andExpect(jsonPath("$.error_description").value(errorDescription))

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        HttpHeaders.CONTENT_TYPE to listOf(APPLICATION_FORM_URLENCODED_VALUE)
                    )
                    request.uri() shouldBe URI.create(tokenEndpoint)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            val requestParameters = decodeFormUrlEncodedParameters(it.content)
                            requestParameters shouldContainExactly mapOf(
                                "username" to username,
                                "grant_type" to grantType,
                                "client_id" to clientId
                            )
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given a token request, when grant type is missing, then status is 400 BAD REQUEST`() {
        val username = "test_user"
        val password = "secret"

        post("/oauth/token")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("username", username)
            .param("password", password)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", `is`("invalid_request")))
            .andExpect(jsonPath("$.error_description", `is`("Missing grant type")))
    }

    @ParameterizedTest
    @ValueSource(strings = ["implicit", "refresh_token", "abcdef"])
    fun `Given a token request, when grant type is invalid, then status is 400 BAD REQUEST`(grantType: String) {
        val username = "test_user"
        val password = "secret"

        post("/oauth/token")
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("password", password)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error", `is`("unsupported_grant_type")))
            .andExpect(jsonPath("$.error_description", `is`("Unsupported grant type")))
    }

    @Test
    fun `Given a token request, when token endpoint is unavailable, then status is 503 SERVICE UNAVILABLE`() {
        val username = "test_user"
        val grantType = "password"

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws IOException("Service unavailable")

        post("/oauth/token")
            .header(HttpHeaders.AUTHORIZATION, createBasicAuthorizationToken())
            .contentType(APPLICATION_FORM_URLENCODED)
            .param("grant_type", grantType)
            .param("username", username)
            .param("client_id", "orkg-client")
            .perform()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.status", `is`(503)))
            .andExpect(jsonPath("$.error", `is`("Service Unavailable")))
            .andExpect(jsonPath("$.message", `is`("""Service unavailable.""")))
            .andExpect(jsonPath("$.path", `is`("/oauth/token")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
    }

    private fun createBasicAuthorizationToken(
        username: String = legacyClientId,
        secret: String = legacyClientSecret,
    ): String = "Basic ${Base64.getEncoder().encodeToString("$username:$secret".toByteArray())}"

    private fun decodeFormUrlEncodedParameters(encoded: String): Map<String, String> =
        encoded.split("&")
            .map { kv -> kv.trim().split("=", limit = 2) }
            .associate { (key, value) -> key.trim() to URLDecoder.decode(value, Charsets.UTF_8) }

    @TestConfiguration
    class LegacyAuthControllerTestConfiguration {
        @Bean
        fun bodyPublisherFactory(): (String) -> HttpRequest.BodyPublisher = ::TestBodyPublisher

        @Bean
        fun httpClient(): HttpClient = mockk(relaxUnitFun = true)
    }
}
