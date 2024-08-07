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

class WikidataRepositoryAdapterUnitTest {
    private val wikidataHostUrl = "https://example.org/wikidata"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val repository = WikidataServiceAdapter(objectMapper, httpClient, wikidataHostUrl)

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
    fun <T> `Given an ontology id and user input, when wikidata returns success, it returns the external object`(
        entityId: String,
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        notFoundResponse: String,
        expectedResult: ExternalThing
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns successResponse

        val result = methodInvoker(repository, "wikidata", userInput)
        result shouldNotBe null
        result shouldBe expectedResult

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=$entityId&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype")
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
    fun <T> `Given an ontology id and user input, when wikidata returns status not found, it returns null`(
        entityId: String,
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        notFoundResponse: String,
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns notFoundResponse

        val result = methodInvoker(repository, "wikidata", userInput)
        result shouldBe null

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=$entityId&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype")
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
    fun <T> `Given an ontology id and user input, when wikidata service is not available, it throws an exception`(
        entityId: String,
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?
    ) {
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        assertThrows<ServiceUnavailable> { methodInvoker(repository, "wikidata", userInput) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """Wikidata service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$wikidataHostUrl/w/api.php?action=wbgetentities&ids=$entityId&format=json&languages=en&props=labels%7Cdescriptions%7Cdatatype")
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
    fun <T> `Given an ontology id and user input, when ontology is invalid, it returns null`(
        entityId: String,
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?
    ) {
        val result = methodInvoker(repository, "not wikidata", userInput)
        result shouldBe null
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        methodInvoker: (WikidataServiceAdapter, String, T) -> ExternalThing?
    ) {
        val result = methodInvoker(repository, "wikidata", userInput)
        result shouldBe null
    }

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Q42",
                "Q42",
                WikidataServiceAdapter::findResourceByShortForm,
                wikidataItemSuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            Arguments.of(
                "Q42",
                "Q42",
                WikidataServiceAdapter::findClassByShortForm,
                wikidataItemSuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            Arguments.of(
                "P30",
                "P30",
                WikidataServiceAdapter::findPredicateByShortForm,
                wikidataPropertySuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/P30"),
                    label = "continent",
                    description = "continent of which the subject is a part"
                )
            ),
            Arguments.of(
                "Q42",
                ParsedIRI("https://www.wikidata.org/entity/Q42"),
                WikidataServiceAdapter::findResourceByURI,
                wikidataItemSuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            Arguments.of(
                "Q42",
                ParsedIRI("https://www.wikidata.org/entity/Q42"),
                WikidataServiceAdapter::findClassByURI,
                wikidataItemSuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/Q42"),
                    label = "Douglas Adams",
                    description = "English author and humourist (1952-2001)"
                )
            ),
            Arguments.of(
                "P30",
                ParsedIRI("https://www.wikidata.org/entity/P30"),
                WikidataServiceAdapter::findPredicateByURI,
                wikidataPropertySuccessResponseJson,
                wikidataNotFoundResponseJson,
                ExternalThing(
                    uri = ParsedIRI("https://www.wikidata.org/entity/P30"),
                    label = "continent",
                    description = "continent of which the subject is a part"
                )
            )
        )

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("A42", WikidataServiceAdapter::findResourceByShortForm),
            Arguments.of("A42", WikidataServiceAdapter::findClassByShortForm),
            Arguments.of("A30", WikidataServiceAdapter::findPredicateByShortForm),
            Arguments.of(ParsedIRI("https://www.wikidata.org/entity/A42"), WikidataServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI("https://www.wikidata.org/entity/A42"), WikidataServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI("https://www.wikidata.org/entity/A30"), WikidataServiceAdapter::findPredicateByURI),
            Arguments.of(ParsedIRI("https://www.wikidata.org/abc/A42"), WikidataServiceAdapter::findResourceByURI),
            Arguments.of(ParsedIRI("https://www.wikidata.org/abc/A42"), WikidataServiceAdapter::findClassByURI),
            Arguments.of(ParsedIRI("https://www.wikidata.org/abc/A30"), WikidataServiceAdapter::findPredicateByURI),
        )
    }
}

private const val wikidataItemSuccessResponseJson = """{
  "entities": {
    "Q42": {
      "type": "item",
      "id": "Q42",
      "labels": {
        "en": {
          "language": "en",
          "value": "Douglas Adams"
        }
      },
      "descriptions": {
        "en": {
          "language": "en",
          "value": "English author and humourist (1952-2001)"
        }
      }
    }
  },
  "success": 1
}"""

private const val wikidataPropertySuccessResponseJson = """{
  "entities": {
    "P30": {
      "type": "property",
      "datatype": "wikibase-item",
      "id": "P30",
      "labels": {
        "en": {
          "language": "en",
          "value": "continent"
        }
      },
      "descriptions": {
        "en": {
          "language": "en",
          "value": "continent of which the subject is a part"
        }
      }
    }
  },
  "success": 1
}"""

private const val wikidataNotFoundResponseJson = """{
  "error": {
    "code": "no-such-entity",
    "info": "Could not find an entity with the ID \"Q42\".",
    "id": "Q42",
    "messages": [
      {
        "name": "wikibase-api-no-such-entity",
        "parameters": [
          "Q42"
        ],
        "html": {
          "*":"Could not find an entity with the ID \"Q42\"."
        }
      }
    ],
    "*": "See https://www.wikidata.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/postorius/lists/mediawiki-api-announce.lists.wikimedia.org/&gt; for notice of API deprecations and breaking changes."
  },
  "servedby": "mw-api-ext.eqiad.main-5dff684f6-jrlxv"
}"""
