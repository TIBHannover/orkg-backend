package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.InvalidFilterConfig
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.SearchFilter.Value
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ObservatoryResourceController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ObservatoryResourceController::class])
internal class ObservatoryResourceControllerUnitTest : MockMvcBaseTest("observatory-resources") {
    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    fun `Given an observatory id, when no parameters are specified and service succeeds, then status is 200 OK and papers are returned`() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val paperResource = createResource(
            observatoryId = id,
            classes = setOf(Classes.paper)
        )
        every {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, emptyList(), VisibilityFilter.ALL_LISTED, any())
        } returns PageImpl(listOf(paperResource))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        get("/api/observatories/{id}/papers", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")

        verify(exactly = 1) {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, emptyList(), VisibilityFilter.ALL_LISTED, any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given an observatory id, when visibility is specified and service succeeds, then status is 200 OK and papers are returned`() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val paperResource = createResource(
            observatoryId = id,
            classes = setOf(Classes.paper),
            visibility = Visibility.FEATURED
        )
        every {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, emptyList(), VisibilityFilter.FEATURED, any())
        } returns PageImpl(listOf(paperResource))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        get("/api/observatories/{id}/papers", id).param("visibility", "FEATURED")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")

        verify(exactly = 1) {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, emptyList(), VisibilityFilter.FEATURED, any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given an observatory id, when filters are specified and service succeeds, then status is 200 OK and papers are returned`() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val paperResource = createResource(
            observatoryId = id,
            classes = setOf(Classes.paper),
            visibility = Visibility.FEATURED
        )
        val filterConfig = listOf(
            SearchFilter(
                path = listOf(ThingId("P105027")),
                range = Classes.string,
                values = setOf(Value(Operator.EQ, "yes")),
                exact = true
            ),
            SearchFilter(
                path = listOf(Predicates.hasResearchProblem),
                range = Classes.resources,
                values = setOf(Value(Operator.EQ, "R1234")),
                exact = false
            )
        )
        val encodedFilterConfig = objectMapper.writeValueAsString(filterConfig)

        every {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, filterConfig, VisibilityFilter.ALL_LISTED, any())
        } returns PageImpl(listOf(paperResource))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        get("/api/observatories/{id}/papers", id)
            .param("filter_config", encodedFilterConfig)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")

        verify(exactly = 1) {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, filterConfig, VisibilityFilter.ALL_LISTED, any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given an observatory id, when filters and visibility are specified and service succeeds, then status is 200 OK and papers are returned")
    fun findAllPapers() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val paperResource = createResource(
            observatoryId = id,
            classes = setOf(Classes.paper),
            visibility = Visibility.FEATURED
        )
        val filterConfig = listOf(
            SearchFilter(
                path = listOf(ThingId("P105027")),
                range = Classes.string,
                values = setOf(Value(Operator.EQ, "yes")),
                exact = true
            ),
            SearchFilter(
                path = listOf(Predicates.hasResearchProblem),
                range = Classes.resources,
                values = setOf(Value(Operator.EQ, "R1234")),
                exact = false
            )
        )
        val encodedFilterConfig = objectMapper.writeValueAsString(filterConfig)

        every {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, filterConfig, VisibilityFilter.FEATURED, any())
        } returns PageImpl(listOf(paperResource))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/observatories/{id}/papers", id)
            .param("visibility", VisibilityFilter.FEATURED.name)
            .param("filter_config", encodedFilterConfig)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDocument {
                tag("Observatories")
                summary("Listing papers of observatories")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of paper <<resources-fetch,resources>>.

                    TIP: This call features filter configs, check the chapter <<filter-config>> for more information on filter configs.

                    NOTE: Sorting is supported for the following fields: `id`, `created_by`, `created_at`.
                    It is also possible to sort by the matched value of each search filter.
                    To sort by the first search filter, the parameter `value0` can be used.
                    If a second search filter is defined, the parameter `value1` can be used.
                    By default, elements are sorted by `created_at` (descending).
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory.")
                )
                pagedQueryParameters(
                    parameterWithName("filter_config").description("The filter config to use. (optional)").optional(),
                    visibilityFilterQueryParameter(),
                )
                pagedResponseFields<ResourceRepresentation>(resourceResponseFields())
                throws(InvalidFilterConfig::class)
            }

        verify(exactly = 1) {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, filterConfig, VisibilityFilter.FEATURED, any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given an observatory id, when filter config is invalid, then status is 400 BAD REQUEST`() {
        val id = ObservatoryId("95565e51-2b80-4c28-918c-6fbc5e2a9b33")
        val encodedFilterConfig = "invalid"

        get("/api/observatories/{id}/papers", id)
            .param("filter_config", encodedFilterConfig)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_filter_config")
    }
}
