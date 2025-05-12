package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.domain.InvalidFilterConfig
import org.orkg.community.input.RetrieveContributorUseCase
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
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@ContextConfiguration(
    classes = [ObservatoryResourceController::class, ExceptionHandler::class, FixedClockConfig::class, WebMvcConfiguration::class]
)
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
        val id = ObservatoryId(UUID.randomUUID())
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
        val id = ObservatoryId(UUID.randomUUID())
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
        val id = ObservatoryId(UUID.randomUUID())
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
    fun getPagedWithFilterConfig() {
        val id = ObservatoryId(UUID.randomUUID())
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory.")
                    ),
                    queryParameters(
                        parameterWithName("filter_config").description("The filter config to use. (optional)"),
                        parameterWithName("visibility").description("Visibility of this resource. Can be one of \"listed\", \"featured\", \"unlisted\" or \"deleted\". (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            resourceService.findAllPapersByObservatoryIdAndFilters(id, filterConfig, VisibilityFilter.FEATURED, any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given an observatory id, when filter config is invalid, then status is 400 BAD REQUEST`() {
        val id = ObservatoryId(UUID.randomUUID())
        val encodedFilterConfig = "invalid"
        val exception = InvalidFilterConfig()

        get("/api/observatories/{id}/papers", id)
            .param("filter_config", encodedFilterConfig)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/observatories/$id/papers"))
    }
}
