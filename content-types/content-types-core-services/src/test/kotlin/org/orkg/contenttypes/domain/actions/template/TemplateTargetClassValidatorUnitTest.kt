package org.orkg.contenttypes.domain.actions.template

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.testing.fixtures.dummyCreateTemplateCommand
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class TemplateTargetClassValidatorUnitTest {
    private val classRepository: ClassRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val templateTargetClassValidator = TemplateTargetClassValidator(classRepository, statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(classRepository, statementRepository)
    }

    @Test
    fun `Given a template create command, when validating the target class, it returns success`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every { classRepository.findById(command.targetClass) } returns Optional.of(createClass())
        every {
            statementRepository.findAllByObjectAndPredicate(
                objectId = command.targetClass,
                predicateId = Predicates.shTargetClass,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        val result = templateTargetClassValidator(command, state)

        result.asClue {
            it.templateId shouldBe null
        }

        verify(exactly = 1) { classRepository.findById(command.targetClass) }
        verify(exactly = 1) {
            statementRepository.findAllByObjectAndPredicate(
                objectId = command.targetClass,
                predicateId = Predicates.shTargetClass,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a template create command, when target class does not exist, it throws an exception`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()

        every { classRepository.findById(command.targetClass) } returns Optional.empty()

        assertThrows<ClassNotFound> { templateTargetClassValidator(command, state) }

        verify(exactly = 1) { classRepository.findById(command.targetClass) }
    }

    @Test
    fun `Given a template create command, when target class already has a template, it throws an exception`() {
        val command = dummyCreateTemplateCommand()
        val state = TemplateState()
        val otherTemplate = createResource()
        val targetClass = createClass(command.targetClass)
        val exception = TemplateAlreadyExistsForClass(command.targetClass, otherTemplate.id)

        every { classRepository.findById(command.targetClass) } returns Optional.of(targetClass)
        every {
            statementRepository.findAllByObjectAndPredicate(
                objectId = command.targetClass,
                predicateId = Predicates.shTargetClass,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                subject = otherTemplate,
                predicate = createPredicate(Predicates.shTargetClass),
                `object` = targetClass
            )
        )

        assertThrows<TemplateAlreadyExistsForClass> { templateTargetClassValidator(command, state) } shouldBe exception

        verify(exactly = 1) { classRepository.findById(command.targetClass) }
        verify(exactly = 1) {
            statementRepository.findAllByObjectAndPredicate(
                objectId = command.targetClass,
                predicateId = Predicates.shTargetClass,
                pageable = PageRequests.SINGLE
            )
        }
    }
}
