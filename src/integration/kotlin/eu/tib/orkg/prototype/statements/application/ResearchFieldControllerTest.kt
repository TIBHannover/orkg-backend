package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.application.ResourceControllerTest.RestDoc.resourceResponseFields
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.assertj.core.api.Assertions
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
    private lateinit var controller: ResearchFieldController

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var classService: ClassService

    @Autowired
    private lateinit var statementService: StatementService

    override fun createController() = controller

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        classService.removeAll()
        statementService.removeAll()

        Assertions.assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(classService.findAll(tempPageable)).hasSize(0)
        Assertions.assertThat(statementService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    @Disabled("Because of the ID problem of Predicates")
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun getProblemsPerField() {

        // Create Research Field
        val researchFieldClass = classService.create(CreateClassRequest(ClassId("ResearchField"), "Research Field", null))
        val fieldResource = resourceService.create(CreateResourceRequest(
            null,
            "Fancy research",
            setOf(researchFieldClass.id!!)
        ))
        // Create Paper
        val paperClass = classService.create(CreateClassRequest(ClassId("Paper"), "Paper", null))
        val paperResource = resourceService.create(CreateResourceRequest(
            null,
            "Paper 1",
            setOf(paperClass.id!!)
        ))
        // Create Contribution
        val contributionClass = classService.create(CreateClassRequest(ClassId("Contribution"), "Contribution", null))
        val contributionResource = resourceService.create(CreateResourceRequest(
            null,
            "Contribution 1",
            setOf(contributionClass.id!!)
        ))
        // Create Problem
        val problemClass = classService.create(CreateClassRequest(ClassId("Problem"), "Problem", null))
        val problemResource = resourceService.create(CreateResourceRequest(
            null,
            "Problem 1",
            setOf(problemClass.id!!)
        ))
        // Link Contribution -> Problem
        val hasResearchProblemPredicate = findOrCreatePredicate("P32", "has research problem")
        statementService.create(
            contributionResource.id!!.value,
            hasResearchProblemPredicate.id!!,
            problemResource.id!!.value
        )
        // Link Paper -> Contribution
        val hasContributionPredicate = findOrCreatePredicate("P31", "has contribution")
        statementService.create(
            paperResource.id!!.value,
            hasContributionPredicate.id!!,
            contributionResource.id!!.value
        )
        // Link Paper -> Research Field
        val hasResearchFieldPredicate = findOrCreatePredicate("P30", "has research field")
        statementService.create(
            paperResource.id!!.value,
            hasResearchFieldPredicate.id!!,
            fieldResource.id!!.value
        )

        mockMvc
            .perform(getRequestTo("/api/research-fields/${fieldResource.id!!}/problems/"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    snippet,
                    researchProblemsPerResearchFieldFields()
                )
            )
    }

    private fun findOrCreatePredicate(predicateId: String, predicateLabel: String): Predicate {
        val found = predicateService.findById(PredicateId(predicateId))
        if (found.isPresent)
            return found.get()
        return predicateService.create(CreatePredicateRequest(PredicateId(predicateId), predicateLabel))
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
