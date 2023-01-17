package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createPredicates
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResourceControllerTest.RestDoc.resourceResponseFields
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.services.PredicateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@DisplayName("Research Field Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ResearchFieldControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        classService.removeAll()
        statementService.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicates(
            "P30" to "Has research field",
            "P31" to "Has contribution",
            "P32" to "Has research problem"
        )

        classService.createClasses("Paper", "Problem", "ResearchField", "Contribution")
    }

    @Test
    @Disabled("Because of the ID problem of Predicates")
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun getProblemsPerField() {
        // Create Research Field
        val fieldResource = resourceService.createResource(
            classes = setOf("ResearchField"),
            label = "Fancy research",
        )
        // Create Paper
        val paperResource = resourceService.createResource(
            classes = setOf("Paper"),
            label = "Paper 1",
        )
        // Create Contribution
        val contributionResource = resourceService.createResource(
            classes = setOf("Contribution"),
            label = "Contribution 1",
        )
        // Create Problem
        val problemResource = resourceService.createResource(
            classes = setOf("Problem"),
            label = "Problem 1",
        )
        // Link Contribution -> Problem
        statementService.create(
            contributionResource.value,
            PredicateId("P32"), // has research problem
            problemResource.value
        )
        // Link Paper -> Contribution
        statementService.create(
            paperResource.value,
            PredicateId("P31"), // has contribution
            contributionResource.value
        )
        // Link Paper -> Research Field
        statementService.create(
            paperResource.value,
            PredicateId("P30"), // has research field
            fieldResource.value
        )

        mockMvc
            .perform(getRequestTo("/api/research-fields/$fieldResource/problems/"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    snippet,
                    researchProblemsPerResearchFieldFields()
                )
            )
    }

    private fun researchProblemsPerResearchFieldFields(): ResponseFieldsSnippet =
        PayloadDocumentation.responseFields(
            listOf(
                fieldWithPath("[]").description("A list of problems."),
                fieldWithPath("[].problem").description("The problem resource"),
                fieldWithPath("[].papers").description("The number of papers addressing this problem")
            )
        )
        .andWithPrefix("[].problem.", resourceResponseFields())
}
