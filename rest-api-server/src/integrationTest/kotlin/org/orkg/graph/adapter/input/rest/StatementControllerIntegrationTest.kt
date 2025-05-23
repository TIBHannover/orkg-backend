package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class StatementControllerIntegrationTest : MockMvcBaseTest("statements") {
    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @BeforeEach
    fun setup() {
        cleanup()

        assertThat(statementService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(resourceService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(predicateService.findAll(PageRequests.SINGLE)).hasSize(0)
        assertThat(literalService.findAll(PageRequests.SINGLE)).hasSize(0)
    }

    @AfterEach
    fun cleanup() {
        statementService.deleteAll()
        resourceService.deleteAll()
        predicateService.deleteAll()
        literalService.deleteAll()
    }

    @Test
    fun fetch() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val l1 = literalService.createLiteral(label = "literal")
        val p1 = predicateService.createPredicate(label = "blah")
        val p2 = predicateService.createPredicate(label = "blub")
        val pl = predicateService.createPredicate(label = "to literal")

        val statement = statementService.createStatement(r1, p1, r2)
        statementService.createStatement(r1, p1, r3)
        statementService.createStatement(r1, p2, r3)
        statementService.createStatement(r2, pl, l1)

        get("/api/statements/{id}", statement)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetchLiteral() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val l1 = literalService.createLiteral(label = "literal")
        val p1 = predicateService.createPredicate(label = "blah")
        val p2 = predicateService.createPredicate(label = "blub")
        val pl = predicateService.createPredicate(label = "to literal")

        statementService.createStatement(r1, p1, r2)
        statementService.createStatement(r1, p1, r3)
        statementService.createStatement(r1, p2, r3)
        val statement = statementService.createStatement(r2, pl, l1)

        get("/api/statements/{id}", statement)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    @TestWithMockUser
    fun addWithResource() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val p = predicateService.createPredicate(label = "less than")

        val body = mapOf(
            "subject_id" to r1,
            "predicate_id" to p,
            "object_id" to r2
        )

        post("/api/statements")
            .content(body)
            .perform()
            .andExpect(status().isCreated)
    }

    @Test
    @TestWithMockUser
    fun addWithLiteral() {
        val r = resourceService.createResource(label = "one")
        val p = predicateService.createPredicate(label = "has symbol")
        val l = literalService.createLiteral(label = "1")

        val body = mapOf(
            "subject_id" to r,
            "predicate_id" to p,
            "object_id" to l
        )

        post("/api/statements")
            .content(body)
            .perform()
            .andExpect(status().isCreated)
    }

    @Test
    @TestWithMockUser
    fun editResourceStatement() {
        val s = resourceService.createResource(label = "ORKG")
        val p = predicateService.createPredicate(label = "created by")
        val o = resourceService.createResource(label = "Awesome Team")
        val st = statementService.createStatement(s, p, o)

        val p2 = predicateService.createPredicate(label = "with love from")
        val o2 = resourceService.createResource(label = "Hannover")

        val body = mapOf(
            "predicate_id" to p2,
            "object_id" to o2
        )

        put("/api/statements/{id}", st)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    @TestWithMockUser
    fun editLiteralStatement() {
        val s = resourceService.createResource(label = "ORKG")
        val p = predicateService.createPredicate(label = "based in")
        val o = literalService.createLiteral(label = "Germany")
        val st = statementService.createStatement(s, p, o)

        val p2 = predicateService.createPredicate(label = "made with love from")

        val body = mapOf(
            "predicate_id" to p2
        )
        put("/api/statements/{id}", st)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)
    }

    @Test
    fun fetchBundle() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val r4 = resourceService.createResource(label = "four")
        val p1 = predicateService.createPredicate(label = "blah")
        val p2 = predicateService.createPredicate(label = "blub")

        statementService.createStatement(r1, p1, r2)
        statementService.createStatement(r1, p2, r3)
        statementService.createStatement(r2, p1, r4)

        get("/api/statements/{id}/bundle", r1)
            .perform()
            .andExpect(status().isOk)
    }

    @Test
    fun fetchBundleListsAllRelationshipsOnce() {
        /* Test setup:
             A → B → C  → D ← F ← E
                 ↑   ↓↑   ↑
                BB ← CC → DD

           Expected result (starting from A):
             - 9 statements (excluding E→F,F→D)
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
        // Inbound, should be excluded
        statementService.createStatement(e, p, f)
        statementService.createStatement(f, p, d)

        get("/api/statements/{id}/bundle", a)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statements", hasSize<Int>(9)))
    }
}
