package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createLiteral
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.PredicateControllerIntegrationTest.RestDoc.predicateResponseFields
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@DisplayName("Statement Controller")
@Transactional
@Import(MockUserDetailsService::class)
internal class StatementControllerIntegrationTest : RestDocsTest("statements") {

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
        val tempPageable = PageRequest.of(0, 10)

        statementService.removeAll()
        resourceService.removeAll()
        predicateService.removeAll()
        literalService.removeAll()

        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(literalService.findAll(tempPageable)).hasSize(0)
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

        val statement = statementService.create(r1, p1, r2)
        statementService.create(r1, p1, r3)
        statementService.create(r1, p2, r3)
        statementService.create(r2, pl, l1)

        mockMvc
            .perform(get("/api/statements/{id}", statement))
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

        statementService.create(r1, p1, r2)
        statementService.create(r1, p1, r3)
        statementService.create(r1, p2, r3)
        val statement = statementService.create(r2, pl, l1)

        mockMvc
            .perform(get("/api/statements/{id}", statement))
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

        mockMvc.perform(
            post("/api/statements")
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
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

        mockMvc.perform(
            post("/api/statements")
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
    }

    @Test
    @WithMockUser
    fun editResourceStatement() {
        val s = resourceService.createResource(label = "ORKG")
        val p = predicateService.createPredicate(label = "created by")
        val o = resourceService.createResource(label = "Awesome Team")
        val st = statementService.create(s, p, o)

        val p2 = predicateService.createPredicate(label = "with love from")
        val o2 = resourceService.createResource(label = "Hannover")

        val body = mapOf(
            "predicate_id" to p2,
            "object_id" to o2
        )

        mockMvc.perform(
            put("/api/statements/{id}", st)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.value))
            .andExpect(jsonPath("$.object.id").value(o2.value))
    }

    @Test
    @WithMockUser
    fun editLiteralStatement() {
        val s = resourceService.createResource(label = "ORKG")
        val p = predicateService.createPredicate(label = "based in")
        val o = literalService.createLiteral(label = "Germany")
        val st = statementService.create(s, p, o)

        val p2 = predicateService.createPredicate(label = "made with love from")

        val body = mapOf(
            "predicate_id" to p2
        )
        mockMvc.perform(
            put("/api/statements/{id}", st)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.value))
    }

    @Test
    fun fetchBundle() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val r4 = resourceService.createResource(label = "four")
        val p1 = predicateService.createPredicate(label = "blah")
        val p2 = predicateService.createPredicate(label = "blub")

        statementService.create(r1, p1, r2)
        statementService.create(r1, p2, r3)
        statementService.create(r2, p1, r4)

        mockMvc
            .perform(documentedGetRequestTo("/api/statements/{id}/bundle", r1))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    bundleResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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

        statementService.create(a, p, b)
        statementService.create(b, p, c)
        statementService.create(c, p, d)
        statementService.create(c, p, cc)
        statementService.create(cc, p, c)
        statementService.create(cc, p, bb)
        statementService.create(cc, p, dd)
        statementService.create(bb, p, b)
        statementService.create(dd, p, d)
        // Inbound, should be excluded
        statementService.create(e, p, f)
        statementService.create(f, p, d)

        mockMvc
            .perform(get("/api/statements/{id}/bundle", a))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statements", hasSize<Int>(9)))
    }

    private fun bundleResponseFields() =
        responseFields(
            listOf(
                fieldWithPath("root").description("The root ID of the object"),
                fieldWithPath("statements").description("The bundle of statements")
            )
        )
            .andWithPrefix("statements[].", statementFields())
            .andWithPrefix("statements[].subject.", resourceResponseFields())
            .andWithPrefix("statements[].predicate.", predicateResponseFields())
            .andWithPrefix("statements[].object.", resourceResponseFields())

    private fun statementFields() = listOf(
        fieldWithPath("id").description("The statement ID"),
        fieldWithPath("created_at").description("The statement creation datetime"),
        fieldWithPath("created_by").description("The ID of the user that created the statement. All zeros if unknown."),
        fieldWithPath("modifiable").description("Whether this statement can be modified.")
    )
}
