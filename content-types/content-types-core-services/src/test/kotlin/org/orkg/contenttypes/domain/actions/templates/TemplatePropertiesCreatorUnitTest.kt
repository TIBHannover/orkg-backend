package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.input.testing.fixtures.createTemplateCommand

internal class TemplatePropertiesCreatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()

    private val templatePropertiesCreator = TemplatePropertiesCreator(abstractTemplatePropertyCreator)

    @Test
    fun `Given a create template command, when creating template properties, it returns success`() {
        val command = createTemplateCommand()
        val state = CreateTemplateState(
            templateId = ThingId("R123")
        )

        every {
            abstractTemplatePropertyCreator.create(command.contributorId, state.templateId!!, any(), any())
        } returns ThingId("R456")

        val result = templatePropertiesCreator(command, state)

        result.asClue {
            it.templateId shouldBe state.templateId
        }

        command.properties.forEachIndexed { index, property ->
            verify(exactly = 1) {
                abstractTemplatePropertyCreator.create(command.contributorId, state.templateId!!, index, property)
            }
        }
    }
}
