package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.community.input.ContributorUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createList
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
internal class ProblemControllerIntegrationTest : RestDocsTest("research-problems") {

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
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        classService.createClasses("Problem", "Contribution", "Author", "Paper")
        predicateService.createPredicate(Predicates.hasResearchProblem)
        predicateService.createPredicate(Predicates.hasAuthors)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.hasListElement)
    }

    @Test
    fun getUsersPerProblem() {
        val predicate = Predicates.hasResearchProblem
        val problem = resourceService.createResource(
            classes = setOf("Problem"),
            label = "save the world"
        )
        val contributorId = contributorService.createContributor()
        val contribution = resourceService.createResource(
            classes = setOf("Contribution"),
            label = "Be healthy",
            userId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL
        )

        statementService.create(contributorId, contribution, predicate, problem)

        documentedGetRequestTo("/api/problems/{id}/users", problem)
            .param("size", "4")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    queryParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("size").description("Number of items to fetch per page (default: 10)").optional()
                    ),
                    listOfUsersPerProblemResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun getAuthorsPerProblem() {
        // Create authors
        val author1 = resourceService.createResource(setOf("Author"), label = "Author A")
        val author2 = literalService.createLiteral(label = "Author B")
        // Create papers
        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf("Paper"), label = "Paper 2")
        val paper3 = resourceService.createResource(setOf("Paper"), label = "Paper 3")
        val paper4 = resourceService.createResource(setOf("Paper"), label = "Paper 4")
        // Create contributions
        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1 of Paper 1")
        val cont2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2 of Paper 1")
        val cont3 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1 of Paper 2")
        val cont4 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1 of Paper 3")
        val cont5 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1 of Paper 4")
        // Create problems
        val problem1 = resourceService.createResource(setOf("Problem"), label = "Problem X")
        val problem2 = resourceService.createResource(setOf("Problem"), label = "Problem Y")

        // Link authors to papers
        val paper1AuthorsList = listService.createList("Authors", listOf(author1, author2))
        val paper2AuthorsList = listService.createList("Authors", listOf(author2))
        val paper3AuthorsList = listService.createList("Authors", listOf(author1))
        val paper4AuthorsList = listService.createList("Authors", listOf(author2))

        statementService.create(paper1, Predicates.hasAuthors, paper1AuthorsList)
        statementService.create(paper2, Predicates.hasAuthors, paper2AuthorsList)
        statementService.create(paper3, Predicates.hasAuthors, paper3AuthorsList)
        statementService.create(paper4, Predicates.hasAuthors, paper4AuthorsList)

        // Link papers to contributions
        statementService.create(paper1, Predicates.hasContribution, cont1)
        statementService.create(paper1, Predicates.hasContribution, cont2)
        statementService.create(paper2, Predicates.hasContribution, cont3)
        statementService.create(paper3, Predicates.hasContribution, cont4)
        statementService.create(paper4, Predicates.hasContribution, cont5)

        // Link problems to contributions
        statementService.create(cont1, Predicates.hasResearchProblem, problem1)
        statementService.create(cont2, Predicates.hasResearchProblem, problem2)
        statementService.create(cont3, Predicates.hasResearchProblem, problem2)
        statementService.create(cont4, Predicates.hasResearchProblem, problem1)
        statementService.create(cont5, Predicates.hasResearchProblem, problem2)

        documentedGetRequestTo("/api/problems/{id}/authors", problem1)
            .param("page", "0")
            .param("size", "1")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].papers").value(2))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    authorsOfPaperResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    private fun authorsOfPaperResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix(
                "content[].",
                listOf(
                    fieldWithPath("author").description("The author which is using the research problem"),
                    fieldWithPath("author.value").type("string").description("author name"),
                    fieldWithPath("author.value").type("resource").description("the author resource object"),
                    fieldWithPath("papers").description("The number of papers composed by the author"),
                ),
            )
            .andWithPrefix("content[].author.value.", resourceResponseFields())
            .andWithPrefix("")

    private fun usersPerProblemResponseFields() = listOf(
        fieldWithPath("user").description("The user object"),
        fieldWithPath("user.id").description("The UUID of the user in the system"),
        fieldWithPath("user.gravatar_id").description("The gravatar id of the user"),
        fieldWithPath("user.display_name").description("The user's display name"),
        fieldWithPath("user.avatar_url").description("The user's avatar url (gravatar url)"),
        fieldWithPath("user.joined_at").description("the datetime when the user was created"),
        fieldWithPath("user.organization_id").description("the organization id that this user belongs to").optional(),
        fieldWithPath("user.observatory_id").description("the observatory id that this user belongs to").optional(),
        fieldWithPath("contributions").description("The number of contributions this user created")
    )

    fun listOfUsersPerProblemResponseFields(): ResponseFieldsSnippet =
        responseFields(fieldWithPath("[]").description("A list of users"))
            .andWithPrefix("[].", usersPerProblemResponseFields())
}
