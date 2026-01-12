package org.orkg.community.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.testing.fixtures.Assets.png
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.input.CreateImageUseCase
import org.orkg.mediastorage.input.ImageUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.MimeType
import orkg.orkg.community.testing.fixtures.CommunityDocumentationContextProvider
import orkg.orkg.community.testing.fixtures.organizationResponseFields

@Neo4jContainerIntegrationTest
@Import(CommonDocumentationContextProvider::class, CommunityDocumentationContextProvider::class)
internal class OrganizationControllerIntegrationTest : MockMvcBaseTest("organizations") {
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

    @Autowired
    private lateinit var imageService: ImageUseCases

    @BeforeEach
    fun setup() {
        assertThat(service.findAll()).hasSize(0)
        assertThat(observatoryService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.createClasses(Classes.researchField)
    }

    @AfterEach
    fun cleanup() {
        observatoryService.deleteAll()
        service.deleteAll()
        contributorService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
    }

    @Test
    @DisplayName("Given an organization, when fetched by id, then status is 200 OK and organization is returned")
    fun findById() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)

        documentedGetRequestTo("/api/organizations/{id}", organizationId)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing organizations")
                description(
                    """
                    A `GET` request provides information about an organization.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the organization."),
                )
                responseFields<Organization>(organizationResponseFields())
                throws(OrganizationNotFound::class)
            }
    }

    @Test
    @DisplayName("Given several organizations, when they are fetched, then status is 200 OK and organizations are returned")
    fun findAll() {
        val contributorId = contributorService.createContributor()
        service.createOrganization(createdBy = contributorId)

        documentedGetRequestTo("/api/organizations")
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing organizations")
                description(
                    """
                    A `GET` request lists all organizations.
                    """
                )
                listResponseFields<Organization>(organizationResponseFields())
            }
    }

    @Test
    fun fetchLogo() {
        val contributorId = contributorService.createContributor()
        val imageId = imageService.create(
            CreateImageUseCase.CreateCommand(
                data = ImageData(png("white_pixel")),
                mimeType = MimeType.valueOf("image/png"),
                createdBy = contributorId,
            )
        )
        val organizationId = service.createOrganization(createdBy = contributorId, logoId = imageId)

        get("/api/organizations/{id}/logo", organizationId)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun lookUpObservatoriesByOrganization() {
        val contributorId = contributorService.createContributor()
        val organizationId = service.createOrganization(createdBy = contributorId)
        val researchField = resourceService.createResource(
            classes = setOf(Classes.researchField)
        )
        observatoryService.createObservatory(
            organizations = setOf(organizationId),
            researchField = researchField
        )

        get("/api/organizations/{id}/observatories", organizationId)
            .perform()
            .andExpect(status().isOk)
    }
}
