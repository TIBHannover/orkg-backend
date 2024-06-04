package org.orkg.contenttypes.adapter.input.rest

import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.LabelAndClassService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createClasses
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("HttpUrlsUsage")
@DisplayName("Benchmark Controller")
@Import(MockUserDetailsService::class)
class BenchmarkControllerTest : RestDocumentationBaseTest() {

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
        literalService.removeAll()
        statementService.removeAll()
        classService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicate(Predicates.hasResearchField)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.hasResearchProblem)
        predicateService.createPredicate(Predicates.hasSubfield)

        listOf(
            labelsAndClasses.benchmarkPredicate,
            labelsAndClasses.datasetPredicate,
            labelsAndClasses.sourceCodePredicate,
            labelsAndClasses.modelPredicate,
            labelsAndClasses.metricPredicate,
            labelsAndClasses.quantityValuePredicate,
            labelsAndClasses.quantityPredicate,
            labelsAndClasses.numericValuePredicate
        ).forEach { predicateService.createPredicate(ThingId(it)) }

        classService.createClasses("Paper", "Problem", "ResearchField", "Contribution")

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

        statementService.create(benchPaper1, Predicates.hasResearchField, fieldWithBenchmark)
        statementService.create(benchPaper2, Predicates.hasResearchField, fieldWithBenchmark)
        statementService.create(normalPaper, Predicates.hasResearchField, fieldWithoutBenchmark)

        statementService.create(benchPaper1, Predicates.hasContribution, benchCont1)
        statementService.create(benchPaper2, Predicates.hasContribution, benchCont2)
        statementService.create(normalPaper, Predicates.hasContribution, normalCont1)

        statementService.create(benchCont1, ThingId(labelsAndClasses.benchmarkPredicate), bench1)
        statementService.create(benchCont2, ThingId(labelsAndClasses.benchmarkPredicate), bench2)

        mockMvc
            .perform(getRequestTo("/api/research-fields/benchmarks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].label", equalTo(fieldWithBenchmarkLabel)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))
            .andDo(
                document(
                    snippet,
                    researchFieldPageResponseFields()
                )
            )
    }

    /**
     * Test setup:
     *
     * ```
     *   (benchPaper:Paper)--->(fieldWithDataset:ResearchField)
     *         |
     *         v
     *    (benchCont)---->(problem1:Problem)
     *               | \->(problem2:Problem)
     *               +--->(benchmark:Benchmark)
     *               |          |--->(dataset1:Dataset)
     *               |          \--->(dataset2:Dataset)
     *               \----(code:Literal) // 5x
     * ```
     */
    @Test
    fun fetchBenchmarkSummaryForResearchField() {
        val fieldWithDataset = resourceService.createResource(setOf("ResearchField"), label = "Field with a dataset")

        val benchPaper = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper, Predicates.hasResearchField, fieldWithDataset)
        statementService.create(benchPaper, Predicates.hasContribution, benchCont)

        statementService.create(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.create(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.create(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        codes.forEach {
            statementService.create(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/research-field/$fieldWithDataset?sort=problem.id,ASC"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            // The resulting summaries are "duplicates", but listed for each research problem:
            .andExpect(jsonPath("$.content[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            .andExpect(jsonPath("$.content[0].research_fields[*].label", equalTo(listOf("Field with a dataset"))))
            .andExpect(jsonPath("$.content[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$.content[0].total_codes", equalTo(5)))
            .andExpect(jsonPath("$.content[1].research_fields[*].label", equalTo(listOf("Field with a dataset"))))
            .andExpect(jsonPath("$.content[1].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[1].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$.content[1].total_codes", equalTo(5)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    benchmarkPageResponseFields()
                )
            )
    }

    @Test
    fun fetchBenchmarkSummaries() {
        val field1 = resourceService.createResource(setOf("ResearchField"), label = "Field with a dataset #1")

        val benchPaper = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf("Contribution"), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper, Predicates.hasResearchField, field1)
        statementService.create(benchPaper, Predicates.hasContribution, benchCont)

        statementService.create(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.create(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.create(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        codes.forEach {
            statementService.create(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            .andExpect(jsonPath("$.content[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$.content[0].total_codes", equalTo(5)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    benchmarkPageResponseFields()
                )
            )
    }

    @Test
    fun aggregateProblemsThatBelongToMultipleResearchFieldsWhenFetchingSummary() {
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

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem 2")

        statementService.create(benchPaper, Predicates.hasResearchField, field1)
        statementService.create(dummyPaper, Predicates.hasResearchField, field2)

        statementService.create(benchPaper, Predicates.hasContribution, benchCont)
        statementService.create(dummyPaper, Predicates.hasContribution, dummyCont)

        statementService.create(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.create(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.create(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        statementService.create(dummyCont, Predicates.hasResearchProblem, problem1)
        statementService.create(dummyCont, ThingId(labelsAndClasses.benchmarkPredicate), dummyBenchmark)

        statementService.create(field1, Predicates.hasSubfield, field2)

        codes.forEach {
            statementService.create(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.create(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        statementService.create(dummyBenchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        mockMvc
            .perform(getRequestTo("/api/benchmarks/summary/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            // Due to limits in JSONPath, we can only filter to the respective object from the list, but not get rid of
            // the list itself, so we need to deal with that. The list of research fields is embedded into a list, so
            // by selecting all elements (via "[*]") we get rid of the innermost list. Because that does not work for
            // the totals, those are checked using a list containing the expected result.
            // Problem 1:
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem1.value}\")].research_fields[*]", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem1.value}\")].total_papers", equalTo(listOf(2))))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem1.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem1.value}\")].total_codes", equalTo(listOf(5))))
            // Problem 2:
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem2.value}\")].research_fields[*]", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem2.value}\")].total_papers", equalTo(listOf(1))))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem2.value}\")].total_datasets", equalTo(listOf(2))))
            .andExpect(jsonPath("$.content[?(@.research_problem.id==\"${problem2.value}\")].total_codes", equalTo(listOf(5))))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    benchmarkPageResponseFields()
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

        statementService.create(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.create(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        statementService.create(cont1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.create(cont2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        statementService.create(cont1, Predicates.hasResearchProblem, problem1)
        statementService.create(cont1, Predicates.hasResearchProblem, problem2)
        statementService.create(cont2, Predicates.hasResearchProblem, problem2)

        statementService.create(paper, Predicates.hasContribution, cont1)
        statementService.create(paper, Predicates.hasContribution, cont2)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    researchProblemPageResponseFields()
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

        val codes = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val models = (1..4).map { resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model $it") }

        val dataset1 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "Dataset 2")

        statementService.create(paper1, Predicates.hasContribution, contributionOfPaper1)
        statementService.create(paper2, Predicates.hasContribution, contributionOfPaper2)

        statementService.create(contributionOfPaper1, Predicates.hasResearchProblem, problem)
        statementService.create(contributionOfPaper2, Predicates.hasResearchProblem, problem)

        statementService.create(contributionOfPaper1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.create(contributionOfPaper2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        models.forEach {
            statementService.create(contributionOfPaper1, ThingId(labelsAndClasses.modelPredicate), it)
        }
        codes.forEach {
            statementService.create(contributionOfPaper2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.create(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        mockMvc
            .perform(getRequestTo("/api/datasets/research-problem/$problem?sort=totalModels,DESC"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[0].total_models", equalTo(4)))
            .andExpect(jsonPath("$.content[0].total_codes", equalTo(0)))
            .andExpect(jsonPath("$.content[1].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[1].total_models", equalTo(0)))
            .andExpect(jsonPath("$.content[1].total_codes", equalTo(3)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
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

        val codes1 = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val codes2 = (1..2).map {
            literalService.createLiteral(label = "https://some-code-$it-$it.cool")
        }

        val model1 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 1")
        val model2 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 2")

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3M")

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

        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.create(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.create(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.create(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.create(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.create(paper, Predicates.hasContribution, contribution1)
        statementService.create(paper, Predicates.hasContribution, contribution2)

        statementService.create(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.create(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        codes1.forEach {
            statementService.create(contribution1, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }
        codes2.forEach {
            statementService.create(contribution2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.create(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.create(contribution1, ThingId(labelsAndClasses.modelPredicate), model1)
        statementService.create(contribution2, ThingId(labelsAndClasses.modelPredicate), model2)

        statementService.create(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.create(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problem/$problem1/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    datasetSummaryPageResponseFields()
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

        val codes1 = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val codes2 = (1..2).map {
            literalService.createLiteral(label = "https://some-code-$it-$it.cool")
        }

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3.2B")

        val quantityB1E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")
        val quantityB1E2 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 2")
        val quantityB2E1 = resourceService.createResource(setOf(labelsAndClasses.quantityClass), label = "Quantity 1")

        val metric1 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 1")
        val metric2 = resourceService.createResource(setOf(labelsAndClasses.metricClass), label = "Metric 2")

        val quantityValueB1E1 = resourceService.createResource(
            classes = setOf(labelsAndClasses.quantityValueClass), label = "Quantity Value 1"
        )
        val quantityValueB1E2 = resourceService.createResource(
            classes = setOf(labelsAndClasses.quantityValueClass), label = "Quantity Value 2"
        )
        val quantityValueB2E1 = resourceService.createResource(
            classes = setOf(labelsAndClasses.quantityValueClass), label = "Quantity Value 3"
        )

        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.create(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.create(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.create(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.create(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.create(paper, Predicates.hasContribution, contribution1)
        statementService.create(paper, Predicates.hasContribution, contribution2)

        statementService.create(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.create(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.create(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.create(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        codes1.forEach {
            statementService.create(contribution1, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }
        codes2.forEach {
            statementService.create(contribution2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.create(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.create(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problem/$problem1/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    datasetSummaryPageResponseFields()
                )
            )
    }

    @Test
    fun fetchDatasetSummaryWithModelsWithoutCode() {
        // This is a regression test for the bug reported here https://gitlab.com/TIBHannover/orkg/orkg-papers/-/issues/14#note_1426964102

        val dataset = resourceService.createResource(setOf(labelsAndClasses.datasetClass), label = "some dataset")

        val problem1 = resourceService.createResource(setOf("Problem"), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf("Paper"), label = "paper")
        val contribution1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(labelsAndClasses.benchmarkClass), label = "Benchmark 2")

        val model1 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 1")
        val model2 = resourceService.createResource(setOf(labelsAndClasses.modelClass), label = "Model 2")

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3M")

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

        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.create(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.create(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.create(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.create(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.create(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.create(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.create(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.create(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.create(paper, Predicates.hasContribution, contribution1)
        statementService.create(paper, Predicates.hasContribution, contribution2)

        statementService.create(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.create(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        statementService.create(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.create(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.create(contribution1, ThingId(labelsAndClasses.modelPredicate), model1)
        statementService.create(contribution2, ThingId(labelsAndClasses.modelPredicate), model2)

        statementService.create(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.create(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        mockMvc
            .perform(getRequestTo("/api/datasets/$dataset/problem/$problem1/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    datasetSummaryPageResponseFields()
                )
            )
    }

    private fun researchFieldResponseFields() =
        listOf(
            fieldWithPath("id").description("Research field ID").type(String::class).optional(), // FIXME: PwC
            fieldWithPath("label").description("Research field label").type(String::class).optional() // FIXME: PwC
        )

    private fun researchFieldPageResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", researchFieldResponseFields())
            .andWithPrefix("")

    private fun researchProblemResponseFields() =
        listOf(
            fieldWithPath("id").description("Research problem ID"),
            fieldWithPath("label").description("Research problem label")
        )

    private fun researchProblemPageResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", researchProblemResponseFields())
            .andWithPrefix("")

    private fun benchmarkResponseFields() =
        listOf(
            fieldWithPath("total_papers").description("Total number of papers"),
            fieldWithPath("total_datasets").description("Total number of datasets related"),
            fieldWithPath("total_codes").description("Total number of code urls"),
            fieldWithPath("research_problem").description("Research problem concerned with this research field"),
            fieldWithPath("research_fields").description("List of RFs for a benchmark summary").optional()
        )

    private fun benchmarkPageResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", benchmarkResponseFields())
            .andWithPrefix("content[].research_fields.[].", researchFieldResponseFields())
            .andWithPrefix("content[].research_problem.", researchProblemResponseFields())
            .andWithPrefix("")

    private fun datasetResponseFields() =
        listOf(
            fieldWithPath("id").description("Id of the dataset"),
            fieldWithPath("label").description("The label of the dataset"),
            fieldWithPath("total_papers").description("Total number of papers"),
            fieldWithPath("total_models").description("Total number of models"),
            fieldWithPath("total_codes").description("Total number of code urls")
        )

    private fun datasetListResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", datasetResponseFields())
            .andWithPrefix("")

    private fun datasetSummaryResponseFields() =
        listOf(
            fieldWithPath("model_name").description("The model name used on the dataset").optional(),
            fieldWithPath("model_id").description("The model id used on the dataset").optional(),
            fieldWithPath("metric").description("The metric used in the evaluation"),
            fieldWithPath("score").description("the score of the evaluation with the corresponding metric"),
            fieldWithPath("paper_id").description("The paper id is where the evaluation is published"),
            fieldWithPath("paper_title").description("The paper title is where the evaluation is published"),
            fieldWithPath("paper_month").description("The month when the paper was published").optional(),
            fieldWithPath("paper_year").description("The year when the paper was published").optional(),
            fieldWithPath("code_urls").description("A list of urls for the codes specified in the papers")
        )

    private fun datasetSummaryPageResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", datasetSummaryResponseFields())
            .andWithPrefix("")
}
