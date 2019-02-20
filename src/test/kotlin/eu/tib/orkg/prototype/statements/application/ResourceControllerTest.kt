package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.http.MediaType.*
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.*

@DisplayName("Resource Controller")
@Transactional
class ResourceControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: ResourceController

    @Autowired
    private lateinit var service: ResourceService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("research contribution")
        service.create("programming language")

        mockMvc
            .perform(
                get("/api/resources/")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    resourceListResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.create("research contribution")
        service.create("programming language")
        service.create("research topic")

        mockMvc
            .perform(
                get("/api/resources/?q=research")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    resourceListResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("research contribution").id

        mockMvc
            .perform(
                get("/api/resources/$id")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    resourceResponseFields()
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(
                post("/api/resources/")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resource))
            )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The resource label")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("Location to the created resource")
                    ),
                    resourceResponseFields()
                )
            )
    }

    private fun resourceResponseFields() =
        responseFields(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label")
        )

    private fun resourceListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The resource ID"),
            fieldWithPath("[].label").description("The resource label")
        )
}
