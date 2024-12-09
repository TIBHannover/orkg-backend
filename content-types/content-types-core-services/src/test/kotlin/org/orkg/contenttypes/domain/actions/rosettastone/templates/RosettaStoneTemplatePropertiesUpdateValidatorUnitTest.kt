package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplatePropertyNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyDefinition

internal class RosettaStoneTemplatePropertiesUpdateValidatorUnitTest {
    private val abstractRosettaStoneTemplatePropertiesValidator: AbstractRosettaStoneTemplatePropertiesValidator = mockk()

    private val rosettaStoneTemplatePropertiesUpdateValidator =
        RosettaStoneTemplatePropertiesUpdateValidator(abstractRosettaStoneTemplatePropertiesValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractRosettaStoneTemplatePropertiesValidator)
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is not used in any rosetta stone statement, it returns success")
    fun whenNotUsedInStatement_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand()
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is not used in a rosetta stone statement and no new properties are set, it does nothing")
    fun whenNotUsedInStatementAndNotSet_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(properties = null)
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties are missing an old property, it returns success")
    fun whenNotUsedInStatementAndOldPropertyIsMissing_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = listOf(rosettaStoneTemplate.properties.first().toTemplatePropertyDefinition())
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties override old properties, it returns success")
    fun whenNotUsedInStatementAndOldPropertyIsOverriden_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyDefinition() }.reversed()
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new property is not optional, it returns success")
    fun whenNotUsedInStatementAndNewPropertyIsNotOptional_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyDefinition() } +
                dummyCreateResourceObjectPositionTemplatePropertyCommand()
        )
        val state = UpdateRosettaStoneTemplateState(rosettaStoneTemplate = rosettaStoneTemplate)

        every { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) } just runs

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)

        verify(exactly = 1) { abstractRosettaStoneTemplatePropertiesValidator.validate(command.properties!!) }
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement, it returns success")
    fun whenUsedInStatement_thenSuccess() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyDefinition() } +
                dummyCreateResourceObjectPositionTemplatePropertyCommand().copy(minCount = 0)
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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(properties = null)
        val state = UpdateRosettaStoneTemplateState(
            rosettaStoneTemplate = rosettaStoneTemplate,
            isUsedInRosettaStoneStatement = true
        )

        rosettaStoneTemplatePropertiesUpdateValidator(command, state)
    }

    @Test
    @DisplayName("Given a rosetta stone template update command, when template is used in a rosetta stone statement and new properties are missing an old property, it throws an exception")
    fun whenUsedInStatementAndOldPropertyIsMissing_thenException() {
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = listOf(rosettaStoneTemplate.properties.first().toTemplatePropertyDefinition())
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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyDefinition() }.reversed()
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
        val rosettaStoneTemplate = createDummyRosettaStoneTemplate()
        val command = dummyUpdateRosettaStoneTemplateCommand().copy(
            properties = rosettaStoneTemplate.properties.map { it.toTemplatePropertyDefinition() } +
                dummyCreateResourceObjectPositionTemplatePropertyCommand()
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
