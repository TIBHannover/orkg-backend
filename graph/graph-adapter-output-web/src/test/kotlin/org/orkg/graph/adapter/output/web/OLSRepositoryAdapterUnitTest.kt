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
import org.springframework.web.util.UriComponentsBuilder

class OLSRepositoryAdapterUnitTest {
    private val olsHostUrl = "https://example.org/ols"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val repository = OLSServiceAdapter(objectMapper, httpClient, olsHostUrl)

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
    fun <T> `Given an ontology id and user input, when ols returns success, it returns the external object`(
        entityId: String,
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?,
        successResponse: String,
        expectedResult: ExternalThing
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
            httpClient.send(withArg {
                it.uri() shouldBe if (userInput is ParsedIRI) {
                    // using UriComponentsBuilder because of different escaping implementations between UriComponentsBuilder and URLEncoder
                    UriComponentsBuilder.fromHttpUrl(olsHostUrl)
                        .path("/ontologies/$ontologyId/terms")
                        .queryParam("iri", userInput.toString())
                        .build()
                        .toUri()
                } else {
                    URI.create("$olsHostUrl/ontologies/$ontologyId/terms?short_form=$userInput")
                }
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
    fun <T> `Given an ontology id and user input, when ols returns status not found, it returns null`(
        entityId: String,
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
            httpClient.send(withArg {
                it.uri() shouldBe if (userInput is ParsedIRI) {
                    // using UriComponentsBuilder because of different escaping implementations between UriComponentsBuilder and URLEncoder
                    UriComponentsBuilder.fromHttpUrl(olsHostUrl)
                        .path("/ontologies/$ontologyId/terms")
                        .queryParam("iri", userInput.toString())
                        .build()
                        .toUri()
                } else {
                    URI.create("$olsHostUrl/ontologies/$ontologyId/terms?short_form=$userInput")
                }
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
    fun <T> `Given an ontology id and user input, when ols service is not available, it throws an exception`(
        entityId: String,
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?
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
            httpClient.send(withArg {
                it.uri() shouldBe if (userInput is ParsedIRI) {
                    // using UriComponentsBuilder because of different escaping implementations between UriComponentsBuilder and URLEncoder
                    UriComponentsBuilder.fromHttpUrl(olsHostUrl)
                        .path("/ontologies/$ontologyId/terms")
                        .queryParam("iri", userInput.toString())
                        .build()
                        .toUri()
                } else {
                    URI.create("$olsHostUrl/ontologies/$ontologyId/terms?short_form=$userInput")
                }
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
    @MethodSource("invalidInputs")
    fun <T> `Given an ontology id and user input, when input is invalid, it returns null`(
        userInput: T,
        ontologyId: String,
        methodInvoker: (OLSServiceAdapter, String, T) -> ExternalThing?
    ) {
        val result = methodInvoker(repository, ontologyId, userInput)
        result shouldBe null
    }

    companion object {
        @JvmStatic
        fun validInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Collection",
                "Collection",
                "skos",
                OLSServiceAdapter::findClassByShortForm,
                olsSuccessResponseJson,
                ExternalThing(
                    uri = ParsedIRI("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            ),
            Arguments.of(
                "Collection",
                ParsedIRI("https://sws.geonames.org/2950159"),
                "skos",
                OLSServiceAdapter::findClassByURI,
                olsSuccessResponseJson,
                ExternalThing(
                    uri = ParsedIRI("http://www.w3.org/2004/02/skos/core#Collection"),
                    label = "Collection",
                    description = "A meaningful collection of concepts."
                )
            )
        )

        @JvmStatic
        fun invalidInputs(): Stream<Arguments> = Stream.of(
            Arguments.of("abc", "!invalid", OLSServiceAdapter::findClassByShortForm),
            Arguments.of(ParsedIRI("https://www.geonames.org/abc"), "!invalid", OLSServiceAdapter::findClassByURI),
        )
    }
}

private const val olsSuccessResponseJson = """{
  "_embedded": {
    "terms": [
      {
        "iri": "http://www.w3.org/2004/02/skos/core#Collection",
        "label": "Collection",
        "description": [
          "A meaningful collection of concepts."
        ],
        "annotation": {
          "isDefinedBy": [
            "http://www.w3.org/2004/02/skos/core"
          ],
          "scope note": [
            "Labelled collections can be used where you would like a set of concepts to be displayed under a 'node label' in the hierarchy."
          ]
        },
        "synonyms": null,
        "ontology_name": "skos",
        "ontology_prefix": "SKOS",
        "ontology_iri": "http://www.w3.org/2004/02/skos/core",
        "is_obsolete": false,
        "term_replaced_by": null,
        "is_defining_ontology": true,
        "has_children": true,
        "is_root": true,
        "short_form": "Collection",
        "obo_id": null,
        "in_subset": null,
        "obo_definition_citation": null,
        "obo_xref": null,
        "obo_synonym": null,
        "is_preferred_root": false,
        "_links": {
          "self": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection"
          },
          "children": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection/children"
          },
          "descendants": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection/descendants"
          },
          "hierarchicalChildren": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection/hierarchicalChildren"
          },
          "hierarchicalDescendants": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection/hierarchicalDescendants"
          },
          "graph": {
            "href": "https://example.org/ols/api/ontologies/skos/terms/http%253A%252F%252Fwww.w3.org%252F2004%252F02%252Fskos%252Fcore%2523Collection/graph"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "https://service.tib.eu/ts4tib/api/ontologies/skos/terms?short_form=Collection"
    }
  },
  "page": {
    "size": 0,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}"""
