package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.statementResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidHopBounds
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.SubgraphUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.repeatable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock

@ContextConfiguration(classes = [SubgraphController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [SubgraphController::class])
internal class SubgraphControllerUnitTest : MockMvcBaseTest("subgraphs") {
    @MockkBean
    private lateinit var subgraphUseCases: SubgraphUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @DisplayName("Given several statements, when subgraph is fetched by root id, then status is 200 OK and statements are returned")
    fun findByRootId() {
        val statement = createStatement()

        every { subgraphUseCases.findByRootId(any(), any(), any(), any(), any(), any(), any()) } returns pageOf(statement)
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        val id = statement.subject.id
        val minHops = 1
        val maxHops = 4
        val denyClasses = setOf(Classes.researchField)
        val allowClasses = setOf(Classes.contribution)
        val terminationClasses = setOf(Classes.literal)

        documentedGetRequestTo("/api/things/{id}/subgraph", id)
            .param("min_hops", minHops.toString())
            .param("max_hops", maxHops.toString())
            .param("deny_classes", denyClasses.joinToString(","))
            .param("allow_classes", allowClasses.joinToString(","))
            .param("termination_classes", terminationClasses.joinToString(","))
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDocument {
                summary("Fetching subgraphs")
                description(
                    """
                    A `GET` request traverses the graph for the given thing and returns a <<sorting-and-pagination,paged>> list of <<statements-fetch,statements>>.
                    If no paging request parameters are provided, the default values will be used.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the root thing."),
                )
                pagedQueryParameters(
                    parameterWithName("min_hops").description("Limits the returned subgraph to paths which are at least as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 1)").optional(),
                    parameterWithName("max_hops").description("Limits the returned subgraph to paths which are at most as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 10)").optional(),
                    parameterWithName("deny_classes").description("Limits the returned subgraph to paths which do not include any of the given classes. (optional)").repeatable().optional(),
                    parameterWithName("allow_classes").description("""Limits subgraph expansion to things assigned to one of the given classes. Termination classes are exempt from this requirement. (optional)""").repeatable().optional(),
                    parameterWithName("termination_classes").description("Limits the returned subgraph to paths that end on a thing assigned to one of the given classes. (optional)").repeatable().optional(),
                )
                pagedResponseFields<StatementRepresentation>(statementResponseFields())
                throws(
                    InvalidHopBounds::class,
                    ThingNotFound::class,
                    UnknownSortingProperty::class,
                )
            }

        verify(exactly = 1) {
            subgraphUseCases.findByRootId(
                id = id,
                pageable = any(),
                minHops = minHops,
                maxHops = maxHops,
                denyClasses = denyClasses,
                allowClasses = allowClasses,
                terminationClasses = terminationClasses,
            )
            statementService.findAllDescriptionsById(any())
        }
    }
}
