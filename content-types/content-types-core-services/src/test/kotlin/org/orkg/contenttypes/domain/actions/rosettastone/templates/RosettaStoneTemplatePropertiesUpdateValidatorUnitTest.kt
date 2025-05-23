package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplatePropertyNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.createResourceObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateRosettaStoneTemplateCommand

internal class RosettaStoneTemplatePropertiesUpdateValidatorUnitTest : MockkBaseTest {
    private val abstractRosettaStoneTemplatePropertiesValidator: AbstractRosettaStoneTemplatePropertiesValidator = mockk()

    private val rosettaStoneTemplatePropertiesUpdateValidator =
        RosettaStoneTemplatePropertiesUpdateValidator(abstractRosettaStoneTemplatePropertiesValidator)

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is not used in any rosetta stone statement, it returns success")
    fun whenNotUsedInStatement_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is not used in a rosetta stone statement and no new properties are set, it does nothing")
    fun whenNotUsedInStatementAndNotSet_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(properties = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties are missing an old property, it returns success")
    fun whenNotUsedInStatementAndOldPropertyIsMissing_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = listOf(rosettaStoneTemplate.properties.first().toTemplatePropertyCommand())
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties override old properties, it returns success")
    fun whenNotUsedInStatementAndOldPropertyIsOverriden_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyCommand() }.reversed()
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new property is not optional, it returns success")
    fun whenNotUsedInStatementAndNewPropertyIsNotOptional_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyCommand() } +
                createResourceObjectPositionTemplatePropertyCommand()
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement, it returns success")
    fun whenUsedInStatement_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyCommand() } +
                createResourceObjectPositionTemplatePropertyCommand().copy(minCount = 0)
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and no new properties are set, it does nothing")
    fun whenUsedInStatementAndNotSet_thenSuccess() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(properties = null)
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties are missing an old property, it throws an exception")
    fun whenUsedInStatementAndOldPropertyIsMissing_thenException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = listOf(rosettaStoneTemplate.properties.first().toTemplatePropertyCommand())
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplatePropertyNotModifiable> {
            rosettaStoneTemplatePropertiesUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties override old properties, it throws an exception")
    fun whenUsedInStatementAndOldPropertyIsOverriden_thenException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyCommand() }.reversed()
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<RosettaStoneTemplatePropertyNotModifiable> {
            rosettaStoneTemplatePropertiesUpdateValidator(command, state)
        }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new property is not optional, it throws an exception")
    fun whenUsedInStatementAndNewPropertyIsNotOptional_thenException() {
        val rosettaStoneTemplate = createRosettaStoneTemplate()
        val command = updateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyCommand() } +
                createResourceObjectPositionTemplatePropertyCommand()
        )
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        assertThrows<NewRosettaStoneTemplatePropertyMustBeOptional> {
            rosettaStoneTemplatePropertiesUpdateValidator(command, state)
        }
    }
}
