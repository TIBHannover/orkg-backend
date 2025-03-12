package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class PredicateControllerIntegrationTest : MockMvcBaseTest("predicates") {
    @Autowired
    private lateinit var service: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @BeforeEach
    fun setup() {
        service.deleteAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    fun index() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "knows")

        get("/api/predicates")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetch() {
        val id = service.createPredicate(label = "has name")

        get("/api/predicates/{id}", id)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun lookup() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "knows")

        get("/api/predicates")
            .param("q", "name")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun lookupWithSpecialChars() {
        service.createPredicate(label = "has name")
        service.createPredicate(label = "gave name to")
        service.createPredicate(label = "know(s)")

        get("/api/predicates").param("q", "know(")
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    @TestWithMockUser
    fun edit() {
        val predicate = service.createPredicate(label = "knows")

        val newLabel = "yaser"
        val resource = mapOf("label" to newLabel)

        put("/api/predicates/{id}", predicate)
            .content(resource)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string("Location", endsWith("api/predicates/$predicate")))
            .andExpect(jsonPath("$.label").value(newLabel))
    }

    @Test
    @TestWithMockUser
    fun deletePredicateNotFound() {
        delete("/api/predicates/{id}", "NONEXISTENT")
            .perform()
            .andExpect(status().isNotFound)
    }

    @Test
    @TestWithMockUser
    fun deletePredicateSuccess() {
        val id = service.createPredicate(label = "bye bye", contributorId = ContributorId(MockUserId.USER))

        delete("/api/predicates/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    @TestWithMockUser
    fun deletePredicateForbidden() {
        val subject = resourceService.createResource(label = "subject")
        val `object` = resourceService.createResource(label = "child")
        val predicate = service.createPredicate(label = "related")
        statementService.createStatement(subject, predicate, `object`)

        delete("/api/predicates/{id}", predicate)
            .perform()
            .andExpect(status().isForbidden)
    }

    @Test
    fun deletePredicateWithoutLogin() {
        val id = service.createPredicate(label = "To Delete")

        delete("/api/predicates/{id}", id)
            .perform()
            .andExpect(status().isForbidden)
    }
}
