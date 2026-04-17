package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceState
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.input.testing.fixtures.createTemplateInstanceCommand

internal class TemplateInstancePropertyValueCreatorUnitTest : MockkBaseTest {
    private val subgraphCreator: SubgraphCreator = mockk()

    private val templateInstancePropertyValueCreator = TemplateInstancePropertyValueCreator(subgraphCreator)

    @Test
    fun `Given a template instance create command, when creating the properties, it returns success`() {
        val command = createTemplateInstanceCommand()
        val templateInstanceId = ThingId("R123")
        val state = CreateTemplateInstanceState(
            templateInstanceId = templateInstanceId,
            statementsToAdd = setOf(Pair("P123", "L123")),
        )
        val thingsCommand = command.copy(literals = state.literals)
        val statementsToAdd = setOf(BakedStatement(templateInstanceId.value, "P123", "L123"))

        every {
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = thingsCommand,
                validationCache = state.validationCache,
                bakedStatements = statementsToAdd,
            )
        } just runs

        val result = templateInstancePropertyValueCreator(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstanceId shouldBe state.templateInstanceId
            it.validationCache shouldBe state.validationCache
            it.statementsToAdd shouldBe state.statementsToAdd
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) {
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = thingsCommand,
                validationCache = state.validationCache,
                bakedStatements = statementsToAdd,
            )
        }
    }
}
