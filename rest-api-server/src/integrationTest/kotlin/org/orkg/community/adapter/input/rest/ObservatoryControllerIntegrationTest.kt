package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.pageOfDetailedResourcesResponseFields
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import orkg.orkg.community.testing.fixtures.observatoryResponseFields

@Neo4jContainerIntegrationTest
@Transactional
@Import(MockUserDetailsService::class)
internal class ObservatoryControllerIntegrationTest : RestDocsTest("observatories") {

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
        contributorService.deleteAll()
    }

    @Test
    fun index() {
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
            .perform(documentedGetRequestTo("/api/observatories"))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pageOfObservatoryResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetch() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
        val researchField = resourceService.createResource(
            classes = setOf("ResearchField")
        )
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        mockMvc
            .perform(documentedGetRequestTo("/api/observatories/{id}", observatoryId))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    responseFields(observatoryResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookUpPapersByObservatoryId() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
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
            .perform(documentedGetRequestTo("/api/observatories/{id}/papers", observatoryId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andDo(
                documentationHandler.document(
                    pageOfDetailedResourcesResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookUpProblemsByObservatoryId() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
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
            .perform(documentedGetRequestTo("/api/observatories/{id}/problems", observatoryId))
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pageOfDetailedResourcesResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookUpByObservatoryIdAndClassAndNotFeatured() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
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
            .perform(get("/api/observatories/{id}/class", observatoryId).param("classes", "SomeClass"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].id").value(resourceId.value))
    }

    @Test
    fun lookUpByObservatoryIdAndClassAndFeatured() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
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
            .perform(
                get("/api/observatories/{id}/class", observatoryId)
                    .param("classes", "SomeClass")
                    .param("featured", "true")
            )
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
