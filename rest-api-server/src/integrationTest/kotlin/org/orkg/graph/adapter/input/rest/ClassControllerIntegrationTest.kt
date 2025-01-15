package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createClass
import org.orkg.graph.adapter.input.rest.testing.fixtures.classResponseFields
import org.orkg.graph.input.ClassUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.pageableDetailedFieldParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
@Import(MockUserDetailsService::class)
internal class ClassControllerIntegrationTest : RestDocsTest("classes") {

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

        get("/api/classes")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetch() {
        val id = service.createClass(label = "research contribution")

        documentedGetRequestTo("/api/classes/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    ),
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun fetchByURI() {
        // Arrange
        val id = "dummy"
        val label = "dummy label"
        val uri = ParsedIRI("https://example.org/exists")
        service.createClass(id = id, label = label, uri = uri)

        // Act and Assert
        documentedGetRequestTo("/api/classes")
            .param("uri", uri.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.label").value(label))
            .andExpect(jsonPath("$.uri").value(uri.toString()))
            .andDo(
                documentationHandler.document(
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookupByIds() {
        val id1 = service.createClass(label = "class1")
        val id2 = service.createClass(label = "class2")

        documentedGetRequestTo("/api/classes")
            .param("ids", "$id1", "$id2")
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    classListDetailedResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun lookup() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language")
        service.createClass(label = "research topic")

        get("/api/classes").param("q", "research")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createClass(label = "research contribution")
        service.createClass(label = "programming language (PL)")
        service.createClass(label = "research topic")

        get("/api/classes").param("q", "PL)")
            .perform()
            .andExpect(status().isOk)
    }

    fun classListDetailedResponseFields(): ResponseFieldsSnippet =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", classResponseFields()
        ).andWithPrefix("")
}
