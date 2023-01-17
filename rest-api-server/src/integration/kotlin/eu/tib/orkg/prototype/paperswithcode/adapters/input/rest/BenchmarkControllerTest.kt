package eu.tib.orkg.prototype.paperswithcode.adapters.input.rest

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createPredicates
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.LabelAndClassService
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.RestDocumentationBaseTest
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

// This is only required because these are integration tests. In a unit test, we would mock the feature flag service.
// But for the time were we have both code bases, we need to run both test sets as well.
@TestPropertySource(properties = ["orkg.features.pwc-legacy-model=true"])
class BenchmarkControllerLegacyTest : BenchmarkControllerTest() {
    @BeforeEach
    override fun setup() {
        super.setup()
        classService.createClasses("Benchmark", "Dataset", "Evaluation", "Model", "Metric")
    }
}

@Suppress("HttpUrlsUsage")
@DisplayName("Benchmark Controller")
@Transactional
@Import(MockUserDetailsService::class)
@TestPropertySource(properties = ["orkg.features.pwc-legacy-model=false"])
class BenchmarkControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var labelsAndClasses: LabelAndClassService

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    protected lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        statementService.removeAll()
        classService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicates(
            "P30" to "Has research field",
            "P31" to "Has contribution",
            "P32" to "Has research problem",
            "P36" to "Has sub-field",
            labelsAndClasses.benchmarkPredicate to "Has benchmark",
            labelsAndClasses.datasetPredicate to "Has dataset",
            labelsAndClasses.sourceCodePredicate to "Has code",
            labelsAndClasses.modelPredicate to "Has model",
            labelsAndClasses.metricPredicate to "Has metric",
            labelsAndClasses.quantityValuePredicate to "Has quantity value", // legacy: HAS_VALUE
            labelsAndClasses.quantityPredicate to "Has evaluation"
        )

        classService.createClasses("Paper", "Problem", "ResearchField", "Contribution")

        if (!flags.isPapersWithCodeLegacyModelEnabled()) {
            predicateService.createPredicate(id = labelsAndClasses.numericValuePredicate, label = "Has numeric value")

            classService.createClass(
                label = "Quantity",
                labelsAndClasses.quantityClass,
                uri = URI("http://qudt.org/2.1/schema/qudt/Quantity")
            )
            classService.createClass(
                label = "Quantity",
                labelsAndClasses.quantityValueClass,
                uri = URI("http://qudt.org/2.1/schema/qudt/QuantityValue")
            )
            classService.createClass(
                label = "Quantity Kind",
                labelsAndClasses.metricClass,
                uri = URI("http://qudt.org/2.1/schema/qudt/QuantityKind")
            )
            classService.createClass("Dataset", labelsAndClasses.datasetClass)
            classService.createClass("Benchmark", labelsAndClasses.benchmarkClass)
            classService.createClass("Model", labelsAndClasses.modelClass)
        }
    }

    @Test
    fun fetchResearchFieldsWithBenchmarks() {
        val fieldWithBenchmarkLabel = "Field 1"
        val fieldWithBenchmark = resourceService.createResource(setOf("ResearchField"), label = fieldWithBenchmarkLabel)
        val fieldWithoutBenchmark = resourceService.createResource(setOf("ResearchField"), label = "Field 2")

        val benchPaper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val benchPaper2 = resourceService.createResource(setOf("Paper"), label = "Paper 2")
        val normalPaper = resourceService.createResource(setOf("Paper"), label = "Paper 3")

        val benchCont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")
        val benchCont2 = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 2")
        val normalCont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 3")

        val bench1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")
        val bench2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 2")

        statementService.create(benchPaper1.value, PredicateId("P30"), fieldWithBenchmark.value)
        statementService.create(benchPaper2.value, PredicateId("P30"), fieldWithBenchmark.value)
        statementService.create(normalPaper.value, PredicateId("P30"), fieldWithoutBenchmark.value)

        statementService.create(benchPaper1.value, PredicateId("P31"), benchCont1.value)
        statementService.create(benchPaper2.value, PredicateId("P31"), benchCont2.value)
        statementService.create(normalPaper.value, PredicateId("P31"), normalCont1.value)

        statementService.create(benchCont1.value, PredicateId(labelsAndClasses.benchmarkPredicate), bench1.value)
        statementService.create(benchCont2.value, PredicateId(labelsAndClasses.benchmarkPredicate), bench2.value)

        mockMvc
            .perform(getRequestTo("/api/research-fields/benchmarks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].label", equalTo(fieldWithBenchmarkLabel)))
            .andDo(
                document(
                    snippet,
                    researchFieldListResponseFields()
                )
            )
    }

    @Test
    fun fetchBenchmarkSummaryForResearchField() {
        val fieldWithDataset = resourceService.createResource(setOf("ResearchField"), label = "Field with a dataset")

        val benchPaper = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper.value, PredicateId("P30"), fieldWithDataset.value)
        statementService.create(benchPaper.value, PredicateId("P31"), benchCont.value)

        statementService.create(benchCont.value, PredicateId("P32"), problem1.value)
        statementService.create(benchCont.value, PredicateId("P32"), problem2.value)
        statementService.create(benchCont.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.value)

        codes.forEach {
            statementService.create(benchCont.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.value)
        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/research-field/$fieldWithDataset"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].research_problem.id", equalTo(problem2.value)))
            .andExpect(jsonPath("$[1].research_problem.id", equalTo(problem1.value)))
            .andExpect(jsonPath("$[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$[0].total_codes", equalTo(5)))
            .andDo(
                document(
                    snippet,
                    benchmarkListResponseFields()
                )
            )
    }

    @Test
    fun fetchBenchmarkSummaries() {
        assumeFalse(flags.isPapersWithCodeLegacyModelEnabled())

        val field1 = resourceService.createResource(setOf("ResearchField"), label = "Field with a dataset #1")

        val benchPaper = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper.value, PredicateId("P30"), field1.value)
        statementService.create(benchPaper.value, PredicateId("P31"), benchCont.value)

        statementService.create(benchCont.value, PredicateId("P32"), problem1.value)
        statementService.create(benchCont.value, PredicateId("P32"), problem2.value)
        statementService.create(benchCont.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.value)

        codes.forEach {
            statementService.create(benchCont.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.value)
        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].research_problem.id", equalTo(problem2.value)))
            .andExpect(jsonPath("$[1].research_problem.id", equalTo(problem1.value)))
            .andExpect(jsonPath("$[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$[0].total_codes", equalTo(5)))
            .andDo(
                document(
                    snippet,
                    benchmarkListResponseFields()
                )
            )
    }

    @Test
    fun `aggregate problems that belong to multiple RFs when fetching summary`() {
        assumeFalse(flags.isPapersWithCodeLegacyModelEnabled())

        val field1 = resourceService.createResource(setOf("ResearchField"), label = "Field #1 with a problem #1")
        val field2 = resourceService.createResource(setOf("ResearchField"), label = "Field #2 with a problem #1")

        val benchPaper = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val dummyPaper = resourceService.createResource(setOf("Paper"), label = "Paper 2")

        val benchCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")
        val dummyCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 2")

        val benchmark = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark #1")
        val dummyBenchmark = resourceService.createResource(
            classes = setOf(labelsAndClasses.benchmarkClass),
            label = "Benchmark #2"
        )

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper.value, PredicateId("P30"), field1.value)
        statementService.create(dummyPaper.value, PredicateId("P30"), field2.value)

        statementService.create(benchPaper.value, PredicateId("P31"), benchCont.value)
        statementService.create(dummyPaper.value, PredicateId("P31"), dummyCont.value)

        statementService.create(benchCont.value, PredicateId("P32"), problem1.value)
        statementService.create(benchCont.value, PredicateId("P32"), problem2.value)
        statementService.create(benchCont.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.value)

        statementService.create(dummyCont.value, PredicateId("P32"), problem1.value)
        statementService.create(dummyCont.value, PredicateId(labelsAndClasses.benchmarkPredicate), dummyBenchmark.value)

        statementService.create(field1.value, PredicateId("P36"), field2.value)

        codes.forEach {
            statementService.create(benchCont.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.value)
        statementService.create(benchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.value)

        statementService.create(dummyBenchmark.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            // Due to limits in JSONPath, we can only filter to the respective object from the list, but not get rid of
            // the list itself, so we need to deal with that. The list of research fields is embedded into a list, so
            // by selecting all elements (via "[*]") we get rid of the innermost list. Because that does not work for
            // the totals, those are checked using a list containing the expected result.
            // Problem 1:
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.value}\")].research_fields[*]", hasSize<Int>(2)))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.value}\")].total_papers", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.value}\")].total_codes", equalTo(listOf(5))))
            // Problem 2:
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.value}\")].research_fields[*]", hasSize<Int>(1)))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.value}\")].total_papers", equalTo(listOf(1))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.value}\")].total_codes", equalTo(listOf(5))))
            .andDo(
                document(
                    snippet,
                    benchmarkListResponseFields()
                )
            )
    }

    @Test
    fun fetchResearchProblemsForADataset() {
        val paper = resourceService.createResource(setOf("Paper"), label = "Paper")

        val dataset = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset")

        val benchmark1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 2")

        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")
        val cont2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2")

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchmark1.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)
        statementService.create(benchmark2.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)

        statementService.create(cont1.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.value)
        statementService.create(cont2.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.value)

        statementService.create(cont1.value, PredicateId("P32"), problem1.value)
        statementService.create(cont1.value, PredicateId("P32"), problem2.value)
        statementService.create(cont2.value, PredicateId("P32"), problem2.value)

        statementService.create(paper.value, PredicateId("P31"), cont1.value)
        statementService.create(paper.value, PredicateId("P31"), cont2.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    researchProblemListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetForResearchProblem() {
        val problem = resourceService.createResource(setOf("Problem"), label = "Problem with a dataset")

        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf("Paper"), label = "Paper 2")

        val contributionOfPaper1 = resourceService.createResource(
            classes = setOf("Contribution"),
            label = "Contribution of Paper 1"
        )
        val contributionOfPaper2 = resourceService.createResource(
            classes = setOf("Contribution"),
            label = "Contribution of Paper 2"
        )

        val benchmark1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark P1")
        val benchmark2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark P2")

        val codes = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val models = (1..4).map { resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model $it") }

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        statementService.create(paper1.value, PredicateId("P31"), contributionOfPaper1.value)
        statementService.create(paper2.value, PredicateId("P31"), contributionOfPaper2.value)

        statementService.create(contributionOfPaper1.value, PredicateId("P32"), problem.value)
        statementService.create(contributionOfPaper2.value, PredicateId("P32"), problem.value)

        statementService.create(contributionOfPaper1.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.value)
        statementService.create(contributionOfPaper2.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.value)

        models.forEach {
            statementService.create(contributionOfPaper1.value, PredicateId(labelsAndClasses.modelPredicate), it.value)
        }
        codes.forEach {
            statementService.create(contributionOfPaper2.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark1.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.value)
        statementService.create(benchmark2.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/research-problem/$problem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$[0].total_models", equalTo(0)))
            .andExpect(jsonPath("$[0].total_codes", equalTo(3)))
            .andExpect(jsonPath("$[1].total_papers", equalTo(1)))
            .andExpect(jsonPath("$[1].total_models", equalTo(4)))
            .andExpect(jsonPath("$[1].total_codes", equalTo(0)))
            .andDo(
                document(
                    snippet,
                    datasetListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetSummary() {
        val dataset = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "some dataset")

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf("Paper"), label = "paper")
        val contribution1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 2")

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val model1 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 1")
        val model2 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 2")

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3M")

        if (flags.isPapersWithCodeLegacyModelEnabled()) {
            val evaluationB1E1 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 1")
            val evaluationB1E2 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 2")
            val evaluationB2E1 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 1")

            val metric1 = resourceService.createResource(setOf("Metric"), label = "Metric 1")
            val metric2 = resourceService.createResource(setOf("Metric"), label = "Metric 2")

            statementService.create(benchmark1.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.value)
            statementService.create(benchmark1.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.value)
            statementService.create(benchmark2.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.value)

            statementService.create(evaluationB1E1.value, PredicateId("HAS_METRIC"), metric1.value)
            statementService.create(evaluationB1E2.value, PredicateId("HAS_METRIC"), metric2.value)
            statementService.create(evaluationB2E1.value, PredicateId("HAS_METRIC"), metric1.value)

            statementService.create(evaluationB1E1.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id.value)
            statementService.create(evaluationB1E2.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id.value)
            statementService.create(evaluationB2E1.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id.value)
        } else {
            val quantityB1E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")
            val quantityB1E2 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 2")
            val quantityB2E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")

            val metric1 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 1")
            val metric2 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 2")

            val quantityValueB1E1 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 1"
            )
            val quantityValueB1E2 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 2"
            )
            val quantityValueB2E1 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 3"
            )

            statementService.create(benchmark1.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E1.value)
            statementService.create(benchmark1.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E2.value)
            statementService.create(benchmark2.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB2E1.value)

            statementService.create(quantityB1E1.value, PredicateId(labelsAndClasses.metricPredicate), metric1.value)
            statementService.create(quantityB1E2.value, PredicateId(labelsAndClasses.metricPredicate), metric2.value)
            statementService.create(quantityB2E1.value, PredicateId(labelsAndClasses.metricPredicate), metric1.value)

            statementService.create(quantityB1E1.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1.value)
            statementService.create(quantityB1E2.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2.value)
            statementService.create(quantityB2E1.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1.value)

            statementService.create(quantityValueB1E1.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1.id.value)
            statementService.create(quantityValueB1E2.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2.id.value)
            statementService.create(quantityValueB2E1.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1.id.value)
        }

        statementService.create(paper.value, PredicateId("P31"), contribution1.value)
        statementService.create(paper.value, PredicateId("P31"), contribution2.value)

        statementService.create(contribution1.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.value)
        statementService.create(contribution2.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.value)

        codes1.forEach {
            statementService.create(contribution1.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }
        codes2.forEach {
            statementService.create(contribution2.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(contribution1.value, PredicateId("P32"), problem1.value)
        statementService.create(contribution2.value, PredicateId("P32"), problem2.value)

        statementService.create(contribution1.value, PredicateId(labelsAndClasses.modelPredicate), model1.value)
        statementService.create(contribution2.value, PredicateId(labelsAndClasses.modelPredicate), model2.value)

        statementService.create(benchmark1.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)
        statementService.create(benchmark2.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problem/$problem1/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    datasetSummaryListResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetSummaryWithoutModels() {
        val dataset = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "some dataset")

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf("Paper"), label = "paper")
        val contribution1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 2")

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3.2B")

        if (flags.isPapersWithCodeLegacyModelEnabled()) {
            val evaluationB1E1 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 1")
            val evaluationB1E2 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 2")
            val evaluationB2E1 = resourceService.createResource(setOf("Evaluation"), label = "Evaluation 1")

            val metric1 = resourceService.createResource(setOf("Metric"), label = "Metric 1")
            val metric2 = resourceService.createResource(setOf("Metric"), label = "Metric 2")

            statementService.create(benchmark1.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.value)
            statementService.create(benchmark1.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.value)
            statementService.create(benchmark2.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.value)

            statementService.create(benchmark1.value, PredicateId("HAS_DATASET"), dataset.value)
            statementService.create(benchmark2.value, PredicateId("HAS_DATASET"), dataset.value)

            statementService.create(evaluationB1E1.value, PredicateId("HAS_METRIC"), metric1.value)
            statementService.create(evaluationB1E2.value, PredicateId("HAS_METRIC"), metric2.value)
            statementService.create(evaluationB2E1.value, PredicateId("HAS_METRIC"), metric1.value)

            statementService.create(evaluationB1E1.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id.value)
            statementService.create(evaluationB1E2.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id.value)
            statementService.create(evaluationB2E1.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id.value)
        } else {
            val quantityB1E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")
            val quantityB1E2 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 2")
            val quantityB2E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")

            val metric1 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 1")
            val metric2 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 2")

            val quantityValueB1E1 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 1"
            )
            val quantityValueB1E2 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 2"
            )
            val quantityValueB2E1 = resourceService.createResource(
                classes = setOf(labelsAndClasses.quantityValueClass),
                label = "Quantity Value 3"
            )

            statementService.create(benchmark1.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E1.value)
            statementService.create(benchmark1.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E2.value)
            statementService.create(benchmark2.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB2E1.value)

            statementService.create(quantityB1E1.value, PredicateId(labelsAndClasses.metricPredicate), metric1.value)
            statementService.create(quantityB1E2.value, PredicateId(labelsAndClasses.metricPredicate), metric2.value)
            statementService.create(quantityB2E1.value, PredicateId(labelsAndClasses.metricPredicate), metric1.value)

            statementService.create(quantityB1E1.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1.value)
            statementService.create(quantityB1E2.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2.value)
            statementService.create(quantityB2E1.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1.value)

            statementService.create(quantityValueB1E1.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1.id.value)
            statementService.create(quantityValueB1E2.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2.id.value)
            statementService.create(quantityValueB2E1.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1.id.value)
        }
        statementService.create(paper.value, PredicateId("P31"), contribution1.value)
        statementService.create(paper.value, PredicateId("P31"), contribution2.value)

        statementService.create(contribution1.value, PredicateId("P32"), problem1.value)
        statementService.create(contribution2.value, PredicateId("P32"), problem2.value)

        statementService.create(contribution1.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.value)
        statementService.create(contribution2.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.value)

        codes1.forEach {
            statementService.create(contribution1.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }
        codes2.forEach {
            statementService.create(contribution2.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark1.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)
        statementService.create(benchmark2.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problem/$problem1/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    datasetSummaryListResponseFields()
                )
            )
    }

    private fun researchFieldResponseFields() =
        listOf(
            fieldWithPath("id").description("Research field ID").type(String::class).optional(), // FIXME: PwC
            fieldWithPath("label").description("Research field label").type(String::class).optional() // FIXME: PwC
        )

    private fun researchFieldListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of research fields"))
            .andWithPrefix("[].", researchFieldResponseFields())

    private fun researchProblemResponseFields() =
        listOf(
            fieldWithPath("id").description("Research problem ID"),
            fieldWithPath("label").description("Research problem label")
        )

    private fun researchProblemListResponseFields() =
        responseFields(fieldWithPath("[]").description("A list of research problems"))
            .andWithPrefix("[].", researchProblemResponseFields())

    private fun benchmarkResponseFields() =
        listOf(
            fieldWithPath("total_papers").description("Total number of papers"),
            fieldWithPath("total_datasets").description("Total number of datasets related"),
            fieldWithPath("total_codes").description("Total number of code urls"),
            fieldWithPath("research_problem").description("Research problem concerned with this research field"),
            fieldWithPath("research_field").description("The research field the problem belongs to.").optional(), // FIXME: PwC
        )

    private fun benchmarkListResponseFields() =
        responseFields(
            fieldWithPath("[]").description("A list of benchmarks"),
            fieldWithPath("[].research_field").description("Legacy RF of a benchmark summary").optional(),
            fieldWithPath("[].research_fields").description("List of RFs for a benchmark summary").optional()
        )
            .andWithPrefix("[].", benchmarkResponseFields())
            .andWithPrefix("[].research_field.", researchFieldResponseFields())
            .andWithPrefix("[].research_fields.[].", researchFieldResponseFields())
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
