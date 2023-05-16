package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createPredicates
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Contribution Comparison Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ContributionComparisonControllerIntegrationTest : RestDocumentationBaseTest() {

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
        statementService.removeAll()
        classService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicates(
            "P29" to "publication year",
            "P31" to "Has contribution",
        )

        classService.createClasses("Paper", "Contribution")
    }

    @Test
    fun fetchContributionInformation() {
        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")
        val cont2 = resourceService.createResource(setOf("Contribution"), label = "Contribution 2")

        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf("Paper"), label = "Paper 2")

        val year1 = literalService.create("2022").id
        val year2 = literalService.create("2023").id

        statementService.create(paper1, ThingId("P31"), cont1)
        statementService.create(paper2, ThingId("P31"), cont2)

        statementService.create(paper1, ThingId("P29"), year1)
        statementService.create(paper2, ThingId("P29"), year2)

        val ids = listOf(cont1, cont2)

        mockMvc
            .perform(getRequestTo("/api/contribution-comparisons/contributions/?ids=${ids.joinToString(separator = ",")}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(ids.size)))
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    contributionInfoPageResponseFields()
                )
            )
    }

    @Test
    fun `passing less than two IDs should raise an error`() {
        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")

        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val year1 = literalService.create("2022").id

        statementService.create(paper1, ThingId("P31"), cont1)

        statementService.create(paper1, ThingId("P29"), year1)

        val ids = listOf(cont1)

        mockMvc
            .perform(getRequestTo("/api/contribution-comparisons/contributions/?ids=${ids.joinToString(separator = ",")}"))
            .andExpect(status().isBadRequest)
    }

    private fun contributionInfoPageResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", contributionInfoResponseFields())
            .andWithPrefix("")

    private fun contributionInfoResponseFields() =
        listOf(
            fieldWithPath("id").description("The resource ID of the contribution"),
            fieldWithPath("label").description("The label of the contribution"),
            fieldWithPath("paper_title").description("The paper title of the parent paper"),
            fieldWithPath("paper_year").description("The publication year of the paper, if available").optional(),
            fieldWithPath("paper_id").description("The resource ID of the parent paper"),
        )
}
