package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import java.util.UUID
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
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
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    fun index() {
        val userId = createTestUser()
        service.create("test organization", userId, "www.example.org")

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
        val organizationId = service.create("test organization", UUID(0, 0), "www.example.org").id

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
        val organizationId = service.create("test organization", userId, "www.example.org").id
        observatoryService.create("test observatory", "test description", service.findById(organizationId!!).get(), "Computer Sciences")

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

    fun createTestUser(): UUID {
        userService.registerUser("abc@gmail.com", "123456", "Test user")
        return userService.findByEmail("abc@gmail.com").get().id!!
    }

    companion object RestDoc {
        private fun organizationResponseFields() = listOf(
            fieldWithPath("id").description("The organization ID"),
            fieldWithPath("name").description("The organization name"),
            fieldWithPath("logo").description("The logo of the organization"),
            fieldWithPath("created_by").description("The ID of the user that created the organization."),
            fieldWithPath("url").description("The URL of the organization."),
            fieldWithPath("observatory_ids").description("The list of observatories that belong to this organization")
        )

        fun listOfOrganizationsResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of organizations"))
                .andWithPrefix("[].", organizationResponseFields())
    }
}
