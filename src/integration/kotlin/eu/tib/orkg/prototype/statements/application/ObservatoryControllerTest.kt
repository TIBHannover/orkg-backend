package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import java.util.UUID
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
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

    @Autowired
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    fun index() {

        var userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        createTestObservatory(organizationId)

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
        var userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val observatoryId = createTestObservatory(organizationId)

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
        var userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val observatoryId = createTestObservatory(organizationId)
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
        var userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val observatoryId = createTestObservatory(organizationId)
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
        var userId = createTestUser()
        val organizationId = createTestOrganization(userId)
        val observatoryId = createTestObservatory(organizationId)
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

    fun createTestUser(): UUID {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return userService.findByEmail("abc@gmail.com").get().id!!
    }

    fun createTestOrganization(userId: UUID): UUID {
        return service.create("test organization", userId, "www.example.org").id!!
    }

    fun createTestObservatory(organizationId: UUID): UUID {
        return observatoryService.create("test observatory", "example description", service.findById(organizationId).get(), "Computer Sciences").id!!
    }

    fun createTestResource(userId: UUID, organizationId: UUID, observatoryId: UUID, resourceType: String) {
        resourceService.create(userId, CreateResourceRequest(null, "test paper", setOf(ClassId(resourceType))), observatoryId, ExtractionMethod.UNKNOWN, organizationId)
    }

    companion object RestDoc {
        private fun observatoryResponseFields() = listOf(
            fieldWithPath("id").description("The observatory ID"),
            fieldWithPath("name").description("The observatory name"),
            fieldWithPath("description").description("The observatory description"),
            fieldWithPath("research_field").description("The research field of an observatory"),
            fieldWithPath("users").description("The members belonging to an observatory"),
            fieldWithPath("organizations.[].id").description("The ID of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].name").description("The name of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].createdBy").description("The ID of the user who has created an organization"),
            fieldWithPath("organizations.[].url").description("The URL of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].observatories").description("The list of the observatories which are handled by this organizations"),
            fieldWithPath("numPapers").description("Total number of papers belong to an observatory"),
            fieldWithPath("numComparisons").description("Total number of comparisons belong to an observatory")
        )

        fun listOfObservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())
    }
}
