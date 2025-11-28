package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.thingResponseFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.ThingUseCases
import org.orkg.graph.testing.asciidoc.allowedVisibilityFilterValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.oneOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [ThingController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ThingController::class])
internal class ThingControllerUnitTest : MockMvcBaseTest("things") {
    @MockkBean
    private lateinit var thingService: ThingUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun findById() {
        val thing = createResource()

        every { thingService.findById(any()) } returns Optional.of(thing)
        every { statementService.countIncomingStatementsById(thing.id) } returns 23

        documentedGetRequestTo("/api/things/{id}", thing.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDocument {
                summary("Fetching things")
                description(
                    """
                    A `GET` request provides information about a thing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the thing to retrieve.")
                )
                responseFields<ThingRepresentation>(
                    oneOf(
                        "_class",
                        mapOf(
                            "class" to ClassRepresentation::class,
                            "resource" to ResourceRepresentation::class,
                            "literal" to LiteralRepresentation::class,
                            "predicate" to PredicateRepresentation::class,
                        )
                    )
                )
                throws(ThingNotFound::class)
            }

        verify(exactly = 1) { thingService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(thing.id) }
    }

    @Test
    @DisplayName("Given several things, when filtering by no parameters, then status is 200 OK and things are returned")
    fun getPaged() {
        every { thingService.findAll(any()) } returns pageOf(createResource())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/things")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { thingService.findAll(any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several things, when they are fetched with all possible filtering parameters, then status is 200 OK and things are returned")
    fun findAll() {
        every { thingService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createResource())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val includeClasses = setOf(ThingId("Include1"), ThingId("Include2"))
        val excludeClasses = setOf(ThingId("Exclude1"), ThingId("Exclude2"))
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")

        documentedGetRequestTo("/api/things")
            .param("q", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .param("include", includeClasses.joinToString(separator = ","))
            .param("exclude", excludeClasses.joinToString(separator = ","))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDocument {
                summary("Listing things")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<things-fetch,things>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("visibility").description("""Filter for visibility. Either of $allowedVisibilityFilterValues. (optional)""").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this thing. (optional)").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned thing can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned thing can have. (optional)").optional(),
                    parameterWithName("include").description("A comma-separated set of classes that the thing must have. The ids `Resource`, `Class`, `Predicate` and `Literal` can be used to filter for a general type of object. (optional)").optional(),
                    parameterWithName("exclude").description("A comma-separated set of classes that the thing must not have. The ids `Resource`, `Class`, `Predicate` and `Literal` can be used to filter for a general type of object. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the thing belongs to. (optional)").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the thing belongs to. (optional)").optional(),
                )
                pagedResponseFields<ThingRepresentation>(thingResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            thingService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given several things, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { thingService.findAll(any()) } throws exception

        get("/api/things")
            .param("sort", "unknown")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) { thingService.findAll(any()) }
    }
}
