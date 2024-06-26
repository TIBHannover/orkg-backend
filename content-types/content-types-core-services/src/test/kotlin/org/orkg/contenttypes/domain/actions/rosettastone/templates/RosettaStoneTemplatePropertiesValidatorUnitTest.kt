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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateStringLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSubjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateUntypedObjectPositionTemplatePropertyCommand
import org.orkg.graph.domain.Predicates

class RosettaStoneTemplatePropertiesValidatorUnitTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val rosettaStoneTemplatePropertiesValidator = RosettaStoneTemplatePropertiesValidator(abstractTemplatePropertyValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertyValidator)
    }

    @Test
    fun `Given a rosetta stone template create command, when validating its properties, it returns success`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState()

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        rosettaStoneTemplatePropertiesValidator(command, state)

        command.properties.forEach {
            verify(exactly = 1) { abstractTemplatePropertyValidator.validate(it) }
        }
    }

    @Test
    fun `Given a rosetta stone template create command, when no subject position is specified, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(properties = emptyList())
        val state = CreateRosettaStoneTemplateState()

        assertThrows<MissingSubjectPosition> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template create command, when object position has a missing placeholder, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateSubjectPositionTemplatePropertyCommand(),
                dummyCreateUntypedObjectPositionTemplatePropertyCommand().copy(placeholder = null)
            )
        )
        val state = CreateRosettaStoneTemplateState()

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        assertThrows<MissingPropertyPlaceholder> { rosettaStoneTemplatePropertiesValidator(command, state) }

        verify { abstractTemplatePropertyValidator.validate(any()) }
    }

    @Test
    fun `Given a rosetta stone template create command, when object position has an invalid path, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateSubjectPositionTemplatePropertyCommand(),
                dummyCreateSubjectPositionTemplatePropertyCommand()
            )
        )
        val state = CreateRosettaStoneTemplateState()

        assertThrows<InvalidObjectPositionPath> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template create command, when subject position has an invalid path, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateStringLiteralObjectPositionTemplatePropertyCommand(),
            )
        )
        val state = CreateRosettaStoneTemplateState()

        assertThrows<InvalidSubjectPositionPath> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template create command, when subject position has a minimum cardinality of less than one, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateSubjectPositionTemplatePropertyCommand().copy(minCount = 0),
                dummyCreateUntypedObjectPositionTemplatePropertyCommand()
            )
        )
        val state = CreateRosettaStoneTemplateState()

        assertThrows<InvalidSubjectPositionCardinality> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template create command, when subject position has a literal type, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateStringLiteralObjectPositionTemplatePropertyCommand().copy(path = Predicates.hasSubjectPosition),
                dummyCreateUntypedObjectPositionTemplatePropertyCommand()
            )
        )
        val state = CreateRosettaStoneTemplateState()

        assertThrows<InvalidSubjectPositionType> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }

    @Test
    fun `Given a rosetta stone template create command, when subject position has a missing placeholder, it throws an exception`() {
        val command = dummyCreateRosettaStoneTemplateCommand().copy(
            properties = listOf(
                dummyCreateSubjectPositionTemplatePropertyCommand().copy(placeholder = null),
                dummyCreateUntypedObjectPositionTemplatePropertyCommand()
            )
        )
        val state = CreateRosettaStoneTemplateState()

        assertThrows<MissingPropertyPlaceholder> { rosettaStoneTemplatePropertiesValidator(command, state) }
    }
}
