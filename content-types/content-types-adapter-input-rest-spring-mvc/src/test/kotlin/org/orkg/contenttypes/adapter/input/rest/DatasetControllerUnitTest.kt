package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.DatasetUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.datasetResponseFields
import org.orkg.contenttypes.input.testing.fixtures.datasetSummaryResponseFields
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [DatasetController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [DatasetController::class])
internal class DatasetControllerUnitTest : MockMvcBaseTest("datasets") {
    @MockkBean
    private lateinit var retrieveDatasets: DatasetUseCases

    @MockkBean
    private lateinit var retrieveProblems: ResearchProblemUseCases

    @Test
    @DisplayName("Given a set of datasets, when fetching by research problem, then status is 200 OK and datasets are returned")
    fun findAllDatasetsByResearchProblemId() {
        val researchProblemId = ThingId("R1655")

        every { retrieveDatasets.findAllDatasetsByResearchProblemId(researchProblemId, any()) } returns pageOf(
            Dataset(
                id = ThingId("R123"),
                label = "Dataset 1",
                totalPapers = 567,
                totalModels = 25,
                totalCodes = 231
            )
        )

        documentedGetRequestTo("/api/datasets/research-problem/{id}", researchProblemId)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing datasets by research problem")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of datasets for a research problem.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the research problem."),
                )
                pagedQueryParameters()
                pagedResponseFields<DatasetRepresentation>(datasetResponseFields())
            }

        verify(exactly = 1) { retrieveDatasets.findAllDatasetsByResearchProblemId(researchProblemId, any()) }
    }

    @Test
    @DisplayName("Given a dataset, when fetching all associated research problems, then status is 200 OK and research problems are returned")
    fun findAllResearchProblemsByDatasetId() {
        val datasetId = ThingId("R123")

        every { retrieveProblems.findAllByDatasetId(datasetId, any()) } returns pageOf(
            ResearchProblem(id = ThingId("R456"), label = "Problem 1"),
            ResearchProblem(id = ThingId("R789"), label = "Problem 2")
        )

        documentedGetRequestTo("/api/datasets/{id}/problems", datasetId)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing research problems by dataset id")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of research fields associated with a given dataset.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the dataset."),
                )
                pagedQueryParameters()
                pagedResponseFields<ResearchProblem>(
                    fieldWithPath("id").description("The identifier of the research problem."),
                    fieldWithPath("label").description("The label of the research problem."),
                )
            }

        verify(exactly = 1) { retrieveProblems.findAllByDatasetId(datasetId, any()) }
    }

    @Test
    @DisplayName("Given a dataset, when fetching its summaries by id and research problem, then status is 200 OK and dataset summaries is returned")
    fun findAllDatasetSummariesByIdAndResearchProblemId() {
        val datasetId = ThingId("R123")
        val researchProblemId = ThingId("R1655")

        every { retrieveDatasets.findAllDatasetSummariesByIdAndResearchProblemId(datasetId, researchProblemId, any()) } returns pageOf(
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

        documentedGetRequestTo("/api/datasets/{id}/problem/{researchProblemId}/summary", datasetId, researchProblemId)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing dataset summaries by dataset id and research problem")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of dataset summaries.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the dataset."),
                    parameterWithName("researchProblemId").description("The identifier of the research problem."),
                )
                pagedQueryParameters()
                pagedResponseFields<DatasetSummaryRepresentation>(datasetSummaryResponseFields())
            }

        verify(exactly = 1) { retrieveDatasets.findAllDatasetSummariesByIdAndResearchProblemId(datasetId, researchProblemId, any()) }
    }
}
