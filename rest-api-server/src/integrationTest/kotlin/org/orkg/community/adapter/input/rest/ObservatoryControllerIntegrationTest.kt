package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.pageOfDetailedResourcesResponseFields
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Observatory Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ObservatoryControllerIntegrationTest : RestDocumentationBaseTest() {

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
        assertThat(observatoryService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(service.listOrganizations()).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.createClasses("ResearchField", "Paper", "Comparison", "Problem", "SomeClass")
    }

    @AfterEach
    fun cleanup() {
        observatoryService.removeAll()
        resourceService.removeAll()
        service.removeAll()
        classService.removeAll()
        userRepository.deleteAll()
    }

    @Test
    fun index() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        observatoryService.createObservatory(organizationId, researchField)

        mockMvc
            .perform(getRequestTo("/api/observatories/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfObservatoryResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(observatoryResponseFields())
                )
            )
    }

    @Test
    fun lookUpPapersByObservatoryId() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)
        resourceService.createResource(
            classes = setOf("Paper"),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/papers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andDo(
                document(
                    snippet,
                    pageOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookUpComparisonsByObservatoryId() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)
        resourceService.createResource(
            classes = setOf("Comparison"),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/comparisons"))
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookUpProblemsByObservatoryId() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)
        resourceService.createResource(
            classes = setOf("Problem"),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/problems"))
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfDetailedResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookUpByObservatoryIdAndClassAndNotFeatured() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)
        val resourceId = resourceService.createResource(
            classes = setOf("SomeClass"),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/class?classes=SomeClass"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].id").value(resourceId.value))
    }

    @Test
    fun lookUpByObservatoryIdAndClassAndFeatured() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(organizationId, researchField)
        resourceService.createResource(
            classes = setOf("SomeClass"),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/class?classes=SomeClass&featured=true"))
            .andExpect(jsonPath("$.content", hasSize<Int>(0)))
            .andExpect(status().isOk)
    }

    companion object RestDoc {
        private fun observatoryResponseFields() = listOf(
            fieldWithPath("id").description("The observatory ID"),
            fieldWithPath("name").description("The observatory name"),
            fieldWithPath("description").description("The observatory description"),
            fieldWithPath("research_field").description("The research field of an observatory"),
            fieldWithPath("research_field.id").description("The research field of an observatory"),
            fieldWithPath("research_field.label").description("The research field of an observatory"),
            fieldWithPath("members").description("The members belonging to the observatory"),
            fieldWithPath("organization_ids").description("The list of organizations that the observatory belongs to"),
            fieldWithPath("display_id").description("The URI of an observatory"),
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored()
        )

        fun listOfObservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())

        fun pageOfObservatoryResponseFields(): ResponseFieldsSnippet =
            responseFields(pageableDetailedFieldParameters())
                .andWithPrefix("content[].", observatoryResponseFields())
                .andWithPrefix("")
    }
}