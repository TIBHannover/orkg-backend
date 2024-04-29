package org.orkg.contenttypes.domain.actions.rosettastone.templates

import io.kotest.assertions.asClue
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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateRosettaStoneTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateFormattedLabelCreatorUnitTest {
    private val literalService: LiteralUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val rosettaStoneTemplateFormattedLabelCreator = RosettaStoneTemplateFormattedLabelCreator(literalService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literalService, statementService)
    }

    @Test
    fun `Given a rosetta stone template create command, then it creates a new formatted label statement`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val rosettaStoneTemplateId = ThingId("R123")
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = rosettaStoneTemplateId
        )
        val formattedLabelLiteralId = ThingId("R124")

        every {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel.value
                )
            )
        } returns formattedLabelLiteralId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.rosettaStoneTemplateId!!,
                predicate = Predicates.templateLabelFormat,
                `object` = formattedLabelLiteralId
            )
        } just runs

        val result = rosettaStoneTemplateFormattedLabelCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = command.formattedLabel.value
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.rosettaStoneTemplateId!!,
                predicate = Predicates.templateLabelFormat,
                `object` = formattedLabelLiteralId
            )
        }
    }
}
