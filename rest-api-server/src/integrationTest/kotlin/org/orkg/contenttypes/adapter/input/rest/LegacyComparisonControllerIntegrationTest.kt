package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createClasses
import org.orkg.createList
import org.orkg.createLiteral
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.LiteralService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class LegacyComparisonControllerIntegrationTest : MockMvcBaseTest("comparisons") {
    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var literalService: LiteralService

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var listService: ListUseCases

    @BeforeEach
    fun setup() {
        statementService.deleteAll()
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
        literalService.deleteAll()

        // Init classes
        classService.createClasses(
            Classes.author,
            Classes.paper,
            Classes.contribution,
            Classes.comparison,
        )

        // Init predicates
        predicateService.createPredicates(
            Predicates.hasAuthors,
            Predicates.hasContribution,
            Predicates.comparesContribution,
            Predicates.yearPublished,
            Predicates.hasListElement,
        )
    }

    @Test
    fun getTopAuthorsOfComparison() {
        // create authors
        val author1 = literalService.createLiteral(label = "Duplicate Author")
        val author2 = literalService.createLiteral(label = "Duplicate Author")
        val authorNotNeeded = literalService.createLiteral(label = "Ignore me")
        val authorResource = resourceService.createResource(label = "Famous author", classes = setOf(Classes.author))
        // create papers
        val paper1 = resourceService.createResource(label = "Paper 1", classes = setOf(Classes.paper))
        val paper2 = resourceService.createResource(label = "Paper 2", classes = setOf(Classes.paper))
        val paper3 = resourceService.createResource(label = "Paper 3", classes = setOf(Classes.paper))
        val paper4 = resourceService.createResource(label = "Paper 4", classes = setOf(Classes.paper))

        // create year
        val year = literalService.createLiteral(label = "2018")

        // create contributions
        val cont1 = resourceService.createResource(label = "Contribution of Paper 1", classes = setOf(Classes.contribution))
        val cont2 = resourceService.createResource(label = "Contribution of Paper 2", classes = setOf(Classes.contribution))
        val cont3 = resourceService.createResource(label = "Contribution of Paper 3", classes = setOf(Classes.contribution))
        // create comparison
        val comparison = resourceService.createResource(label = "Comparison", classes = setOf(Classes.comparison))

        // Link authors to papers
        val paper1AuthorsList = listService.createList("Authors", listOf(author1))
        val paper2AuthorsList = listService.createList("Authors", listOf(author2))
        val paper3AuthorsList = listService.createList("Authors", listOf(authorResource))
        val paper4AuthorsList = listService.createList("Authors", listOf(authorNotNeeded))

        statementService.createStatement(paper1, Predicates.hasAuthors, paper1AuthorsList)
        statementService.createStatement(paper2, Predicates.hasAuthors, paper2AuthorsList)
        statementService.createStatement(paper3, Predicates.hasAuthors, paper3AuthorsList)
        statementService.createStatement(paper4, Predicates.hasAuthors, paper4AuthorsList)

        // Link paper 1 to year
        statementService.createStatement(paper1, Predicates.yearPublished, year)

        // Link papers to contributions
        statementService.createStatement(paper1, Predicates.hasContribution, cont1)
        statementService.createStatement(paper2, Predicates.hasContribution, cont2)
        statementService.createStatement(paper3, Predicates.hasContribution, cont3)

        // Link comparison to contributions
        statementService.createStatement(comparison, Predicates.comparesContribution, cont1)
        statementService.createStatement(comparison, Predicates.comparesContribution, cont2)
        statementService.createStatement(comparison, Predicates.comparesContribution, cont3)

        documentedGetRequestTo("/api/comparisons/{id}/authors", comparison)
            .param("size", "2")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.page.total_elements").value(2))
            .andExpect(jsonPath("$.content[0].info[?(@.paper_id == '$paper1')].paper_year").value(2018))
            .andExpect(
                jsonPath(
                    "$.content[0].info[?(@.paper_id == '$paper2')][0].paper_year",
                    anyOf(nullValue(), `is`<List<Int?>>(emptyList()))
                )
            )
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison.")
                    ),
                    authorsOfComparisonResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    private fun authorsOfComparisonResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix(
                "content[].",
                listOf(
                    fieldWithPath("author").description("author"),
                    fieldWithPath("author.value").type("string").description("The author name"),
                    *applyPathPrefix("author.value.", resourceResponseFields()).toTypedArray(),
                    fieldWithPath("info[]").description("Information about the compared papers of each author"),
                    fieldWithPath("info[].paper_id").description("The paper resource ID"),
                    fieldWithPath("info[].paper_year").description("The year in which the paper was published (optional)").optional(),
                    fieldWithPath("info[].author_index").description("Zero-based index of the author in the authors list")
                ),
            )
            .andWithPrefix("content[].author.value.", resourceResponseFields())
            .andWithPrefix("")
}
