package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.util.UUID

internal class SingleStatementPropertyCreatorUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val singleStatementPropertyCreator = SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a literal value and a subject id, it creates a new literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = ThingId("L1")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        } returns literal
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = literal
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyCreator.create(contributorId, subjectId, Predicates.description, description)

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
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
    fun `Given a literal value with a data type and a subject id, it creates a new literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "true"
        val literal = ThingId("L1")
        val literalCreateCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = description,
            datatype = Literals.XSD.BOOLEAN.prefixedUri
        )

        every { unsafeLiteralUseCases.create(literalCreateCommand) } returns literal
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.isAnonymized,
                    objectId = literal
                )
            )
        } returns StatementId("S1")

        singleStatementPropertyCreator.create(
            contributorId = contributorId,
            subjectId = subjectId,
            predicateId = Predicates.isAnonymized,
            label = description,
            datatype = Literals.XSD.BOOLEAN.prefixedUri
        )

        verify(exactly = 1) { unsafeLiteralUseCases.create(literalCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.isAnonymized,
                    objectId = literal
                )
            )
        }
    }
}
