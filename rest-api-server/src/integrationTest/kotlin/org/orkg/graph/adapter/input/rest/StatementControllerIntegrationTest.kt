package org.orkg.graph.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.graph.adapter.input.rest.LiteralControllerIntegrationTest.RestDoc.literalResponseFields
import org.orkg.graph.adapter.input.rest.PredicateControllerTest.RestDoc.predicateResponseFields
import org.orkg.graph.adapter.input.rest.ResourceControllerIntegrationTest.RestDoc.resourceResponseFields
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestBody
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Statement Controller")
@Transactional
@Import(MockUserDetailsService::class)
class StatementControllerIntegrationTest : RestDocumentationBaseTest() {

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
    fun index() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val l1 = literalService.create("literal")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")
        val pl = predicateService.create("to literal")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p2.id, r3.id)
        statementService.create(r1.id, p2.id, r3.id)
        statementService.create(r1.id, pl.id, l1.id)

        mockMvc
            .perform(getRequestTo("/api/statements/"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfStatementsWithAnyObjectResponseFields()
                )
            )
    }

    @Test
    fun fetch() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val l1 = literalService.create("literal")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")
        val pl = predicateService.create("to literal")

        val statement = statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p1.id, r3.id)
        statementService.create(r1.id, p2.id, r3.id)
        statementService.create(r2.id, pl.id, l1.id)

        mockMvc
            .perform(getRequestTo("/api/statements/$statement"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementWithResourceResponseFields()
                )
            )
    }

    @Test
    fun fetchLiteral() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val l1 = literalService.create("literal")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")
        val pl = predicateService.create("to literal")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p1.id, r3.id)
        statementService.create(r1.id, p2.id, r3.id)
        val statement = statementService.create(r2.id, pl.id, l1.id)

        mockMvc
            .perform(getRequestTo("/api/statements/$statement"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementWithLiteralResponseFields()
                )
            )
    }

    @Test
    fun lookupBySubject() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p2.id, r3.id)

        mockMvc
            .perform(getRequestTo("/api/statements/subject/${r1.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfStatementsWithAnyObjectResponseFields()
                )
            )
    }

    @Test
    fun lookupBySubjectAndPredicate() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p2.id, r2.id)
        statementService.create(r1.id, p2.id, r3.id)

        mockMvc
            .perform(getRequestTo("/api/statements/subject/${r1.id}/predicate/${p1.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andDo(
                document(
                    snippet,
                    pageOfStatementsWithAnyObjectResponseFields()
                )
            )
    }

    @Test
    fun lookupByPredicate() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p1.id, r3.id)
        statementService.create(r1.id, p2.id, r3.id)

        mockMvc
            .perform(getRequestTo("/api/statements/predicate/${p1.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    pageOfStatementsWithAnyObjectResponseFields()
                )
            )
    }

    @Test
    fun lookupByObjectAndPredicate() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("owns")
        val p2 = predicateService.create("have")
        val l1 = literalService.create("money")

        statementService.create(r1.id, p1.id, l1.id)
        statementService.create(r2.id, p2.id, l1.id)
        statementService.create(r3.id, p1.id, l1.id)

        mockMvc
            .perform(getRequestTo("/api/statements/object/${l1.id}/predicate/${p1.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andDo(
                document(
                    snippet,
                    pageOfStatementsWithAnyObjectResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun addWithResource() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val p = predicateService.create("less than")

        val body = mapOf(
            "subject_id" to r1.id,
            "predicate_id" to p.id,
            "object_id" to r2.id
        )

        mockMvc.perform(postRequestWithBody("/api/statements/", body))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    createdResponseHeaders(),
                    statementWithResourceResponseFields()
                )
            )
    }

    @Test
    @TestWithMockUser
    fun addWithLiteral() {
        val r = resourceService.create("one")
        val p = predicateService.create("has symbol")
        val l = literalService.create("1")

        val body = mapOf(
            "subject_id" to r.id,
            "predicate_id" to p.id,
            "object_id" to l.id
        )

        mockMvc.perform(postRequestWithBody("/api/statements/", body))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    createdResponseHeaders(),
                    statementWithLiteralResponseFields()
                )
            )
    }

    @Test
    @WithMockUser
    fun editResourceStatement() {
        val s = resourceService.create("ORKG")
        val p = predicateService.create("created by")
        val o = resourceService.create("Awesome Team")
        val st = statementService.create(s.id, p.id, o.id)

        val p2 = predicateService.create("with love from")
        val o2 = resourceService.create("Hannover")

        val body = mapOf(
            "predicate_id" to p2.id,
            "object_id" to o2.id
        )
        mockMvc.perform(putRequestWithBody("/api/statements/$st", body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.id.value))
            .andExpect(jsonPath("$.object.id").value(o2.id.value))
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    statementWithResourceResponseFields()
                )
            )
    }

    @Test
    @WithMockUser
    fun editLiteralStatement() {
        val s = resourceService.create("ORKG")
        val p = predicateService.create("based in")
        val o = literalService.create("Germany")
        val st = statementService.create(s.id, p.id, o.id)

        val p2 = predicateService.create("made with love from")

        val body = mapOf(
            "predicate_id" to p2.id
        )
        mockMvc.perform(putRequestWithBody("/api/statements/$st", body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.id.value))
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    statementWithLiteralResponseFields()
                )
            )
    }

    @Test
    fun fetchBundle() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val r4 = resourceService.create("four")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        statementService.create(r1.id, p1.id, r2.id)
        statementService.create(r1.id, p2.id, r3.id)
        statementService.create(r2.id, p1.id, r4.id)

        mockMvc
            .perform(getRequestTo("/api/statements/${r1.id}/bundle"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    bundleResponseFields()
                )
            )
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

        val a = resourceService.create("A")
        val b = resourceService.create("B")
        val c = resourceService.create("C")
        val d = resourceService.create("D")
        val e = resourceService.create("E")
        val f = resourceService.create("F")
        val bb = resourceService.create("BB")
        val cc = resourceService.create("CC")
        val dd = resourceService.create("DD")

        val p = predicateService.create("relation")

        statementService.create(a.id, p.id, b.id)
        statementService.create(b.id, p.id, c.id)
        statementService.create(c.id, p.id, d.id)
        statementService.create(c.id, p.id, cc.id)
        statementService.create(cc.id, p.id, c.id)
        statementService.create(cc.id, p.id, bb.id)
        statementService.create(cc.id, p.id, dd.id)
        statementService.create(bb.id, p.id, b.id)
        statementService.create(dd.id, p.id, d.id)
        // Inbound, should be excluded
        statementService.create(e.id, p.id, f.id)
        statementService.create(f.id, p.id, d.id)

        mockMvc
            .perform(getRequestTo("/api/statements/${a.id}/bundle"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statements", hasSize<Int>(9)))
            .andDo(
                document(
                    snippet,
                    bundleResponseFields()
                )
            )
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
        fieldWithPath("created_by").description("The ID of the user that created the statement. All zeros if unknown.")
    )

    private fun pageOfStatementsWithAnyObjectResponseFields() =
        responseFields(pageableDetailedFieldParameters())
            .andWithPrefix("content[].", statementFields())
            .andWithPrefix("content[].subject.", resourceResponseFields())
            .andWithPrefix("content[].predicate.", predicateResponseFields())
            .and(subsectionWithPath("content[].object").description("An object. Can be either a resource or a literal."))

    private fun sharedStatementResponseFields() =
        responseFields(statementFields())
            .andWithPrefix("subject.", resourceResponseFields())
            .andWithPrefix("predicate.", predicateResponseFields())

    private fun statementWithResourceResponseFields() = sharedStatementResponseFields()
        .andWithPrefix("object.", resourceResponseFields())

    private fun statementWithLiteralResponseFields() = sharedStatementResponseFields()
        .andWithPrefix("object.", literalResponseFields())
}
