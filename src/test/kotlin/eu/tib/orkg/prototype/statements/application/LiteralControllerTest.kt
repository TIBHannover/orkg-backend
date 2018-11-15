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
import org.springframework.test.web.servlet.result.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.*

@DisplayName("Literal Controller")
@Transactional
class LiteralControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: LiteralController

    @Autowired
    private lateinit var service: LiteralService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("research contribution")
        service.create("programming language")

        mockMvc
            .perform(
                get("/api/literals/")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].id").description("The resource ID"),
                        fieldWithPath("[].label").description("The resource label")
                    )
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
                get("/api/literals/?q=research")
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
                    responseFields(
                        fieldWithPath("[].id").description("The resource ID"),
                        fieldWithPath("[].label").description("The resource label")
                    )
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("research contribution").id

        mockMvc
            .perform(
                get("/api/literals/$id")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("id").description("The resource ID"),
                        fieldWithPath("label").description("The resource label")
                    )
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(
                post("/api/literals/")
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
                    responseFields(
                        fieldWithPath("id").description("The resource ID"),
                        fieldWithPath("label").description("The resource label")
                    )
                )
            )
    }
}
