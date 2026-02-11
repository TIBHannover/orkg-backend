package org.orkg.graph.adapter.output.web

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.ExternalEntityIsNotAClass
import org.orkg.graph.domain.ExternalEntityIsNotAResource
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

internal class WikidataRepositoryAdapterUnitTest : MockkBaseTest {
    private val wikidataHostUrl = "https://example.org/wikidata"
    private val userAgent = "test user agent"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .addModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .build()
    private val repository = WikidataServiceAdapter(objectMapper, httpClient, userAgent, wikidataHostUrl)

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T> `Given an ontology id and user input, when wikidata returns success, it returns the external object`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns params.successResponse

        val result = params.methodInvoker(repository, "wikidata", params.userInput)
        result shouldNotBe null
        result shouldBe params.expectedResult

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=${params.entityId}&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype%7Cclaims")
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
    fun <T> `Given an ontology id and user input, when wikidata returns status not found, it returns null`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns params.notFoundResponse

        val result = params.methodInvoker(repository, "wikidata", params.userInput)
        result shouldBe null

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=${params.entityId}&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype%7Cclaims")
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
    @MethodSource("typeMismatch")
    fun <T> `Given an ontology id and user input, when wikidata returns returns success but entity does not match expected type, it throws an exception`(params: TypeMismatchTestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns params.notFoundResponse

        Assertions.assertThrows(params.throwable::class.java) {
            params.methodInvoker(repository, "wikidata", params.userInput)
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=${params.entityId}&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype%7Cclaims")
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
    fun <T> `Given an ontology id and user input, when wikidata service is not available, it throws an exception`(params: TestParameters<T>) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        assertThrows<ServiceUnavailable> { params.methodInvoker(repository, "wikidata", params.userInput) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """Wikidata service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=${params.entityId}&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype%7Cclaims")
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
    fun <T> `Given an ontology id and user input, when ontology is invalid, it returns null`(params: TestParameters<T>) {
        val result = params.methodInvoker(repository, "not wikidata", params.userInput)
        result shouldBe null
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?,
    ) {
        val result = methodInvoker(repository, "wikidata", userInput)
        result shouldBe null
    }

    data class TestParameters<T>(
        val entityId: String,
        val userInput: T,
        val methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?,
        val successResponse: String,
        val notFoundResponse: String,
        val expectedResult: ExternalThing,
    )

    data class TypeMismatchTestParameters<T>(
        val entityId: String,
        val userInput: T,
        val methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?,
        val successResponse: String,
        val notFoundResponse: String,
        val throwable: Throwable,
    )

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            TestParameters(
                entityId = "Q42",
                userInput = "Q42",
                methodInvoker = WikidataServiceAdapter::findResourceByShortForm,
                successResponse = responseJson("wikidata/itemSuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            TestParameters(
                entityId = "Q42",
                userInput = "Q42",
                methodInvoker = WikidataServiceAdapter::findClassByShortForm,
                successResponse = responseJson("wikidata/itemSuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            TestParameters(
                entityId = "P30",
                userInput = "P30",
                methodInvoker = WikidataServiceAdapter::findPredicateByShortForm,
                successResponse = responseJson("wikidata/propertySuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/P30"),
                    label = "continent",
                    description = "continent of which the subject is a part"
                )
            ),
            TestParameters(
                entityId = "Q42",
                userInput = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                methodInvoker = WikidataServiceAdapter::findResourceByURI,
                successResponse = responseJson("wikidata/itemSuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            TestParameters(
                entityId = "Q42",
                userInput = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                methodInvoker = WikidataServiceAdapter::findClassByURI,
                successResponse = responseJson("wikidata/itemSuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            TestParameters(
                entityId = "P30",
                userInput = ParsedIRI.create("https://www.wikidata.org/entity/P30"),
                methodInvoker = WikidataServiceAdapter::findPredicateByURI,
                successResponse = responseJson("wikidata/propertySuccess"),
                notFoundResponse = responseJson("wikidata/entityNotFound"),
                expectedResult = ExternalThing(
                    uri = ParsedIRI.create("https://www.wikidata.org/entity/P30"),
                    label = "continent",
                    description = "continent of which the subject is a part"
                )
            )
        ).map(Arguments::of)

        @JvmStatic
        fun typeMismatch(): Stream<Arguments> = Stream.of(
            TypeMismatchTestParameters(
                entityId = "Q42",
                userInput = "Q42",
                methodInvoker = WikidataServiceAdapter::findResourceByShortForm,
                successResponse = responseJson("wikidata/resourceSuccess"),
                notFoundResponse = responseJson("wikidata/classSuccess"), // should fail, because response contains a class and not a resource
                throwable = ExternalEntityIsNotAResource("wikidata", "Q42")
            ),
            TypeMismatchTestParameters(
                entityId = "Q42",
                userInput = "Q42",
                methodInvoker = WikidataServiceAdapter::findClassByShortForm,
                successResponse = responseJson("wikidata/classSuccess"),
                notFoundResponse = responseJson("wikidata/resourceSuccess"), // should fail, because response contains a resource and not a class
                throwable = ExternalEntityIsNotAClass("wikidata", "Q42")
            ),
            TypeMismatchTestParameters(
                entityId = "Q42",
                userInput = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                methodInvoker = WikidataServiceAdapter::findResourceByURI,
                successResponse = responseJson("wikidata/resourceSuccess"),
                notFoundResponse = responseJson("wikidata/classSuccess"), // should fail, because response contains a class and not a resource
                throwable = ExternalEntityIsNotAResource("wikidata", ParsedIRI.create("https://www.wikidata.org/entity/Q42"))
            ),
            TypeMismatchTestParameters(
                entityId = "Q42",
                userInput = ParsedIRI.create("https://www.wikidata.org/entity/Q42"),
                methodInvoker = WikidataServiceAdapter::findClassByURI,
                successResponse = responseJson("wikidata/classSuccess"),
                notFoundResponse = responseJson("wikidata/resourceSuccess"), // should fail, because response contains a resource and not a class
                throwable = ExternalEntityIsNotAClass("wikidata", ParsedIRI.create("https://www.wikidata.org/entity/Q42"))
            ),
        ).map(Arguments::of)

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("A42", WikidataServiceAdapter::findResourceByShortForm),
            Arguments.of("A42", WikidataServiceAdapter::findClassByShortForm),
            Arguments.of("A30", WikidataServiceAdapter::findPredicateByShortForm),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/entity/A42"), WikidataServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/entity/A42"), WikidataServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/entity/A30"), WikidataServiceAdapter::findPredicateByURI),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/abc/A42"), WikidataServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/abc/A42"), WikidataServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI.create("https://www.wikidata.org/abc/A30"), WikidataServiceAdapter::findPredicateByURI),
        )
    }
}
