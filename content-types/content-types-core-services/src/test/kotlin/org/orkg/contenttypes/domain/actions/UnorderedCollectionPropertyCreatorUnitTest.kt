package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

internal class UnorderedCollectionPropertyCreatorUnitTest : MockkBaseTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val unorderedCollectionPropertyCreator = UnorderedCollectionPropertyCreator(literalService, statementService)

    @Test
    fun `Given a list of labels and a subject id, when list of labels is empty, it does nothing`() {
        unorderedCollectionPropertyCreator.create(
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
                CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        } returns literal
        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.description,
                `object` = literal
            )
        } just runs

        unorderedCollectionPropertyCreator.create(contributorId, subjectId, Predicates.description, listOf(description))

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.description,
                `object` = literal
            )
        }
    }
}
