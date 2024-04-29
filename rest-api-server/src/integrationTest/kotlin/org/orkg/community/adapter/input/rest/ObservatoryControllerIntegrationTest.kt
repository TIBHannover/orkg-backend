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
import orkg.orkg.community.testing.fixtures.observatoryResponseFields

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
        observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

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
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

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
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )
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
    fun lookUpProblemsByObservatoryId() {
        val userId = userService.createUser()
        val organizationId = service.createOrganization(createdBy = ContributorId(userId))
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )
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
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )
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
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )
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
        fun listOfObservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())

        fun pageOfObservatoryResponseFields(): ResponseFieldsSnippet =
            responseFields(pageableDetailedFieldParameters())
                .andWithPrefix("content[].", observatoryResponseFields())
                .andWithPrefix("")
    }
}
