package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.LabelAndClassService
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class BenchmarkControllerIntegrationTest : MockMvcBaseTest("benchmarks") {
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
        resourceService.deleteAll()
        predicateService.deleteAll()
        literalService.deleteAll()
        classService.deleteAll()

        assertThat(resourceService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(predicateService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(statementService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(classService.findAll(PageRequests.SINGLE)).hasSize(0)

        predicateService.createPredicates(
            Predicates.hasResearchField,
            Predicates.hasContribution,
            Predicates.hasResearchProblem,
            Predicates.hasSubfield,
            ThingId(labelsAndClasses.benchmarkPredicate),
            ThingId(labelsAndClasses.datasetPredicate),
            ThingId(labelsAndClasses.sourceCodePredicate),
        )

        classService.createClasses(
            Classes.paper,
            Classes.problem,
            Classes.researchField,
            Classes.contribution,
        )

        classService.createClass("Dataset", ThingId(labelsAndClasses.datasetClass))
        classService.createClass("Benchmark", ThingId(labelsAndClasses.benchmarkClass))
    }

    @Test
    fun fetchResearchFieldsWithBenchmarks() {
        val fieldWithBenchmarkLabel = "Field 1"
        val fieldWithBenchmark = resourceService.createResource(setOf(Classes.researchField), label = fieldWithBenchmarkLabel)
        val fieldWithoutBenchmark = resourceService.createResource(setOf(Classes.researchField), label = "Field 2")

        val benchPaper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val benchPaper2 = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")
        val normalPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 3")

        val benchCont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 1")
        val benchCont2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 2")
        val normalCont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 3")

        val bench1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")
        val bench2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 2")

        statementService.createStatement(benchPaper1, Predicates.hasResearchField, fieldWithBenchmark)
        statementService.createStatement(benchPaper2, Predicates.hasResearchField, fieldWithBenchmark)
        statementService.createStatement(normalPaper, Predicates.hasResearchField, fieldWithoutBenchmark)

        statementService.createStatement(benchPaper1, Predicates.hasContribution, benchCont1)
        statementService.createStatement(benchPaper2, Predicates.hasContribution, benchCont2)
        statementService.createStatement(normalPaper, Predicates.hasContribution, normalCont1)

        statementService.createStatement(benchCont1, ThingId(labelsAndClasses.benchmarkPredicate), bench1)
        statementService.createStatement(benchCont2, ThingId(labelsAndClasses.benchmarkPredicate), bench2)

        get("/api/research-fields/benchmarks")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].label", equalTo(fieldWithBenchmarkLabel)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))
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
        val fieldWithDataset = resourceService.createResource(setOf(Classes.researchField), label = "Field with a dataset")

        val benchPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 2")

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "Problem 2")

        statementService.createStatement(benchPaper, Predicates.hasResearchField, fieldWithDataset)
        statementService.createStatement(benchPaper, Predicates.hasContribution, benchCont)

        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        codes.forEach {
            statementService.createStatement(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        get("/api/benchmarks/summary/research-field/{id}", fieldWithDataset)
            .param("sort", "problem.id", "ASC")
            .perform()
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
    }

    @Test
    fun fetchBenchmarkSummaries() {
        val field1 = resourceService.createResource(setOf(Classes.researchField), label = "Field with a dataset #1")

        val benchPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")

        val benchCont = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 1")

        val benchmark = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")

        val dataset1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 2")

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "Problem 2")

        statementService.createStatement(benchPaper, Predicates.hasResearchField, field1)
        statementService.createStatement(benchPaper, Predicates.hasContribution, benchCont)

        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        codes.forEach {
            statementService.createStatement(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        get("/api/benchmarks/summary")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            .andExpect(jsonPath("$.content[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$.content[0].total_codes", equalTo(5)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun aggregateProblemsThatBelongToMultipleResearchFieldsWhenFetchingSummary() {
        val field1 = resourceService.createResource(setOf(Classes.researchField), label = "Field #1 with a problem #1")
        val field2 = resourceService.createResource(setOf(Classes.researchField), label = "Field #2 with a problem #1")

        val benchPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val dummyPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")

        val benchCont = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 1")
        val dummyCont = resourceService.createResource(setOf(Classes.contribution), label = "Contribution of Paper 2")

        val benchmark = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark #1")
        val dummyBenchmark = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.benchmarkClass)),
            label = "Benchmark #2"
        )

        val dataset1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 2")

        val codes = (1..5).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "Problem 2")

        statementService.createStatement(benchPaper, Predicates.hasResearchField, field1)
        statementService.createStatement(dummyPaper, Predicates.hasResearchField, field2)

        statementService.createStatement(benchPaper, Predicates.hasContribution, benchCont)
        statementService.createStatement(dummyPaper, Predicates.hasContribution, dummyCont)

        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(benchCont, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(benchCont, ThingId(labelsAndClasses.benchmarkPredicate), benchmark)

        statementService.createStatement(dummyCont, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(dummyCont, ThingId(labelsAndClasses.benchmarkPredicate), dummyBenchmark)

        statementService.createStatement(field1, Predicates.hasSubfield, field2)

        codes.forEach {
            statementService.createStatement(benchCont, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.createStatement(benchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        statementService.createStatement(dummyBenchmark, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        get("/api/benchmarks/summary")
            .perform()
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
    }
}
