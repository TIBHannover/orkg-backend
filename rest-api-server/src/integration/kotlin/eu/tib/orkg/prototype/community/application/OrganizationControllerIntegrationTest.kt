package eu.tib.orkg.prototype.community.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClasses
import eu.tib.orkg.prototype.createObservatory
import eu.tib.orkg.prototype.createOrganization
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createUser
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.RestDocumentationBaseTest
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Organization Controller")
@Transactional
@Import(MockUserDetailsService::class)
class OrganizationControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var service: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        service.removeAll()
        observatoryService.removeAll()
        resourceService.removeAll()
        classService.removeAll()

        assertThat(service.listOrganizations()).hasSize(0)
        assertThat(observatoryService.listObservatories(PageRequest.of(0, 10))).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.createClasses("ResearchField")
    }

    @Test
    fun index() {
        val userId = userService.createUser()
        service.createOrganization(createdBy = ContributorId(userId))

        mockMvc
            .perform(getRequestTo("/api/organizations/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    listOfOrganizationsResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))

        mockMvc
            .perform(getRequestTo("/api/organizations/$organizationId"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(organizationResponseFields(OrganizationType))
                )
            )
    }

    @Test
    fun lookUpObservatoriesByOrganization() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        observatoryService.createObservatory(organizationId, researchField)

        mockMvc
            .perform(getRequestTo("/api/organizations/$organizationId/observatories"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    ObservatoryControllerTest.listOfObservatoriesResponseFields()
                )
            )
    }

    companion object RestDoc {
        private fun organizationResponseFields(OrganizationType: OrganizationType.Companion) = listOf(
            fieldWithPath("id").description("The organization ID"),
            fieldWithPath("name").description("The organization name"),
            fieldWithPath("created_by").description("The ID of the user that created the organization."),
            fieldWithPath("homepage").description("The URL of the organization's homepage."),
            fieldWithPath("observatory_ids").description("The list of observatories that belong to this organization"),
            fieldWithPath("display_id").description("The URL of an organization"),
            fieldWithPath("type").description(OrganizationType)
        )

        fun listOfOrganizationsResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of organizations"))
                .andWithPrefix("[].", organizationResponseFields(OrganizationType))
    }
}
