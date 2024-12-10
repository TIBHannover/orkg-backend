package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand

internal class RosettaStoneTemplateExampleUsageUpdateValidatorUnitTest {
    private val rosettaStoneTemplateExampleUsageUpdateValidator = RosettaStoneTemplateExampleUsageUpdateValidator()

    @Test
    fun `Given a rosetta stone template update command, when validating the example usage, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateExampleUsageUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when example usage did not change, it does nothing`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            exampleUsage = rosettaStoneTemplate.exampleUsage
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateExampleUsageUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when old example usage is not set, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(exampleUsage = null)
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateExampleUsageUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when example usage is not set, it does nothing`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(exampleUsage = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateExampleUsageUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when rosetta stone template is used in a rosetta stone statement and new example usage does not start with old example usage, it throws an exception")
    fun doesNotStartWithPreviousExampleUsage_throwsException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage> {
            rosettaStoneTemplateExampleUsageUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when rosetta stone template is used in a rosetta stone statement and new example usage starts with old example usage, it returns success")
    fun startsWithPreviousExampleUsage_success() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            exampleUsage = rosettaStoneTemplate.exampleUsage + " appendix."
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        val result = rosettaStoneTemplateExampleUsageUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe true
        }
    }
}
