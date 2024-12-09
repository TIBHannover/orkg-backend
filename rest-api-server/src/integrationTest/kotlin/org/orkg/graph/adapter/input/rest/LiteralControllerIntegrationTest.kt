package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createLiteral
import org.orkg.graph.input.LiteralUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Literal Controller")
@Transactional
@Import(MockUserDetailsService::class)
internal class LiteralControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: LiteralUseCases

    @BeforeEach
    fun setup() {
        service.removeAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    fun index() {
        service.createLiteral(label = "research contribution")
        service.createLiteral(label = "programming language")

        mockMvc
            .perform(getRequestTo("/api/literals"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(),
                    pagedLiteralResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.createLiteral(label = "research contribution")
        service.createLiteral(label = "programming language")
        service.createLiteral(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/literals").param("q", "research"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    pagedLiteralResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createLiteral(label = "research contribution")
        service.createLiteral(label = "programming language (PL)")
        service.createLiteral(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/literals").param("q", "PL)"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))
            .andDo(
                document(
                    snippet,
                    pageableRequestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    pagedLiteralResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.createLiteral(label = "research contribution")

        mockMvc
            .perform(getRequestTo("/api/literals/{id}", id))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(literalResponseFields())
                )
            )
    }

    @Test
    @TestWithMockUser
    fun add() {
        val input = mapOf("label" to "foo", "datatype" to "xs:foo")

        mockMvc
            .perform(postRequestWithBody("/api/literals", input))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.label").value(input["label"] as String))
            .andExpect(jsonPath("$.datatype").value(input["datatype"] as String))
            .andDo(
                document(
                    snippet,
                    requestFields(ofCreateAndUpdateRequests()),
                    createdResponseHeaders(),
                    responseFields(literalResponseFields())
                )
            )
    }

    @Test
    @TestWithMockUser
    fun edit() {
        val resource = service.createLiteral(
            label = "foo",
            datatype = "dt:old"
        )

        val update = mapOf("label" to "bar", "datatype" to "dt:new")

        mockMvc
            .perform(putRequestWithBody("/api/literals/{id}", update, resource))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(update["label"] as String))
            .andExpect(jsonPath("$.datatype").value(update["datatype"] as String))
            .andDo(
                document(
                    snippet,
                    requestFields(ofCreateAndUpdateRequests()),
                    responseFields(literalResponseFields())
                )
            )
    }

    fun pagedLiteralResponseFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix(
                "content[].", literalResponseFields()
            ).andWithPrefix("")

    companion object RestDoc {
        fun ofCreateAndUpdateRequests() = listOf(
            fieldWithPath("label").description("The updated value of the literal."),
            fieldWithPath("datatype").description("The updated datatype of the literal value.")
        )

        fun literalResponseFields() = listOf(
            fieldWithPath("id").description("The resource ID"),
            fieldWithPath("label").description("The resource label"),
            fieldWithPath("datatype").description("The data type of the literal value. Defaults to `xsd:string`."),
            fieldWithPath("created_at").description("The resource creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the literal. All zeros if unknown."),
            fieldWithPath("_class").optional().ignored(),
            fieldWithPath("featured").optional().ignored(),
            fieldWithPath("unlisted").optional().ignored(),
            fieldWithPath("modifiable").description("Whether this literal can be modified.").optional().ignored(),
        )
    }
}
