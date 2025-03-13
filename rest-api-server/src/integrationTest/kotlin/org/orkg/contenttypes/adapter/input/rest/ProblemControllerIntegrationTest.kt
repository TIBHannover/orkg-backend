package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.community.input.ContributorUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createList
import org.orkg.createLiteral
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
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
internal class ProblemControllerIntegrationTest : MockMvcBaseTest("research-problems") {
    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var listService: ListUseCases

    @BeforeEach
    fun setup() {
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()

        classService.createClasses(
            Classes.problem,
            Classes.contribution,
            Classes.author,
            Classes.paper,
        )

        predicateService.createPredicates(
            Predicates.hasResearchProblem,
            Predicates.hasAuthors,
            Predicates.hasContribution,
            Predicates.hasListElement,
        )
    }

    @Test
    fun getUsersPerProblem() {
        val predicate = Predicates.hasResearchProblem
        val problem = resourceService.createResource(
            classes = setOf(Classes.problem),
            label = "save the world"
        )
        val contributorId = contributorService.createContributor()
        val contribution = resourceService.createResource(
            classes = setOf(Classes.contribution),
            label = "Be healthy",
            userId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL
        )

        statementService.createStatement(
            contributorId = contributorId,
            subject = contribution,
            predicate = predicate,
            `object` = problem
        )

        get("/api/problems/{id}/users", problem)
            .param("size", "4")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun getAuthorsPerProblem() {
        // Create authors
        val author1 = resourceService.createResource(setOf(Classes.author), label = "Author A")
        val author2 = literalService.createLiteral(label = "Author B")
        // Create papers
        val paper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")
        val paper3 = resourceService.createResource(setOf(Classes.paper), label = "Paper 3")
        val paper4 = resourceService.createResource(setOf(Classes.paper), label = "Paper 4")
        // Create contributions
        val cont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1 of Paper 1")
        val cont2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2 of Paper 1")
        val cont3 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1 of Paper 2")
        val cont4 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1 of Paper 3")
        val cont5 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1 of Paper 4")
        // Create problems
        val problem1 = resourceService.createResource(setOf(Classes.problem), label = "Problem X")
        val problem2 = resourceService.createResource(setOf(Classes.problem), label = "Problem Y")

        // Link authors to papers
        val paper1AuthorsList = listService.createList("Authors", listOf(author1, author2))
        val paper2AuthorsList = listService.createList("Authors", listOf(author2))
        val paper3AuthorsList = listService.createList("Authors", listOf(author1))
        val paper4AuthorsList = listService.createList("Authors", listOf(author2))

        statementService.createStatement(paper1, Predicates.hasAuthors, paper1AuthorsList)
        statementService.createStatement(paper2, Predicates.hasAuthors, paper2AuthorsList)
        statementService.createStatement(paper3, Predicates.hasAuthors, paper3AuthorsList)
        statementService.createStatement(paper4, Predicates.hasAuthors, paper4AuthorsList)

        // Link papers to contributions
        statementService.createStatement(paper1, Predicates.hasContribution, cont1)
        statementService.createStatement(paper1, Predicates.hasContribution, cont2)
        statementService.createStatement(paper2, Predicates.hasContribution, cont3)
        statementService.createStatement(paper3, Predicates.hasContribution, cont4)
        statementService.createStatement(paper4, Predicates.hasContribution, cont5)

        // Link problems to contributions
        statementService.createStatement(cont1, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(cont2, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(cont3, Predicates.hasResearchProblem, problem2)
        statementService.createStatement(cont4, Predicates.hasResearchProblem, problem1)
        statementService.createStatement(cont5, Predicates.hasResearchProblem, problem2)

        get("/api/problems/{id}/authors", problem1)
            .param("page", "0")
            .param("size", "1")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].papers").value(2))
    }
}
