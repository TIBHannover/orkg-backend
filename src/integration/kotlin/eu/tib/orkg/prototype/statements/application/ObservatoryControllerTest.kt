package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
    fun index() {

        userService.registerUser("abc@gmail.com", "123456", "M Haris")
        val id = service.create("test organization", userService.findByEmail("abc@gmail.com").get().id!!).id
        val observatoryId = observatoryService.create("test observatory", service.findById(id!!).get())

        mockMvc
            .perform(getRequestTo("/api/observatories/"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    listOfobservatoriesResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        userService.registerUser("abc@gmail.com", "123456", "M Haris")
        val id = service.create("test organization", userService.findByEmail("abc@gmail.com").get().id!!).id
        val observatoryId = observatoryService.create("test observatory", service.findById(id!!).get()).id

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(observatoryResponseFields())
                )
            )
    }

    @Test
    fun lookUpResourcesByObservatoryId() {

        userService.registerUser("abc@gmail.com", "123456", "M Haris")
        val id = service.create("test organization", userService.findByEmail("abc@gmail.com").get().id!!).id
        val observatoryId = observatoryService.create("test observatory", service.findById(id!!).get()).id
        resourceService.create(userService.findByEmail("abc@gmail.com").get().id!!, "test resource", observatoryId!!, ExtractionMethod.UNKNOWN, id)

        mockMvc
            .perform(getRequestTo("/api/observatories/$observatoryId/resources"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet
                )
            )
    }

    companion object RestDoc {
        private fun observatoryResponseFields() = listOf(
            fieldWithPath("id").description("The observatory ID"),
            fieldWithPath("name").description("The observatory name"),
            fieldWithPath("users").description("The users belong to an observatory"),
            fieldWithPath("organizations.[].id").description("The ID of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].name").description("The name of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].createdBy").description("The user ID of the organizations which are managing this observatory"),
            fieldWithPath("organizations.[].observatories").description("The list of the observatories which are handled by this organizations")
        )

        fun listOfobservatoriesResponseFields(): ResponseFieldsSnippet =
            responseFields(fieldWithPath("[]").description("A list of observatories"))
                .andWithPrefix("[].", observatoryResponseFields())
    }
}
