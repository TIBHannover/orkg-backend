package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.graph.adapter.input.rest.BulkStatementController.BulkStatementUpdateRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.statementResponseFields
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementInUse
import org.orkg.graph.domain.StatementNotFound
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectStatement
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.pagedResponseFields
import org.orkg.testing.spring.restdocs.referencesPageOf
import org.orkg.testing.spring.restdocs.repeatable
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [BulkStatementController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [BulkStatementController::class])
internal class BulkStatementControllerUnitTest : MockMvcBaseTest("bulk-statements") {
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    fun findAllBySubjectIds() {
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
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/subjects")
            .param("ids", "$r1", "$r3")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDocument {
                deprecated()
                summary("Fetching statements by subjects (bulk)")
                description(
                    """
                    A `GET` request to get statements of multiple resources in the subject position.
                    """
                )
                pagedQueryParameters(parameterWithName("ids").description("The list of resource ids to fetch.").repeatable())
                listResponseFields<BulkStatementRepresentation>(
                    fieldWithPath("id").description("The subject id that was used to fetch the following statements."),
                    fieldWithPath("statements").description("The page of statements.").referencesPageOf<StatementRepresentation>(),
                    *applyPathPrefix("statements.", pagedResponseFields(statementResponseFields(), StatementRepresentation::class, false)).toTypedArray(),
                )
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            statementService.findAll(subjectId = r1, pageable = any())
            statementService.findAll(subjectId = r3, pageable = any())
        }
        verify(exactly = 2) {
            statementService.countAllIncomingStatementsById(any<Set<ThingId>>())
        }
        verify(exactly = 2) {
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    fun findAllByObjectIds() {
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
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/statements/objects")
            .param("ids", "$r2", "$r4")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectPage("$[*].statements")
            .andDocument {
                deprecated()
                summary("Fetching statements by objects (bulk)")
                description(
                    """
                    A `GET` request to get statements of multiple resources/literals in the object position.
                    """
                )
                pagedQueryParameters(parameterWithName("ids").description("The list of object ids to fetch.").repeatable())
                listResponseFields<BulkStatementRepresentation>(
                    fieldWithPath("id").description("The object id that was used to fetch the following statements."),
                    fieldWithPath("statements").description("The page of statements.").referencesPageOf<StatementRepresentation>(),
                    *applyPathPrefix("statements.", pagedResponseFields(statementResponseFields(), StatementRepresentation::class, false)).toTypedArray(),
                )
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            statementService.findAll(objectId = r2, pageable = any())
            statementService.findAll(objectId = r4, pageable = any())
        }
        verify(exactly = 2) {
            statementService.countAllIncomingStatementsById(any<Set<ThingId>>())
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    @TestWithMockUser
    fun updateAllByIds() {
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

        val payload = BulkStatementUpdateRequest(
            predicateId = newP.id,
            objectId = newO.id,
        )

        every { statementService.update(match { it.statementId == s1.id || it.statementId == s2.id }) } just runs
        every { statementService.findById(s1.id) } returns Optional.of(newS1)
        every { statementService.findById(s2.id) } returns Optional.of(newS2)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedPutRequestTo("/api/statements")
            .param("ids", "${s1.id}", "${s2.id}")
            .content(payload)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Int>(2)))
            .andExpect(jsonPath("$[*].id").isNotEmpty)
            .andExpectStatement("$[*].statement")
            .andDocument {
                deprecated()
                summary("Updating statements (bulk)")
                description(
                    """
                    A `PUT` request to edit multiple statements, with the same update body.
                    """
                )
                queryParameters(parameterWithName("ids").description("The list of statements to update").repeatable())
                requestFields<BulkStatementUpdateRequest>(
                    fieldWithPath("subject_id").description("The updated id of the subject.").optional(),
                    fieldWithPath("predicate_id").description("The updated id of the predicate.").optional(),
                    fieldWithPath("object_id").description("The updated id of the object.").optional(),
                )
                listResponseFields<BulkPutStatementResponse>(
                    fieldWithPath("id").description("The statement id"),
                    subsectionWithPath("statement").description("The statement representation of the updated statement"),
                )
                throws(StatementNotFound::class, StatementNotModifiable::class, InvalidStatement::class)
            }

        verify(exactly = 1) {
            statementService.update(match { it.statementId == s1.id })
            statementService.update(match { it.statementId == s2.id })
            statementService.findById(s1.id)
            statementService.findById(s2.id)
            statementService.countAllIncomingStatementsById(any<Set<ThingId>>())
            statementService.findAllDescriptionsById(any())
        }
    }

    @Test
    @TestWithMockUser
    fun deleteAllByIds() {
        val s1 = StatementId("S1")
        val s2 = StatementId("S2")

        every { statementService.deleteAllById(setOf(s1, s2)) } just runs

        // TODO: For unknown reasons, delete requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedDeleteRequestTo("/api/statements?ids=$s1,$s2")
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                deprecated()
                summary("Deleting statements (bulk)")
                description(
                    """
                    A `DELETE` request to delete multiple statements simultaneously.
                    """
                )
                queryParameters(parameterWithName("ids").description("The list of ids of statements to delete"))
                throws(StatementNotModifiable::class, StatementInUse::class)
            }

        verify(exactly = 1) { statementService.deleteAllById(setOf(s1, s2)) }
    }
}
