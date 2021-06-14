package eu.tib.orkg.prototype.paperswithcode.adapters.input.rest

import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.RestDocumentationBaseTest
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Benchmark Controller")
@Transactional
@Import(MockUserDetailsService::class)
class BenchmarkControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: BenchmarkController

    @Autowired
    private lateinit var statementService: StatementService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var literalService: LiteralService

    @Autowired
    private lateinit var predicateService: PredicateService

    override fun createController() = controller

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        statementService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)

        predicateService.create(CreatePredicateRequest(PredicateId("P30"), "Has research field"))
        predicateService.create(CreatePredicateRequest(PredicateId("P31"), "Has contribution"))
        predicateService.create(CreatePredicateRequest(PredicateId("P32"), "Has research problem"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_BENCHMARK"), "Has benchmark"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_DATASET"), "Has dataset"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_SOURCE_CODE"), "Has code"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_MODEL"), "Has model"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_EVALUATION"), "Has evaluation"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_METRIC"), "Has metric"))
        predicateService.create(CreatePredicateRequest(PredicateId("HAS_VALUE"), "Has value"))
    }

    // FIXME: Is this the correct place for this call or should it be moved to RF test
    @Test
    fun fetchResearchFieldsWithBenchmarks() {
        val fieldWithBenchmark = resourceService.create(CreateResourceRequest(null, "Field 1", setOf(ClassId("ResearchField"))))
        val fieldWithoutBenchmark = resourceService.create(CreateResourceRequest(null, "Field 2", setOf(ClassId("ResearchField"))))

        val benchPaper1 = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))
        val benchPaper2 = resourceService.create(CreateResourceRequest(null, "Paper 2", setOf(ClassId("Paper"))))
        val normalPaper = resourceService.create(CreateResourceRequest(null, "Paper 3", setOf(ClassId("Paper"))))

        val benchCont1 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))
        val benchCont2 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 2", setOf(ClassId("Contribution"))))
        val normalCont1 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 3", setOf(ClassId("Contribution"))))

        val bench1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId("Benchmark"))))
        val bench2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId("Benchmark"))))

        statementService.create(benchPaper1.id!!.value, PredicateId("P30"), fieldWithBenchmark.id!!.value)
        statementService.create(benchPaper2.id!!.value, PredicateId("P30"), fieldWithBenchmark.id!!.value)
        statementService.create(normalPaper.id!!.value, PredicateId("P30"), fieldWithoutBenchmark.id!!.value)

        statementService.create(benchPaper1.id!!.value, PredicateId("P31"), benchCont1.id!!.value)
        statementService.create(benchPaper2.id!!.value, PredicateId("P31"), benchCont2.id!!.value)
        statementService.create(normalPaper.id!!.value, PredicateId("P31"), normalCont1.id!!.value)

        statementService.create(benchCont1.id!!.value, PredicateId("HAS_BENCHMARK"), bench1.id!!.value)
        statementService.create(benchCont2.id!!.value, PredicateId("HAS_BENCHMARK"), bench2.id!!.value)

        mockMvc
            .perform(getRequestTo("/api/research-fields/benchmarks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].label", Matchers.equalTo(fieldWithBenchmark.label)))
            .andDo(
                document(
                    snippet,
                    researchFieldListResponseFields()
                )
            )
    }

    @Test
    fun fetchBenchmarkSummaryForResearchField() {
        val fieldWithDataset = resourceService.create(CreateResourceRequest(null, "Field with a dataset", setOf(ClassId("ResearchField"))))

        val benchPaper = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))

        val benchCont = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))

        val benchmark = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId("Benchmark"))))

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId("Dataset"))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId("Dataset"))))

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.create(CreateResourceRequest(null, "Problem 1", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "Problem 2", setOf(ClassId("Problem"))))

        statementService.create(benchPaper.id!!.value, PredicateId("P30"), fieldWithDataset.id!!.value)
        statementService.create(benchPaper.id!!.value, PredicateId("P31"), benchCont.id!!.value)

        statementService.create(benchCont.id!!.value, PredicateId("P32"), problem1.id!!.value)
        statementService.create(benchCont.id!!.value, PredicateId("P32"), problem2.id!!.value)
        statementService.create(benchCont.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark.id!!.value)

        codes.forEach {
            statementService.create(benchCont.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }

        statementService.create(benchmark.id!!.value, PredicateId("HAS_DATASET"), dataset1.id!!.value)
        statementService.create(benchmark.id!!.value, PredicateId("HAS_DATASET"), dataset2.id!!.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/research-field/${fieldWithDataset.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].research_problem.id", Matchers.equalTo(problem2.id!!.value)))
            .andExpect(jsonPath("$[1].research_problem.id", Matchers.equalTo(problem1.id!!.value)))
            .andExpect(jsonPath("$[0].total_papers", Matchers.equalTo(1)))
            .andExpect(jsonPath("$[0].total_datasets", Matchers.equalTo(2)))
            .andExpect(jsonPath("$[0].total_codes", Matchers.equalTo(5)))
            .andDo(
                document(
                    snippet,
                    benchmarkListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetForResearchProblem() {
        val problem = resourceService.create(CreateResourceRequest(null, "Problem with a dataset", setOf(ClassId("Problem"))))

        val paper1 = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))
        val paper2 = resourceService.create(CreateResourceRequest(null, "Paper 2", setOf(ClassId("Paper"))))

        val contributionOfPaper1 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))
        val contributionOfPaper2 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark P1", setOf(ClassId("Benchmark"))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark P2", setOf(ClassId("Benchmark"))))

        val codes = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val models = (1..4).map { resourceService.create(CreateResourceRequest(null, "Model $it", setOf(ClassId("Model")))) }

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId("Dataset"))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId("Dataset"))))

        statementService.create(paper1.id!!.value, PredicateId("P31"), contributionOfPaper1.id!!.value)
        statementService.create(paper2.id!!.value, PredicateId("P31"), contributionOfPaper2.id!!.value)

        statementService.create(contributionOfPaper1.id!!.value, PredicateId("P32"), problem.id!!.value)
        statementService.create(contributionOfPaper2.id!!.value, PredicateId("P32"), problem.id!!.value)

        statementService.create(contributionOfPaper1.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark1.id!!.value)
        statementService.create(contributionOfPaper2.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark2.id!!.value)

        models.forEach {
            statementService.create(contributionOfPaper1.id!!.value, PredicateId("HAS_MODEL"), it.id!!.value)
        }
        codes.forEach {
            statementService.create(contributionOfPaper2.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }

        statementService.create(benchmark1.id!!.value, PredicateId("HAS_DATASET"), dataset1.id!!.value)
        statementService.create(benchmark2.id!!.value, PredicateId("HAS_DATASET"), dataset2.id!!.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/research-problem/${problem.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].total_papers", Matchers.equalTo(1)))
            .andExpect(jsonPath("$[0].total_models", Matchers.equalTo(0)))
            .andExpect(jsonPath("$[0].total_codes", Matchers.equalTo(3)))
            .andExpect(jsonPath("$[1].total_papers", Matchers.equalTo(1)))
            .andExpect(jsonPath("$[1].total_models", Matchers.equalTo(4)))
            .andExpect(jsonPath("$[1].total_codes", Matchers.equalTo(0)))
            .andDo(
                document(
                    snippet,
                    datasetListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetSummary() {
        val dataset = resourceService.create(CreateResourceRequest(null, "some dataset", setOf(ClassId("Dataset"))))

        val paper = resourceService.create(CreateResourceRequest(null, "paper", setOf(ClassId("Paper"))))
        val contribution1 = resourceService.create(CreateResourceRequest(null, "Contribution 1", setOf(ClassId("Contribution"))))
        val contribution2 = resourceService.create(CreateResourceRequest(null, "Contribution 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId("Benchmark"))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId("Benchmark"))))

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val model1 = resourceService.create(CreateResourceRequest(null, "Model 1", setOf(ClassId("Model"))))
        val model2 = resourceService.create(CreateResourceRequest(null, "Model 2", setOf(ClassId("Model"))))

        val evaluationB1E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))
        val evaluationB1E2 = resourceService.create(CreateResourceRequest(null, "Evaluation 2", setOf(ClassId("Evaluation"))))
        val evaluationB2E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))

        val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId("Metric"))))
        val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId("Metric"))))

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3M")

        statementService.create(paper.id!!.value, PredicateId("P31"), contribution1.id!!.value)
        statementService.create(paper.id!!.value, PredicateId("P31"), contribution2.id!!.value)

        statementService.create(contribution1.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark1.id!!.value)
        statementService.create(contribution2.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark2.id!!.value)

        codes1.forEach {
            statementService.create(contribution1.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }
        codes2.forEach {
            statementService.create(contribution2.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }

        statementService.create(contribution1.id!!.value, PredicateId("HAS_MODEL"), model1.id!!.value)
        statementService.create(contribution2.id!!.value, PredicateId("HAS_MODEL"), model2.id!!.value)

        statementService.create(benchmark1.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.id!!.value)
        statementService.create(benchmark1.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.id!!.value)
        statementService.create(benchmark2.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.id!!.value)

        statementService.create(benchmark1.id!!.value, PredicateId("HAS_DATASET"), dataset.id!!.value)
        statementService.create(benchmark2.id!!.value, PredicateId("HAS_DATASET"), dataset.id!!.value)

        statementService.create(evaluationB1E1.id!!.value, PredicateId("HAS_METRIC"), metric1.id!!.value)
        statementService.create(evaluationB1E2.id!!.value, PredicateId("HAS_METRIC"), metric2.id!!.value)
        statementService.create(evaluationB2E1.id!!.value, PredicateId("HAS_METRIC"), metric1.id!!.value)

        statementService.create(evaluationB1E1.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id!!.value)
        statementService.create(evaluationB1E2.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id!!.value)
        statementService.create(evaluationB2E1.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id!!.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/${dataset.id}/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(3)))
            .andDo(
                document(
                    snippet,
                    datasetSummaryListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetSummaryWithoutModels() {
        val dataset = resourceService.create(CreateResourceRequest(null, "some dataset", setOf(ClassId("Dataset"))))

        val paper = resourceService.create(CreateResourceRequest(null, "paper", setOf(ClassId("Paper"))))
        val contribution1 = resourceService.create(CreateResourceRequest(null, "Contribution 1", setOf(ClassId("Contribution"))))
        val contribution2 = resourceService.create(CreateResourceRequest(null, "Contribution 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId("Benchmark"))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId("Benchmark"))))

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val evaluationB1E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))
        val evaluationB1E2 = resourceService.create(CreateResourceRequest(null, "Evaluation 2", setOf(ClassId("Evaluation"))))
        val evaluationB2E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))

        val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId("Metric"))))
        val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId("Metric"))))

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3.2B")

        statementService.create(paper.id!!.value, PredicateId("P31"), contribution1.id!!.value)
        statementService.create(paper.id!!.value, PredicateId("P31"), contribution2.id!!.value)

        statementService.create(contribution1.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark1.id!!.value)
        statementService.create(contribution2.id!!.value, PredicateId("HAS_BENCHMARK"), benchmark2.id!!.value)

        codes1.forEach {
            statementService.create(contribution1.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }
        codes2.forEach {
            statementService.create(contribution2.id!!.value, PredicateId("HAS_SOURCE_CODE"), it.id!!.value)
        }

        statementService.create(benchmark1.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.id!!.value)
        statementService.create(benchmark1.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.id!!.value)
        statementService.create(benchmark2.id!!.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.id!!.value)

        statementService.create(benchmark1.id!!.value, PredicateId("HAS_DATASET"), dataset.id!!.value)
        statementService.create(benchmark2.id!!.value, PredicateId("HAS_DATASET"), dataset.id!!.value)

        statementService.create(evaluationB1E1.id!!.value, PredicateId("HAS_METRIC"), metric1.id!!.value)
        statementService.create(evaluationB1E2.id!!.value, PredicateId("HAS_METRIC"), metric2.id!!.value)
        statementService.create(evaluationB2E1.id!!.value, PredicateId("HAS_METRIC"), metric1.id!!.value)

        statementService.create(evaluationB1E1.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id!!.value)
        statementService.create(evaluationB1E2.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id!!.value)
        statementService.create(evaluationB2E1.id!!.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id!!.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/${dataset.id}/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(3)))
            .andDo(
                document(
                    snippet,
                    datasetSummaryListResponseFields()
                )
            )
    }

    private fun researchFieldResponseFields() =
        listOf(
            fieldWithPath("id").description("Research field ID"),
            fieldWithPath("label").description("Research field label")
        )

    private fun researchFieldListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of research fields"))
            .andWithPrefix("[].", researchFieldResponseFields())

    private fun researchProblemResponseFields() =
        listOf(
            fieldWithPath("id").description("Research problem ID"),
            fieldWithPath("label").description("Research problem label")
        )

    private fun benchmarkResponseFields() =
        listOf(
            fieldWithPath("total_papers").description("Total number of papers"),
            fieldWithPath("total_datasets").description("Total number of datasets related"),
            fieldWithPath("total_codes").description("Total number of code urls"),
            fieldWithPath("research_problem").description("Research problem concerned with this research field")
        )

    private fun benchmarkListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of benchmarks"))
            .andWithPrefix("[].", benchmarkResponseFields())
            .andWithPrefix("[].research_problem.", researchProblemResponseFields())

    private fun datasetResponseFields() =
        listOf(
            fieldWithPath("id").description("Id of the dataset"),
            fieldWithPath("label").description("The label of the dataset"),
            fieldWithPath("total_papers").description("Total number of papers"),
            fieldWithPath("total_models").description("Total number of models"),
            fieldWithPath("total_codes").description("Total number of code urls")
        )

    private fun datasetListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of datasets"))
            .andWithPrefix("[].", datasetResponseFields())

    private fun datasetSummaryResponseFields() =
        listOf(
            fieldWithPath("model_name").description("The model name used on the dataset").optional(),
            fieldWithPath("metric").description("The metric used in the evaluation"),
            fieldWithPath("score").description("the score of the evaluation with the corresponding metric"),
            fieldWithPath("paper_id").description("The paper id is where the evaluation is published"),
            fieldWithPath("paper_title").description("The paper title is where the evaluation is published"),
            fieldWithPath("paper_month").description("The month when the paper was published").optional(),
            fieldWithPath("paper_year").description("The year when the paper was published").optional(),
            fieldWithPath("code_urls").description("A list of urls for the codes specified in the papers")
        )

    private fun datasetSummaryListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of dataset summaries"))
            .andWithPrefix("[].", datasetSummaryResponseFields())
}
