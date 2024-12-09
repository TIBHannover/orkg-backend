package org.orkg.contenttypes.domain.actions.templates

import io.kotest.matchers.shouldBe
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
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.testing.fixtures.createDummyNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyResourceTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyStringLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyUntypedTemplateProperty
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateNumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateOtherLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateStringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateUntypedTemplatePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toTemplatePropertyDefinition

internal class TemplatePropertiesValidatorUnitTest {
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator = mockk()

    private val templatePropertiesValidator =
        TemplatePropertiesValidator<List<TemplatePropertyDefinition>, List<TemplateProperty>>(
            abstractTemplatePropertyValidator = abstractTemplatePropertyValidator,
            newValueSelector = { it },
            oldValueSelector = { it }
        )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractTemplatePropertyValidator)
    }

    @Test
    fun `Given a template update command, when no previous template properties exist, it validates each template property`() {
        val command = listOf(
            dummyUpdateUntypedTemplatePropertyCommand(),
            dummyUpdateStringLiteralTemplatePropertyCommand(),
            dummyUpdateNumberLiteralTemplatePropertyCommand(),
            dummyUpdateOtherLiteralTemplatePropertyCommand(),
            dummyUpdateResourceTemplatePropertyCommand()
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
            dummyUpdateUntypedTemplatePropertyCommand(),
            dummyUpdateStringLiteralTemplatePropertyCommand(),
            dummyUpdateNumberLiteralTemplatePropertyCommand(),
            dummyUpdateOtherLiteralTemplatePropertyCommand(),
            dummyUpdateResourceTemplatePropertyCommand()
        )
        val state = listOf(
            createDummyUntypedTemplateProperty(),
            createDummyStringLiteralTemplateProperty(),
            createDummyNumberLiteralTemplateProperty(),
            createDummyOtherLiteralTemplateProperty(),
            createDummyResourceTemplateProperty()
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
            createDummyUntypedTemplateProperty(),
            createDummyStringLiteralTemplateProperty(),
            createDummyNumberLiteralTemplateProperty(),
            createDummyOtherLiteralTemplateProperty(),
            createDummyResourceTemplateProperty()
        )
        val command = state.map { it.toTemplatePropertyDefinition() }

        templatePropertiesValidator(command, state) shouldBe state
    }
}
