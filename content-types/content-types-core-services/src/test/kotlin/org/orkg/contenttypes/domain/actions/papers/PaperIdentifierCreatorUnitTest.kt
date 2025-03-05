package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class PaperIdentifierCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val paperIdentifierCreator = PaperIdentifierCreator(unsafeStatementUseCases, unsafeLiteralUseCases)

    @Test
    fun `Given a paper create command, it crates new paper identifiers`() {
        val command = createPaperCommand()
        val paperId = ThingId("R123")
        val state = CreatePaperState(paperId = paperId)

        val doi = command.identifiers["doi"]!!.first()
        val doiLiteralId = ThingId("L1")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        } returns doiLiteralId
        val statementCommand = CreateStatementUseCase.CreateCommand(
            contributorId = command.contributorId,
            subjectId = paperId,
            predicateId = Predicates.hasDOI,
            objectId = doiLiteralId
        )
        every { unsafeStatementUseCases.create(statementCommand) } returns StatementId("S435")

        val result = paperIdentifierCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = doi
                )
            )
        }
        verify(exactly = 1) { unsafeStatementUseCases.create(statementCommand) }
    }

    @Test
    fun `Given a paper create command, when an unknown identifier is specified, it does not create the identifier`() {
        val command = createPaperCommand().copy(
            identifiers = mapOf("unknown" to listOf("value"))
        )
        val paperId = ThingId("R123")
        val state = CreatePaperState(paperId = paperId)

        val result = paperIdentifierCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 0) { unsafeLiteralUseCases.create(any()) }
        verify(exactly = 0) { unsafeStatementUseCases.create(any()) }
    }
}
