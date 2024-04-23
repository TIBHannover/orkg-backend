package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand
import org.orkg.graph.domain.FormattedLabel

class RosettaStoneTemplateFormattedLabelValidatorUnitTest {
    private val rosettaStoneTemplateFormattedLabelValidator = RosettaStoneTemplateFormattedLabelValidator()

    @Test
    fun `Given a create rosetta stone template command, when validating the formatted label, it returns success`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState()

        val result = rosettaStoneTemplateFormattedLabelValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }
    }

    @Test
    fun `Given a create rosetta stone template command, when formatted label placeholder is missing, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(formattedLabel = FormattedLabel.of("placeholder missing"))
        val state = CreateRosettaStoneTemplateState()

        assertThrows<MissingFormattedLabelPlaceholder> { rosettaStoneTemplateFormattedLabelValidator(command, state) }
    }
}
