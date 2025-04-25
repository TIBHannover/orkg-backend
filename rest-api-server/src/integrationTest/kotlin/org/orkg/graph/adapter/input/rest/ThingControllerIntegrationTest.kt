package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.createClass
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.testing.andExpectClass
import org.orkg.testing.andExpectLiteral
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPredicate
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
@TestPropertySource(properties = ["orkg.features.formatted_labels=false"])
internal class ThingControllerIntegrationTest : MockMvcBaseTest("things") {
    @Autowired
    private lateinit var resourceUseCases: ResourceUseCases

    @Autowired
    private lateinit var classUseCases: ClassUseCases

    @Autowired
    private lateinit var predicateUseCases: PredicateUseCases

    @Autowired
    private lateinit var literalUseCases: LiteralUseCases

    @Autowired
    private lateinit var statementUseCases: StatementUseCases

    @Autowired
    @Suppress("unused")
    private lateinit var unsafeResourceUseCases: UnsafeResourceUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceUseCases.deleteAll()
        classUseCases.deleteAll()
        predicateUseCases.deleteAll()
        statementUseCases.deleteAll()
        literalUseCases.deleteAll()

        assertThat(resourceUseCases.findAll(tempPageable)).hasSize(0)
        assertThat(classUseCases.findAll(tempPageable)).hasSize(0)
        assertThat(predicateUseCases.findAll(tempPageable)).hasSize(0)
        assertThat(statementUseCases.findAll(tempPageable)).hasSize(0)
        assertThat(literalUseCases.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun fetchResource() {
        val id = resourceUseCases.createResource()

        get("/api/things/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
    }

    @Test
    fun fetchClass() {
        val id = classUseCases.createClass()

        get("/api/things/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass()
    }

    @Test
    fun fetchPredicate() {
        val id = predicateUseCases.createPredicate()

        get("/api/things/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPredicate()
    }

    @Test
    fun fetchLiteral() {
        val id = literalUseCases.createLiteral()

        get("/api/things/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectLiteral()
    }

    @Test
    fun getPaged() {
        resourceUseCases.createResource()
        classUseCases.createClass()
        predicateUseCases.createPredicate()
        literalUseCases.createLiteral()

        get("/api/things")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("$.page.total_elements").value(4))
    }
}
