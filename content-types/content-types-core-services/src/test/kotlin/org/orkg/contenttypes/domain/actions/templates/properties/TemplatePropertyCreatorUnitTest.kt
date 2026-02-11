package org.orkg.contenttypes.domain.actions.templates.properties

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyState
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.testing.fixtures.createUntypedTemplatePropertyCommand

internal class TemplatePropertyCreatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()

    private val templatePropertyCreator = TemplatePropertyCreator(abstractTemplatePropertyCreator)

    @Test
    fun `Given a create template property command, when property is created, it properly updates the state`() {
        val template = createTemplate()
        val command = createUntypedTemplatePropertyCommand()
        val state = CreateTemplatePropertyState(
            template = template,
            templatePropertyId = command.templateId,
        )
        val templatePropertyId = ThingId("R1568")

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = template.properties.size,
                property = command
            )
        } returns templatePropertyId

        val result = templatePropertyCreator(command, state)

        result.asClue {
            it.template shouldBe template
            it.templatePropertyId shouldBe templatePropertyId
        }

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = template.properties.size,
                property = command
            )
        }
    }
}
