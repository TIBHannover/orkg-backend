package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.Assets.responseJson
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.TestBodyPublisher
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepositoryAdapter
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingAddRequest
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.adapter.output.simcomp.json.SimCompJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonConfig
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonData
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
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

internal class SimCompThingRepositoryAdapterUnitTest : MockkBaseTest {
    private val simCompHostUrl = "https://example.org/simcomp"
    private val simCompApiKey = "TEST_API_KEY"
    private val userAgent = "test user agent"
    private val httpClient: HttpClient = mockk()
    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .registerModules(CommonJacksonModule(), GraphJacksonModule(), SimCompJacksonModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val adapter =
        SimCompThingRepositoryAdapter(objectMapper, httpClient, ::TestBodyPublisher, userAgent, simCompHostUrl, simCompApiKey)

    @Test
    fun `Given a thing id, when fetching list contents, it returns the list`() {
        val id = ThingId("R123")
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 200
        every { response.body() } returns responseJson("simcomp/thingResponse")

        val result = adapter.findById(id, ThingType.LIST)
        result.isPresent shouldBe true
        result.get().asClue {
            it.id shouldBe UUID.fromString("116eddfd-d6f3-4a31-a96b-24574b3d62cc")
            it.createdAt shouldBe LocalDateTime.parse("2022-02-22T06:02:14.292384")
            it.updatedAt shouldBe LocalDateTime.parse("2022-02-22T06:02:14.292395")
            it.thingType shouldBe ThingType.LIST
            it.thingKey shouldBe ThingId("R166718")
            objectMapper.treeToValue(it.config, Map::class.java) shouldBe emptyMap<String, Any>()
            val data = it.toPublishedContentType(objectMapper)
            data.rootId shouldBe ThingId("R465")
            data.subgraph shouldBe listOf(
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
                        uri = ParsedIRI.create("https://orkg.org/class/C12457"),
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
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify(exactly = 1) { response.statusCode() }
        verify(exactly = 1) { response.body() }
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
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
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
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$simCompHostUrl/thing/?thing_type=LIST&thing_key=$id")
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
        verify { response.body() }
    }

    @Test
    fun `Given a thing id, when fetching list contents but a connection error occurs, it throws an exception`() {
        val id = ThingId("R123")
        val exception = IOException()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws exception

        shouldThrow<ServiceUnavailable> { adapter.findById(id, ThingType.LIST) }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service threw an exception."""
            it.cause shouldBe exception
        }

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
    }

    @Test
    fun `Given a thing id, thing type and contents, when saving paper contents, it returns success`() {
        val id = ThingId("R123")
        val type = ThingType.PAPER_VERSION
        val data = mapOf(
            "rootResource" to ThingId("C1"),
            "statements" to listOf(createStatement())
        )
        val config = emptyMap<String, Any>()
        val request = ThingAddRequest(
            thingType = type,
            thingKey = id,
            config = config,
            data = data
        )
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 201

        adapter.save(id, type, data, config)

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$simCompHostUrl/thing/")
                    it.headers().map()[CONTENT_TYPE]!!.single() shouldBe APPLICATION_JSON_VALUE
                    it.headers().map()[USER_AGENT]!!.single() shouldBe userAgent
                    it.bodyPublisher().isPresent shouldBe true
                    it.bodyPublisher().get().shouldBeInstanceOf<TestBodyPublisher>().asClue { bodyPublisher ->
                        bodyPublisher.content shouldBe objectMapper.writeValueAsString(request)
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
    }

    @Test
    fun `Given a thing id, thing type and contents, when saving paper contents but service returns error, it throws an exception`() {
        val id = ThingId("R123")
        val type = ThingType.PAPER_VERSION
        val data = mapOf(
            "rootResource" to ThingId("C1"),
            "statements" to listOf(createStatement())
        )
        val config = emptyMap<String, Any>()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 403
        every { response.body() } returns "Error message"

        shouldThrow<ServiceUnavailable> {
            adapter.save(id, type, data, config)
        }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service returned status 403 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg { it.uri() shouldBe URI.create("$simCompHostUrl/thing/") },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
        verify { response.body() }
    }

    @Test
    fun `Given a thing id, thing type and contents, when saving paper contents but a connection error occurs, it throws an exception`() {
        val id = ThingId("R123")
        val type = ThingType.PAPER_VERSION
        val data = mapOf(
            "rootResource" to ThingId("C1"),
            "statements" to listOf(createStatement())
        )
        val config = emptyMap<String, Any>()
        val exception = IOException()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws exception

        shouldThrow<ServiceUnavailable> {
            adapter.save(id, type, data, config)
        }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service threw an exception."""
            it.cause shouldBe exception
        }

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
    }

    @Test
    fun `Given a thing id, thing type and contents, when updating draft comparison contents, it returns success`() {
        val id = ThingId("R123")
        val type = ThingType.DRAFT_COMPARISON
        val data = createComparisonData()
        val config = createComparisonConfig()
        val request = ThingAddRequest(
            thingType = type,
            thingKey = id,
            config = config,
            data = data
        )
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 204

        adapter.update(id, type, data, config)

        verify(exactly = 1) {
            httpClient.send(
                withArg {
                    it.uri() shouldBe URI.create("$simCompHostUrl/thing/")
                    it.headers().map()[CONTENT_TYPE]!!.single() shouldBe APPLICATION_JSON_VALUE
                    it.headers().map()[USER_AGENT]!!.single() shouldBe userAgent
                    it.headers().map()["X-API-KEY"]!!.single() shouldBe simCompApiKey
                    it.bodyPublisher().isPresent shouldBe true
                    it.bodyPublisher().get().shouldBeInstanceOf<TestBodyPublisher>().asClue { bodyPublisher ->
                        bodyPublisher.content shouldBe objectMapper.writeValueAsString(request)
                    }
                },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
    }

    @Test
    fun `Given a thing id, thing type and contents, when updating draft comparison contents but service returns error, it throws an exception`() {
        val id = ThingId("R123")
        val type = ThingType.DRAFT_COMPARISON
        val data = createComparisonData()
        val config = createComparisonConfig()
        // Mock HttpClient dsl
        val response = mockk<HttpResponse<String>>()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns response
        every { response.statusCode() } returns 403
        every { response.body() } returns "Error message"

        shouldThrow<ServiceUnavailable> {
            adapter.update(id, type, data, config)
        }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service returned status 403 with error response: "Error message"."""
        }

        verify(exactly = 1) {
            httpClient.send(
                withArg { it.uri() shouldBe URI.create("$simCompHostUrl/thing/") },
                any<HttpResponse.BodyHandler<String>>()
            )
        }
        verify { response.statusCode() }
        verify { response.body() }
    }

    @Test
    fun `Given a thing id, thing type and contents, when updating draft comparison contents but a connection error occurs, it throws an exception`() {
        val id = ThingId("R123")
        val type = ThingType.DRAFT_COMPARISON
        val data = createComparisonData()
        val config = createComparisonConfig()
        val exception = IOException()

        every { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } throws exception

        shouldThrow<ServiceUnavailable> {
            adapter.update(id, type, data, config)
        }.asClue {
            it.message shouldBe "Service unavailable."
            it.internalMessage shouldBe """SimComp service threw an exception."""
            it.cause shouldBe exception
        }

        verify(exactly = 1) { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) }
    }
}
