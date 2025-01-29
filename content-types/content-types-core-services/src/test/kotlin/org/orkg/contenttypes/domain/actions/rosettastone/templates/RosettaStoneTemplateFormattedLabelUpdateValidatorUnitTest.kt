package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.TooManyNewRosettaStoneTemplateLabelSections
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.createResourceObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createStringLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createSubjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createUntypedObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneTemplateCommand
import org.orkg.graph.domain.FormattedLabel

internal class RosettaStoneTemplateFormattedLabelUpdateValidatorUnitTest {
    private val rosettaStoneTemplateFormattedLabelUpdateValidator = RosettaStoneTemplateFormattedLabelUpdateValidator()

    @Test
    fun `Given a rosetta stone template update command, when validating the formatted label, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when formatted label did not change, it does nothing`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = rosettaStoneTemplate.formattedLabel
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when formatted label is not set, it does nothing`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(formattedLabel = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        val result = rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe false
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when formatted label placeholder is missing, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("placeholder missing")
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        assertThrows<MissingFormattedLabelPlaceholder> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when formatted label placeholder is missing and template property does not have a placeholder, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("placeholder missing"),
            properties = listOf(createResourceObjectPositionTemplatePropertyCommand().copy(placeholder = null))
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        assertThrows<MissingFormattedLabelPlaceholder> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when validating the formatted label and template is used in a rosetta stone statement, it returns success`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(
            formattedLabel = FormattedLabel.of("[{0}] travels [to {1}]")
        )
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("[{0}] travels [to {1}][by {2}]"),
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createStringLiteralObjectPositionTemplatePropertyCommand(),
                createResourceObjectPositionTemplatePropertyCommand()
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        val result = rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)

        result.asClue {
            it.rosettaStoneTemplate shouldBe rosettaStoneTemplate
            it.statements shouldBe emptyMap()
            it.isUsedInRosettaStoneStatement shouldBe true
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when template is used in a rosetta stone statement and properties did not change, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(properties = null)
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when template is used in a rosetta stone statement and property count did not change, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createUntypedObjectPositionTemplatePropertyCommand(),
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new formatted label has less sections than before, it throws an exception")
    fun lessSectionThanBefore_throwsError() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}] by car")
        )
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("[{0}][to {1}][on {2}]"),
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createStringLiteralObjectPositionTemplatePropertyCommand(),
                createResourceObjectPositionTemplatePropertyCommand()
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateLabelMustStartWithPreviousVersion> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["[{2}] Entity [{0}] travels [to {1}] by car", "[{0}] travels [to {1}] by car [{2}]"])
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new formatted label does not start with previous formatted label sections, it throws an exception")
    fun doesNotStartWithPreviousFormattedLabel_throwsError(newFormattedLabelPattern: String) {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}] by car")
        )
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of(newFormattedLabelPattern),
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createStringLiteralObjectPositionTemplatePropertyCommand(),
                createResourceObjectPositionTemplatePropertyCommand()
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateLabelMustStartWithPreviousVersion> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new formatted label defines more than one new section per new template property, it throws an exception")
    fun moreThanOneNewSectionPerTemplateProperty_throwsException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}]")
        )
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}][on {2}] by car"),
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createStringLiteralObjectPositionTemplatePropertyCommand(),
                createResourceObjectPositionTemplatePropertyCommand()
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<TooManyNewRosettaStoneTemplateLabelSections> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new formatted label defines a new section that is not optional, it throws an exception")
    fun newSectionIsNotOptional_throwsException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}]")
        )
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = FormattedLabel.of("Entity [{0}] travels [to {1}]{2}"),
            properties = listOf(
                createSubjectPositionTemplatePropertyCommand(),
                createStringLiteralObjectPositionTemplatePropertyCommand(),
                createResourceObjectPositionTemplatePropertyCommand()
            )
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<NewRosettaStoneTemplateLabelSectionsMustBeOptional> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }

    @Test
    fun `Given a rosetta stone template update command, when formatted label is not set and rosetta stone template properties have changed, it throws an exception`() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            formattedLabel = null,
            properties = listOf(createResourceObjectPositionTemplatePropertyCommand())
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplateLabelMustBeUpdated> {
            rosettaStoneTemplateFormattedLabelUpdateValidator(command, state)
        }
    }
}
