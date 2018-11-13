package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.infrastructure.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.http.*
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@DisplayName("Statement Controller")
class StatementControllerTest : RestDocumentationBaseTest() {

    override fun createController() = controller

    @Autowired
    private lateinit var statementService: StatementWithResourceService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var controller: StatementController

    @Test
    fun index() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        statementService.create(r1.id!!, p1.id!!, r2.id!!)
        statementService.create(r1.id!!, p2.id!!, r3.id!!)
        statementService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(
                get("/api/statements/")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
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
                        fieldWithPath("[].object.label").description("The label of the object")
                    )
                )
            )
    }

    @Test
    fun fetch() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        val statement = statementService.create(r1.id!!, p1.id!!, r2.id!!)
        statementService.create(r1.id!!, p1.id!!, r3.id!!)
        statementService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(
                get("/api/statements/${statement.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
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
                        fieldWithPath("object.label").description("The label of the object")
                    )
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

        statementService.create(r1.id!!, p1.id!!, r2.id!!)
        statementService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(
                get("/api/statements/subject/${r1.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
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
                        fieldWithPath("[].object.label").description("The label of the object")
                    )
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

        statementService.create(r1.id!!, p1.id!!, r2.id!!)
        statementService.create(r1.id!!, p1.id!!, r3.id!!)
        statementService.create(r1.id!!, p2.id!!, r3.id!!)

        mockMvc
            .perform(
                get("/api/statements/predicate/${p1.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
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
                        fieldWithPath("[].object.label").description("The label of the object")
                    )
                )
            )
    }

    @Test
    fun addWithResource() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val p = predicateService.create("less than")

        mockMvc.perform(
            post(
                "/api/statements/{subject}/{predicate}/{object}",
                r1.id,
                p.id,
                r2.id
            )
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    snippet,
                    pathParameters(
                        parameterWithName("subject").description("The resource ID describing the subject"),
                        parameterWithName("predicate").description("The predicate ID describing the predicate"),
                        parameterWithName("object").description("The resource ID describing the object")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("Location to the created statement")
                    )
                )
            )
    }
}
