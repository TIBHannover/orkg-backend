package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.DuplicateTemplatePropertyPaths
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.testing.fixtures.createNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createUntypedTemplateProperty
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.updateUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Predicates

internal class TemplatePropertiesValidatorUnitTest : MockkBaseTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val templatePropertiesValidator =
        TemplatePropertiesValidator<List<TemplatePropertyCommand>, List<TemplateProperty>>(
            abstractTemplatePropertyValidator = abstractTemplatePropertyValidator,
            newValueSelector = { it },
            oldValueSelector = { it }
        )

    @Test
    fun `Given a template update command, when no previous template properties exist, it validates each template property`() {
        val command = listOf(
            updateUntypedTemplatePropertyCommand(),
            updateStringLiteralTemplatePropertyCommand(),
            updateNumberLiteralTemplatePropertyCommand(),
            updateOtherLiteralTemplatePropertyCommand(),
            updateResourceTemplatePropertyCommand()
        )
        val state = emptyList<TemplateProperty>()

        command.forEach {
            every { abstractTemplatePropertyValidator.validate(it) } just runs
        }

        templatePropertiesValidator(command, state) shouldBe state

        command.forEach {
            verify(exactly = 1) { abstractTemplatePropertyValidator.validate(it) }
        }
    }

    @Test
    fun `Given a template update command, when new template properties are different, it validates each template property`() {
        val command = listOf(
            updateUntypedTemplatePropertyCommand(),
            updateStringLiteralTemplatePropertyCommand(),
            updateNumberLiteralTemplatePropertyCommand(),
            updateOtherLiteralTemplatePropertyCommand(),
            updateResourceTemplatePropertyCommand()
        )
        val state = listOf(
            createUntypedTemplateProperty(),
            createStringLiteralTemplateProperty(),
            createNumberLiteralTemplateProperty(),
            createOtherLiteralTemplateProperty(),
            createResourceTemplateProperty()
        )

        command.forEach {
            every { abstractTemplatePropertyValidator.validate(it) } just runs
        }

        templatePropertiesValidator(command, state) shouldBe state

        command.forEach {
            verify(exactly = 1) { abstractTemplatePropertyValidator.validate(it) }
        }
    }

    @Test
    fun `Given a template update command, when template properties are identical to existing template properties, they are not validated again`() {
        val state = listOf(
            createUntypedTemplateProperty(),
            createStringLiteralTemplateProperty(),
            createNumberLiteralTemplateProperty(),
            createOtherLiteralTemplateProperty(),
            createResourceTemplateProperty()
        )
        val command = state.map { it.toTemplatePropertyCommand() }

        templatePropertiesValidator(command, state) shouldBe state
    }

    @Test
    fun `Given a template update command, when new set of template properties contains a duplicate path, it throws an exception`() {
        val command = listOf(
            updateUntypedTemplatePropertyCommand(),
            updateStringLiteralTemplatePropertyCommand().copy(path = Predicates.field),
        )
        val state = listOf(
            createUntypedTemplateProperty(),
            createStringLiteralTemplateProperty(),
        )

        shouldThrow<DuplicateTemplatePropertyPaths> { templatePropertiesValidator(command, state) }
    }
}
