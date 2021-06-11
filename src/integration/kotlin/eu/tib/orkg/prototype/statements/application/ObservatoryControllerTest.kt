package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Observatory Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ObservatoryControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var controller: ObservatoryController

    @Autowired
    private lateinit var service: OrganizationService

    @Autowired
    private lateinit var observatoryService: ObservatoryService

    @Autowired
    private lateinit var resourceService: ResourceService

    override fun createController() = controller

    @BeforeEach
    fun setup() {
        observatoryService.removeAll()
        resourceService.removeAll()
        service.removeAll()

        assertThat(observatoryService.listObservatories()).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(service.listOrganizations()).hasSize(0)
    }

    @Test
    fun index() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        createTestObservatory(organizationId, resource.id.toString())

        mockMvc
            .perform(getRequestTo("/api/observatories/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    listOfObservatoriesResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id

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
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id!!
        createTestResource(userId, organizationId, observatoryId, "Paper")

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/papers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andDo(
                document(
                    snippet,
                    ResourceControllerTest.listOfResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookUpComparisonsByObservatoryId() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id!!
        createTestResource(userId, organizationId, observatoryId, "Comparison")

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/comparisons"))
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    ResourceControllerTest.listOfResourcesResponseFields()
                )
            )
    }

    @Test
    fun lookUpProblemsByObservatoryId() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(ContributorId.createUnknownContributor(), OrganizationId.createUnknownOrganization(), ObservatoryId.createUnknownObservatory(), "ResearchField")
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id!!
        createTestResource(userId, organizationId, observatoryId, "Problem")

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/problems"))
            .andExpect(jsonPath("$", hasSize<Int>(1)))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    ResourceControllerTest.listOfResourcesResponseFields()
                )
            )
    }

    fun createTestUser(): ContributorId {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return ContributorId(userService.findByEmail("abc@gmail.com").get().id!!)
    }

    fun createTestOrganization(userId: ContributorId): OrganizationId {
        return service.create("test organization", userId, "www.example.org", "test_organization").id!!
    }

    fun createTestObservatory(organizationId: OrganizationId, resourceId: String): Observatory {
        return observatoryService.create("test observatory", "example description", service.findById(organizationId).get(), resourceId, "test-observatory")
    }

    fun createTestResource(userId: ContributorId, organizationId: OrganizationId, observatoryId: ObservatoryId, resourceType: String): Resource {
        return resourceService.create(userId, CreateResourceRequest(null, "test paper", setOf(ClassId(resourceType))), observatoryId, ExtractionMethod.UNKNOWN, organizationId)
    }

    fun listOfObservatoriesResponseFields2(): ResponseFieldsSnippet =
        responseFields(
            pageableDetailedFieldParameters()
        ).andWithPrefix("content[].", observatoryResponseFields())
            .andWithPrefix("")

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
            fieldWithPath("featured").optional().ignored()
        )

        fun listOfObservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())
    }
}
