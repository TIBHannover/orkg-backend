package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
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
    private lateinit var userService: AuthUseCase

    @Autowired
    private lateinit var userRepository: UserRepository

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
        assertThat(service.listOrganizations()).hasSize(0)
        assertThat(observatoryService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.createClasses("ResearchField")
    }

    @AfterEach
    fun cleanup() {
        service.removeAll()
        observatoryService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        userRepository.deleteAll()
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
        observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        mockMvc
            .perform(getRequestTo("/api/organizations/$organizationId/observatories"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    ObservatoryControllerIntegrationTest.listOfObservatoriesResponseFields()
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
