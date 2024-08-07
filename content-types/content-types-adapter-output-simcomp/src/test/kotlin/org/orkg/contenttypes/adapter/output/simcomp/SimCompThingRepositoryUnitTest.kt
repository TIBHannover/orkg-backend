package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.adapter.output.simcomp.json.SimCompJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Visibility

class SimCompThingRepositoryUnitTest {
    private val simCompHostUrl = "https://example.org/simcomp"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule(), SimCompJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val adapter = SimCompThingRepository(objectMapper, httpClient, simCompHostUrl)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(httpClient)
    }

    @Test
    fun `Given a thing id, when fetching list contents, it returns the list`() {
        val id = ThingId("R123")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns simCompThingResponseJson

        val result = adapter.findById(id, ThingType.LIST)
        result.isPresent shouldBe true
        result.get().asClue {
            it.rootId shouldBe ThingId("R465")
            it.subgraph shouldBe listOf(
                GeneralStatement(
                    id = StatementId("S663825"),
                    subject = Resource(
                        id = ThingId("R166714"),
                        label = "Entry",
                        createdAt = OffsetDateTime.parse("2022-02-22T08:01:13.261082+01:00"),
                        classes = emptySet(),
                        createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b"),
                        observatoryId = ObservatoryId.UNKNOWN,
                        extractionMethod = ExtractionMethod.UNKNOWN,
                        organizationId = OrganizationId.UNKNOWN,
                        visibility = Visibility.DEFAULT
                    ),
                    predicate = Predicate(
                        id = Predicates.hasPaper,
                        label = "has paper",
                        createdAt = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00"),
                        createdBy = ContributorId.UNKNOWN
                    ),
                    `object` = Class(
                        id = ThingId("C12457"),
                        label = "Some class",
                        uri = ParsedIRI("https://orkg.org/class/C12457"),
                        createdAt = OffsetDateTime.parse("2022-02-22T08:01:13.261082+01:00"),
                        createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
                    ),
                    createdAt = OffsetDateTime.parse("2022-02-22T08:01:15.253502+01:00"),
                    createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
                ),
                GeneralStatement(
                    id = StatementId("S5436"),
                    subject = Resource(
                        id = ThingId("R56984"),
                        label = "Other resource",
                        createdAt = OffsetDateTime.parse("2022-02-22T08:01:12.709843+01:00"),
                        classes = setOf(ThingId("Entry")),
                        createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b"),
                        observatoryId = ObservatoryId.UNKNOWN,
                        extractionMethod = ExtractionMethod.MANUAL,
                        organizationId = OrganizationId.UNKNOWN,
                        visibility = Visibility.FEATURED
                    ),
                    predicate = Predicate(
                        id = Predicates.hasPaper,
                        label = "has paper",
                        createdAt = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00"),
                        createdBy = ContributorId.UNKNOWN
                    ),
                    `object` = Literal(
                        id = ThingId("L354354"),
                        label = "Some literal",
                        datatype = "xsd:string",
                        createdAt = OffsetDateTime.parse("2022-02-22T08:01:12.709843+01:00"),
                        createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
                    ),
                    createdAt = OffsetDateTime.parse("2023-02-22T08:01:15.253502+01:00"),
                    createdBy = ContributorId("d5416c16-1a45-4aee-8069-be1b6097478b")
                )
            )
        }

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }

        confirmVerified(response)
    }

    @Test
    fun `Given a thing id, when fetching list contents but list is not found, it returns an empty response`() {
        val id = ThingId("R123")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 404

        val result = adapter.findById(id, ThingType.LIST)
        result.isPresent shouldBe false

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify { response.statusCode() }

        confirmVerified(response)
    }

    @Test
    fun `Given a thing id, when fetching list contents but service returns error, it throws an exception`() {
        val id = ThingId("R123")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 500
        every { response.body() } returns "Error message"

        shouldThrow<ServiceUnavailable> { adapter.findById(id, ThingType.LIST) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service returned status 500 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(withArg {
                it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
            }, any<HttpResponse.BodyHandler<String>>())
        }
        verify { response.statusCode() }
        verify { response.body() }

        confirmVerified(response)
    }

    @Test
    fun `Given a thing id, when fetching list contents but a connection error occurs, it throws an exception`() {
        val id = ThingId("R123")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()
        val exception = IOException()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws exception

        shouldThrow<ServiceUnavailable> { adapter.findById(id, ThingType.LIST) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service threw an exception."""
            it.cause shouldBe exception
        }

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }

        confirmVerified(response)
    }
}

private const val simCompThingResponseJson = """{
    "timestamp": "2024-01-26T09:26:34.297967",
    "uuid": "b8e897af-e9c9-46e0-884b-90e2407b2341",
    "payload": {
    "thing": { 
      "id": "116eddfd-d6f3-4a31-a96b-24574b3d62cc",
      "created_at": "2022-02-22T06:02:14.292384Z",
      "updated_at": "2022-02-22T06:02:14.292395Z",
      "thing_type": "LIST",
      "thing_key": "R166718",
      "config": {},
      "data": {
        "rootResource": "R465",
        "statements": [
          {
            "id": "S663825",
            "subject": {
              "id": "R166714",
              "label": "Entry",
              "created_at": "2022-02-22T08:01:13.261082+01:00",
              "classes": [],
              "shared": 1,
              "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b",
              "_class": "resource",
              "observatory_id": "00000000-0000-0000-0000-000000000000",
              "extraction_method": "UNKNOWN",
              "organization_id": "00000000-0000-0000-0000-000000000000",
              "featured": false,
              "unlisted": false
            },
            "predicate": {
              "id": "HasPaper",
              "label": "has paper",
              "created_at": "2021-04-26T16:57:34.745465+02:00",
              "created_by": "00000000-0000-0000-0000-000000000000",
              "_class": "predicate",
              "description": null
            },
            "object": {
              "id": "C12457",
              "label": "Some class",
              "uri": "https://orkg.org/class/C12457",
              "description": "class description",
              "created_at": "2022-02-22T08:01:13.261082+01:00",
              "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b",
              "_class": "class"
            },
            "created_at": "2022-02-22T08:01:15.253502+01:00",
            "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b"
          },
          {
            "id": "S5436",
            "subject": {
              "id": "R56984",
              "label": "Other resource",
              "created_at": "2022-02-22T08:01:12.709843+01:00",
              "classes": [
                "Entry"
              ],
              "shared": 1,
              "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b",
              "_class": "resource",
              "observatory_id": "00000000-0000-0000-0000-000000000000",
              "extraction_method": "MANUAL",
              "organization_id": "00000000-0000-0000-0000-000000000000",
              "visibility": "FEATURED"
            },
            "predicate": {
              "id": "HasPaper",
              "label": "has paper",
              "created_at": "2021-04-26T16:57:34.745465+02:00",
              "created_by": "00000000-0000-0000-0000-000000000000",
              "_class": "predicate",
              "description": null
            },
            "object": {
              "id": "L354354",
              "label": "Some literal",
              "created_at": "2022-02-22T08:01:12.709843+01:00",
              "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b",
              "_class": "literal",
              "datatype": "xsd:string"
            },
            "created_at": "2023-02-22T08:01:15.253502+01:00",
            "created_by": "d5416c16-1a45-4aee-8069-be1b6097478b"
          }
        ]
      }
    }
  }
}"""
