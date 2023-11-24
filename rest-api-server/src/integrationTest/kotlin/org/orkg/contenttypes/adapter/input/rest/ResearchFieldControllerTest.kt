package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClasses
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import org.orkg.graph.input.ClassUseCases
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
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Research Field Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ResearchFieldControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

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
            contributionResource,
            ThingId("P32"), // has research problem
            problemResource
        )
        // Link Paper -> Contribution
        statementService.create(
            paperResource,
            ThingId("P31"), // has contribution
            contributionResource
        )
        // Link Paper -> Research Field
        statementService.create(
            paperResource,
            ThingId("P30"), // has research field
            fieldResource
        )

        mockMvc
            .perform(getRequestTo("/api/research-fields/$fieldResource/problems/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    researchProblemsPerResearchFieldFields()
                )
            )
    }

    private fun researchProblemsPerResearchFieldFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].problem.", resourceResponseFields())
            .andWithPrefix("content[]", fieldWithPath("papers").description("The number of papers addressing this problem"))
            .andWithPrefix("")
}
