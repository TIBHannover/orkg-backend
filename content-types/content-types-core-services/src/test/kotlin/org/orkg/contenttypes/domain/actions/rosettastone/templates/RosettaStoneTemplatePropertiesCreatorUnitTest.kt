package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand

internal class RosettaStoneTemplatePropertiesCreatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator = mockk()

    private val rosettaStoneTemplatePropertiesCreator = RosettaStoneTemplatePropertiesCreator(abstractTemplatePropertyCreator)

    @Test
    fun `Given a rosetta stone create template command, when creating template properties, it returns success`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = ThingId("R123")
        )

        every {
            abstractTemplatePropertyCreator.create(command.contributorId, state.rosettaStoneTemplateId!!, any(), any())
        } returns ThingId("R456")

        val result = rosettaStoneTemplatePropertiesCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        command.properties.forEachIndexed { index, property ->
            verify(exactly = 1) {
                abstractTemplatePropertyCreator.create(command.contributorId, state.rosettaStoneTemplateId!!, index, property)
            }
        }
    }
}
