package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.BenchmarkUseCases
import org.orkg.contenttypes.input.LegacyResearchFieldUseCases
import org.orkg.contenttypes.input.testing.fixtures.benchmarkSummaryResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [BenchmarkController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [BenchmarkController::class])
internal class BenchmarkControllerUnitTest : MockMvcBaseTest("benchmarks") {
    @MockkBean
    private lateinit var retrieveResearchField: LegacyResearchFieldUseCases

    @MockkBean
    private lateinit var retrieveBenchmarks: BenchmarkUseCases

    @Test
    @DisplayName("Given a set of benchmarks, when fetching all associated research fields, then status is 200 OK and research fields are returned")
    fun findAllResearchFieldsWithBenchmarks() {
        every { retrieveResearchField.findAllWithBenchmarks(any()) } returns pageOf(
            ResearchField(id = "R11", label = "Science"),
            ResearchField(id = "R12", label = "Life Sciences")
        )

        documentedGetRequestTo("/api/research-fields/benchmarks")
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing research fields with benchmarks")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of all the research fields associated with benchmarks.
                    This includes all research fields that have papers containing benchmarks in their contributions.
                    """
                )
                pagedQueryParameters()
                pagedResponseFields<ResearchField>(
                    fieldWithPath("id").description("The identifier of the research field.").optional(),
                    fieldWithPath("label").description("The label of the research field.").optional(),
                )
            }

        verify(exactly = 1) { retrieveResearchField.findAllWithBenchmarks(any()) }
    }

    @Test
    @DisplayName("Given a set of benchmarks, when fetching their summaries by research field, then status is 200 OK and benchmark summaries are returned")
    fun findAllBenchmarkSummariesByResearchField() {
        val researchFieldId = ThingId("R11")

        every { retrieveBenchmarks.findAllBenchmarkSummariesByResearchFieldId(researchFieldId, any()) } returns pageOf(
            BenchmarkSummary(
                researchProblem = ResearchProblem(id = ThingId("R456"), label = "Problem 1"),
                researchFields = listOf(
                    ResearchField(id = "R11", label = "Science")
                ),
                totalPapers = 567,
                totalDatasets = 2,
                totalCodes = 231
            )
        )

        documentedGetRequestTo("/api/benchmarks/summary/research-field/{id}", researchFieldId)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing benchmark summaries by research field")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of benchmark summaries under a certain research field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the research field.")
                )
                pagedQueryParameters()
                pagedResponseFields<BenchmarkSummaryRepresentation>(benchmarkSummaryResponseFields())
            }

        verify(exactly = 1) { retrieveBenchmarks.findAllBenchmarkSummariesByResearchFieldId(researchFieldId, any()) }
    }

    @Test
    @DisplayName("Given a set of benchmarks, when fetching their summaries, then status is 200 OK and benchmark summaries are returned")
    fun findAllBenchmarkSummaries() {
        every { retrieveBenchmarks.findAllBenchmarkSummaries(any()) } returns pageOf(
            BenchmarkSummary(
                researchProblem = ResearchProblem(id = ThingId("R456"), label = "Problem 1"),
                researchFields = listOf(
                    ResearchField(id = "R11", label = "Science")
                ),
                totalPapers = 567,
                totalDatasets = 2,
                totalCodes = 231
            )
        )

        documentedGetRequestTo("/api/benchmarks/summary")
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing benchmark summaries")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of benchmark summaries.
                    """
                )
                pagedQueryParameters()
                pagedResponseFields<BenchmarkSummaryRepresentation>(benchmarkSummaryResponseFields())
            }

        verify(exactly = 1) { retrieveBenchmarks.findAllBenchmarkSummaries(any()) }
    }
}
