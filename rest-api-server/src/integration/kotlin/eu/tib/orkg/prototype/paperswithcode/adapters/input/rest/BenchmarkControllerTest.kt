package eu.tib.orkg.prototype.paperswithcode.adapters.input.rest

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.LabelAndClassService
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.RestDocumentationBaseTest
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.services.ClassService
import eu.tib.orkg.prototype.statements.services.PredicateService
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
        classService.create(CreateClassRequest(ClassId("Benchmark"), "Benchmark", null))
        classService.create(CreateClassRequest(ClassId("Dataset"), "Dataset", null))
        classService.create(CreateClassRequest(ClassId("Evaluation"), "Evaluation", null))
        classService.create(CreateClassRequest(ClassId("Model"), "Model", null))
        classService.create(CreateClassRequest(ClassId("Metric"), "Metric", null))
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
    private lateinit var predicateService: PredicateService

    @Autowired
    protected lateinit var classService: ClassService

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

        predicateService.create(CreatePredicateRequest(PredicateId("P30"), "Has research field"))
        predicateService.create(CreatePredicateRequest(PredicateId("P31"), "Has contribution"))
        predicateService.create(CreatePredicateRequest(PredicateId("P32"), "Has research problem"))
        predicateService.create(CreatePredicateRequest(PredicateId("P36"), "Has sub-field"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.benchmarkPredicate), "Has benchmark"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.datasetPredicate), "Has dataset"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.sourceCodePredicate), "Has code"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.modelPredicate), "Has model"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.metricPredicate), "Has metric"))
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.quantityValuePredicate), "Has quantity value")) // legacy: HAS_VALUE
        predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.quantityPredicate), "Has evaluation"))

        classService.create(CreateClassRequest(ClassId("Paper"), "Paper", null))
        classService.create(CreateClassRequest(ClassId("Problem"), "Problem", null))
        classService.create(CreateClassRequest(ClassId("ResearchField"), "ResearchField", null))
        classService.create(CreateClassRequest(ClassId("Contribution"), "Contribution", null))

        if (!flags.isPapersWithCodeLegacyModelEnabled()) {
            predicateService.create(CreatePredicateRequest(PredicateId(labelsAndClasses.numericValuePredicate), "Has numeric value"))

            classService.create(CreateClassRequest(ClassId(labelsAndClasses.quantityClass), "Quantity", URI("http://qudt.org/2.1/schema/qudt/Quantity")))
            classService.create(CreateClassRequest(ClassId(labelsAndClasses.quantityValueClass), "Quantity", URI("http://qudt.org/2.1/schema/qudt/QuantityValue")))
            classService.create(CreateClassRequest(ClassId(labelsAndClasses.metricClass), "Quantity Kind", URI("http://qudt.org/2.1/schema/qudt/QuantityKind")))
            classService.create(CreateClassRequest(ClassId(labelsAndClasses.datasetClass), "Dataset", null))
            classService.create(CreateClassRequest(ClassId(labelsAndClasses.benchmarkClass), "Benchmark", null))
            classService.create(CreateClassRequest(ClassId(labelsAndClasses.modelClass), "Model", null))
        }
    }

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

        val bench1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val bench2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        statementService.create(benchPaper1.id.value, PredicateId("P30"), fieldWithBenchmark.id.value)
        statementService.create(benchPaper2.id.value, PredicateId("P30"), fieldWithBenchmark.id.value)
        statementService.create(normalPaper.id.value, PredicateId("P30"), fieldWithoutBenchmark.id.value)

        statementService.create(benchPaper1.id.value, PredicateId("P31"), benchCont1.id.value)
        statementService.create(benchPaper2.id.value, PredicateId("P31"), benchCont2.id.value)
        statementService.create(normalPaper.id.value, PredicateId("P31"), normalCont1.id.value)

        statementService.create(benchCont1.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), bench1.id.value)
        statementService.create(benchCont2.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), bench2.id.value)

        mockMvc
            .perform(getRequestTo("/api/research-fields/benchmarks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].label", equalTo(fieldWithBenchmark.label)))
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

        val benchmark = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId(labelsAndClasses.datasetClass))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId(labelsAndClasses.datasetClass))))

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.create(CreateResourceRequest(null, "Problem 1", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "Problem 2", setOf(ClassId("Problem"))))

        statementService.create(benchPaper.id.value, PredicateId("P30"), fieldWithDataset.id.value)
        statementService.create(benchPaper.id.value, PredicateId("P31"), benchCont.id.value)

        statementService.create(benchCont.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(benchCont.id.value, PredicateId("P32"), problem2.id.value)
        statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.id.value)

        codes.forEach {
            statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.id.value)
        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.id.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/research-field/${fieldWithDataset.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].research_problem.id", equalTo(problem2.id.value)))
            .andExpect(jsonPath("$[1].research_problem.id", equalTo(problem1.id.value)))
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

        val field1 = resourceService.create(CreateResourceRequest(null, "Field with a dataset #1", setOf(ClassId("ResearchField"))))

        val benchPaper = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))

        val benchCont = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))

        val benchmark = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId(labelsAndClasses.datasetClass))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId(labelsAndClasses.datasetClass))))

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.create(CreateResourceRequest(null, "Problem 1", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "Problem 2", setOf(ClassId("Problem"))))

        statementService.create(benchPaper.id.value, PredicateId("P30"), field1.id.value)
        statementService.create(benchPaper.id.value, PredicateId("P31"), benchCont.id.value)

        statementService.create(benchCont.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(benchCont.id.value, PredicateId("P32"), problem2.id.value)
        statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.id.value)

        codes.forEach {
            statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.id.value)
        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.id.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[0].research_problem.id", equalTo(problem2.id.value)))
            .andExpect(jsonPath("$[1].research_problem.id", equalTo(problem1.id.value)))
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

        val field1 = resourceService.create(CreateResourceRequest(null, "Field #1 with a problem #1", setOf(ClassId("ResearchField"))))
        val field2 = resourceService.create(CreateResourceRequest(null, "Field #2 with a problem #1", setOf(ClassId("ResearchField"))))

        val benchPaper = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))
        val dummyPaper = resourceService.create(CreateResourceRequest(null, "Paper 2", setOf(ClassId("Paper"))))

        val benchCont = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))
        val dummyCont = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 2", setOf(ClassId("Contribution"))))

        val benchmark = resourceService.create(CreateResourceRequest(null, "Benchmark #1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val dummyBenchmark = resourceService.create(CreateResourceRequest(null, "Benchmark #2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId(labelsAndClasses.datasetClass))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId(labelsAndClasses.datasetClass))))

        val codes = (1..5).map { literalService.create("https://some-code-$it.cool") }

        val problem1 = resourceService.create(CreateResourceRequest(null, "Problem 1", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "Problem 2", setOf(ClassId("Problem"))))

        statementService.create(benchPaper.id.value, PredicateId("P30"), field1.id.value)
        statementService.create(dummyPaper.id.value, PredicateId("P30"), field2.id.value)

        statementService.create(benchPaper.id.value, PredicateId("P31"), benchCont.id.value)
        statementService.create(dummyPaper.id.value, PredicateId("P31"), dummyCont.id.value)

        statementService.create(benchCont.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(benchCont.id.value, PredicateId("P32"), problem2.id.value)
        statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark.id.value)

        statementService.create(dummyCont.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(dummyCont.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), dummyBenchmark.id.value)

        statementService.create(field1.id.value, PredicateId("P36"), field2.id.value)

        codes.forEach {
            statementService.create(benchCont.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.id.value)
        statementService.create(benchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.id.value)

        statementService.create(dummyBenchmark.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.id.value)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].research_problem.id", containsInAnyOrder(problem1.id.value, problem2.id.value)))
            // Due to limits in JSONPath, we can only filter to the respective object from the list, but not get rid of
            // the list itself, so we need to deal with that. The list of research fields is embedded into a list, so
            // by selecting all elements (via "[*]") we get rid of the innermost list. Because that does not work for
            // the totals, those are checked using a list containing the expected result.
            // Problem 1:
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.id.value}\")].research_fields[*]", hasSize<Int>(2)))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.id.value}\")].total_papers", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.id.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem1.id.value}\")].total_codes", equalTo(listOf(5))))
            // Problem 2:
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.id.value}\")].research_fields[*]", hasSize<Int>(1)))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.id.value}\")].total_papers", equalTo(listOf(1))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.id.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$[?(@.research_problem.id==\"${problem2.id.value}\")].total_codes", equalTo(listOf(5))))
            .andDo(
                document(
                    snippet,
                    benchmarkListResponseFields()
                )
            )
    }

    @Test
    fun fetchResearchProblemsForADataset() {
        val paper = resourceService.create(CreateResourceRequest(null, "Paper", setOf(ClassId("Paper"))))

        val dataset = resourceService.create(CreateResourceRequest(null, "Dataset", setOf(ClassId(labelsAndClasses.datasetClass))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val cont1 = resourceService.create(CreateResourceRequest(null, "Contribution 1", setOf(ClassId("Contribution"))))
        val cont2 = resourceService.create(CreateResourceRequest(null, "Contribution 2", setOf(ClassId("Contribution"))))

        val problem1 = resourceService.create(CreateResourceRequest(null, "Problem 1", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "Problem 2", setOf(ClassId("Problem"))))

        statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)
        statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)

        statementService.create(cont1.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.id.value)
        statementService.create(cont2.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.id.value)

        statementService.create(cont1.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(cont1.id.value, PredicateId("P32"), problem2.id.value)
        statementService.create(cont2.id.value, PredicateId("P32"), problem2.id.value)

        statementService.create(paper.id.value, PredicateId("P31"), cont1.id.value)
        statementService.create(paper.id.value, PredicateId("P31"), cont2.id.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/${dataset.id}/problems"))
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
        val problem = resourceService.create(CreateResourceRequest(null, "Problem with a dataset", setOf(ClassId("Problem"))))

        val paper1 = resourceService.create(CreateResourceRequest(null, "Paper 1", setOf(ClassId("Paper"))))
        val paper2 = resourceService.create(CreateResourceRequest(null, "Paper 2", setOf(ClassId("Paper"))))

        val contributionOfPaper1 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 1", setOf(ClassId("Contribution"))))
        val contributionOfPaper2 = resourceService.create(CreateResourceRequest(null, "Contribution of Paper 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark P1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark P2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val codes = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val models = (1..4).map { resourceService.create(CreateResourceRequest(null, "Model $it", setOf(ClassId(labelsAndClasses.modelClass)))) }

        val dataset1 = resourceService.create(CreateResourceRequest(null, "Dataset 1", setOf(ClassId(labelsAndClasses.datasetClass))))
        val dataset2 = resourceService.create(CreateResourceRequest(null, "Dataset 2", setOf(ClassId(labelsAndClasses.datasetClass))))

        statementService.create(paper1.id.value, PredicateId("P31"), contributionOfPaper1.id.value)
        statementService.create(paper2.id.value, PredicateId("P31"), contributionOfPaper2.id.value)

        statementService.create(contributionOfPaper1.id.value, PredicateId("P32"), problem.id.value)
        statementService.create(contributionOfPaper2.id.value, PredicateId("P32"), problem.id.value)

        statementService.create(contributionOfPaper1.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.id.value)
        statementService.create(contributionOfPaper2.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.id.value)

        models.forEach {
            statementService.create(contributionOfPaper1.id.value, PredicateId(labelsAndClasses.modelPredicate), it.id.value)
        }
        codes.forEach {
            statementService.create(contributionOfPaper2.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset1.id.value)
        statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset2.id.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/research-problem/${problem.id}"))
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
        val dataset = resourceService.create(CreateResourceRequest(null, "some dataset", setOf(ClassId(labelsAndClasses.datasetClass))))

        val problem1 = resourceService.create(CreateResourceRequest(null, "Fancy problem", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "not so fancy problem", setOf(ClassId("Problem"))))

        val paper = resourceService.create(CreateResourceRequest(null, "paper", setOf(ClassId("Paper"))))
        val contribution1 = resourceService.create(CreateResourceRequest(null, "Contribution 1", setOf(ClassId("Contribution"))))
        val contribution2 = resourceService.create(CreateResourceRequest(null, "Contribution 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val model1 = resourceService.create(CreateResourceRequest(null, "Model 1", setOf(ClassId(labelsAndClasses.modelClass))))
        val model2 = resourceService.create(CreateResourceRequest(null, "Model 2", setOf(ClassId(labelsAndClasses.modelClass))))

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3M")

        if (flags.isPapersWithCodeLegacyModelEnabled()) {
            val evaluationB1E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))
            val evaluationB1E2 = resourceService.create(CreateResourceRequest(null, "Evaluation 2", setOf(ClassId("Evaluation"))))
            val evaluationB2E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))

            val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId("Metric"))))
            val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId("Metric"))))

            statementService.create(benchmark1.id.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.id.value)
            statementService.create(benchmark1.id.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.id.value)
            statementService.create(benchmark2.id.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.id.value)

            statementService.create(evaluationB1E1.id.value, PredicateId("HAS_METRIC"), metric1.id.value)
            statementService.create(evaluationB1E2.id.value, PredicateId("HAS_METRIC"), metric2.id.value)
            statementService.create(evaluationB2E1.id.value, PredicateId("HAS_METRIC"), metric1.id.value)

            statementService.create(evaluationB1E1.id.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id.value)
            statementService.create(evaluationB1E2.id.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id.value)
            statementService.create(evaluationB2E1.id.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id.value)
        } else {
            val quantityB1E1 = resourceService.create(CreateResourceRequest(null, "Quantity 1", setOf(ClassId(labelsAndClasses.quantityClass))))
            val quantityB1E2 = resourceService.create(CreateResourceRequest(null, "Quantity 2", setOf(ClassId(labelsAndClasses.quantityClass))))
            val quantityB2E1 = resourceService.create(CreateResourceRequest(null, "Quantity 1", setOf(ClassId(labelsAndClasses.quantityClass))))

            val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId(labelsAndClasses.metricClass))))
            val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId(labelsAndClasses.metricClass))))

            val quantityValueB1E1 = resourceService.create(CreateResourceRequest(null, "Quantity Value 1", setOf(ClassId(labelsAndClasses.quantityValueClass))))
            val quantityValueB1E2 = resourceService.create(CreateResourceRequest(null, "Quantity Value 2", setOf(ClassId(labelsAndClasses.quantityValueClass))))
            val quantityValueB2E1 = resourceService.create(CreateResourceRequest(null, "Quantity Value 3", setOf(ClassId(labelsAndClasses.quantityValueClass))))

            statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E1.id.value)
            statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E2.id.value)
            statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB2E1.id.value)

            statementService.create(quantityB1E1.id.value, PredicateId(labelsAndClasses.metricPredicate), metric1.id.value)
            statementService.create(quantityB1E2.id.value, PredicateId(labelsAndClasses.metricPredicate), metric2.id.value)
            statementService.create(quantityB2E1.id.value, PredicateId(labelsAndClasses.metricPredicate), metric1.id.value)

            statementService.create(quantityB1E1.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1.id.value)
            statementService.create(quantityB1E2.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2.id.value)
            statementService.create(quantityB2E1.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1.id.value)

            statementService.create(quantityValueB1E1.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1.id.value)
            statementService.create(quantityValueB1E2.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2.id.value)
            statementService.create(quantityValueB2E1.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1.id.value)
        }

        statementService.create(paper.id.value, PredicateId("P31"), contribution1.id.value)
        statementService.create(paper.id.value, PredicateId("P31"), contribution2.id.value)

        statementService.create(contribution1.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.id.value)
        statementService.create(contribution2.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.id.value)

        codes1.forEach {
            statementService.create(contribution1.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }
        codes2.forEach {
            statementService.create(contribution2.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(contribution1.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(contribution2.id.value, PredicateId("P32"), problem2.id.value)

        statementService.create(contribution1.id.value, PredicateId(labelsAndClasses.modelPredicate), model1.id.value)
        statementService.create(contribution2.id.value, PredicateId(labelsAndClasses.modelPredicate), model2.id.value)

        statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)
        statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/${dataset.id}/problem/${problem1.id}/summary"))
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
        val dataset = resourceService.create(CreateResourceRequest(null, "some dataset", setOf(ClassId(labelsAndClasses.datasetClass))))

        val problem1 = resourceService.create(CreateResourceRequest(null, "Fancy problem", setOf(ClassId("Problem"))))
        val problem2 = resourceService.create(CreateResourceRequest(null, "not so fancy problem", setOf(ClassId("Problem"))))

        val paper = resourceService.create(CreateResourceRequest(null, "paper", setOf(ClassId("Paper"))))
        val contribution1 = resourceService.create(CreateResourceRequest(null, "Contribution 1", setOf(ClassId("Contribution"))))
        val contribution2 = resourceService.create(CreateResourceRequest(null, "Contribution 2", setOf(ClassId("Contribution"))))

        val benchmark1 = resourceService.create(CreateResourceRequest(null, "Benchmark 1", setOf(ClassId(labelsAndClasses.benchmarkClass))))
        val benchmark2 = resourceService.create(CreateResourceRequest(null, "Benchmark 2", setOf(ClassId(labelsAndClasses.benchmarkClass))))

        val codes1 = (1..3).map { literalService.create("https://some-code-$it.cool") }
        val codes2 = (1..2).map { literalService.create("https://some-code-$it-$it.cool") }

        val scoreOfM1B1E1 = literalService.create("2.55")
        val scoreOfM1B1E2 = literalService.create("4548")
        val scoreOfM1B2E1 = literalService.create("3.2B")

        if (flags.isPapersWithCodeLegacyModelEnabled()) {
            val evaluationB1E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))
            val evaluationB1E2 = resourceService.create(CreateResourceRequest(null, "Evaluation 2", setOf(ClassId("Evaluation"))))
            val evaluationB2E1 = resourceService.create(CreateResourceRequest(null, "Evaluation 1", setOf(ClassId("Evaluation"))))

            val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId("Metric"))))
            val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId("Metric"))))

            statementService.create(benchmark1.id.value, PredicateId("HAS_EVALUATION"), evaluationB1E1.id.value)
            statementService.create(benchmark1.id.value, PredicateId("HAS_EVALUATION"), evaluationB1E2.id.value)
            statementService.create(benchmark2.id.value, PredicateId("HAS_EVALUATION"), evaluationB2E1.id.value)

            statementService.create(benchmark1.id.value, PredicateId("HAS_DATASET"), dataset.id.value)
            statementService.create(benchmark2.id.value, PredicateId("HAS_DATASET"), dataset.id.value)

            statementService.create(evaluationB1E1.id.value, PredicateId("HAS_METRIC"), metric1.id.value)
            statementService.create(evaluationB1E2.id.value, PredicateId("HAS_METRIC"), metric2.id.value)
            statementService.create(evaluationB2E1.id.value, PredicateId("HAS_METRIC"), metric1.id.value)

            statementService.create(evaluationB1E1.id.value, PredicateId("HAS_VALUE"), scoreOfM1B1E1.id.value)
            statementService.create(evaluationB1E2.id.value, PredicateId("HAS_VALUE"), scoreOfM1B1E2.id.value)
            statementService.create(evaluationB2E1.id.value, PredicateId("HAS_VALUE"), scoreOfM1B2E1.id.value)
        } else {
            val quantityB1E1 = resourceService.create(CreateResourceRequest(null, "Quantity 1", setOf(ClassId(labelsAndClasses.quantityClass))))
            val quantityB1E2 = resourceService.create(CreateResourceRequest(null, "Quantity 2", setOf(ClassId(labelsAndClasses.quantityClass))))
            val quantityB2E1 = resourceService.create(CreateResourceRequest(null, "Quantity 1", setOf(ClassId(labelsAndClasses.quantityClass))))

            val metric1 = resourceService.create(CreateResourceRequest(null, "Metric 1", setOf(ClassId(labelsAndClasses.metricClass))))
            val metric2 = resourceService.create(CreateResourceRequest(null, "Metric 2", setOf(ClassId(labelsAndClasses.metricClass))))

            val quantityValueB1E1 = resourceService.create(CreateResourceRequest(null, "Quantity Value 1", setOf(ClassId(labelsAndClasses.quantityValueClass))))
            val quantityValueB1E2 = resourceService.create(CreateResourceRequest(null, "Quantity Value 2", setOf(ClassId(labelsAndClasses.quantityValueClass))))
            val quantityValueB2E1 = resourceService.create(CreateResourceRequest(null, "Quantity Value 3", setOf(ClassId(labelsAndClasses.quantityValueClass))))

            statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E1.id.value)
            statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB1E2.id.value)
            statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.quantityPredicate), quantityB2E1.id.value)

            statementService.create(quantityB1E1.id.value, PredicateId(labelsAndClasses.metricPredicate), metric1.id.value)
            statementService.create(quantityB1E2.id.value, PredicateId(labelsAndClasses.metricPredicate), metric2.id.value)
            statementService.create(quantityB2E1.id.value, PredicateId(labelsAndClasses.metricPredicate), metric1.id.value)

            statementService.create(quantityB1E1.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1.id.value)
            statementService.create(quantityB1E2.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2.id.value)
            statementService.create(quantityB2E1.id.value, PredicateId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1.id.value)

            statementService.create(quantityValueB1E1.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1.id.value)
            statementService.create(quantityValueB1E2.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2.id.value)
            statementService.create(quantityValueB2E1.id.value, PredicateId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1.id.value)
        }
        statementService.create(paper.id.value, PredicateId("P31"), contribution1.id.value)
        statementService.create(paper.id.value, PredicateId("P31"), contribution2.id.value)

        statementService.create(contribution1.id.value, PredicateId("P32"), problem1.id.value)
        statementService.create(contribution2.id.value, PredicateId("P32"), problem2.id.value)

        statementService.create(contribution1.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark1.id.value)
        statementService.create(contribution2.id.value, PredicateId(labelsAndClasses.benchmarkPredicate), benchmark2.id.value)

        codes1.forEach {
            statementService.create(contribution1.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }
        codes2.forEach {
            statementService.create(contribution2.id.value, PredicateId(labelsAndClasses.sourceCodePredicate), it.id.value)
        }

        statementService.create(benchmark1.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)
        statementService.create(benchmark2.id.value, PredicateId(labelsAndClasses.datasetPredicate), dataset.id.value)

        mockMvc
            .perform(getRequestTo("/api/datasets/${dataset.id}/problem/${problem1.id}/summary"))
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
