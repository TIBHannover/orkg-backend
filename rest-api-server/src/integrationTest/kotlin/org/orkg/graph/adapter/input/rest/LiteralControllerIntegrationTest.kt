package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createLiteral
import org.orkg.graph.input.LiteralUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class LiteralControllerIntegrationTest : MockMvcBaseTest("literals") {
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

        get("/api/literals")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun lookup() {
        service.createLiteral(label = "research contribution")
        service.createLiteral(label = "programming language")
        service.createLiteral(label = "research topic")

        get("/api/literals").param("q", "research")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createLiteral(label = "research contribution")
        service.createLiteral(label = "programming language (PL)")
        service.createLiteral(label = "research topic")

        get("/api/literals").param("q", "PL)")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun fetch() {
        val id = service.createLiteral(label = "research contribution")

        get("/api/literals/{id}", id)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    @TestWithMockUser
    fun add() {
        val input = mapOf("label" to "foo", "datatype" to "xs:foo")

        documentedPostRequestTo("/api/literals")
            .content(input)
            .perform()
            .andExpect(status().isCreated)
//            TODO: Needs to be converted into a unit test in order to test location header path
//            .andExpect(header().string("Location", endsWith("api/literals/$id")))
            .andExpect(jsonPath("$.label").value(input["label"] as String))
            .andExpect(jsonPath("$.datatype").value(input["datatype"] as String))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created literal can be fetched from.")
                    ),
                    requestFields(ofCreateAndUpdateRequests()),
                    responseFields(literalResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @TestWithMockUser
    fun edit() {
        val literalId = service.createLiteral(
            label = "foo",
            datatype = "dt:old"
        )

        val update = mapOf("label" to "bar", "datatype" to "dt:new")

        documentedPutRequestTo("/api/literals/{id}", literalId)
            .content(update)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string("Location", endsWith("api/literals/$literalId")))
            .andExpect(jsonPath("$.label").value(update["label"] as String))
            .andExpect(jsonPath("$.datatype").value(update["datatype"] as String))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the literal.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literal can be fetched from.")
                    ),
                    requestFields(ofCreateAndUpdateRequests()),
                    responseFields(literalResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

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
