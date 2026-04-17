package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createTemplateInstanceCommand
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createResource

internal class TemplateInstanceThingsCommandCreateValidatorUnitTest : MockkBaseTest {
    private val thingsCommandValidator: ThingsCommandValidator = mockk()

    private val templateInstanceThingsCommandCreateValidator = TemplateInstanceThingsCommandCreateValidator(thingsCommandValidator)

    @Test
    fun `Given a template instance create command, when validating its thing commands, it returns success`() {
        val command = createTemplateInstanceCommand()
        val state = CreateTemplateInstanceState()

        val validationCache = mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "R100" to Either.right(createResource()),
        )

        every { thingsCommandValidator.validate(command, state.validationCache) } returns validationCache

        val result = templateInstanceThingsCommandCreateValidator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstanceId shouldBe state.templateInstanceId
            it.validationCache shouldBe validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { thingsCommandValidator.validate(command, state.validationCache) }
    }
}
