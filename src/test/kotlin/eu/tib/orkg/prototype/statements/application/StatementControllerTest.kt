package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.result.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.*

@DisplayName("Statement Controller")
@Transactional
class StatementControllerTest : RestDocumentationBaseTest() {

    override fun createController() = controller

    @Autowired
    private lateinit var statementWithResourceService: StatementWithResourceService

    @Autowired
    private lateinit var statementWithLiteralService: StatementWithLiteralService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var literalService: LiteralService

    @Autowired
    private lateinit var controller: StatementController

    @Test
    fun index() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val l1 = literalService.create("literal")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")
        val pl = predicateService.create("to literal")

        statementWithResourceService.create(r1.id!!, p1.id!!, r2.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)
        statementWithLiteralService.create(r1.id!!, pl.id!!, l1.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementListResponseFields()
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

        val statement =
            statementWithResourceService.create(r1.id!!, p1.id!!, r2.id!!)
        statementWithResourceService.create(r1.id!!, p1.id!!, r3.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)
        statementWithLiteralService.create(r2.id!!, pl.id!!, l1.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/${statement.id}"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementResponseFields()
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

        statementWithResourceService.create(r1.id!!, p1.id!!, r2.id!!)
        statementWithResourceService.create(r1.id!!, p1.id!!, r3.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)
        val statement =
            statementWithLiteralService.create(r2.id!!, pl.id!!, l1.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/${statement.id}"))
            .andDo(MockMvcResultHandlers.print())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementResponseFields()
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

        statementWithResourceService.create(r1.id!!, p1.id!!, r2.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/subject/${r1.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementListResponseFields()
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

        statementWithResourceService.create(r1.id!!, p1.id!!, r2.id!!)
        statementWithResourceService.create(r1.id!!, p1.id!!, r3.id!!)
        statementWithResourceService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/predicate/${p1.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    statementListResponseFields()
                )
            )
    }

    @Test
    fun addWithResource() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val p = predicateService.create("less than")

        val body = mapOf(
            "subject_id" to r1.id,
            "predicate_id" to p.id,
            "object" to mapOf("id" to r2.id, "_class" to "resource")
        )

        mockMvc.perform(postRequestWithBody("/api/statements/", body))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    createdResponseHeaders(),
                    statementResponseFields()
                )
            )
    }

    @Test
    fun addWithLiteral() {
        val r = resourceService.create("one")
        val p = predicateService.create("has symbol")
        val l = literalService.create("1")

        val body = mapOf(
            "subject_id" to r.id,
            "predicate_id" to p.id,
            "object" to mapOf("id" to l.id, "_class" to "literal")
        )

        mockMvc.perform(postRequestWithBody("/api/statements/", body))
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    createdResponseHeaders(),
                    statementResponseFields()
                )
            )
    }

    private fun statementResponseFields() =
        responseFields(
            fieldWithPath("id").description("The statement ID"),
            fieldWithPath("subject").description("A resource"),
            fieldWithPath("subject.id").description("The ID of the subject resource"),
            fieldWithPath("subject.label").description("The label of the subject resource"),
            fieldWithPath("predicate").description("A predicate"),
            fieldWithPath("predicate.id").description("The ID of the predicate"),
            fieldWithPath("predicate.label").description("The label of the predicate"),
            fieldWithPath("object").description("An object"),
            fieldWithPath("object.id").description("The ID of the object"),
            fieldWithPath("object.label").description("The label of the object"),
            fieldWithPath("object._class").description("The type of the object (resource or literal).")
        )

    private fun statementListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The statement ID"),
            fieldWithPath("[].subject").description("A resource"),
            fieldWithPath("[].subject.id").description("The ID of the subject resource"),
            fieldWithPath("[].subject.label").description("The label of the subject resource"),
            fieldWithPath("[].predicate").description("A predicate"),
            fieldWithPath("[].predicate.id").description("The ID of the predicate"),
            fieldWithPath("[].predicate.label").description("The label of the predicate"),
            fieldWithPath("[].object").description("An object"),
            fieldWithPath("[].object.id").description("The ID of the object"),
            fieldWithPath("[].object.label").description("The label of the object"),
            fieldWithPath("[].object._class").description("The type of the object (resource or literal).")
        )
}
