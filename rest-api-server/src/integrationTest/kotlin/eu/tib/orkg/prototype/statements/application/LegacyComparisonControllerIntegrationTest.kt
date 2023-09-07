package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createList
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.LiteralService
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Comparison Controller")
@Transactional
@Import(MockUserDetailsService::class)
class LegacyComparisonControllerIntegrationTest : RestDocumentationBaseTest() {

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
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        // Init classes
        classService.createClasses("Author", "Paper", "Contribution", "Comparison")

        // Init predicates
        predicateService.createPredicate("Has Authors", id = "hasAuthors")
        predicateService.createPredicate("Has Contribution", id = "P31")
        predicateService.createPredicate("Compares Contribution", id = "compareContribution")
        predicateService.createPredicate("publication year", id = "P29")
        predicateService.createPredicate("has list element", id = Predicates.hasListElement.value)
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun getTopAuthorsOfComparison() {
        // create authors
        val author1 = literalService.create("Author 1").id
        val author2 = literalService.create("Author 1").id
        val authorNotNeeded = literalService.create("Ignore me").id
        val authorResource = resourceService.createResource(label = "Famous author", classes = setOf("Author"))
        // create papers
        val paper1 = resourceService.createResource(label = "Paper 1", classes = setOf("Paper"))
        val paper2 = resourceService.createResource(label = "Paper 2", classes = setOf("Paper"))
        val paper3 = resourceService.createResource(label = "Paper 3", classes = setOf("Paper"))
        val paper4 = resourceService.createResource(label = "Paper 4", classes = setOf("Paper"))

        // create year
        val year = literalService.create("2018").id

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

        statementService.create(paper1, ThingId("hasAuthors"), paper1AuthorsList)
        statementService.create(paper2, ThingId("hasAuthors"), paper2AuthorsList)
        statementService.create(paper3, ThingId("hasAuthors"), paper3AuthorsList)
        statementService.create(paper4, ThingId("hasAuthors"), paper4AuthorsList)

        // Link paper 1 to year
        statementService.create(paper1, ThingId("P29"), year)

        // Link papers to contributions
        statementService.create(paper1, ThingId("P31"), cont1)
        statementService.create(paper2, ThingId("P31"), cont2)
        statementService.create(paper3, ThingId("P31"), cont3)

        // Link comparison to contributions
        statementService.create(comparison, ThingId("compareContribution"), cont1)
        statementService.create(comparison, ThingId("compareContribution"), cont2)
        statementService.create(comparison, ThingId("compareContribution"), cont3)

        mockMvc
            .perform(getRequestTo("/api/comparisons/$comparison/authors?size=2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].info[?(@.paper_id == '$paper1')].paper_year").value(2018))
            .andExpect(jsonPath("$.content[0].info[?(@.paper_id == '$paper2')][0].paper_year", anyOf(nullValue(), `is`<List<Int?>>(emptyList()))))
            .andDo(
                document(
                    snippet,
                    authorsOfComparisonResponseFields()
                )
            )
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
