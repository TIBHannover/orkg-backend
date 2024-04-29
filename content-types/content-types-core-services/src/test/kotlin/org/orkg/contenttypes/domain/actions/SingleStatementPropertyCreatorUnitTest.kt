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
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SingleStatementPropertyCreatorUnitTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val singleStatementPropertyCreator = SingleStatementPropertyCreator(literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literalService, statementService)
    }

    @Test
    fun `Given a literal value and a subject id, it creates a new literal and links it to the subject resource`() {
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

        singleStatementPropertyCreator.create(contributorId, subjectId, Predicates.description, description)

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
    fun `Given a literal value with a data type and a subject id, it creates a new literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "true"
        val literal = ThingId("L1")
        val literalCreateCommand = CreateCommand(
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.BOOLEAN.prefixedUri
        )

        every { literalService.create(literalCreateCommand) } returns literal
        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.isAnonymized,
                `object` = literal
            )
        } just runs

        singleStatementPropertyCreator.create(
            contributorId = contributorId,
            subjectId = subjectId,
            predicateId = Predicates.isAnonymized,
            label = description,
            datatype = Literals.XSD.BOOLEAN.prefixedUri
        )

        verify(exactly = 1) { literalService.create(literalCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.isAnonymized,
                `object` = literal
            )
        }
    }
}
