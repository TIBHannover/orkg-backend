package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.application.bulk.BulkStatementController
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestBody
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Bulk Statement Controller")
@Transactional
class BulkStatementControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var controller: BulkStatementController

    @Autowired
    private lateinit var service: StatementWithResourceService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    override fun createController() = controller

    @Test
    fun lookupBySubjects() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val r4 = resourceService.create("four")
        val r5 = resourceService.create("five")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        service.create(r1.id!!, p1.id!!, r2.id!!)
        service.create(r1.id!!, p2.id!!, r3.id!!)
        service.create(r4.id!!, p2.id!!, r3.id!!)
        service.create(r4.id!!, p1.id!!, r5.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/subjects/?ids=${r1.id},${r4.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("ids").description("the list of resource Ids to fetch on")
                    ),
                    bulkStatementListResponseFields()
                )
            )
    }

    @Test
    fun lookupByObjects() {
        val r1 = resourceService.create("one")
        val r2 = resourceService.create("two")
        val r3 = resourceService.create("three")
        val r4 = resourceService.create("four")
        val p1 = predicateService.create("blah")
        val p2 = predicateService.create("blub")

        service.create(r1.id!!, p1.id!!, r3.id!!)
        service.create(r1.id!!, p1.id!!, r4.id!!)
        service.create(r2.id!!, p2.id!!, r4.id!!)
        service.create(r1.id!!, p1.id!!, r3.id!!)
        service.create(r3.id!!, p2.id!!, r4.id!!)

        mockMvc
            .perform(getRequestTo("/api/statements/objects/?ids=${r3.id},${r4.id}"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("ids").description("the list of resource Ids to fetch on")
                    ),
                    bulkStatementListResponseFields()
                )
            )
    }

    @Test
    fun deleteResourceStatements() {
        val s = resourceService.create("one")
        val p1 = predicateService.create("has creator")
        val r1 = resourceService.create("Leibniz")
        val p2 = predicateService.create("head quarter")
        val r2 = resourceService.create("Hanover, Germany")
        val st1 = service.create(s.id!!, p1.id!!, r1.id!!)
        val st2 = service.create(s.id!!, p2.id!!, r2.id!!)

        mockMvc.perform(deleteRequest("/api/statements/?ids=${st1.id},${st2.id}"))
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("ids").description("the list of statement Ids to delete")
                    ),
                    requestBody()
                )
            )
    }

    @Test
    fun editResourceStatements() {
        val s = resourceService.create("ORKG")
        val p = predicateService.create("created by")
        val o = resourceService.create("Awesome Team")
        val st = service.create(s.id!!, p.id!!, o.id!!)

        val s2 = resourceService.create("Other projects")
        val o2 = resourceService.create("The A-Team")
        val st2 = service.create(s2.id!!, p.id!!, o2.id!!)

        val newP = predicateService.create("with love from")
        val newO = resourceService.create("Hannover, Germany")

        val body = mapOf(
            "predicate_id" to newP.id!!,
            "object_id" to newO.id!!
        )
        mockMvc.perform(putRequestWithBody("/api/statements/?ids=${st.id},${st2.id}", body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].statement.predicate.id").value(newP.id!!.toString()))
            .andExpect(jsonPath("$[1].statement.object.id").value(newO.id!!.toString()))
            .andDo(
                document(
                    snippet,
                    requestParameters(
                        parameterWithName("ids").description("the list of resource Ids to fetch on")
                    ),
                    requestBody(),
                    bulkStatementListEditResponseFields())
            )
    }

    private fun bulkStatementListEditResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The updated statement ID"),
            fieldWithPath("[].statement.id").description("The statement ID"),
            fieldWithPath("[].statement.created_at").description("The statement creation datetime"),
            fieldWithPath("[].statement.subject").description("A resource"),
            fieldWithPath("[].statement.subject.id").description("The ID of the subject resource"),
            fieldWithPath("[].statement.subject.label").description("The label of the subject resource"),
            fieldWithPath("[].statement.subject.created_at").description("The subject creation datetime"),
            fieldWithPath("[].statement.subject.classes").description("The classes the subject resource belongs to"),
            fieldWithPath("[].statement.subject.shared").description("The number of time this resource has been shared"),
            fieldWithPath("[].statement.predicate").description("A predicate"),
            fieldWithPath("[].statement.predicate.id").description("The ID of the predicate"),
            fieldWithPath("[].statement.predicate.label").description("The label of the predicate"),
            fieldWithPath("[].statement.predicate.created_at").description("The predicate creation datetime"),
            fieldWithPath("[].statement.object").description("An object"),
            fieldWithPath("[].statement.object.id").description("The ID of the object"),
            fieldWithPath("[].statement.object.label").description("The label of the object"),
            fieldWithPath("[].statement.object._class").description("The type of the object (resource or literal)."),
            fieldWithPath("[].statement.object.created_at").description("The object creation datetime"),
            fieldWithPath("[].statement.object.classes").description("The classes the object resource belongs to"),
            fieldWithPath("[].statement.object.shared").optional().ignored()
        )

    private fun bulkStatementListResponseFields() =
        responseFields(
            fieldWithPath("[].id").description("The subject or object ID that was used to fetch the following statements"),
            fieldWithPath("[].statements.[].id").description("The statement ID"),
            fieldWithPath("[].statements.[].created_at").description("The statement creation datetime"),
            fieldWithPath("[].statements.[].subject").description("A resource"),
            fieldWithPath("[].statements.[].subject.id").description("The ID of the subject resource"),
            fieldWithPath("[].statements.[].subject.label").description("The label of the subject resource"),
            fieldWithPath("[].statements.[].subject.created_at").description("The subject creation datetime"),
            fieldWithPath("[].statements.[].subject.classes").description("The classes the subject resource belongs to"),
            fieldWithPath("[].statements.[].subject.shared").description("The number of time this resource has been shared"),
            fieldWithPath("[].statements.[].predicate").description("A predicate"),
            fieldWithPath("[].statements.[].predicate.id").description("The ID of the predicate"),
            fieldWithPath("[].statements.[].predicate.label").description("The label of the predicate"),
            fieldWithPath("[].statements.[].predicate.created_at").description("The predicate creation datetime"),
            fieldWithPath("[].statements.[].object").description("An object"),
            fieldWithPath("[].statements.[].object.id").description("The ID of the object"),
            fieldWithPath("[].statements.[].object.label").description("The label of the object"),
            fieldWithPath("[].statements.[].object._class").description("The type of the object (resource or literal)."),
            fieldWithPath("[].statements.[].object.created_at").description("The object creation datetime"),
            fieldWithPath("[].statements.[].object.classes").description("The classes the object resource belongs to"),
            fieldWithPath("[].statements.[].object.shared").optional().ignored()
        )
}
