package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [ResearchFieldController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ResearchFieldController::class])
internal class ResearchFieldControllerUnitTest : MockMvcBaseTest("research-fields") {
    @MockkBean
    private lateinit var researchFieldService: ResearchFieldUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @DisplayName("Given a research field, when fetched by id, then status is 200 OK and research field is returned")
    fun findById() {
        val resource = createResource(classes = setOf(Classes.researchField))
        every { researchFieldService.findById(any()) } returns Optional.of(resource)
        every { statementService.countIncomingStatementsById(resource.id) } returns 23

        documentedGetRequestTo("/api/research-fields/{id}", resource.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDocument {
                summary("Fetching research fields")
                description(
                    """
                    A `GET` request provides information about a resource.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the research field to retrieve."),
                )
                responseFields<ResourceRepresentation>(resourceResponseFields())
                throws(ResearchFieldNotFound::class)
            }

        verify(exactly = 1) { researchFieldService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(resource.id) }
    }

    @Test
    @DisplayName("Given several research fields, when filtering by no parameters, then status is 200 OK and research fields are returned")
    fun getPaged() {
        every { researchFieldService.findAll(any()) } returns pageOf(createResource(classes = setOf(Classes.researchField)))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { researchFieldService.findAll(any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several research fields, when they are fetched with all possible filtering parameters, then status is 200 OK and research fields are returned")
    fun findAll() {
        every { researchFieldService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createResource())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
        val researchProblem = ThingId("R456")
        val includeSubproblems = true

        documentedGetRequestTo("/api/research-fields")
            .param("visibility", visibility.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_problem", researchProblem.toString())
            .param("include_subproblems", includeSubproblems.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDocument {
                summary("Listing research fields")
                description(
                    """
                    A `GET` request lists all research-fields.
                    """
                )
                pagedQueryParameters(
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this resource. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("research_problem").description("Filter for research problem id. (optional)").optional(),
                    parameterWithName("include_subproblems").description("Flag for whether subproblems are included in the search or not. (optional, default: false)").optional(),
                )
                pagedResponseFields<ResourceRepresentation>(resourceResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            researchFieldService.findAll(
                pageable = any(),
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchProblem = researchProblem,
                includeSubproblems = includeSubproblems,
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }
}
