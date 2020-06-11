package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
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
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var classService: ClassService

    override fun createController() = controller

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun add() {

        userService.registerUser("abc@gmail.com", "123456", "M Haris")
        service.create("test organization", userService.findByEmail("abc@gmail.com").get().id!!)

        mockMvc
            .perform(getRequestTo("/api/organizations/"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    listOfOrganizationsResponseFields()
                )
            )
    }

    companion object RestDoc {
        private fun organizationResponseFields() = listOf(
            fieldWithPath("id").description("The organization ID"),
            fieldWithPath("name").description("The organization name"),
            fieldWithPath("created_by").description("The ID of the user that created the organization."),
            fieldWithPath("observatories").description("The list of the observatories belong to an organization")
        )

        fun listOfOrganizationsResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of organizations"))
                .andWithPrefix("[].", organizationResponseFields())
    }
}
