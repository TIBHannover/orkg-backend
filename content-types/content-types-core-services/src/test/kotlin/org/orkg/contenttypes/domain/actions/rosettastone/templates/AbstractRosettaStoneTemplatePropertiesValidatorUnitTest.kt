package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createNumberLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createOtherLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createResourceObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createStringLiteralObjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createSubjectPositionTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.createUntypedObjectPositionTemplatePropertyCommand
import org.orkg.graph.domain.Predicates

internal class AbstractRosettaStoneTemplatePropertiesValidatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val abstractRosettaStoneTemplatePropertiesValidator = AbstractRosettaStoneTemplatePropertiesValidator(abstractTemplatePropertyValidator)

    @Test
    fun `Given a list of template properties, when validating, it returns success`() {
        val properties = listOf(
            createSubjectPositionTemplatePropertyCommand(),
            createUntypedObjectPositionTemplatePropertyCommand(),
            createStringLiteralObjectPositionTemplatePropertyCommand(),
            createNumberLiteralObjectPositionTemplatePropertyCommand(),
            createOtherLiteralObjectPositionTemplatePropertyCommand(),
            createResourceObjectPositionTemplatePropertyCommand()
        )

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        abstractRosettaStoneTemplatePropertiesValidator.validate(properties)

        properties.forEach {
            verify(exactly = 1) { abstractTemplatePropertyValidator.validate(it) }
        }
    }

    @Test
    fun `Given a list of template properties, when no subject position is specified, it throws an exception`() {
        val properties = emptyList<TemplatePropertyCommand>()

        assertThrows<MissingSubjectPosition> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when object position has a missing placeholder, it throws an exception`() {
        val properties = listOf(
            createSubjectPositionTemplatePropertyCommand(),
            createUntypedObjectPositionTemplatePropertyCommand().copy(placeholder = null)
        )

        every { abstractTemplatePropertyValidator.validate(any()) } just runs

        assertThrows<MissingPropertyPlaceholder> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }

        verify { abstractTemplatePropertyValidator.validate(any()) }
    }

    @Test
    fun `Given a list of template properties, when object position has an invalid path, it throws an exception`() {
        val properties = listOf(
            createSubjectPositionTemplatePropertyCommand(),
            createSubjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidObjectPositionPath> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has an invalid path, it throws an exception`() {
        val properties = listOf(
            createStringLiteralObjectPositionTemplatePropertyCommand(),
        )

        assertThrows<InvalidSubjectPositionPath> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a minimum cardinality of less than one, it throws an exception`() {
        val properties = listOf(
            createSubjectPositionTemplatePropertyCommand().copy(minCount = 0),
            createUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidSubjectPositionCardinality> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a literal type, it throws an exception`() {
        val properties = listOf(
            createStringLiteralObjectPositionTemplatePropertyCommand().copy(path = Predicates.hasSubjectPosition),
            createUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<InvalidSubjectPositionType> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }

    @Test
    fun `Given a list of template properties, when subject position has a missing placeholder, it throws an exception`() {
        val properties = listOf(
            createSubjectPositionTemplatePropertyCommand().copy(placeholder = null),
            createUntypedObjectPositionTemplatePropertyCommand()
        )

        assertThrows<MissingPropertyPlaceholder> { abstractRosettaStoneTemplatePropertiesValidator.validate(properties) }
    }
}
