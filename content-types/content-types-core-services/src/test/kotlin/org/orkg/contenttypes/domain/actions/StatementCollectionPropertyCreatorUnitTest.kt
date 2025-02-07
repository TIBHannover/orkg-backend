package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

internal class StatementCollectionPropertyCreatorUnitTest : MockkBaseTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val statementCollectionPropertyCreator = StatementCollectionPropertyCreator(literalService, statementService)

    @Test
    fun `Given a list of labels and a subject id, when list of labels is empty, it does nothing`() {
        statementCollectionPropertyCreator.create(
            contributorId = ContributorId(UUID.randomUUID()),
            subjectId = ThingId("R123"),
            predicateId = Predicates.description,
            labels = emptyList()
        )
    }

    @Test
    fun `Given a list of labels and a subject id, it creates a new description literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = ThingId("L1")

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        } returns literal
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = literal
                )
            )
        } returns StatementId("S1")

        statementCollectionPropertyCreator.create(contributorId, subjectId, Predicates.description, listOf(description))

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = literal
                )
            )
        }
    }

    @Test
    fun `Given a list of thing ids and a subject id, it creates a statement from the subject resource to all objects`() {
        val ids = listOf(ThingId("R12"), ThingId("R13"))
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")

        every { statementService.add(any()) } returns StatementId("S1")

        statementCollectionPropertyCreator.create(contributorId, subjectId, Predicates.hasLink, ids)

        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasLink,
                    objectId = ids.first()
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasLink,
                    objectId = ids.last()
                )
            )
        }
    }
}
