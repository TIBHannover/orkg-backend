package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
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
@Suppress("HttpUrlsUsage")
internal class DatasetControllerIntegrationTest : MockMvcBaseTest("datasets") {
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
            ThingId(labelsAndClasses.modelPredicate),
            ThingId(labelsAndClasses.metricPredicate),
            ThingId(labelsAndClasses.quantityValuePredicate),
            ThingId(labelsAndClasses.quantityPredicate),
            ThingId(labelsAndClasses.numericValuePredicate),
        )

        classService.createClasses(
            Classes.paper,
            Classes.problem,
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

        get("/api/datasets/{id}/problems", dataset)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
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

        get("/api/datasets/research-problem/{id}", problem)
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

        get("/api/datasets/{id}/problem/{researchProblemId}/summary", dataset, problem1)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
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
}
