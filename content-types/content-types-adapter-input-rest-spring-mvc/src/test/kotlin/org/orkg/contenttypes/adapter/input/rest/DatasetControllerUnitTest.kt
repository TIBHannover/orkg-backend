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
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.DatasetUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
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
import java.util.Optional

@ContextConfiguration(
    classes = [
        DatasetController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [DatasetController::class])
internal class DatasetControllerUnitTest : MockMvcBaseTest("datasets") {
    @MockkBean
    private lateinit var retrieveDatasets: DatasetUseCases

    @MockkBean
    private lateinit var retrieveProblems: ResearchProblemUseCases

    @Test
    @DisplayName("Given a set of datasets, when fetching by research problem, then status is 200 OK and datasets are returned")
    fun fetchDatasetForResearchProblem() {
        val researchProblemId = ThingId("R1655")

        every { retrieveDatasets.findAllDatasetsByResearchProblemId(researchProblemId, any()) } returns Optional.of(
            pageOf(
                Dataset(
                    id = ThingId("R123"),
                    label = "Dataset 1",
                    totalPapers = 567,
                    totalModels = 25,
                    totalCodes = 231
                )
            )
        )

        documentedGetRequestTo("/api/datasets/research-problem/{id}", researchProblemId)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    responseFields(
                        fieldWithPath("content[].id").description("The identifier of the dataset."),
                        fieldWithPath("content[].label").description("The label of the dataset."),
                        fieldWithPath("content[].total_papers").description("The total number of papers."),
                        fieldWithPath("content[].total_models").description("The total number of models."),
                        fieldWithPath("content[].total_codes").description("The total number of code urls."),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveDatasets.findAllDatasetsByResearchProblemId(researchProblemId, any()) }
    }

    @Test
    @DisplayName("Given a dataset, when fetching all associated research problems, then status is 200 OK and research problems are returned")
    fun fetchResearchProblemsForADataset() {
        val datasetId = ThingId("R123")

        every { retrieveProblems.findAllByDatasetId(datasetId, any()) } returns Optional.of(
            pageOf(
                ResearchProblem(id = ThingId("R456"), label = "Problem 1"),
                ResearchProblem(id = ThingId("R789"), label = "Problem 2")
            )
        )

        documentedGetRequestTo("/api/datasets/{id}/problems", datasetId)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the dataset.")
                    ),
                    responseFields(
                        fieldWithPath("content[].id").description("The identifier of the research problem."),
                        fieldWithPath("content[].label").description("The label of the research problem."),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveProblems.findAllByDatasetId(datasetId, any()) }
    }

    @Test
    @DisplayName("Given a dataset, when fetching its summaries by id and research problem, then status is 200 OK and dataset summaries is returned")
    fun fetchDatasetSummary() {
        val datasetId = ThingId("R123")
        val researchProblemId = ThingId("R1655")

        every { retrieveDatasets.findAllDatasetSummariesByIdAndResearchProblemId(datasetId, researchProblemId, any()) } returns Optional.of(
            pageOf(
                DatasetSummary(
                    modelName = "Model 1",
                    modelId = ThingId("R153"),
                    score = "132",
                    metric = "Metric 1",
                    paperId = ThingId("R789"),
                    paperTitle = "Fancy paper title",
                    paperMonth = 3,
                    paperYear = 2025,
                    codeURLs = listOf(
                        "https://some-code-1.cool",
                        "https://some-code-2.cool",
                    ),
                )
            )
        )

        documentedGetRequestTo("/api/datasets/{id}/problem/{researchProblemId}/summary", datasetId, researchProblemId)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the dataset."),
                        parameterWithName("researchProblemId").description("The identifier of the research problem.")
                    ),
                    responseFields(
                        fieldWithPath("content[].model_name").description("The model name used on the dataset. (optional)").optional(),
                        fieldWithPath("content[].model_id").description("The model id used on the dataset. (optional)").optional(),
                        fieldWithPath("content[].metric").description("The metric used in the evaluation."),
                        fieldWithPath("content[].score").description("the score of the evaluation with the corresponding metric."),
                        fieldWithPath("content[].paper_id").description("The paper id is where the evaluation is published."),
                        fieldWithPath("content[].paper_title").description("The paper title is where the evaluation is published."),
                        fieldWithPath("content[].paper_month").description("The month when the paper was published. (optional)").optional(),
                        fieldWithPath("content[].paper_year").description("The year when the paper was published. (optional)").optional(),
                        fieldWithPath("content[].code_urls").description("A list of urls for the codes specified in the papers."),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { retrieveDatasets.findAllDatasetSummariesByIdAndResearchProblemId(datasetId, researchProblemId, any()) }
    }
}
