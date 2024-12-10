package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createClasses
import org.orkg.createList
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import org.orkg.graph.domain.LiteralService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
@DisplayName("Comparison Controller")
@Import(MockUserDetailsService::class)
internal class LegacyComparisonControllerIntegrationTest : RestDocsTest("comparisons") {

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
        statementService.removeAll()
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        literalService.removeAll()

        // Init classes
        classService.createClasses("Author", "Paper", "Contribution", "Comparison")

        // Init predicates
        predicateService.createPredicate(Predicates.hasAuthors)
        predicateService.createPredicate(Predicates.hasContribution)
        predicateService.createPredicate(Predicates.comparesContribution)
        predicateService.createPredicate(Predicates.yearPublished)
        predicateService.createPredicate(Predicates.hasListElement)
    }

    @Test
    fun getTopAuthorsOfComparison() {
        // create authors
        val author1 = literalService.createLiteral(label = "Duplicate Author")
        val author2 = literalService.createLiteral(label = "Duplicate Author")
        val authorNotNeeded = literalService.createLiteral(label = "Ignore me")
        val authorResource = resourceService.createResource(label = "Famous author", classes = setOf("Author"))
        // create papers
        val paper1 = resourceService.createResource(label = "Paper 1", classes = setOf("Paper"))
        val paper2 = resourceService.createResource(label = "Paper 2", classes = setOf("Paper"))
        val paper3 = resourceService.createResource(label = "Paper 3", classes = setOf("Paper"))
        val paper4 = resourceService.createResource(label = "Paper 4", classes = setOf("Paper"))

        // create year
        val year = literalService.createLiteral(label = "2018")

        // create contributions
        val cont1 = resourceService.createResource(label = "Contribution of Paper 1", classes = setOf("Contribution"))
        val cont2 = resourceService.createResource(label = "Contribution of Paper 2", classes = setOf("Contribution"))
        val cont3 = resourceService.createResource(label = "Contribution of Paper 3", classes = setOf("Contribution"))
        // create comparison
        val comparison = resourceService.createResource(label = "Comparison", classes = setOf("Comparison"))

        // Link authors to papers
        val paper1AuthorsList = listService.createList("Authors", listOf(author1))
        val paper2AuthorsList = listService.createList("Authors", listOf(author2))
        val paper3AuthorsList = listService.createList("Authors", listOf(authorResource))
        val paper4AuthorsList = listService.createList("Authors", listOf(authorNotNeeded))

        statementService.create(paper1, Predicates.hasAuthors, paper1AuthorsList)
        statementService.create(paper2, Predicates.hasAuthors, paper2AuthorsList)
        statementService.create(paper3, Predicates.hasAuthors, paper3AuthorsList)
        statementService.create(paper4, Predicates.hasAuthors, paper4AuthorsList)

        // Link paper 1 to year
        statementService.create(paper1, Predicates.yearPublished, year)

        // Link papers to contributions
        statementService.create(paper1, Predicates.hasContribution, cont1)
        statementService.create(paper2, Predicates.hasContribution, cont2)
        statementService.create(paper3, Predicates.hasContribution, cont3)

        // Link comparison to contributions
        statementService.create(comparison, Predicates.comparesContribution, cont1)
        statementService.create(comparison, Predicates.comparesContribution, cont2)
        statementService.create(comparison, Predicates.comparesContribution, cont3)

        mockMvc
            .perform(documentedGetRequestTo("/api/comparisons/$comparison/authors").param("size", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].info[?(@.paper_id == '$paper1')].paper_year").value(2018))
            .andExpect(
                jsonPath(
                    "$.content[0].info[?(@.paper_id == '$paper2')][0].paper_year",
                    anyOf(nullValue(), `is`<List<Int?>>(emptyList()))
                )
            )
            .andDo(
                documentationHandler.document(
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
                    fieldWithPath("author.value").type("resource").description("The author as a resource object"),
                    fieldWithPath("info[]").description("Information about the compared papers of each author"),
                    fieldWithPath("info[].paper_id").description("The paper resource ID"),
                    fieldWithPath("info[].paper_year").description("The year in which the paper was published (optional)").optional(),
                    fieldWithPath("info[].author_index").description("Zero-based index of the author in the authors list")
                ),
            )
            .andWithPrefix("content[].author.value.", resourceResponseFields())
            .andWithPrefix("")
}
