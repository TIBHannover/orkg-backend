package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
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

        val year1 = literalService.createLiteral(label = "2022")
        val year2 = literalService.createLiteral(label = "2023")

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
    fun passingLessThanTwoIDsShouldRaiseAnError() {
        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")

        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val year1 = literalService.createLiteral(label = "2022")

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
