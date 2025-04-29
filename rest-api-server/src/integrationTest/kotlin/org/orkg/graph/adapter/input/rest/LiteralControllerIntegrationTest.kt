package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createLiteral
import org.orkg.graph.input.LiteralUseCases
import org.orkg.testing.andExpectLiteral
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class LiteralControllerIntegrationTest : MockMvcBaseTest("literals") {
    @Autowired
    private lateinit var service: LiteralUseCases

    @BeforeEach
    fun setup() {
        service.deleteAll()

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
    fun create() {
        val input = mapOf("label" to "foo", "datatype" to "xs:foo")

        documentedPostRequestTo("/api/literals")
            .content(input)
            .perform()
            .andExpect(status().isCreated)
            .andExpectLiteral()
            .andExpect(jsonPath("$.label").value(input["label"] as String))
            .andExpect(jsonPath("$.datatype").value(input["datatype"] as String))
    }

    @Test
    @TestWithMockUser
    fun update() {
        val literalId = service.createLiteral(
            label = "foo",
            datatype = "dt:old"
        )

        val update = mapOf("label" to "bar", "datatype" to "dt:new")

        put("/api/literals/{id}", literalId)
            .content(update)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string("Location", endsWith("api/literals/$literalId")))
            .andExpect(jsonPath("$.label").value(update["label"] as String))
            .andExpect(jsonPath("$.datatype").value(update["datatype"] as String))
    }
}
