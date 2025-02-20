@file:Suppress("HttpUrlsUsage")

package org.orkg.graph.adapter.output.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.stream.Stream

internal class OLSRepositoryAdapterUnitTest : MockkBaseTest {
    private val olsHostUrl = "https://example.org/ols"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val repository = OLSServiceAdapter(objectMapper, httpClient, olsHostUrl)

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T : Any> `Given an ontology id and user input, when ols returns success, it returns the external object`(
        entityType: String,
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        expectedResult: ExternalThing,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns successResponse

        val result = methodInvoker(repository, ontologyId, userInput)
        result shouldNotBe null
        result shouldBe expectedResult

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                    it.headers().map() shouldContainAll mapOf(
                        "Accept" to listOf("application/json")
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
    fun <T : Any> `Given an ontology id and user input, when ols returns status not found, it returns null`(
        entityType: String,
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 404

        val result = methodInvoker(repository, ontologyId, userInput)
        result shouldBe null

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                    it.headers().map() shouldContainAll mapOf(
                        "Accept" to listOf("application/json")
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T : Any> `Given an ontology id and user input, when ols service is not available, it throws an exception`(
        entityType: String,
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        assertThrows<ServiceUnavailable> { methodInvoker(repository, ontologyId, userInput) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """OntologyLookupService service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                    it.headers().map() shouldContainAll mapOf(
                        "Accept" to listOf("application/json")
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
    ) {
        val result = methodInvoker(repository, ontologyId, userInput)
        result shouldBe null
    }

    private fun <T : Any> T.toUri(ontologyId: String, entityType: String): URI =
        if (this is ParsedIRI) {
            UriComponentsBuilder.fromUriString(olsHostUrl)
                .path("/ontologies/$ontologyId/$entityType")
                .queryParam("iri", toString())
                .queryParam("exactMatch", true)
                .queryParam("lang", "en")
                .queryParam("includeObsoleteEntities", false)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .build()
                .toUri()
        } else {
            UriComponentsBuilder.fromUriString(olsHostUrl)
                .path("/ontologies/$ontologyId/$entityType")
                .queryParam("shortForm", this)
                .queryParam("exactMatch", true)
                .queryParam("lang", "en")
                .queryParam("includeObsoleteEntities", false)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .build()
                .toUri()
        }

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "individuals",
                "AbsenceObservation",
                "abcd",
                OLSServiceAdapter::findResourceByShortForm,
                responseJson("ols/individualSuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                    label = "AbsenceObservation",
                    description = "A record describing an output of an observation process with indication of the absence of an observation"
                )
            ),
            Arguments.of(
                "individuals",
                ParsedIRI("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                "abcd",
                OLSServiceAdapter::findResourceByURI,
                responseJson("ols/individualSuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                    label = "AbsenceObservation",
                    description = "A record describing an output of an observation process with indication of the absence of an observation"
                )
            ),
            Arguments.of(
                "classes",
                "Collection",
                "skos",
                OLSServiceAdapter::findClassByShortForm,
                responseJson("ols/termSuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            ),
            Arguments.of(
                "classes",
                ParsedIRI("https://sws.geonames.org/2950159"),
                "skos",
                OLSServiceAdapter::findClassByURI,
                responseJson("ols/termSuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            ),
            Arguments.of(
                "properties",
                "hasCountry",
                "abcd",
                OLSServiceAdapter::findPredicateByShortForm,
                responseJson("ols/propertySuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://rs.tdwg.org/abcd/terms/hasCountry"),
                    label = "has Country",
                    description = "Property to connect an instance of a class to a Country."
                )
            ),
            Arguments.of(
                "properties",
                ParsedIRI("http://rs.tdwg.org/abcd/terms/hasCountry"),
                "abcd",
                OLSServiceAdapter::findPredicateByURI,
                responseJson("ols/propertySuccess"),
                ExternalThing(
                    uri = ParsedIRI("http://rs.tdwg.org/abcd/terms/hasCountry"),
                    label = "has Country",
                    description = "Property to connect an instance of a class to a Country."
                )
            )
        )

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findResourceByShortForm),
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findClassByShortForm),
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findPredicateByShortForm),
            Arguments.of(ParsedIRI("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findPredicateByURI),
        )
    }
}
