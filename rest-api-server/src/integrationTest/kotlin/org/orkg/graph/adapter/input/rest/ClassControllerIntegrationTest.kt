package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createClass
import org.orkg.graph.adapter.input.rest.testing.fixtures.classResponseFields
import org.orkg.graph.input.ClassUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Class Controller")
@Transactional
@Import(MockUserDetailsService::class)
class ClassControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.removeAll()

        assertThat(service.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun index() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language")

        mockMvc
            .perform(getRequestTo("/api/classes/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val id = service.createClass(label = "research contribution")

        mockMvc
            .perform(getRequestTo("/api/classes/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    responseFields(classResponseFields())
                )
            )
    }

    @Test
    fun fetchByURI() {
        // Arrange
        val id = "dummy"
        val label = "dummy label"
        val uri = ParsedIRI("http://example.org/exists")
        service.createClass(id = id, label = label, uri = uri)

        // Act and Assert
        mockMvc
            .perform(getRequestTo("/api/classes/?uri=http://example.org/exists"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.label").value(label))
            .andExpect(jsonPath("$.uri").value(uri.toString()))
            .andDo(
                document(
                    snippet,
                    responseFields(classResponseFields())
                )
            )
    }

    @Test
    fun lookupByIds() {
        val id1 = service.createClass(label = "class1")
        val id2 = service.createClass(label = "class2")

        mockMvc
            .perform(getRequestTo("/api/classes/?ids=$id1,$id2"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookup() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language")
        service.createClass(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=research"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListDetailedResponseFields()
                )
            )
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language (PL)")
        service.createClass(label = "research topic")

        mockMvc
            .perform(getRequestTo("/api/classes/?q=PL)"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label")
                    ),
                    classListDetailedResponseFields()
                )
            )
    }

    fun classListDetailedResponseFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", classResponseFields()
        ).andWithPrefix("")
}
