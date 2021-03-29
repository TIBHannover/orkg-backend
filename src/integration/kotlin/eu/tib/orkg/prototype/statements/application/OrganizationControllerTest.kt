package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
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
class OrganizationControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var controller: OrganizationController

    @Autowired
    private lateinit var service: OrganizationService

    @Autowired
    private lateinit var observatoryService: ObservatoryService

    @Autowired
    private lateinit var resourceService: ResourceService

    override fun createController() = controller

    @BeforeEach
    fun setup() {
        service.removeAll()
        observatoryService.removeAll()
        resourceService.removeAll()

        assertThat(service.listOrganizations()).hasSize(0)
        assertThat(observatoryService.listObservatories()).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    fun index() {
        val userId = createTestUser()
        service.create("test organization", userId, "www.example.org", "organization-test")

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
        val organizationId = service.create("test organization", ContributorId.createUnknownContributor(), "www.example.org", "test-organization").id

        mockMvc
            .perform(getRequestTo("/api/organizations/$organizationId"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(organizationResponseFields())
                )
            )
    }

    @Test
    fun lookUpObservatoriesByOrganization() {
        val userId = createTestUser()
        val organizationId = service.create("test organization", userId, "www.example.org", "organization_test").id
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        observatoryService.create("test observatory", "example description", service.findById(organizationId!!).get(), resource.id.toString(), "test_observatory").id!!

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

    fun createTestUser(): ContributorId {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return ContributorId(userService.findByEmail("abc@gmail.com").get().id!!)
    }

    fun createTestResource(userId: ContributorId, organizationId: OrganizationId, observatoryId: ObservatoryId, resourceType: String): Resource {
        return resourceService.create(userId, CreateResourceRequest(null, "test paper", setOf(ClassId(resourceType))), observatoryId, ExtractionMethod.UNKNOWN, organizationId)
    }

    companion object RestDoc {
        private fun organizationResponseFields() = listOf(
            fieldWithPath("id").description("The organization ID"),
            fieldWithPath("name").description("The organization name"),
            fieldWithPath("logo").description("The logo of the organization"),
            fieldWithPath("created_by").description("The ID of the user that created the organization."),
            fieldWithPath("homepage").description("The URL of the organization's homepage."),
            fieldWithPath("observatory_ids").description("The list of observatories that belong to this organization"),
            fieldWithPath("display_id").description("The URL of an organization")
        )

        fun listOfOrganizationsResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of organizations"))
                .andWithPrefix("[].", organizationResponseFields())
    }
}
