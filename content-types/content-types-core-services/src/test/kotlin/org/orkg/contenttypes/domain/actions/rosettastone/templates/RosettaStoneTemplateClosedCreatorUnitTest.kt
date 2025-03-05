package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneTemplateCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class RosettaStoneTemplateClosedCreatorUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val rosettaStoneTemplateClosedCreator = RosettaStoneTemplateClosedCreator(unsafeLiteralUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a rosetta stone template create command, then it creates a new closed statement`() {
        val command = createRosettaStoneTemplateCommand()
        val rosettaStoneTemplateId = ThingId("R123")
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = rosettaStoneTemplateId
        )
        val closedLiteralId = ThingId("R125")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
        } returns closedLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.rosettaStoneTemplateId!!,
                    predicateId = Predicates.shClosed,
                    objectId = closedLiteralId
                )
            )
        } returns StatementId("S1")

        val result = rosettaStoneTemplateClosedCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.rosettaStoneTemplateId!!,
                    predicateId = Predicates.shClosed,
                    objectId = closedLiteralId
                )
            )
        }
    }
}
