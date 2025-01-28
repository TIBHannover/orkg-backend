package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [BulkStatementController::class, ExceptionHandler::class, CommonJacksonModule::class, GraphJacksonModule::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [BulkStatementController::class])
internal class BulkStatementControllerUnitTest : MockMvcBaseTest("bulk-statements") {

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    fun lookupBySubjects() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(
            id = StatementId("S1"),
            subject = createResource(r1),
            predicate = createPredicate(p1),
            `object` = createResource(r2)
        )
        val s2 = createStatement(
            id = StatementId("S2"),
            subject = createResource(id = r3),
            predicate = createPredicate(id = p2),
            `object` = createResource(id = r4)
        )
        val pageable = PageRequest.of(0, 5)

        every { statementService.findAll(subjectId = r1, pageable = any()) } returns pageOf(s1, pageable = pageable)
        every { statementService.findAll(subjectId = r3, pageable = any()) } returns pageOf(s2, pageable = pageable)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/subjects")
            .param("ids", "$r1", "$r3")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDo(
                documentationHandler.document(
                    queryParameters(parameterWithName("ids").description("The list of resource ids to fetch.")),
                    responseFields(
                        fieldWithPath("[].id").description("The subject id that was used to fetch the following statements."),
                        subsectionWithPath("[].statements").description("Page of statements whose subject id matches the id from the search parameter."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAll(subjectId = r1, pageable = any())
            statementService.findAll(subjectId = r3, pageable = any())
        }
        verify(exactly = 2) {
            statementService.countIncomingStatements(any<Set<ThingId>>())
        }
        verify(exactly = 2) {
            statementService.findAllDescriptions(any())
        }
    }

    @Test
    fun lookupByObjects() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(
            id = StatementId("S1"),
            subject = createResource(r1),
            predicate = createPredicate(p1),
            `object` = createResource(r2)
        )
        val s2 = createStatement(
            id = StatementId("S2"),
            subject = createResource(r3),
            predicate = createPredicate(p2),
            `object` = createResource(r4)
        )
        val pageable = PageRequest.of(0, 5)

        every { statementService.findAll(objectId = r2, pageable = any()) } returns pageOf(s1, pageable = pageable)
        every { statementService.findAll(objectId = r4, pageable = any()) } returns pageOf(s2, pageable = pageable)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/objects")
            .param("ids", "$r2", "$r4")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDo(
                documentationHandler.document(
                    queryParameters(parameterWithName("ids").description("The list of object ids to fetch.")),
                    responseFields(
                        fieldWithPath("[].id").description("The object id that was used to fetch the following statements."),
                        subsectionWithPath("[].statements").description("Page of statements whose object id matches the id from the search parameter."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.findAll(objectId = r2, pageable = any())
            statementService.findAll(objectId = r4, pageable = any())
        }
        verify(exactly = 2) {
            statementService.countIncomingStatements(any<Set<ThingId>>())
            statementService.findAllDescriptions(any())
        }
    }

    @Test
    @TestWithMockUser
    fun editResourceStatements() {
        val r1 = ThingId("R1")
        val r2 = ThingId("R2")
        val r3 = ThingId("R3")
        val r4 = ThingId("R4")
        val p1 = ThingId("P1")
        val p2 = ThingId("P2")
        val s1 = createStatement(
            id = StatementId("S1"),
            subject = createResource(r1),
            predicate = createPredicate(p1),
            `object` = createResource(r2)
        )
        val s2 = createStatement(
            id = StatementId("S2"),
            subject = createResource(r3),
            predicate = createPredicate(p2),
            `object` = createResource(r4)
        )

        val newP = createPredicate(ThingId("P3"))
        val newO = createResource(ThingId("R5"))

        val newS1 = createStatement(
            id = s1.id,
            subject = s1.subject,
            predicate = newP,
            `object` = newO
        )
        val newS2 = createStatement(
            id = s2.id,
            subject = s2.subject,
            predicate = newP,
            `object` = newO
        )

        val payload = objectMapper.writeValueAsString(
            mapOf(
                "predicate_id" to newP.id,
                "object_id" to newO.id,
            )
        )

        every { statementService.update(match { it.statementId == s1.id || it.statementId == s2.id }) } just runs
        every { statementService.findById(s1.id) } returns Optional.of(newS1)
        every { statementService.findById(s2.id) } returns Optional.of(newS2)
        every { statementService.countIncomingStatements(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        documentedPutRequestTo("/api/statements")
            .param("ids", "${s1.id}", "${s2.id}")
            .content(payload)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectStatement("$[*].statement")
            .andDo(
                documentationHandler.document(
                    queryParameters(parameterWithName("ids").description("The list of statements to update")),
                    responseFields(
                        fieldWithPath("[].id").description("The statement id"),
                        subsectionWithPath("[].statement").description("The statement representation of the updated statement"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            statementService.update(match { it.statementId == s1.id })
            statementService.update(match { it.statementId == s2.id })
            statementService.findById(s1.id)
            statementService.findById(s2.id)
            statementService.countIncomingStatements(any<Set<ThingId>>())
            statementService.findAllDescriptions(any())
        }
    }

    @Test
    @TestWithMockUser
    fun delete() {
        val s1 = StatementId("S1")
        val s2 = StatementId("S2")

        every { statementService.delete(setOf(s1, s2)) } just runs

        // TODO: For unknown reasons, delete requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedDeleteRequestTo("/api/statements?ids=$s1,$s2")
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    queryParameters(parameterWithName("ids").description("The list of ids of statements to delete")),
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statementService.delete(setOf(s1, s2)) }
    }
}
