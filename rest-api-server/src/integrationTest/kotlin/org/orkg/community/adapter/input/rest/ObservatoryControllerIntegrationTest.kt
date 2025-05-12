package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.pageOfDetailedResourcesResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import orkg.orkg.community.testing.fixtures.observatoryResponseFields

@Neo4jContainerIntegrationTest
internal class ObservatoryControllerIntegrationTest : MockMvcBaseTest("observatories") {
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
        assertThat(service.findAll()).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.createClasses(
            Classes.researchField,
            Classes.paper,
            Classes.comparison,
            Classes.problem,
            ThingId("SomeClass")
        )
    }

    @AfterEach
    fun cleanup() {
        observatoryService.deleteAll()
        resourceService.deleteAll()
        service.deleteAll()
        classService.deleteAll()
        contributorService.deleteAll()
    }

    @Test
    fun index() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
        val researchField = resourceService.createResource(
            classes = setOf(Classes.researchField)
        )
        observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        documentedGetRequestTo("/api/observatories")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                        parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                    ),
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
            classes = setOf(Classes.researchField)
        )
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        documentedGetRequestTo("/api/observatories/{id}", observatoryId)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory.")
                    ),
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
            classes = setOf(Classes.researchField)
        )
        val observatoryId = observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )
        resourceService.createResource(
            classes = setOf(Classes.paper),
            organizationId = organizationId,
            observatoryId = observatoryId
        )

        documentedGetRequestTo("/api/observatories/{id}/papers", observatoryId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory.")
                    ),
                    pageOfDetailedResourcesResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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
