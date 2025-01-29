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
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand
import org.orkg.graph.domain.Predicates

internal class TemplateDescriptionCreatorUnitTest : MockkBaseTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()

    private val templateDescriptionCreator = TemplateDescriptionCreator(singleStatementPropertyCreator)

    @Test
    fun `Given a template create command, when description is not null, it creates a new statement`() {
        val command = createTemplateCommand()
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )

        every {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.templateId!!,
                predicateId = Predicates.description,
                label = command.description!!
            )
        } just runs

        val result = templateDescriptionCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.templateId!!,
                predicateId = Predicates.description,
                label = command.description!!
            )
        }
    }

    @Test
    fun `Given a template create command, when description is null, it does not create a statement`() {
        val command = createTemplateCommand().copy(
            description = null
        )
        val templateId = ThingId("R123")
        val state = CreateTemplateState(
            templateId = templateId
        )

        val result = templateDescriptionCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }
    }
}
