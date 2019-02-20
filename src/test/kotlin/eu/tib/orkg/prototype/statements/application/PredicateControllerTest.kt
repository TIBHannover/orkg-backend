package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
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
            .perform(getRequestTo("/api/predicates/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    predicateListResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("has name").id

        mockMvc
            .perform(getRequestTo("/api/predicates/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    predicateResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.create("has name")
        service.create("gave name to")
        service.create("knows")

        mockMvc
            .perform(getRequestTo("/api/predicates/?q=name"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    predicateListResponseFields()
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "knows")

        mockMvc
            .perform(postRequestWithBody("/api/predicates/", resource))
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
                    predicateResponseFields()
                )
            )
    }

    private fun predicateResponseFields() =
        responseFields(
            fieldWithPath("id").description("The predicate ID"),
            fieldWithPath("label").description("The predicate label")
        )

    private fun predicateListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The predicate ID"),
            fieldWithPath("[].label").description("The predicate label")
        )
}
