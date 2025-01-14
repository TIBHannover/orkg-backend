package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

internal class TemplateFormattedLabelCreatorUnitTest : MockkBaseTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templateFormattedLabelCreator = TemplateFormattedLabelCreator(literalService, statementService)

    @Test
    fun `Given a template create command, when formatted label is not null, it creates a new statement`() {
        val command = dummyCreateTemplateCommand()
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )
        val formattedLabelLiteralId = ThingId("R124")

        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel!!.value
                )
            )
        } returns formattedLabelLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateLabelFormat,
                `object` = formattedLabelLiteralId
            )
        } just runs

        val result = templateFormattedLabelCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel!!.value
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateLabelFormat,
                `object` = formattedLabelLiteralId
            )
        }
    }

    @Test
    fun `Given a template create command, when formatted label is null, it does not create a statement`() {
        val command = dummyCreateTemplateCommand().copy(
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
