package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

@Nested
internal class TemplateInstanceThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val templateInstanceThingsCommandValidator = TemplateInstanceThingsCommandValidator(thingsCommandValidator)

    @Test
    fun `Given a template instance update command, when validating its thing commands, it returns success`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState()

        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R100" to Either.right(createResource())
        )

        every { thingsCommandValidator.validate(command, state.validationCache) } returns validationCache

        val result = templateInstanceThingsCommandValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.validationCache shouldBe validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { thingsCommandValidator.validate(command, state.validationCache) }
    }
}
