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
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class TemplateFormattedLabelCreatorUnitTest : MockkBaseTest {
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val templateFormattedLabelCreator = TemplateFormattedLabelCreator(unsafeLiteralUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a template create command, when formatted label is not null, it creates a new statement`() {
        val command = createTemplateCommand()
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )
        val formattedLabelLiteralId = ThingId("R124")

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel!!.value
                )
            )
        } returns formattedLabelLiteralId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateLabelFormat,
                    objectId = formattedLabelLiteralId
                )
            )
        } returns StatementId("S1")

        val result = templateFormattedLabelCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel!!.value
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateLabelFormat,
                    objectId = formattedLabelLiteralId
                )
            )
        }
    }

    @Test
    fun `Given a template create command, when formatted label is null, it does not create a statement`() {
        val command = createTemplateCommand().copy(
            formattedLabel = null
        )
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )

        val result = templateFormattedLabelCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
