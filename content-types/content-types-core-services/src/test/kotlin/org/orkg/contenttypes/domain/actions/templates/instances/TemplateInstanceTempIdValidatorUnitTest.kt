package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.input.testing.fixtures.updateTemplateInstanceCommand

internal class TemplateInstanceTempIdValidatorUnitTest : MockkBaseTest {
    private val templateInstanceTempIdValidator = TemplateInstanceTempIdValidator()

    @Test
    fun `Given a template instance update command, when validating its temp ids, it returns success`() {
        val command = updateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState()

        val result = templateInstanceTempIdValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5", "#temp6")
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }
    }

    @Test
    fun `Given a template instance update command, when it has no new thing definitions, it returns success`() {
        val command = updateTemplateInstanceCommand().copy(
            resources = emptyMap(),
            predicates = emptyMap(),
            literals = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = UpdateTemplateInstanceState()

        val result = templateInstanceTempIdValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe emptySet()
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }
    }
}
