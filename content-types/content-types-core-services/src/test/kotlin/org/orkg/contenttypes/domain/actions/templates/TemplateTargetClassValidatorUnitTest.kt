package org.orkg.contenttypes.domain.actions.templates

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.Optional

internal class TemplateTargetClassValidatorUnitTest : MockkBaseTest {
    private val classRepository: ClassRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val templateTargetClassValidator =
        TemplateTargetClassValidator<ThingId?, ThingId?>(classRepository, statementRepository, { it }, { it })

    @Test
    fun `Given a target class id, when validating, it returns success`() {
        val targetClassId = ThingId("targetClass")

        every { classRepository.findById(targetClassId) } returns Optional.of(createClass())
        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.nodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()

        assertDoesNotThrow { templateTargetClassValidator(targetClassId, null) }

        verify(exactly = 1) { classRepository.findById(targetClassId) }
        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        }
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
    fun `Given a target class id, when null, it returns success`() {
        assertDoesNotThrow { templateTargetClassValidator(null, null) }
    }

    @Test
    fun `Given a target class id, when target class does not exist, it throws an exception`() {
        val targetClassId = ThingId("targetClass")

        every { classRepository.findById(targetClassId) } returns Optional.empty()

        assertThrows<ClassNotFound> { templateTargetClassValidator(targetClassId, null) }

        verify(exactly = 1) { classRepository.findById(targetClassId) }
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
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        } returns pageOf()
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
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        }
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
    fun `Given a target class id, when target class id equals rosetta stone statement class id, it throws an exception`() {
        val targetClassId = Classes.rosettaStoneStatement
        val exception = ReservedClass(targetClassId)
        assertThrows<ReservedClass> { templateTargetClassValidator(targetClassId, null) } shouldBe exception
    }

    @Test
    fun `Given a target class id, when target class already has a rosetta stone template, it throws an exception`() {
        val targetClassId = ThingId("targetClass")
        val otherTemplate = createResource()
        val targetClass = createClass(targetClassId)
        val exception = ReservedClass(targetClassId)

        every { classRepository.findById(targetClassId) } returns Optional.of(targetClass)
        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.rosettaNodeShape),
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

        assertThrows<ReservedClass> { templateTargetClassValidator(targetClassId, null) } shouldBe exception

        verify(exactly = 1) { classRepository.findById(targetClassId) }
        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.rosettaNodeShape),
                predicateId = Predicates.shTargetClass,
                objectId = targetClassId,
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    fun `Given a target class id, when it is identical to old target class, it returns success`() {
        val targetClassId = ThingId("targetClass")
        assertDoesNotThrow { templateTargetClassValidator(targetClassId, targetClassId) }
    }
}
