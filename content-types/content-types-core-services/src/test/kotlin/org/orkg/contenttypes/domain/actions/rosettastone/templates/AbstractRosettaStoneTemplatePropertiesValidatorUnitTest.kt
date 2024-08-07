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
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateNumberLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateOtherLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateResourceObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateStringLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSubjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateUntypedObjectPositionTemplatePropertyCommand
import org.orkg.graph.domain.Predicates

class AbstractRosettaStoneTemplatePropertiesValidatorUnitTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val abstractRosettaStoneTemplatePropertiesValidator = AbstractRosettaStoneTemplatePropertiesValidator(abstractTemplatePropertyValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertyValidator)
    }

    @Test
    fun `Given a list of template properties, when validating, it returns success`() {
        val properties = listOf(
            dummyCreateSubjectPositionTemplatePropertyCommand(),
            dummyCreateUntypedObjectPositionTemplatePropertyCommand(),
            dummyCreateStringLiteralObjectPositionTemplatePropertyCommand(),
            dummyCreateNumberLiteralObjectPositionTemplatePropertyCommand(),
            dummyCreateOtherLiteralObjectPositionTemplatePropertyCommand(),
            dummyCreateResourceObjectPositionTemplatePropertyCommand()
        )

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        abstractRosettaStoneTemplatePropertiesValidator.validate(properties)

        properties.forEach {
            verify(exactly = 1) { abstractTemplatePropertyValidator.validate(it) }
        }
    }

    @Test
    fun `Given a list of template properties, when no subject position is specified, it throws an exception`() {
        val properties = emptyList<TemplatePropertyDefinition>()

        assertThrows<MissingSubjectPosition> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when object position has a missing placeholder, it throws an exception`() {
        val properties = listOf(
            dummyCreateSubjectPositionTemplatePropertyCommand(),
            dummyCreateUntypedObjectPositionTemplatePropertyCommand().copy(placeholder = null)
        )

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        assertThrows<MissingPropertyPlaceholder> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }

        verify { abstractTemplatePropertyValidator.validate(any()) }
    }

    @Test
    fun `Given a list of template properties, when object position has an invalid path, it throws an exception`() {
        val properties = listOf(
            dummyCreateSubjectPositionTemplatePropertyCommand(),
            dummyCreateSubjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidObjectPositionPath> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has an invalid path, it throws an exception`() {
        val properties = listOf(
            dummyCreateStringLiteralObjectPositionTemplatePropertyCommand(),
        )

        assertThrows<InvalidSubjectPositionPath> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a minimum cardinality of less than one, it throws an exception`() {
        val properties = listOf(
            dummyCreateSubjectPositionTemplatePropertyCommand().copy(minCount = 0),
            dummyCreateUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidSubjectPositionCardinality> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a literal type, it throws an exception`() {
        val properties = listOf(
            dummyCreateStringLiteralObjectPositionTemplatePropertyCommand().copy(path = Predicates.hasSubjectPosition),
            dummyCreateUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidSubjectPositionType> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a missing placeholder, it throws an exception`() {
        val properties = listOf(
            dummyCreateSubjectPositionTemplatePropertyCommand().copy(placeholder = null),
            dummyCreateUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<MissingPropertyPlaceholder> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }
}
