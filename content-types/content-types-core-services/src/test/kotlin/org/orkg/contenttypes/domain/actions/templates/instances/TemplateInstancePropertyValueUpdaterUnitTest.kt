package org.orkg.contenttypes.domain.actions.templates.instances

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateInstanceCommand
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

internal class TemplateInstancePropertyValueUpdaterUnitTest : MockkBaseTest {
    private val subgraphCreator: SubgraphCreator = mockk()
    private val statementService: StatementUseCases = mockk()

    private val templateInstancePropertyValueUpdater = TemplateInstancePropertyValueUpdater(subgraphCreator, statementService)

    @Test
    fun `Given a template instance update command, when updating the properties, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val state = UpdateTemplateInstanceState(
            statementsToAdd = setOf(BakedStatement("R123", "P123", "L123"))
        )
        val thingDefinitions = command.copy(literals = state.literals)

        every {
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = thingDefinitions,
                validatedIds = state.validatedIds,
                bakedStatements = state.statementsToAdd
            )
        } just runs

        val result = templateInstancePropertyValueUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) {
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = thingDefinitions,
                validatedIds = state.validatedIds,
                bakedStatements = state.statementsToAdd
            )
        }
    }

    @Test
    fun `Given a template instance update command, when deleting outdated properties, it returns success`() {
        val command = dummyUpdateTemplateInstanceCommand()
        val statementToRemove = createStatement()
        val state = UpdateTemplateInstanceState(
            statementsToRemove = setOf(BakedStatement(
                first = statementToRemove.subject.id.value,
                second = statementToRemove.predicate.id.value,
                third = statementToRemove.`object`.id.value
            ))
        )

        every { statementService.findAll(subjectId = command.subject, pageable = PageRequests.ALL) } returns pageOf(statementToRemove)
        every { statementService.delete(setOf(statementToRemove.id)) } just runs

        val result = templateInstancePropertyValueUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
            it.templateInstance shouldBe state.templateInstance
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.statementsToAdd shouldBe state.statementsToAdd
            it.statementsToRemove shouldBe state.statementsToRemove
            it.literals shouldBe state.literals
        }

        verify(exactly = 1) { statementService.findAll(subjectId = command.subject, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(setOf(statementToRemove.id)) }
    }
}
