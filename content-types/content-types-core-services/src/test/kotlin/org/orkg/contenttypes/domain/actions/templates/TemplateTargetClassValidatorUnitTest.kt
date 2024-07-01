package org.orkg.contenttypes.domain.actions.templates

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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
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

    private val templateTargetClassValidator =
        TemplateTargetClassValidator<ThingId?, ThingId?>(classRepository, statementRepository, { it }, { it })

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(classRepository, statementRepository)
    }

    @Test
    fun `Given a target class id, when validating, it returns success`() {
        val targetClass = ThingId("targetClass")

        every { classRepository.findById(targetClass) } returns Optional.of(createClass())
        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClass,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertDoesNotThrow { templateTargetClassValidator(targetClass, null) }

        verify(exactly = 1) { classRepository.findById(targetClass) }
        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClass,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a target class id, when null, it returns success`() {
        assertDoesNotThrow { templateTargetClassValidator(null, null) }
    }

    @Test
    fun `Given a target class id, when target class does not exist, it throws an exception`() {
        val targetClass = ThingId("targetClass")

        every { classRepository.findById(targetClass) } returns Optional.empty()

        assertThrows<ClassNotFound> { templateTargetClassValidator(targetClass, null) }

        verify(exactly = 1) { classRepository.findById(targetClass) }
    }

    @Test
    fun `Given a target class id, when target class already has a template, it throws an exception`() {
        val targetClassId = ThingId("targetClass")
        val otherTemplate = createResource()
        val targetClass = createClass(targetClassId)
        val exception = TemplateAlreadyExistsForClass(targetClassId, otherTemplate.id)

        every { classRepository.findById(targetClassId) } returns Optional.of(targetClass)
        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                subject = otherTemplate,
                predicate = createPredicate(Predicates.shTargetClass),
                `object` = targetClass
            )
        )

        assertThrows<TemplateAlreadyExistsForClass> { templateTargetClassValidator(targetClassId, null) } shouldBe exception

        verify(exactly = 1) { classRepository.findById(targetClassId) }
        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a target class id, when it is identical to old target class, it returns success`() {
        val targetClass = ThingId("targetClass")
        assertDoesNotThrow { templateTargetClassValidator(targetClass, targetClass) }
    }
}
