@file:Suppress("HttpUrlsUsage")

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
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.stream.Stream

private const val CALLER = "caller"

internal class OLSRepositoryAdapterUnitTest : MockkBaseTest {
    private val olsHostUrl = "https://example.org/ols"
    private val userAgent = "test user agent"
    private val caller = "Some Component"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .addModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .build()
    private val repository = OLSServiceAdapter(objectMapper, httpClient, userAgent, olsHostUrl, caller)

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T : Any> `Given an ontology id and user input, when ols returns success, it returns the external object`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns params.successResponse

        val result = params.methodInvoker(repository, params.ontologyId, params.userInput)
        result shouldNotBe null
        result shouldBe params.expectedResult

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe params.userInput.toUri(params.ontologyId, params.entityType)
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                        CALLER to listOf(caller),
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
    fun <T : Any> `Given an ontology id and user input, when ols returns status not found, it returns null`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 404

        val result = params.methodInvoker(repository, params.ontologyId, params.userInput)
        result shouldBe null

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe params.userInput.toUri(params.ontologyId, params.entityType)
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                        CALLER to listOf(caller),
                    )
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T : Any> `Given an ontology id and user input, when ols service is not available, it throws an exception`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        assertThrows<ServiceUnavailable> { params.methodInvoker(repository, params.ontologyId, params.userInput) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """OntologyLookupService service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe params.userInput.toUri(params.ontologyId, params.entityType)
                    it.headers().map() shouldContainAll mapOf(
                        ACCEPT to listOf(APPLICATION_JSON_VALUE),
                        USER_AGENT to listOf(userAgent),
                        CALLER to listOf(caller),
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

    data class TestParameters<T>(
        val entityType: String,
        val userInput: T,
        val ontologyId: String,
        val methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
        val successResponse: String,
        val expectedResult: ExternalThing,
    )

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            TestParameters(
                entityType = "individuals",
                userInput = "AbsenceObservation",
                ontologyId = "abcd",
                methodInvoker = OLSServiceAdapter::findResourceByShortForm,
                successResponse = responseJson("ols/individualSuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                    label = "AbsenceObservation",
                    description = "A record describing an output of an observation process with indication of the absence of an observation"
                )
            ),
            TestParameters(
                entityType = "individuals",
                userInput = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                ontologyId = "abcd",
                methodInvoker = OLSServiceAdapter::findResourceByURI,
                successResponse = responseJson("ols/individualSuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/AbsenceObservation"),
                    label = "AbsenceObservation",
                    description = "A record describing an output of an observation process with indication of the absence of an observation"
                )
            ),
            TestParameters(
                entityType = "classes",
                userInput = "Collection",
                ontologyId = "skos",
                methodInvoker = OLSServiceAdapter::findClassByShortForm,
                successResponse = responseJson("ols/termSuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            ),
            TestParameters(
                entityType = "classes",
                userInput = ParsedIRI.create("https://sws.geonames.org/2950159"),
                ontologyId = "skos",
                methodInvoker = OLSServiceAdapter::findClassByURI,
                successResponse = responseJson("ols/termSuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            ),
            TestParameters(
                entityType = "properties",
                userInput = "hasCountry",
                ontologyId = "abcd",
                methodInvoker = OLSServiceAdapter::findPredicateByShortForm,
                successResponse = responseJson("ols/propertySuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/hasCountry"),
                    label = "has Country",
                    description = "Property to connect an instance of a class to a Country."
                )
            ),
            TestParameters(
                entityType = "properties",
                userInput = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/hasCountry"),
                ontologyId = "abcd",
                methodInvoker = OLSServiceAdapter::findPredicateByURI,
                successResponse = responseJson("ols/propertySuccess"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("http://rs.tdwg.org/abcd/terms/hasCountry"),
                    label = "has Country",
                    description = "Property to connect an instance of a class to a Country."
                )
            )
        ).map(Arguments::of)

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findResourceByShortForm),
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findClassByShortForm),
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findPredicateByShortForm),
            Arguments.of(ParsedIRI.create("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI.create("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI.create("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findPredicateByURI),
        )
    }
}
