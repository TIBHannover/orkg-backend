package org.orkg.contenttypes.domain.actions.templates

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
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class TemplateTargetClassUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val templateTargetClassUpdater = TemplateTargetClassUpdater(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a template update command, when new target class has changed, it updates the target class statement`() {
        val command = dummyUpdateTemplateCommand()
        val state = UpdateTemplateState(template = createDummyTemplate())
        val statement = createStatement()

        every {
            statementService.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.shTargetClass,
                objectClasses = setOf(Classes.`class`),
                pageable = PageRequests.ALL
            )
        } returns pageOf(statement)
        every { statementService.delete(statement.id) } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass!!
            )
        } just runs

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }

        verify(exactly = 1) {
            statementService.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.shTargetClass,
                objectClasses = setOf(Classes.`class`),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementService.delete(statement.id) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass!!
            )
        }
    }

    @Test
    fun `Given a template update command, when target class did not change, id noes nothing`() {
        val command = dummyUpdateTemplateCommand().copy(targetClass = ThingId("targetClass"))
        val state = UpdateTemplateState(template = createDummyTemplate())

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }

    @Test
    fun `Given a template update command, when new target class is not set, id noes nothing`() {
        val command = dummyUpdateTemplateCommand().copy(targetClass = null)
        val state = UpdateTemplateState(template = createDummyTemplate())

        val result = templateTargetClassUpdater(command, state)

        result.asClue {
            it.template shouldBe state.template
        }
    }
}
