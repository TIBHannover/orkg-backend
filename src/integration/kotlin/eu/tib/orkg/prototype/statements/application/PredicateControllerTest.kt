package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

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
                    createdResponseHeaders(),
                    predicateResponseFields()
                )
            )
    }

    private fun predicateResponseFields() =
        responseFields(
            fieldWithPath("id").description("The predicate ID"),
            fieldWithPath("label").description("The predicate label"),
            fieldWithPath("created_at").description("The predicate creation datetime")
        )

    private fun predicateListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The predicate ID"),
            fieldWithPath("[].label").description("The predicate label"),
            fieldWithPath("[].created_at").description("The predicate creation datetime")
        )
}
