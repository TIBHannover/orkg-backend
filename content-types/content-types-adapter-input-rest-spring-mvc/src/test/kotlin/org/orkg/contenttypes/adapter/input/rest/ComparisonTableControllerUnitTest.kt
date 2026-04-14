package org.orkg.contenttypes.adapter.input.rest

import com.epages.restdocs.apispec.SimpleType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.responseCsv
import org.orkg.contenttypes.adapter.input.rest.ComparisonTableController.UpdateComparisonTableRequest
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonPathNotFound
import org.orkg.contenttypes.domain.ComparisonTableNotFound
import org.orkg.contenttypes.domain.InvalidComparisonPath
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonTable
import org.orkg.contenttypes.domain.testing.fixtures.createLabeledComparisonPaths
import org.orkg.contenttypes.domain.testing.fixtures.createSimpleComparisonPaths
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.testing.fixtures.comparisonTableResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.labeledComparisonPathResponseFields
import org.orkg.contenttypes.input.testing.fixtures.simpleComparisonPathResponseFields
import org.orkg.testing.andExpectComparisonPath
import org.orkg.testing.andExpectComparisonTable
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [ComparisonTableController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ComparisonTableController::class])
internal class ComparisonTableControllerUnitTest : MockMvcBaseTest("comparison-tables") {
    @MockkBean
    private lateinit var comparisonTableService: ComparisonTableUseCases

    @Test
    @DisplayName("Given a comparison table, when it is fetched by id and service succeeds, then status is 200 OK and comparison table is returned")
    fun findTableByComparisonId() {
        val id = ThingId("R123")
        val table = createComparisonTable()

        every { comparisonTableService.findByComparisonId(id) } returns Optional.of(table)

        documentedGetRequestTo("/api/comparisons/{id}/contents", id)
            .accept(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonTable()
            .andDocument {
                summary("Fetching comparison tables")
                description(
                    """
                    A `GET` request provides information about a comparison table.
                    
                    [NOTE]
                    If present, formatted labels will be returned for resource labels instead of the original resource label.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison to fetch the table for."),
                )
                responseFields<ComparisonTableRepresentation>(comparisonTableResponseFields())
                throws(ComparisonNotFound::class, ComparisonTableNotFound::class)
            }

        verify(exactly = 1) { comparisonTableService.findByComparisonId(id) }
    }

    @Test
    @DisplayName("Given a comparison table, when it is fetched by id as CSV and service succeeds, then status is 200 OK and comparison table is returned")
    fun findTableByComparisonId_textCsv() {
        val id = ThingId("R123")
        val table = createComparisonTable()

        every { comparisonTableService.findByComparisonId(id) } returns Optional.of(table)

        documentedGetRequestTo("/api/comparisons/{id}/contents", id)
            .accept("text/csv")
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(responseCsv("comparisonTableSuccess")))
            .andDocument {
                summary("Fetching comparison tables")
                description(
                    """
                    A `GET` request provides information about a comparison table.
                    
                    [NOTE]
                    If present, formatted labels will be returned for resource labels instead of the original resource label.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison to fetch the table for."),
                )
                simpleResponse(SimpleType.STRING)
                throws(ComparisonNotFound::class, ComparisonTableNotFound::class)
            }

        verify(exactly = 1) { comparisonTableService.findByComparisonId(id) }
    }

    @Test
    @DisplayName("Given a comparison table, when it is fetched by id as CSV and service succeeds, then status is 200 OK and comparison table is returned")
    fun findTableByComparisonId_textCsv_transposed() {
        val id = ThingId("R123")
        val table = createComparisonTable()

        every { comparisonTableService.findByComparisonId(id) } returns Optional.of(table)

        documentedGetRequestTo("/api/comparisons/{id}/contents", id)
            .accept("text/csv;transposed=true")
            .perform()
            .andPrint()
            .andExpect(status().isOk)
            .andExpect(content().string(responseCsv("comparisonTableTransposedSuccess")))
            .andDocument {
                summary("Fetching comparison tables")
                description(
                    """
                    A `GET` request provides information about a comparison table.
                    
                    [NOTE]
                    If present, formatted labels will be returned for resource labels instead of the original resource label.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison to fetch the table for."),
                )
                simpleResponse(SimpleType.STRING)
                throws(ComparisonNotFound::class, ComparisonTableNotFound::class)
            }

        verify(exactly = 1) { comparisonTableService.findByComparisonId(id) }
    }

    @Test
    @DisplayName("Given a comparison, when finding all comparison paths by comparison id, then status is 200 OK and comparison paths are returned")
    fun findAllPathsByComparisonId() {
        val id = ThingId("R123")
        val comparisonPaths = createLabeledComparisonPaths()

        every { comparisonTableService.findAllPathsByComparisonId(id) } returns comparisonPaths

        documentedGetRequestTo("/api/comparisons/{id}/table-paths", id)
            .accept(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonPath("[*]")
            .andDocument {
                summary("Listing comparison table paths")
                description(
                    """
                    A `GET` request returns all possible row paths for a comparison table (up to a depth of 10).
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison to fetch the predicate paths for."),
                )
                listResponseFields<LabeledComparisonPathRepresentation>(labeledComparisonPathResponseFields())
            }

        verify(exactly = 1) { comparisonTableService.findAllPathsByComparisonId(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an update comparison table request, when service succeeds, it updates the comparison table")
    fun updateTable() {
        val id = ThingId("R123")
        every { comparisonTableService.update(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}/contents", id)
            .content(updateComparisonTableRequest())
            .accept(COMPARISON_JSON_V3)
            .contentType(COMPARISON_JSON_V3)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$id/contents")))
            .andDocument {
                summary("Updating comparison tables")
                description(
                    """
                    A `PUT` request updates a comparison table configuration with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated comparison table (object) can be retrieved by following the URI in the `Location` header field.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated comparison contents can be fetched from."),
                )
                requestFields<UpdateComparisonTableRequest>(
                    fieldWithPath("selected_paths[]").description("The list of paths that define the rows of the comparison table."),
                    *applyPathPrefix("selected_paths[].", simpleComparisonPathResponseFields()).toTypedArray(),
                )
                throws(
                    ComparisonNotFound::class,
                    ComparisonAlreadyPublished::class,
                    InvalidComparisonPath::class,
                    ComparisonPathNotFound::class,
                )
            }

        verify(exactly = 1) { comparisonTableService.update(any()) }
    }

    private fun updateComparisonTableRequest() = UpdateComparisonTableRequest(createSimpleComparisonPaths())
}
