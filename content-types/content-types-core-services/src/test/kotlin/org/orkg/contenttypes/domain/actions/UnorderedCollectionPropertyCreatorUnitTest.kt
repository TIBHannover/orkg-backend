package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.util.UUID

internal class UnorderedCollectionPropertyCreatorUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val unorderedCollectionPropertyCreator = UnorderedCollectionPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a list of labels and a subject id, when list of labels is empty, it does nothing`() {
        unorderedCollectionPropertyCreator.create(
            contributorId = ContributorId(UUID.randomUUID()),
            subjectId = ThingId("R123"),
            predicateId = Predicates.description,
            labels = emptyList(),
            extractionMethod = ExtractionMethod.MANUAL,
        )
    }

    @Test
    fun `Given a list of labels and a subject id, it creates a new description literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val description = "some description"
        val literal = ThingId("L1")
        val extractionMethod = ExtractionMethod.MANUAL
        val createLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = description,
            extractionMethod = extractionMethod,
        )

        every { unsafeLiteralUseCases.create(createLiteralCommand) } returns literal
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = literal,
                ),
            )
        } returns StatementId("S1")

        unorderedCollectionPropertyCreator.create(contributorId, subjectId, Predicates.description, listOf(description), extractionMethod)

        verify(exactly = 1) { unsafeLiteralUseCases.create(createLiteralCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.description,
                    objectId = literal,
                ),
            )
        }
    }
}
