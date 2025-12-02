package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClass
import org.orkg.graph.input.ClassUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class ClassControllerIntegrationTest : MockMvcBaseTest("classes") {
    @Autowired
    private lateinit var service: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        service.deleteAll()

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

        get("/api/classes/{id}", id)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetchByURI() {
        // Arrange
        val id = ThingId("dummy")
        val label = "dummy label"
        val uri = ParsedIRI.create("https://example.org/exists")
        service.createClass(label = label, id = id, uri = uri)

        // Act and Assert
        get("/api/classes")
            .param("uri", uri.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(id.value))
            .andExpect(jsonPath("$.content[0].label").value(label))
            .andExpect(jsonPath("$.content[0].uri").value(uri.toString()))
            .andExpect(jsonPath("$.page.total_elements").value(1))
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
}
