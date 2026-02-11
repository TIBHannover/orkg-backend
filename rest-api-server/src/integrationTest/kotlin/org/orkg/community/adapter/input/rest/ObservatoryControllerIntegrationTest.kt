package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.ThingId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.testing.fixtures.CommunityDocumentationContextProvider
import org.orkg.community.testing.fixtures.observatoryResponseFields
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.PostgresContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
@PostgresContainerIntegrationTest
@Import(CommonDocumentationContextProvider::class, CommunityDocumentationContextProvider::class)
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
    @DisplayName("Given several observatories, when they are fetched, then status is 200 OK and observatories are returned")
    fun findAll() {
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
            .andDocument {
                summary("Listing observatories")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<observatories-fetch,observatories>>.
                    If no paging request parameters are provided, the default values will be used.
                    
                    NOTE: Query parameters cannot be combined for this endpoint.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                )
                pagedResponseFields<ObservatoryRepresentation>(observatoryResponseFields())
            }
    }

    @Test
    @DisplayName("Given an observatory, when fetched by id, and observatory exists, then status is 200 OK and observatory is returned")
    fun findById() {
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
            .andDocument {
                summary("Fetching observatories")
                description(
                    """
                    A `GET` request provides information about an observatory.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory.")
                )
                responseFields<ObservatoryRepresentation>(observatoryResponseFields())
            }
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

        get("/api/observatories/{id}/papers", observatoryId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
    }
}
