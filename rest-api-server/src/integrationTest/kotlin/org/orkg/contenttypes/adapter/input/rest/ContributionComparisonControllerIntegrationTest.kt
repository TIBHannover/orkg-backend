package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
internal class ContributionComparisonControllerIntegrationTest : MockMvcBaseTest("contribution-comparison") {

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

        predicateService.createPredicate(Predicates.yearPublished)
        predicateService.createPredicate(Predicates.hasContribution)

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

        statementService.create(paper1, Predicates.hasContribution, cont1)
        statementService.create(paper2, Predicates.hasContribution, cont2)

        statementService.create(paper1, Predicates.yearPublished, year1)
        statementService.create(paper2, Predicates.yearPublished, year2)

        val ids = listOf(cont1, cont2)

        documentedGetRequestTo("/api/contribution-comparisons/contributions")
            .param("ids", *ids.map(ThingId::value).toTypedArray())
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(ids.size)))
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andDo(
                documentationHandler.document(
                    contributionInfoPageResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun passingLessThanTwoIDsShouldRaiseAnError() {
        val cont1 = resourceService.createResource(setOf("Contribution"), label = "Contribution 1")

        val paper1 = resourceService.createResource(setOf("Paper"), label = "Paper 1")

        val year1 = literalService.createLiteral(label = "2022")

        statementService.create(paper1, Predicates.hasContribution, cont1)

        statementService.create(paper1, Predicates.yearPublished, year1)

        val ids = listOf(cont1)

        get("/api/contribution-comparisons/contributions")
            .param("ids", *ids.map(ThingId::value).toTypedArray())
            .perform()
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
