package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.http.*
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.*

@DisplayName("Predicate Controller")
@Transactional
class PredicateControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: PredicateController

    @Autowired
    private lateinit var service: PredicateService

    override fun createController() = controller

    @Test
    fun index() {
        service.create("has name")
        service.create("knows")

        mockMvc
            .perform(
                get("/api/predicates/")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("[].id").description("The predicate ID"),
                        fieldWithPath("[].label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("has name").id

        mockMvc
            .perform(
                get("/api/predicates/$id")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(
                        fieldWithPath("id").description("The predicate ID"),
                        fieldWithPath("label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun lookup() {
        service.create("has name")
        service.create("gave name to")
        service.create("knows")

        mockMvc
            .perform(
                get("/api/predicates/?q=name")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("The predicate ID"),
                        fieldWithPath("[].label").description("The predicate label")
                    )
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "knows")

        mockMvc
            .perform(
                post("/api/predicates/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resource))
            )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The predicate label")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("Location to the created resource")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The predicate ID"),
                        fieldWithPath("label").description("The predicate label")
                    )
                )
            )
    }
}
