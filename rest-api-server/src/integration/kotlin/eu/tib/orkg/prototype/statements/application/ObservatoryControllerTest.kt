package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
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
    private lateinit var service: OrganizationService

    @Autowired
    private lateinit var observatoryService: ObservatoryService

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        observatoryService.removeAll()
        resourceService.removeAll()
        service.removeAll()
        classService.removeAll()

        assertThat(observatoryService.listObservatories()).hasSize(0)
        assertThat(resourceService.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(service.listOrganizations()).hasSize(0)
        assertThat(classService.findAll(PageRequest.of(0, 10))).hasSize(0)

        classService.create(CreateClassRequest(ClassId("ResearchField"), "ResearchField", null))
        classService.create(CreateClassRequest(ClassId("Paper"), "Paper", null))
        classService.create(CreateClassRequest(ClassId("Comparison"), "Comparison", null))
        classService.create(CreateClassRequest(ClassId("Problem"), "Problem", null))
        classService.create(CreateClassRequest(ClassId("SomeClass"), "SomeClass", null))
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

    @Test
    fun lookUpByObservatoryIdAndClassAndNotFeatured() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(
            ContributorId.createUnknownContributor(),
            OrganizationId.createUnknownOrganization(),
            ObservatoryId.createUnknownObservatory(),
            "ResearchField"
        )
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id!!
        val resourceId = createTestResource(userId, organizationId, observatoryId, "SomeClass").id.value

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/class?classes=SomeClass"))
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.content[0].id", `is`(resourceId)))
            .andExpect(status().isOk)
    }

    @Test
    fun lookUpByObservatoryIdAndClassAndFeatured() {
        val userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val resource = createTestResource(
            ContributorId.createUnknownContributor(),
            OrganizationId.createUnknownOrganization(),
            ObservatoryId.createUnknownObservatory(),
            "ResearchField"
        )
        val observatoryId = createTestObservatory(organizationId, resource.id.toString()).id!!
        createTestResource(userId, organizationId, observatoryId, "SomeClass")

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/class?classes=SomeClass&featured=true"))
            .andExpect(jsonPath("$.content", hasSize<Int>(0)))
            .andExpect(status().isOk)
    }

    fun createTestUser(): ContributorId {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return ContributorId(userService.findByEmail("abc@gmail.com").get().id!!)
    }

    fun createTestOrganization(userId: ContributorId): OrganizationId {
        return service.create("test organization", userId, "www.example.org", "test_organization", OrganizationType.GENERAL).id!!
    }

    fun createTestObservatory(organizationId: OrganizationId, resourceId: String): Observatory {
        return observatoryService.create("test observatory", "example description", service.findById(organizationId).get(), resourceId, "test-observatory")
    }

    fun createTestResource(userId: ContributorId, organizationId: OrganizationId, observatoryId: ObservatoryId, resourceType: String): ResourceRepresentation {
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
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored()
        )

        fun listOfObservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())
    }
}
