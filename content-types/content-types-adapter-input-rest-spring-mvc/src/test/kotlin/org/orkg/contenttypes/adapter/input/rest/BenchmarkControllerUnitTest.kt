package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.BenchmarkUseCases
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.ignorePageableFieldsExceptContent
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        BenchmarkController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [BenchmarkController::class])
internal class BenchmarkControllerUnitTest : MockMvcBaseTest("benchmarks") {
    @MockkBean
    private lateinit var retrieveResearchField: ResearchFieldUseCases

    @MockkBean
    private lateinit var retrieveBenchmarks: BenchmarkUseCases

    @Test
    @DisplayName("Given a set of benchmarks, when fetching all associated research fields, then status is 200 OK and research fields are returned")
    fun fetchResearchFieldsWithBenchmarks() {
        every { retrieveResearchField.findAllWithBenchmarks(any()) } returns pageOf(
            ResearchField(id = "R11", label = "Science"),
            ResearchField(id = "R12", label = "Life Sciences")
        )

        documentedGetRequestTo("/api/research-fields/benchmarks")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("content[].id").description("The identifier of the research field.").optional(),
                        fieldWithPath("content[].label").description("The label of the research field.").optional(),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveResearchField.findAllWithBenchmarks(any()) }
    }

    @Test
    @DisplayName("Given a set of benchmarks, when fetching their summaries by research field, then status is 200 OK and benchmark summaries are returned")
    fun fetchBenchmarkSummaryForResearchField() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research field.")
                    ),
                    responseFields(
                        fieldWithPath("content[].total_papers").description("The total number of papers."),
                        fieldWithPath("content[].total_datasets").description("The total number of datasets related."),
                        fieldWithPath("content[].total_codes").description("The total number of code urls."),
                        fieldWithPath("content[].research_problem").description("Research problem concerned with this research field."),
                        fieldWithPath("content[].research_problem.id").description("The identifier of the research problem."),
                        fieldWithPath("content[].research_problem.label").description("The label of the research problem."),
                        fieldWithPath("content[].research_fields").description("List of research fields for a benchmark summary"),
                        fieldWithPath("content[].research_fields[].id").description("The identifier of the research field.").optional(),
                        fieldWithPath("content[].research_fields[].label").description("The label of the research field.").optional(),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveBenchmarks.findAllBenchmarkSummariesByResearchFieldId(researchFieldId, any()) }
    }

    @Test
    @DisplayName("Given a set of benchmarks, when fetching their summaries, then status is 200 OK and benchmark summaries are returned")
    fun fetchBenchmarkSummaries() {
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
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("content[].total_papers").description("The total number of papers."),
                        fieldWithPath("content[].total_datasets").description("The total number of datasets related."),
                        fieldWithPath("content[].total_codes").description("The total number of code urls."),
                        fieldWithPath("content[].research_problem").description("Research problem concerned with this research field."),
                        fieldWithPath("content[].research_problem.id").description("The identifier of the research problem."),
                        fieldWithPath("content[].research_problem.label").description("The label of the research problem."),
                        fieldWithPath("content[].research_fields").description("List of research fields for a benchmark summary"),
                        fieldWithPath("content[].research_fields[].id").description("The identifier of the research field.").optional(),
                        fieldWithPath("content[].research_fields[].label").description("The label of the research field.").optional(),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveBenchmarks.findAllBenchmarkSummaries(any()) }
    }
}
