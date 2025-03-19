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

        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R100" to Either.right(createResource())
        )

        every {
            thingsCommandValidator.validate(
                thingsCommand = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        } returns validatedIds

        val result = templateInstanceThingsCommandValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) {
            thingsCommandValidator.validate(
                thingsCommand = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        }
    }
}
