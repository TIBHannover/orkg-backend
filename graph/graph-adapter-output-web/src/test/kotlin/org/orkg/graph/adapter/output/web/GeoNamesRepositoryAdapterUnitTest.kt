package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.stream.Stream
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.ExternalThing

internal class GeoNamesRepositoryAdapterUnitTest {
    private val geoNamesHostUrl = "https://example.org/geonames"
    private val geoNamesUser = "testuser"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val repository = GeoNamesServiceAdapter(objectMapper, httpClient, geoNamesHostUrl, geoNamesUser)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(httpClient)
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T> `Given an ontology id and user input, when geonames returns success, it returns the external object`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        expectedResult: ExternalThing
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
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }

        confirmVerified(response)
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
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 1) { response.statusCode() }

        confirmVerified(response)
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T> `Given an ontology id and user input, when geonames service is not available, it throws an exception`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?
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
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$geoNamesHostUrl/get?geonameId=$entityId&username=$geoNamesUser")
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }

        confirmVerified(response)
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    @Suppress("UNUSED_PARAMETER")
    fun <T> `Given an ontology id and user input, when ontology is invalid, it returns null`(
        entityId: String,
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?
    ) {
        val result = methodInvoker(repository, "not geonames", userInput)
        result shouldBe null
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        methodInvoker: (GeoNamesServiceAdapter, String, T) -> ExternalThing?
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
                geonamesSuccessResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://sws.geonames.org/2950159"),
                    label = "Berlin",
                    description = null
                )
            ),
            Arguments.of(
                "2950159",
                ParsedIRI("https://sws.geonames.org/2950159"),
                GeoNamesServiceAdapter::findResourceByURI,
                geonamesSuccessResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://sws.geonames.org/2950159"),
                    label = "Berlin",
                    description = null
                )
            )
        )

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", GeoNamesServiceAdapter::findResourceByShortForm),
            Arguments.of("46568486468", GeoNamesServiceAdapter::findResourceByShortForm),
            Arguments.of(ParsedIRI("https://sws.geonames.org/abc"), GeoNamesServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI("https://www.geonames.org/46568486468"), GeoNamesServiceAdapter::findResourceByURI),
        )
    }
}

private const val geonamesSuccessResponseJson = """{
  "timezone": {
    "gmtOffset": 1,
    "timeZoneId": "Europe/Berlin",
    "dstOffset": 2
  },
  "bbox": {
    "east": 13.7604692835498,
    "south": 52.3382418348765,
    "north": 52.6749171487584,
    "west": 13.0883332178678,
    "accuracyLevel": 0
  },
  "asciiName": "Berlin",
  "astergdem": 52,
  "countryId": "2921044",
  "fcl": "P",
  "srtm3": 43,
  "adminId3": "6547383",
  "countryCode": "DE",
  "adminId4": "6547539",
  "adminCodes1": {
    "ISO3166_2": "BE"
  },
  "adminId1": "2950157",
  "lat": "52.52437",
  "fcode": "PPLC",
  "continentCode": "EU",
  "elevation": 74,
  "adminCode2": "00",
  "adminCode3": "11000",
  "adminCode1": "16",
  "lng": "13.41053",
  "geonameId": 2950159,
  "toponymName": "Berlin",
  "adminCode4": "11000000",
  "population": 3426354,
  "wikipediaURL": "en.wikipedia.org/wiki/Berlin",
  "adminName5": "",
  "adminName4": "Berlin",
  "adminName3": "Berlin, Stadt",
  "alternateNames": [
    {
      "isPreferredName": true,
      "name": "Berlin",
      "lang": "de"
    },
    {
      "name": "Berlin",
      "lang": "en"
    }
  ],
  "adminName2": "",
  "name": "Berlin",
  "fclName": "city, village,...",
  "countryName": "Germany",
  "fcodeName": "capital of a political entity",
  "adminName1": "Berlin"
}"""
