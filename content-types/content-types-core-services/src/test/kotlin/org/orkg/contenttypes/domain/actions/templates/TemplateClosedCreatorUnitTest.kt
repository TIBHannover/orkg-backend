package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class TemplateClosedCreatorUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val templateClosedCreator = TemplateClosedCreator(unsafeLiteralUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a template create command, when closed is true, it creates a new statement`() {
        val command = createTemplateCommand()
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
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
                    subjectId = state.templateId!!,
                    predicateId = Predicates.shClosed,
                    objectId = closedLiteralId
                )
            )
        } returns StatementId("S1")

        val result = templateClosedCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
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
                    subjectId = state.templateId!!,
                    predicateId = Predicates.shClosed,
                    objectId = closedLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a template create command, when closed is false, it does not create a statement`() {
        val command = createTemplateCommand().copy(
            isClosed = false
        )
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )

        val result = templateClosedCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
