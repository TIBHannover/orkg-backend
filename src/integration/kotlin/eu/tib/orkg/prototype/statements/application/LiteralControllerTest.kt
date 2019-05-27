package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.LiteralService
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
            .perform(getRequestTo("/api/literals/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    literalListResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.create("research contribution")
        service.create("programming language")
        service.create("research topic")

        mockMvc
            .perform(getRequestTo("/api/literals/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    literalListResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.create("research contribution").id

        mockMvc
            .perform(getRequestTo("/api/literals/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    literalResponseFields()
                )
            )
    }

    @Test
    fun add() {
        val resource = mapOf("label" to "foo")

        mockMvc
            .perform(postRequestWithBody("/api/literals/", resource))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestFields(
                        fieldWithPath("label").description("The resource label")
                    ),
                    createdResponseHeaders(),
                    literalResponseFields()
                )
            )
    }

    private fun literalResponseFields() =
        responseFields(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label"),
            fieldWithPath("createdAt").description("The resource creation datetime")
        )

    private fun literalListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The resource ID"),
            fieldWithPath("[].label").description("The resource label"),
            fieldWithPath("[].createdAt").description("The resource creation datetime")
        )
}
