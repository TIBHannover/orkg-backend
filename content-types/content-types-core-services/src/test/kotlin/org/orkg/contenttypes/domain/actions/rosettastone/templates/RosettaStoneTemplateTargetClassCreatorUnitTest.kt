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
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateTargetClassCreatorUnitTest {
    private val classService: ClassUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val rosettaStoneTemplateTargetClassCreator = RosettaStoneTemplateTargetClassCreator(classService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(classService, statementService)
    }

    @Test
    fun `Given a rosetta stone template create command, it crate a new target class and links it to the root resource`() {
        val command = dummyCreateRosettaStoneTemplateCommand()
        val state = CreateRosettaStoneTemplateState(
            rosettaStoneTemplateId = ThingId("R45665")
        )
        val classId = ThingId("C123")

        every { classService.create(any()) } returns classId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.rosettaStoneTemplateId!!,
                predicate = Predicates.shTargetClass,
                `object` = classId
            )
        } just runs

        val result = rosettaStoneTemplateTargetClassCreator(command, state)

        result.asClue {
            it.rosettaStoneTemplateId shouldBe state.rosettaStoneTemplateId
        }

        verify(exactly = 1) {
            classService.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "${command.label} (class)",
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.rosettaStoneTemplateId!!,
                predicate = Predicates.shTargetClass,
                `object` = classId
            )
        }
    }
}
