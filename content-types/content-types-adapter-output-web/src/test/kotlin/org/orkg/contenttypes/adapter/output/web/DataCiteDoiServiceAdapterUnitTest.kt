package org.orkg.contenttypes.adapter.output.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.DOI
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.TestBodyPublisher
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.configuration.DataCiteConfiguration
import org.orkg.contenttypes.output.testing.fixtures.createRegisterDoiCommand
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

internal class DataCiteDoiServiceAdapterUnitTest : MockkBaseTest {
    private val dataciteConfiguration: DataCiteConfiguration = mockk()
    private val httpClient: HttpClient = mockk()
    private val userAgent = "test user agent"
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val adapter =
        DataCiteDoiServiceAdapter(dataciteConfiguration, objectMapper, httpClient, ::TestBodyPublisher, userAgent, fixedClock)

    @Test
    fun `Given a doi, when fetching its associated metadata, and external service returns success, it returns the medatadata`() {
        val doi = DOI.of("10.3897/rio.7.e68513")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()
        val responseHeaders = createResponseHeaders()
        val body = responseJson("datacite/doiLookupSuccess")

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.headers() } returns HttpHeaders.of(responseHeaders) { _, _ -> true }
        every { response.body() } returns ByteArrayInputStream(body.toByteArray())

        val result = adapter.findMetadataByDoi(doi)
        result.isPresent shouldBe true
        result.get() shouldBe objectMapper.readTree(body)

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "GET"
                    request.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf("application/vnd.citationstyles.csl+json"),
                        USER_AGENT to listOf(userAgent),
                    )
                    request.uri() shouldBe URI.create(doi.uri)
                },
                any<HttpResponse.BodyHandler<InputStream>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.headers() }
        verify(exactly = 1) { response.body() }
    }

    @Test
    fun `Given a doi, when fetching its associated metadata, and external service returns an invalid content type, it returns an empty result`() {
        val doi = DOI.of("10.1000/182")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()
        val responseHeaders = createResponseHeaders(MediaType.TEXT_HTML_VALUE)

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.headers() } returns HttpHeaders.of(responseHeaders) { _, _ -> true }

        val result = adapter.findMetadataByDoi(doi)
        result.isPresent shouldBe false

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "GET"
                    request.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf("application/vnd.citationstyles.csl+json"),
                        USER_AGENT to listOf(userAgent),
                    )
                    request.uri() shouldBe URI.create(doi.uri)
                },
                any<HttpResponse.BodyHandler<InputStream>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.headers() }
    }

    @Test
    fun `Given a doi, when fetching its associated metadata, and external service returns an error, it returns an empty result`() {
        val doi = DOI.of("10.1000/182")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<InputStream>>()
        val responseHeaders = createResponseHeaders()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>()) } returns response
        every { response.statusCode() } returns 404
        every { response.headers() } returns HttpHeaders.of(responseHeaders) { _, _ -> true }

        val result = adapter.findMetadataByDoi(doi)
        result.isPresent shouldBe false

        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "GET"
                    request.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf("application/vnd.citationstyles.csl+json"),
                        USER_AGENT to listOf(userAgent),
                    )
                    request.uri() shouldBe URI.create(doi.uri)
                },
                any<HttpResponse.BodyHandler<InputStream>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.headers() }
    }

    @Test
    fun `Creating a doi, returns success`() {
        val command = createRegisterDoiCommand()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()
        val dataCiteUri = "https://api.test.datacite.org/dois"
        val dataCitePrefix = "10.7484"
        val encodedCredentials = Base64.getEncoder().encodeToString("username:password".toByteArray())
        // Deserialize and serialize json to remove formatting
        val json = objectMapper.writeValueAsString(objectMapper.readTree(requestJson("datacite/registerDoi")))

        every { dataciteConfiguration.publish } returns "draft"
        every { dataciteConfiguration.url } returns dataCiteUri
        every { dataciteConfiguration.doiPrefix } returns dataCitePrefix
        every { dataciteConfiguration.encodedCredentials } returns encodedCredentials
        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 201

        val result = adapter.register(command)
        result shouldBe DOI.of("$dataCitePrefix/${command.suffix}")

        verify(exactly = 1) { dataciteConfiguration.publish }
        verify(exactly = 1) { dataciteConfiguration.url }
        verify(exactly = 2) { dataciteConfiguration.doiPrefix }
        verify(exactly = 1) { dataciteConfiguration.encodedCredentials }
        verify(exactly = 1) {
            httpClient.send(
                withArg<HttpRequest> { request ->
                    request.method() shouldBe "POST"
                    request.headers().map() shouldContainAll mapOf(
                        CONTENT_TYPE to listOf("application/vnd.api+json; utf-8"),
                        AUTHORIZATION to listOf("Basic $encodedCredentials"),
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                    request.uri() shouldBe URI.create(dataCiteUri)
                    request.bodyPublisher().asClue { body ->
                        body.isPresent shouldBe true
                        body.get().shouldBeInstanceOf<TestBodyPublisher>().asClue {
                            it.content shouldBe json
                        }
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @Test
    fun `Creating a doi, when data cite returns error, then an exception is thrown`() {
        val command = createRegisterDoiCommand()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()
        val dataCiteUri = "https://api.test.datacite.org/dois"
        val dataCitePrefix = "10.7484"
        val encodedCredentials = Base64.getEncoder().encodeToString("username:password".toByteArray())

        every { dataciteConfiguration.publish } returns "draft"
        every { dataciteConfiguration.url } returns dataCiteUri
        every { dataciteConfiguration.doiPrefix } returns dataCitePrefix
        every { dataciteConfiguration.encodedCredentials } returns encodedCredentials
        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns responseJson("datacite/internalServerError")

        shouldThrow<ServiceUnavailable> {
            adapter.register(command)
        }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """DOI service returned status 500 with error response: "Internal error"."""
        }

        verify(exactly = 1) { dataciteConfiguration.publish }
        verify(exactly = 1) { dataciteConfiguration.url }
        verify(exactly = 1) { dataciteConfiguration.doiPrefix }
        verify(exactly = 1) { dataciteConfiguration.encodedCredentials }
        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    private fun createResponseHeaders(
        contentType: String = "application/vnd.citationstyles.csl+json",
    ): Map<String, List<String>> = mapOf(
        "Content-Type" to listOf(contentType),
        "Content-Length" to listOf("2012"),
        "Connection" to listOf("keep-alive"),
        "vary" to listOf("Accept", "Accept-Encoding"),
        "access-control-expose-headers" to listOf("Link"),
        "access-control-allow-headers" to listOf("X-Requested-With", "Accept", "Accept-Encoding", "Accept-Charset", "Accept-Language", "Accept-Ranges", "Cache-Control"),
        "access-control-allow-origin" to listOf("*"),
        "content-encoding" to listOf("gzip"),
        "server" to listOf("Jetty(9.4.40.v20210413)"),
        "x-rate-limit-limit" to listOf("50"),
        "x-rate-limit-interval" to listOf("1s"),
        "x-api-pool" to listOf("public"),
        "permissions-policy" to listOf("interest-cohort=()"),
    )
}
