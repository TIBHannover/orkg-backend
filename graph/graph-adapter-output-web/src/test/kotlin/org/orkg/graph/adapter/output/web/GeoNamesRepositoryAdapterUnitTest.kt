package org.orkg.graph.adapter.output.web

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.ExternalThing
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.stream.Stream

internal class GeoNamesRepositoryAdapterUnitTest : MockkBaseTest {
    private val geoNamesHostUrl = "https://example.org/geonames"
    private val geoNamesUser = "testuser"
    private val userAgent = "test user agent"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .addModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .build()
    private val repository = GeoNamesServiceAdapter(objectMapper, httpClient, userAgent, geoNamesHostUrl, geoNamesUser)

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T> `Given an ontology id and user input, when geonames returns success, it returns the external object`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        expectedResult: ExternalThing,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns successResponse

        val result = methodInvoker(repository, "geonames", userInput)
        result shouldNotBe null
        result shouldBe expectedResult

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    @Suppress("UNUSED_PARAMETER")
    fun <T> `Given an ontology id and user input, when geonames returns status not found, it returns null`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 404

        val result = methodInvoker(repository, "geonames", userInput)
        result shouldBe null

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T> `Given an ontology id and user input, when geonames service is not available, it throws an exception`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        assertThrows<ServiceUnavailable> { methodInvoker(repository, "geonames", userInput) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """GeoNames service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    @Suppress("UNUSED_PARAMETER")
    fun <T> `Given an ontology id and user input, when ontology is invalid, it returns null`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
    ) {
        val result = methodInvoker(repository, "not geonames", userInput)
        result shouldBe null
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
    ) {
        val result = methodInvoker(repository, "geonames", userInput)
        result shouldBe null
    }

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "2950159",
                "2950159",
                GeoNamesServiceAdapter::findResourceByShortForm,
                responseJson("geonames/citySuccess"),
                ExternalThing(
                    uri = ParsedIRI.create("https://sws.geonames.org/2950159"),
                    label = "Berlin",
                    description = null
                )
            ),
            Arguments.of(
                "2950159",
                ParsedIRI.create("https://sws.geonames.org/2950159"),
                GeoNamesServiceAdapter::findResourceByURI,
                responseJson("geonames/citySuccess"),
                ExternalThing(
                    uri = ParsedIRI.create("https://sws.geonames.org/2950159"),
                    label = "Berlin",
                    description = null
                )
            )
        )

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", GeoNamesServiceAdapter::findResourceByShortForm),
            Arguments.of("46568486468", GeoNamesServiceAdapter::findResourceByShortForm),
            Arguments.of(ParsedIRI.create("https://sws.geonames.org/abc"), GeoNamesServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI.create("https://www.geonames.org/46568486468"), GeoNamesServiceAdapter::findResourceByURI),
        )
    }
}
