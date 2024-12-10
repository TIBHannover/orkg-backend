package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
@Import(MockUserDetailsService::class)
internal class OrganizationControllerIntegrationTest : RestDocsTest("organizations") {

    @Autowired
    private lateinit var contributorService: ContributorUseCases

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
        contributorService.deleteAll()
    }

    @Test
    fun index() {
        val contributorId = contributorService.createContributor()
        service.createOrganization(createdBy = contributorId)

        mockMvc
            .perform(documentedGetRequestTo("/api/organizations"))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    listOfOrganizationsResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetch() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)

        mockMvc
            .perform(documentedGetRequestTo("/api/organizations/{id}", organizationId))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    responseFields(organizationResponseFields(OrganizationType))
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookUpObservatoriesByOrganization() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        mockMvc
            .perform(documentedGetRequestTo("/api/organizations/{id}/observatories", organizationId))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    ObservatoryControllerIntegrationTest.listOfObservatoriesResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    companion object RestDoc {
        private fun organizationResponseFields(type: OrganizationType.Companion) = listOf(
            fieldWithPath("id").description("The organization ID"),
            fieldWithPath("name").description("The organization name"),
            fieldWithPath("created_by").description("The ID of the user that created the organization."),
            fieldWithPath("homepage").description("The URL of the organization's homepage."),
            fieldWithPath("observatory_ids").description("The list of observatories that belong to this organization"),
            fieldWithPath("display_id").description("The URL of an organization"),
            fieldWithPath("type").description(type)
        )

        fun listOfOrganizationsResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of organizations"))
                .andWithPrefix("[].", organizationResponseFields(OrganizationType))
    }
}
