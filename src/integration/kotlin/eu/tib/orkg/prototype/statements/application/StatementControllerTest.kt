package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.auth.MockUserDetailsService
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteralService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestBody
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Statement Controller")
@Transactional
@Import(MockUserDetailsService::class)
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
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional()
                    ),
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
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional()
                    ),
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
                    requestParameters(
                        parameterWithName("page").description("Page number of items to fetch (default: 1)").optional(),
                        parameterWithName("items").description("Number of items to fetch per page (default: 10)").optional(),
                        parameterWithName("sortBy").description("Key to sort by (default: not provided)").optional(),
                        parameterWithName("desc").description("Direction of the sorting (default: false)").optional()
                    ),
                    statementListResponseFields()
                )
            )
    }

    @Test
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
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
    @WithUserDetails("user", userDetailsServiceBeanName = "mockUserDetailsService")
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

    @Test
    fun deleteLiteralStatement() {
        val s = resourceService.create("one")
        val p = predicateService.create("has symbol")
        val l = literalService.create("1")
        val st = statementWithLiteralService.create(s.id!!, p.id!!, l.id!!)

        mockMvc.perform(deleteRequest("/api/statements/${st.id}"))
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    snippet,
                    requestBody())
            )
    }

    @Test
    fun deleteResourceStatement() {
        val s = resourceService.create("one")
        val p = predicateService.create("has creator")
        val r = resourceService.create("Leibniz")
        val st = statementWithResourceService.create(s.id!!, p.id!!, r.id!!)

        mockMvc.perform(deleteRequest("/api/statements/${st.id}"))
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    snippet,
                    requestBody())
            )
    }

    @Test
    fun editResourceStatement() {
        val s = resourceService.create("ORKG")
        val p = predicateService.create("created by")
        val o = resourceService.create("Awesome Team")
        val st = statementWithResourceService.create(s.id!!, p.id!!, o.id!!)

        val p2 = predicateService.create("with love from")
        val o2 = resourceService.create("Hannover")

        val body = mapOf(
            "predicate_id" to p2.id!!,
            "object_id" to o2.id!!
        )
        mockMvc.perform(putRequestWithBody("/api/statements/${st.id}", body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.id!!.toString()))
            .andExpect(jsonPath("$.object.id").value(o2.id!!.toString()))
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    statementResponseFields())
            )
    }

    @Test
    fun editLiteralStatement() {
        val s = resourceService.create("ORKG")
        val p = predicateService.create("based in")
        val o = literalService.create("Germany")
        val st = statementWithLiteralService.create(s.id!!, p.id!!, o.id!!)

        val p2 = predicateService.create("made with love from")

        val body = mapOf(
            "predicate_id" to p2.id!!
        )
        mockMvc.perform(putRequestWithBody("/api/statements/${st.id}", body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.predicate.id").value(p2.id!!.toString()))
            .andDo(
                document(
                    snippet,
                    requestBody(),
                    statementResponseFields())
            )
    }

    private fun statementResponseFields() =
        responseFields(
            fieldWithPath("id").description("The statement ID"),
            fieldWithPath("created_at").description("The statement creation datetime"),
            fieldWithPath("created_by").description("The ID of the user that created the statement. All zeros if unknown."),
            fieldWithPath("subject").description("A resource"),
            fieldWithPath("subject.id").description("The ID of the subject resource"),
            fieldWithPath("subject.label").description("The label of the subject resource"),
            fieldWithPath("subject.created_at").description("The subject creation datetime"),
            fieldWithPath("subject.created_by").description("The ID of the user that created the subject. All zeros if unknown."),
            fieldWithPath("subject.classes").description("The classes the subject resource belongs to"),
            fieldWithPath("subject.shared").description("The number of time this resource has been shared"),
            fieldWithPath("predicate").description("A predicate"),
            fieldWithPath("predicate.id").description("The ID of the predicate"),
            fieldWithPath("predicate.label").description("The label of the predicate"),
            fieldWithPath("predicate.created_at").description("The predicate creation datetime"),
            fieldWithPath("predicate.created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
            fieldWithPath("object").description("An object"),
            fieldWithPath("object.id").description("The ID of the object"),
            fieldWithPath("object.label").description("The label of the object"),
            fieldWithPath("object._class").description("The type of the object (resource or literal)."),
            fieldWithPath("object.created_at").description("The object creation datetime"),
            fieldWithPath("object.created_by").description("The ID of the user that created the object. All zeros if unknown."),
            fieldWithPath("object.classes").description("The classes the object resource belongs to"),
            fieldWithPath("object.shared").optional().ignored()
        )

    private fun statementListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The statement ID"),
            fieldWithPath("[].created_at").description("The statement creation datetime"),
            fieldWithPath("[].created_by").description("The ID of the user that created the statement. All zeros if unknown."),
            fieldWithPath("[].subject").description("A resource"),
            fieldWithPath("[].subject.id").description("The ID of the subject resource"),
            fieldWithPath("[].subject.label").description("The label of the subject resource"),
            fieldWithPath("[].subject.created_at").description("The subject creation datetime"),
            fieldWithPath("[].subject.created_by").description("The ID of the user that created the subject. All zeros if unknown."),
            fieldWithPath("[].subject.classes").description("The classes the subject resource belongs to"),
            fieldWithPath("[].subject.shared").description("The number of time this resource has been shared"),
            fieldWithPath("[].predicate").description("A predicate"),
            fieldWithPath("[].predicate.id").description("The ID of the predicate"),
            fieldWithPath("[].predicate.label").description("The label of the predicate"),
            fieldWithPath("[].predicate.created_at").description("The predicate creation datetime"),
            fieldWithPath("[].predicate.created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
            fieldWithPath("[].object").description("An object"),
            fieldWithPath("[].object.id").description("The ID of the object"),
            fieldWithPath("[].object.label").description("The label of the object"),
            fieldWithPath("[].object._class").description("The type of the object (resource or literal)."),
            fieldWithPath("[].object.created_at").description("The object creation datetime"),
            fieldWithPath("[].object.created_by").description("The ID of the user that created the object. All zeros if unknown."),
            fieldWithPath("[].object.classes").description("The classes the object resource belongs to"),
            fieldWithPath("[].object.shared").optional().ignored()
        )
}
