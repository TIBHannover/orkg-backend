package org.orkg.graph.adapter.input.rest

import com.epages.restdocs.apispec.ParameterType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.inversePathResponseFields
import org.orkg.graph.adapter.input.rest.testing.fixtures.pathResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidHopBounds
import org.orkg.graph.domain.PathDirection
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.PathUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.allowedPathDirectionValues
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.enumValues
import org.orkg.testing.spring.restdocs.repeatable
import org.orkg.testing.spring.restdocs.type
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock

@ContextConfiguration(classes = [PathController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [PathController::class])
internal class PathControllerUnitTest : MockMvcBaseTest("paths") {
    @MockkBean
    private lateinit var pathUseCases: PathUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    // dummy class for documentation generation
    abstract class PathRepresentation : Iterable<ThingRepresentation>

    @Test
    @DisplayName("Given several statements, when paths are fetched by root id, then status is 200 OK and paths are returned")
    fun findAllByRootId() {
        val id = ThingId("R123")
        val minHops = 1
        val maxHops = 4
        val denyClasses = setOf(Classes.researchField)
        val allowClasses = setOf(Classes.contribution)
        val terminationClasses = setOf(Classes.literal)
        val pathDirection = PathDirection.OUTGOING
        val includeRoot = true
        val page = pageOf(*arrayOf(listOf(createResource(id), createPredicate(), createClass())))

        every { pathUseCases.findAllByRootId(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns page
        every { statementService.countAllIncomingStatementsById(any()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/things/{id}/paths", id)
            .param("min_hops", minHops.toString())
            .param("max_hops", maxHops.toString())
            .param("deny_classes", denyClasses.joinToString(","))
            .param("allow_classes", allowClasses.joinToString(","))
            .param("termination_classes", terminationClasses.joinToString(","))
            .param("direction", pathDirection.name)
            .param("include_root", includeRoot.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDocument {
                summary("Listing paths by root id")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<paths-fetch,paths>>.
                    If no paging request parameters are provided, the default values will be used.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the root thing."),
                )
                pagedQueryParameters(
                    parameterWithName("min_hops").description("Requires the path returned paths to at least as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 1)").type(ParameterType.INTEGER).optional(),
                    parameterWithName("max_hops").description("Requires the path returned paths to be at most as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 10)").type(ParameterType.INTEGER).optional(),
                    parameterWithName("deny_classes").description("Requires each thing of the path to not include any of the given classes. (optional)").repeatable().optional(),
                    parameterWithName("allow_classes").description("""Requires each thing of the path to be assigned to one of the given classes. Termination classes are exempt from this requirement. (optional)""").repeatable().optional(),
                    parameterWithName("termination_classes").description("Requires paths to end on a thing assigned to one of the given classes. (optional)").repeatable().optional(),
                    parameterWithName("direction").description("Limits the direction in which paths can be expanded. Can be one of $allowedPathDirectionValues. (optional, default: `${PathDirection.OUTGOING}`)").enumValues(PathDirection::class).optional(),
                    parameterWithName("include_root").description("Whether to include the root thing in all returned paths. (optional, default: true)").type(ParameterType.BOOLEAN).optional(),
                )
                pagedResponseFields<PathRepresentation>(pathResponseFields())
                throws(
                    InvalidHopBounds::class,
                    ThingNotFound::class,
                )
            }

        verify(exactly = 1) {
            pathUseCases.findAllByRootId(
                id = id,
                pageable = any(),
                minHops = minHops,
                maxHops = maxHops,
                denyClasses = denyClasses,
                allowClasses = allowClasses,
                terminationClasses = terminationClasses,
                pathDirection = pathDirection,
                includeRoot = includeRoot,
            )
            statementService.countAllIncomingStatementsById(any())
            statementService.findAllDescriptionsById(any())
        }
    }

    // dummy class for documentation generation
    abstract class InversePathRepresentation : Iterable<PathRepresentation>

    @Test
    @DisplayName("Given several statements, when inverse paths are fetched by root id, then status is 200 OK and paths are returned")
    fun findAllByRootIdInverse() {
        val id = ThingId("R123")
        val minHops = 1
        val maxHops = 4
        val denyClasses = setOf(Classes.researchField)
        val allowClasses = setOf(Classes.contribution)
        val terminationClasses = setOf(Classes.literal)
        val pathDirection = PathDirection.OUTGOING
        val includeRoot = true
        val page = pageOf(*arrayOf(listOf(listOf(createResource(id), createPredicate(), createClass()))))

        every { pathUseCases.findAllByRootIdInverse(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns page
        every { statementService.countAllIncomingStatementsById(any()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/things/{id}/inverse-paths", id)
            .param("min_hops", minHops.toString())
            .param("max_hops", maxHops.toString())
            .param("deny_classes", denyClasses.joinToString(","))
            .param("allow_classes", allowClasses.joinToString(","))
            .param("termination_classes", terminationClasses.joinToString(","))
            .param("direction", pathDirection.name)
            .param("include_root", includeRoot.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andDocument {
                summary("Listing inverse paths by root id")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of inversed <<paths-fetch,paths>>.
                    Returned paths are grouped by the furthest thing from the root, or in other words the thing that terminated the path.
                    If no paging request parameters are provided, the default values will be used.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the root thing."),
                )
                pagedQueryParameters(
                    parameterWithName("min_hops").description("Requires the path returned paths to at least as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 1)").type(ParameterType.INTEGER).optional(),
                    parameterWithName("max_hops").description("Requires the path returned paths to be at most as long as the given value. Must be greater than 0 and less than or equal to 10. (optional, default: 10)").type(ParameterType.INTEGER).optional(),
                    parameterWithName("deny_classes").description("Requires each thing of the path to not include any of the given classes. (optional)").repeatable().optional(),
                    parameterWithName("allow_classes").description("""Requires each thing of the path to be assigned to one of the given classes. Termination classes are exempt from this requirement. (optional)""").repeatable().optional(),
                    parameterWithName("termination_classes").description("Requires paths to end on a thing assigned to one of the given classes. (optional)").repeatable().optional(),
                    parameterWithName("direction").description("Limits the direction in which paths can be expanded. Can be one of $allowedPathDirectionValues. (optional, default: `${PathDirection.OUTGOING}`)").enumValues(PathDirection::class).optional(),
                    parameterWithName("include_root").description("Whether to include the root thing in all returned paths. (optional, default: true)").type(ParameterType.BOOLEAN).optional(),
                )
                pagedResponseFields<InversePathRepresentation>(inversePathResponseFields())
                throws(
                    InvalidHopBounds::class,
                    ThingNotFound::class,
                )
            }

        verify(exactly = 1) {
            pathUseCases.findAllByRootIdInverse(
                id = id,
                pageable = any(),
                minHops = minHops,
                maxHops = maxHops,
                denyClasses = denyClasses,
                allowClasses = allowClasses,
                terminationClasses = terminationClasses,
                pathDirection = pathDirection,
                includeRoot = includeRoot,
            )
            statementService.countAllIncomingStatementsById(any())
            statementService.findAllDescriptionsById(any())
        }
    }
}
