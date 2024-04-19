package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class StatementCollectionPropertyCreatorUnitTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val statementCollectionPropertyCreator = StatementCollectionPropertyCreator(literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literalService, statementService)
    }

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

        statementCollectionPropertyCreator.create(contributorId, subjectId, Predicates.description, listOf(description))

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

    @Test
    fun `Given a list of thing ids and a subject id, it creates a statement from the subject resource to all objects`() {
        val ids = listOf(ThingId("R12"), ThingId("R13"))
        val contributorId = ContributorId(UUID.randomUUID())
        val subjectId = ThingId("R123")

        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasLink,
                `object` = any()
            )
        } just runs

        statementCollectionPropertyCreator.create(contributorId, subjectId, Predicates.hasLink, ids)

        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasLink,
                `object` = ids.first()
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasLink,
                `object` = ids.last()
            )
        }
    }
}
