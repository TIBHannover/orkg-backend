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
import org.springframework.http.MediaType
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64

internal class DataCiteDoiServiceAdapterUnitTest : MockkBaseTest {
    private val dataciteConfiguration: DataCiteConfiguration = mockk()
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val adapter =
        DataCiteDoiServiceAdapter(dataciteConfiguration, objectMapper, httpClient, ::TestBodyPublisher, fixedClock)

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
                        "Content-Type" to listOf("application/vnd.api+json; utf-8"),
                        "Authorization" to listOf("Basic $encodedCredentials"),
                        "Accept" to listOf(MediaType.APPLICATION_JSON_VALUE)
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
}
