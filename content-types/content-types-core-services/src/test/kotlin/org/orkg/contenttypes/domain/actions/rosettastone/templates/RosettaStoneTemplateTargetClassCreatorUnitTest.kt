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
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class RosettaStoneTemplateTargetClassCreatorUnitTest : MockkBaseTest {
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val rosettaStoneTemplateTargetClassCreator = RosettaStoneTemplateTargetClassCreator(
        unsafeClassUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases
    )

    @Test
    fun `Given a rosetta stone template create command, it crates a new target class and links it to the root resource and creates a new example usage statement`() {
        val command = createRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = ThingId("R45665")
        )
        val classId = ThingId("C123")
        val exampleUsageId = ThingId("L123")
        val descriptionId = ThingId("L456")

        every { unsafeClassUseCases.create(any()) } returns classId
        every { unsafeLiteralUseCases.create(any()) } returns exampleUsageId andThen descriptionId
        every { unsafeStatementUseCases.create(any()) } returns StatementId("S1")

        val result = rosettaStoneTemplateTargetClassCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        verify(exactly = 1) {
            unsafeClassUseCases.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "${command.label} (class)",
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.rosettaStoneTemplateId!!,
                    predicateId = Predicates.shTargetClass,
                    objectId = classId
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.exampleUsage
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = classId,
                    predicateId = Predicates.exampleOfUsage,
                    objectId = exampleUsageId
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "${command.description}\n\nThis is a Rosetta Statement class. Every Rosetta Stone Statement class has a template associated that should be used when adding a statement of this type to the ORKG."
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = classId,
                    predicateId = Predicates.description,
                    objectId = descriptionId
                )
            )
        }
    }
}
