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
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

@Nested
internal class TemplateInstanceThingDefinitionValidatorUnitTest : MockkBaseTest {
    private val thingDefinitionValidator: ThingDefinitionValidator = mockk()

    private val templateInstanceThingDefinitionValidator = TemplateInstanceThingDefinitionValidator(thingDefinitionValidator)

    @Test
    fun `Given a template instance update command, when validating its thing definitions, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState()

        val validatedIds = mapOf<String, Either<String, Thing>>(
            "R100" to Either.right(createResource())
        )

        every {
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        } returns validatedIds

        val result = templateInstanceThingDefinitionValidator(command, state)

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
            thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        }
    }
}
