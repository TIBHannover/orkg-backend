package org.orkg.graph.adapter.input.rest

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.testing.fixtures.PageRepresentation
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.annotations.IntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue

@IntegrationTest
internal class PathControllerIntegrationTest : MockMvcBaseTest("paths") {
    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @BeforeEach
    fun setup() {
        assertThat(statementService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(resourceService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(predicateService.findAll(PageRequests.SINGLE)).hasSize(0)
    }

    @AfterEach
    fun cleanup() {
        statementService.deleteAll()
        resourceService.deleteAll()
        predicateService.deleteAll()
    }

    @Test
    fun findAllByRootId() {
        /* Test setup:
             A → B → C  → D ← F ← E
                 ↑   ↓↑   ↑
                BB ← CC → DD

           Expected result (starting from A):
             - 9 paths (excluding E→F,F→D)
         */

        val a = resourceService.createResource(label = "A")
        val b = resourceService.createResource(label = "B")
        val c = resourceService.createResource(label = "C")
        val d = resourceService.createResource(label = "D")
        val e = resourceService.createResource(label = "E")
        val f = resourceService.createResource(label = "F")
        val bb = resourceService.createResource(label = "BB")
        val cc = resourceService.createResource(label = "CC")
        val dd = resourceService.createResource(label = "DD")

        val p = predicateService.createPredicate(label = "relation")

        val expected = listOf(
            statementService.createStatement(a, p, b),
            statementService.createStatement(b, p, c),
            statementService.createStatement(c, p, d),
            statementService.createStatement(c, p, cc),
            statementService.createStatement(cc, p, c),
            statementService.createStatement(cc, p, bb),
            statementService.createStatement(cc, p, dd),
            statementService.createStatement(bb, p, b),
            statementService.createStatement(dd, p, d),
        )
        // Inbound, should be excluded
        statementService.createStatement(e, p, f)
        statementService.createStatement(f, p, d)

        val result = get("/api/things/{id}/paths", a)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<PageRepresentation<PathRepresentation>>(it) }

        result.content.size shouldBe expected.size
    }

    @Test
    fun findAllByRootIdInverse() {
        /* Test setup:
             A → B → C  → D ← F ← E
                 ↑   ↓↑   ↑
                BB ← CC → DD

           Expected result (starting from A):
             - 9 paths (excluding E→F,F→D), grouped into 6 lists (B, C and D are reachable via two paths)
         */

        val a = resourceService.createResource(label = "A")
        val b = resourceService.createResource(label = "B")
        val c = resourceService.createResource(label = "C")
        val d = resourceService.createResource(label = "D")
        val e = resourceService.createResource(label = "E")
        val f = resourceService.createResource(label = "F")
        val bb = resourceService.createResource(label = "BB")
        val cc = resourceService.createResource(label = "CC")
        val dd = resourceService.createResource(label = "DD")

        val p = predicateService.createPredicate(label = "relation")

        statementService.createStatement(a, p, b)
        statementService.createStatement(b, p, c)
        statementService.createStatement(c, p, d)
        statementService.createStatement(c, p, cc)
        statementService.createStatement(cc, p, c)
        statementService.createStatement(cc, p, bb)
        statementService.createStatement(cc, p, dd)
        statementService.createStatement(bb, p, b)
        statementService.createStatement(dd, p, d)
        statementService.createStatement(e, p, f)
        statementService.createStatement(f, p, d)

        val expected = listOf(
            listOf(
                listOf("B", "relation", "A"),
                listOf("B", "relation", "BB", "relation", "CC", "relation", "C", "relation", "B", "relation", "A"),
            ),
            listOf(
                listOf("C", "relation", "B", "relation", "A"),
                listOf("C", "relation", "CC", "relation", "C", "relation", "B", "relation", "A"),
            ),
            listOf(listOf("CC", "relation", "C", "relation", "B", "relation", "A")),
            listOf(
                listOf("D", "relation", "C", "relation", "B", "relation", "A"),
                listOf("D", "relation", "DD", "relation", "CC", "relation", "C", "relation", "B", "relation", "A"),
            ),
            listOf(listOf("DD", "relation", "CC", "relation", "C", "relation", "B", "relation", "A")),
            listOf(listOf("BB", "relation", "CC", "relation", "C", "relation", "B", "relation", "A")),
        )

        val result = get("/api/things/{id}/inverse-paths", a)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectStatement("$.content[*]")
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue<PageRepresentation<List<PathRepresentation>>>(it) }

        result.content.size shouldBe expected.size
        result.content.map { paths -> paths.map { paths -> paths.map { thing -> thing.label } } } shouldBe expected
    }
}
