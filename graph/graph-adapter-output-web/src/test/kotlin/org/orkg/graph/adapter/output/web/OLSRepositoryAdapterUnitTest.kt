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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.stream.Stream
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.ExternalThing
import org.springframework.web.util.UriComponentsBuilder

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
                it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
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
            httpClient.send(withArg {
                it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 1) { response.statusCode() }
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun <T : Any> `Given an ontology id and user input, when ols service is not available, it throws an exception`(
        entityType: String,
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
                it.uri() shouldBe userInput.toUri(ontologyId, entityType)
                it.headers().map() shouldContainAll mapOf(
                    "Accept" to listOf("application/json")
                )
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 2) { response.statusCode() }
        verify(exactly = 1) { response.body() }
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
                olsIndividualSuccessResponseJson,
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
                olsIndividualSuccessResponseJson,
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
                olsTermSuccessResponseJson,
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
                olsTermSuccessResponseJson,
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
                olsPropertySuccessResponseJson,
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
                olsPropertySuccessResponseJson,
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

private const val olsIndividualSuccessResponseJson = """{
  "page": 0,
  "numElements": 1,
  "totalPages": 1,
  "totalElements": 1,
  "elements": [
    {
      "appearsIn": [
        "abcd"
      ],
      "curie": "AbsenceObservation",
      "definedBy": [
        "abcd"
      ],
      "definition": [
        "A record describing an output of an observation process with indication of the absence of an observation",
        "A record describing an output of an observation process with indication of the absence of an observation"
      ],
      "definitionProperty": [
        "http://www.w3.org/2000/01/rdf-schema#comment",
        "http://www.w3.org/2004/02/skos/core#definition"
      ],
      "directAncestor": [
        "http://rs.tdwg.org/abcd/terms/RecordBasis"
      ],
      "directParent": [
        "http://rs.tdwg.org/abcd/terms/RecordBasis"
      ],
      "hasDirectChildren": false,
      "hasDirectParents": true,
      "hasHierarchicalChildren": false,
      "hasHierarchicalParents": false,
      "imported": false,
      "iri": "http://rs.tdwg.org/abcd/terms/AbsenceObservation",
      "isDefiningOntology": true,
      "isObsolete": false,
      "label": [
        "AbsenceObservation"
      ],
      "linkedEntities": {
        "http://www.w3.org/2004/02/skos/core#definition": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 31,
          "hasLocalDefinition": true,
          "label": [
            "definition"
          ],
          "curie": "definition",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#comment": {
          "definedBy": [
            "prov"
          ],
          "numAppearsIn": 53,
          "hasLocalDefinition": false,
          "label": [
            "comment"
          ],
          "curie": "comment",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://purl.org/dc/terms/modified": {
          "definedBy": [
            "dcterms"
          ],
          "numAppearsIn": 43,
          "hasLocalDefinition": true,
          "label": [
            "Date Modified"
          ],
          "curie": "dcterms:/modified",
          "type": [
            "dataProperty",
            "property",
            "entity"
          ]
        },
        "http://rs.tdwg.org/dwc/terms/attributes/status": {
          "numAppearsIn": 2,
          "hasLocalDefinition": true,
          "label": [
            "status"
          ],
          "curie": "status",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": {
          "numAppearsIn": 25,
          "hasLocalDefinition": false,
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ],
          "label": [
            "isDefinedBy"
          ],
          "curie": "isDefinedBy"
        },
        "http://purl.org/dc/terms/issued": {
          "definedBy": [
            "dcterms"
          ],
          "numAppearsIn": 35,
          "hasLocalDefinition": true,
          "label": [
            "Date Issued"
          ],
          "curie": "dcterms:/issued",
          "type": [
            "dataProperty",
            "property",
            "entity"
          ]
        },
        "http://rs.tdwg.org/abcd/terms/RecordBasis": {
          "definedBy": [
            "abcd"
          ],
          "numAppearsIn": 1,
          "hasLocalDefinition": true,
          "label": [
            "Record Basis"
          ],
          "curie": "RecordBasis",
          "type": [
            "class",
            "entity"
          ]
        }
      },
      "numDescendants": 0,
      "numHierarchicalDescendants": 0,
      "ontologyId": "abcd",
      "ontologyIri": "http://rs.tdwg.org/abcd/terms/",
      "ontologyPreferredPrefix": "abcd",
      "searchableAnnotationValues": [
        false
      ],
      "shortForm": "AbsenceObservation",
      "type": [
        "individual",
        "entity"
      ],
      "http://purl.org/dc/terms/issued": "2019-01-31",
      "http://purl.org/dc/terms/modified": "2019-01-31",
      "http://rs.tdwg.org/dwc/terms/attributes/status": "recommended",
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type": [
        "http://www.w3.org/2002/07/owl#NamedIndividual",
        "http://rs.tdwg.org/abcd/terms/RecordBasis"
      ],
      "http://www.w3.org/2000/01/rdf-schema#comment": "A record describing an output of an observation process with indication of the absence of an observation",
      "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": "http://rs.tdwg.org/abcd/terms/",
      "http://www.w3.org/2004/02/skos/core#definition": "A record describing an output of an observation process with indication of the absence of an observation"
    }
  ],
  "facetFieldsToCounts": {}
}"""

@Language("JSON")
private const val olsTermSuccessResponseJson = """{
  "page": 0,
  "numElements": 1,
  "totalPages": 1,
  "totalElements": 1,
  "elements": [
    {
      "appearsIn": [
        "cf",
        "meth",
        "afo",
        "skos",
        "stw",
        "linsearch",
        "modsci",
        "skosxl"
      ],
      "curie": "Collection",
      "definedBy": [
        "stw",
        "skos"
      ],
      "definition": [
        "A meaningful collection of concepts."
      ],
      "definitionProperty": "http://www.w3.org/2004/02/skos/core#definition",
      "hasDirectChildren": true,
      "hasDirectParents": false,
      "hasHierarchicalChildren": true,
      "hasHierarchicalParents": false,
      "imported": false,
      "iri": "http://www.w3.org/2004/02/skos/core#Collection",
      "isDefiningOntology": true,
      "isObsolete": false,
      "isPreferredRoot": false,
      "label": [
        "Collection"
      ],
      "linkedEntities": {
        "http://www.w3.org/2004/02/skos/core#definition": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 31,
          "hasLocalDefinition": true,
          "label": [
            "definition"
          ],
          "curie": "definition",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://www.w3.org/2004/02/skos/core#Concept": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 14,
          "hasLocalDefinition": true,
          "label": [
            "Concept"
          ],
          "curie": "Concept",
          "type": [
            "class",
            "entity"
          ]
        },
        "http://www.w3.org/2004/02/skos/core#scopeNote": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 18,
          "hasLocalDefinition": true,
          "label": [
            "scope note"
          ],
          "curie": "scopeNote",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": {
          "numAppearsIn": 25,
          "hasLocalDefinition": false,
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ],
          "label": [
            "isDefinedBy"
          ],
          "curie": "isDefinedBy"
        },
        "http://www.w3.org/2004/02/skos/core#ConceptScheme": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 11,
          "hasLocalDefinition": true,
          "label": [
            "Concept Scheme"
          ],
          "curie": "ConceptScheme",
          "type": [
            "class",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#label": {
          "definedBy": [
            "prov"
          ],
          "numAppearsIn": 53,
          "hasLocalDefinition": false,
          "label": [
            "label"
          ],
          "curie": "label",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        }
      },
      "numDescendants": 1,
      "numHierarchicalDescendants": 1,
      "ontologyId": "skos",
      "ontologyIri": "http://www.w3.org/2004/02/skos/core",
      "ontologyPreferredPrefix": "SKOS",
      "searchableAnnotationValues": [
        "A meaningful collection of concepts.",
        "Labelled collections can be used where you would like a set of concepts to be displayed under a 'node label' in the hierarchy.",
        false
      ],
      "shortForm": "Collection",
      "type": [
        "class",
        "entity"
      ],
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://www.w3.org/2002/07/owl#Class",
      "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": "http://www.w3.org/2004/02/skos/core",
      "http://www.w3.org/2000/01/rdf-schema#label": "Collection",
      "http://www.w3.org/2002/07/owl#disjointWith": [
        "http://www.w3.org/2004/02/skos/core#Concept",
        "http://www.w3.org/2004/02/skos/core#ConceptScheme"
      ],
      "http://www.w3.org/2004/02/skos/core#definition": "A meaningful collection of concepts.",
      "http://www.w3.org/2004/02/skos/core#scopeNote": "Labelled collections can be used where you would like a set of concepts to be displayed under a 'node label' in the hierarchy."
    }
  ],
  "facetFieldsToCounts": {}
}"""

private const val olsPropertySuccessResponseJson = """{
  "page": 0,
  "numElements": 1,
  "totalPages": 1,
  "totalElements": 1,
  "elements": [
    {
      "appearsIn": [
        "abcd"
      ],
      "curie": "hasCountry",
      "definedBy": [
        "abcd"
      ],
      "definition": [
        "Property to connect an instance of a class to a Country.",
        "Property to connect an instance of a class to a Country."
      ],
      "definitionProperty": [
        "http://www.w3.org/2000/01/rdf-schema#comment",
        "http://www.w3.org/2004/02/skos/core#definition"
      ],
      "hasDirectChildren": false,
      "hasDirectParents": false,
      "hasHierarchicalChildren": false,
      "hasHierarchicalParents": false,
      "imported": false,
      "iri": "http://rs.tdwg.org/abcd/terms/hasCountry",
      "isDefiningOntology": true,
      "isObsolete": false,
      "isPreferredRoot": false,
      "label": [
        "has Country"
      ],
      "linkedEntities": {
        "http://www.w3.org/2004/02/skos/core#definition": {
          "definedBy": [
            "stw",
            "skos"
          ],
          "numAppearsIn": 31,
          "hasLocalDefinition": true,
          "label": [
            "definition"
          ],
          "curie": "definition",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://rs.tdwg.org/dwc/terms/attributes/status": {
          "numAppearsIn": 2,
          "hasLocalDefinition": true,
          "label": [
            "status"
          ],
          "curie": "status",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#range": {
          "numAppearsIn": 1,
          "hasLocalDefinition": false,
          "type": [
            "property",
            "entity"
          ],
          "label": [
            "range"
          ],
          "curie": "range"
        },
        "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": {
          "numAppearsIn": 25,
          "hasLocalDefinition": false,
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ],
          "label": [
            "isDefinedBy"
          ],
          "curie": "isDefinedBy"
        },
        "http://rs.tdwg.org/abcd/terms/Country": {
          "definedBy": [
            "abcd"
          ],
          "numAppearsIn": 1,
          "hasLocalDefinition": true,
          "label": [
            "Country"
          ],
          "curie": "Country",
          "type": [
            "class",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#comment": {
          "definedBy": [
            "prov"
          ],
          "numAppearsIn": 53,
          "hasLocalDefinition": false,
          "label": [
            "comment"
          ],
          "curie": "comment",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        },
        "http://purl.org/dc/terms/modified": {
          "definedBy": [
            "dcterms"
          ],
          "numAppearsIn": 43,
          "hasLocalDefinition": true,
          "label": [
            "Date Modified"
          ],
          "curie": "dcterms:/modified",
          "type": [
            "dataProperty",
            "property",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#domain": {
          "numAppearsIn": 1,
          "hasLocalDefinition": false,
          "type": [
            "property",
            "entity"
          ],
          "label": [
            "domain"
          ],
          "curie": "domain"
        },
        "http://purl.org/dc/terms/issued": {
          "definedBy": [
            "dcterms"
          ],
          "numAppearsIn": 35,
          "hasLocalDefinition": true,
          "label": [
            "Date Issued"
          ],
          "curie": "dcterms:/issued",
          "type": [
            "dataProperty",
            "property",
            "entity"
          ]
        },
        "http://www.w3.org/2000/01/rdf-schema#label": {
          "definedBy": [
            "prov"
          ],
          "numAppearsIn": 53,
          "hasLocalDefinition": false,
          "label": [
            "label"
          ],
          "curie": "label",
          "type": [
            "property",
            "annotationProperty",
            "entity"
          ]
        }
      },
      "numDescendants": 0,
      "numHierarchicalDescendants": 0,
      "ontologyId": "abcd",
      "ontologyIri": "http://rs.tdwg.org/abcd/terms/",
      "ontologyPreferredPrefix": "abcd",
      "searchableAnnotationValues": [
        false
      ],
      "shortForm": "hasCountry",
      "type": [
        "property",
        "objectProperty",
        "entity"
      ],
      "http://purl.org/dc/terms/issued": "2019-01-31",
      "http://purl.org/dc/terms/modified": "2019-01-31",
      "http://rs.tdwg.org/dwc/terms/attributes/status": "recommended",
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://www.w3.org/2002/07/owl#ObjectProperty",
      "http://www.w3.org/2000/01/rdf-schema#comment": "Property to connect an instance of a class to a Country.",
      "http://www.w3.org/2000/01/rdf-schema#domain": "http://www.w3.org/2002/07/owl#Thing",
      "http://www.w3.org/2000/01/rdf-schema#isDefinedBy": "http://rs.tdwg.org/abcd/terms/",
      "http://www.w3.org/2000/01/rdf-schema#label": "has Country",
      "http://www.w3.org/2000/01/rdf-schema#range": "http://rs.tdwg.org/abcd/terms/Country",
      "http://www.w3.org/2004/02/skos/core#definition": "Property to connect an instance of a class to a Country."
    }
  ],
  "facetFieldsToCounts": {}
}"""
