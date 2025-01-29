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
import org.orkg.contenttypes.input.testing.fixtures.createUntypedTemplatePropertyCommand

internal class TemplatePropertyCreatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()

    private val templatePropertyCreator = TemplatePropertyCreator(abstractTemplatePropertyCreator)

    @Test
    fun `Given a create template property command, when property is created, it properly updates the state`() {
        val command = createUntypedTemplatePropertyCommand()
        val state = CreateTemplatePropertyState(
            templatePropertyId = command.templateId,
            propertyCount = 4
        )
        val templatePropertyId = ThingId("R1568")

        every {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = state.propertyCount!!,
                property = command
            )
        } returns templatePropertyId

        val result = templatePropertyCreator(command, state)

        result.asClue {
            it.templatePropertyId shouldBe templatePropertyId
            it.propertyCount shouldBe state.propertyCount!! + 1
        }

        verify(exactly = 1) {
            abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = state.propertyCount!!,
                property = command
            )
        }
    }
}
