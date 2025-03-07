package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
@Suppress("HttpUrlsUsage")
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
        val tempPageable = PageRequest.of(0, 10)

        resourceService.deleteAll()
        predicateService.deleteAll()
        literalService.deleteAll()
        statementService.deleteAll()
        classService.deleteAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicates(
            Predicates.hasResearchField,
            Predicates.hasContribution,
            Predicates.hasResearchProblem,
            Predicates.hasSubfield,
            ThingId(labelsAndClasses.benchmarkPredicate),
            ThingId(labelsAndClasses.datasetPredicate),
            ThingId(labelsAndClasses.sourceCodePredicate),
            ThingId(labelsAndClasses.modelPredicate),
            ThingId(labelsAndClasses.metricPredicate),
            ThingId(labelsAndClasses.quantityValuePredicate),
            ThingId(labelsAndClasses.quantityPredicate),
            ThingId(labelsAndClasses.numericValuePredicate),
        )

        classService.createClasses(
            Classes.paper,
            Classes.problem,
            Classes.researchField,
            Classes.contribution,
        )

        classService.createClass(
            label = "Quantity",
            id = ThingId(labelsAndClasses.quantityClass),
            uri = ParsedIRI("http://qudt.org/2.1/schema/qudt/Quantity")
        )
        classService.createClass(
            label = "Quantity",
            id = ThingId(labelsAndClasses.quantityValueClass),
            uri = ParsedIRI("http://qudt.org/2.1/schema/qudt/QuantityValue")
        )
        classService.createClass(
            label = "Quantity Kind",
            id = ThingId(labelsAndClasses.metricClass),
            uri = ParsedIRI("http://qudt.org/2.1/schema/qudt/QuantityKind")
        )
        classService.createClass("Dataset", ThingId(labelsAndClasses.datasetClass))
        classService.createClass("Benchmark", ThingId(labelsAndClasses.benchmarkClass))
        classService.createClass("Model", ThingId(labelsAndClasses.modelClass))
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

        documentedGetRequestTo("/api/research-fields/benchmarks")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].label", equalTo(fieldWithBenchmarkLabel)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))
            .andDo(
                documentationHandler.document(
                    researchFieldPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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

        documentedGetRequestTo("/api/benchmarks/summary/research-field/{id}", fieldWithDataset)
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research field.")
                    ),
                    benchmarkPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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

        documentedGetRequestTo("/api/benchmarks/summary")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.content[*].research_problem.id", containsInAnyOrder(problem1.value, problem2.value)))
            .andExpect(jsonPath("$.content[0].total_papers", equalTo(1)))
            .andExpect(jsonPath("$.content[0].total_datasets", equalTo(2)))
            .andExpect(jsonPath("$.content[0].total_codes", equalTo(5)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                documentationHandler.document(
                    benchmarkPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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

        documentedGetRequestTo("/api/benchmarks/summary")
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
            .andDo(
                documentationHandler.document(
                    benchmarkPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetchResearchProblemsForADataset() {
        val paper = resourceService.createResource(setOf(Classes.paper), label = "Paper")

        val dataset = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset")

        val benchmark1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 2")

        val cont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")
        val cont2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2")

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Problem 1")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "Problem 2")

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        statementService.createStatement(cont1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.createStatement(cont2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        statementService.createStatement(cont1, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(cont1, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(cont2, Predicates.hasResearchProblem, problem2)

        statementService.createStatement(paper, Predicates.hasContribution, cont1)
        statementService.createStatement(paper, Predicates.hasContribution, cont2)

        documentedGetRequestTo("/api/datasets/{id}/problems", dataset)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the dataset.")
                    ),
                    researchProblemPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetchDatasetForResearchProblem() {
        val problem = resourceService.createResource(setOf(Classes.problem), label = "Problem with a dataset")

        val paper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")

        val contributionOfPaper1 = resourceService.createResource(
            classes = setOf(Classes.contribution),
            label = "Contribution of Paper 1"
        )
        val contributionOfPaper2 = resourceService.createResource(
            classes = setOf(Classes.contribution),
            label = "Contribution of Paper 2"
        )

        val benchmark1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark P1")
        val benchmark2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark P2")

        val codes = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val models = (1..4).map { resourceService.createResource(setOf(ThingId(labelsAndClasses.modelClass)), label = "Model $it") }

        val dataset1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 1")
        val dataset2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "Dataset 2")

        statementService.createStatement(paper1, Predicates.hasContribution, contributionOfPaper1)
        statementService.createStatement(paper2, Predicates.hasContribution, contributionOfPaper2)

        statementService.createStatement(contributionOfPaper1, Predicates.hasResearchProblem, problem)
        statementService.createStatement(contributionOfPaper2, Predicates.hasResearchProblem, problem)

        statementService.createStatement(contributionOfPaper1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.createStatement(contributionOfPaper2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        models.forEach {
            statementService.createStatement(contributionOfPaper1, ThingId(labelsAndClasses.modelPredicate), it)
        }
        codes.forEach {
            statementService.createStatement(contributionOfPaper2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset1)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset2)

        documentedGetRequestTo("/api/datasets/research-problem/{id}", problem)
            .param("sort", "totalModels,DESC")
            .perform()
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
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    datasetListResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetchDatasetSummary() {
        val dataset = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "some dataset")

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf(Classes.paper), label = "paper")
        val contribution1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 2")

        val codes1 = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val codes2 = (1..2).map {
            literalService.createLiteral(label = "https://some-code-$it-$it.cool")
        }

        val model1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.modelClass)), label = "Model 1")
        val model2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.modelClass)), label = "Model 2")

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3M")

        val quantityB1E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")
        val quantityB1E2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 2")
        val quantityB2E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")

        val metric1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 1")
        val metric2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 2")

        val quantityValueB1E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 1"
        )
        val quantityValueB1E2 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 2"
        )
        val quantityValueB2E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 3"
        )

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.createStatement(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.createStatement(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.createStatement(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.createStatement(paper, Predicates.hasContribution, contribution1)
        statementService.createStatement(paper, Predicates.hasContribution, contribution2)

        statementService.createStatement(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.createStatement(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        codes1.forEach {
            statementService.createStatement(contribution1, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }
        codes2.forEach {
            statementService.createStatement(contribution2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.createStatement(contribution1, ThingId(labelsAndClasses.modelPredicate), model1)
        statementService.createStatement(contribution2, ThingId(labelsAndClasses.modelPredicate), model2)

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        documentedGetRequestTo("/api/datasets/{id}/problem/{researchProblemId}/summary", dataset, problem1)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the dataset."),
                        parameterWithName("researchProblemId").description("The identifier of the research problem.")
                    ),
                    datasetSummaryPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetchDatasetSummaryWithoutModels() {
        val dataset = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "some dataset")

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf(Classes.paper), label = "paper")
        val contribution1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 2")

        val codes1 = (1..3).map {
            literalService.createLiteral(label = "https://some-code-$it.cool")
        }
        val codes2 = (1..2).map {
            literalService.createLiteral(label = "https://some-code-$it-$it.cool")
        }

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3.2B")

        val quantityB1E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")
        val quantityB1E2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 2")
        val quantityB2E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")

        val metric1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 1")
        val metric2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 2")

        val quantityValueB1E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 1"
        )
        val quantityValueB1E2 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 2"
        )
        val quantityValueB2E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 3"
        )

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.createStatement(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.createStatement(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.createStatement(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.createStatement(paper, Predicates.hasContribution, contribution1)
        statementService.createStatement(paper, Predicates.hasContribution, contribution2)

        statementService.createStatement(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.createStatement(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.createStatement(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        codes1.forEach {
            statementService.createStatement(contribution1, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }
        codes2.forEach {
            statementService.createStatement(contribution2, ThingId(labelsAndClasses.sourceCodePredicate), it)
        }

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        get("/api/datasets/{id}/problem/{researchProblemId}/summary", dataset, problem1)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun fetchDatasetSummaryWithModelsWithoutCode() {
        // This is a regression test for the bug reported here https://gitlab.com/TIBHannover/orkg/orkg-papers/-/issues/14#note_1426964102

        val dataset = resourceService.createResource(setOf(ThingId(labelsAndClasses.datasetClass)), label = "some dataset")

        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Fancy problem")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "not so fancy problem")

        val paper = resourceService.createResource(setOf(Classes.paper), label = "paper")
        val contribution1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")
        val contribution2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2")

        val benchmark1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 1")
        val benchmark2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.benchmarkClass)), label = "Benchmark 2")

        val model1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.modelClass)), label = "Model 1")
        val model2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.modelClass)), label = "Model 2")

        val scoreOfM1B1E1 = literalService.createLiteral(label = "2.55")
        val scoreOfM1B1E2 = literalService.createLiteral(label = "4548")
        val scoreOfM1B2E1 = literalService.createLiteral(label = "3M")

        val quantityB1E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")
        val quantityB1E2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 2")
        val quantityB2E1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.quantityClass)), label = "Quantity 1")

        val metric1 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 1")
        val metric2 = resourceService.createResource(setOf(ThingId(labelsAndClasses.metricClass)), label = "Metric 2")

        val quantityValueB1E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 1"
        )
        val quantityValueB1E2 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 2"
        )
        val quantityValueB2E1 = resourceService.createResource(
            classes = setOf(ThingId(labelsAndClasses.quantityValueClass)),
            label = "Quantity Value 3"
        )

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E1)
        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.quantityPredicate), quantityB1E2)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.quantityPredicate), quantityB2E1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.metricPredicate), metric1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.metricPredicate), metric2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.metricPredicate), metric1)

        statementService.createStatement(quantityB1E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E1)
        statementService.createStatement(quantityB1E2, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB1E2)
        statementService.createStatement(quantityB2E1, ThingId(labelsAndClasses.quantityValuePredicate), quantityValueB2E1)

        statementService.createStatement(quantityValueB1E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E1)
        statementService.createStatement(quantityValueB1E2, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B1E2)
        statementService.createStatement(quantityValueB2E1, ThingId(labelsAndClasses.numericValuePredicate), scoreOfM1B2E1)

        statementService.createStatement(paper, Predicates.hasContribution, contribution1)
        statementService.createStatement(paper, Predicates.hasContribution, contribution2)

        statementService.createStatement(contribution1, ThingId(labelsAndClasses.benchmarkPredicate), benchmark1)
        statementService.createStatement(contribution2, ThingId(labelsAndClasses.benchmarkPredicate), benchmark2)

        statementService.createStatement(contribution1, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(contribution2, Predicates.hasResearchProblem, problem2)

        statementService.createStatement(contribution1, ThingId(labelsAndClasses.modelPredicate), model1)
        statementService.createStatement(contribution2, ThingId(labelsAndClasses.modelPredicate), model2)

        statementService.createStatement(benchmark1, ThingId(labelsAndClasses.datasetPredicate), dataset)
        statementService.createStatement(benchmark2, ThingId(labelsAndClasses.datasetPredicate), dataset)

        get("/api/datasets/{id}/problem/{researchProblemId}/summary", dataset, problem1)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    private fun researchFieldResponseFields() =
        listOf(
            fieldWithPath("id").description("Research field ID").type(String::class).optional(),
            fieldWithPath("label").description("Research field label").type(String::class).optional()
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
