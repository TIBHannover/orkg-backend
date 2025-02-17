package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.input.testing.fixtures.createStatementCommand
import org.orkg.graph.input.testing.fixtures.updateStatementCommand
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId

internal class UnsafeStatementServiceUnitTest : MockkBaseTest {

    private val thingRepository: ThingRepository = mockk()
    private val predicateRepository: PredicateRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val literalRepository: LiteralRepository = mockk()

    private val service = UnsafeStatementService(thingRepository, predicateRepository, statementRepository, literalRepository, fixedClock)

    @Test
    fun `Given a statement create command, when inputs are valid, it creates a new statement`() {
        val command = createStatementCommand()
        val subject = createResource(command.subjectId)
        val predicate = createPredicate(command.predicateId)
        val `object` = createLiteral(command.objectId)

        every { thingRepository.findById(command.subjectId) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId) } returns Optional.of(predicate)
        every { thingRepository.findById(command.objectId) } returns Optional.of(`object`)
        every { statementRepository.save(any()) } just runs

        service.create(command) shouldBe command.id

        verify(exactly = 1) { thingRepository.findById(command.subjectId) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId) }
        verify(exactly = 1) { thingRepository.findById(command.objectId) }
        verify(exactly = 1) {
            statementRepository.save(withArg {
                it.id shouldBe command.id
                it.subject shouldBe subject
                it.predicate shouldBe predicate
                it.`object` shouldBe `object`
                it.createdBy shouldBe command.contributorId
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `Given a statement create command, when no id is provided, it get a new id from the repository and creates a new statement`() {
        val id = StatementId("S123")
        val command = createStatementCommand().copy(id = null)
        val subject = createResource(command.subjectId)
        val predicate = createPredicate(command.predicateId)
        val `object` = createLiteral(command.objectId)

        every { thingRepository.findById(command.subjectId) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId) } returns Optional.of(predicate)
        every { thingRepository.findById(command.objectId) } returns Optional.of(`object`)
        every { statementRepository.nextIdentity() } returns id
        every { statementRepository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { thingRepository.findById(command.subjectId) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId) }
        verify(exactly = 1) { thingRepository.findById(command.objectId) }
        verify(exactly = 1) { statementRepository.nextIdentity() }
        verify(exactly = 1) {
            statementRepository.save(withArg {
                it.id shouldBe id
                it.subject shouldBe subject
                it.predicate shouldBe predicate
                it.`object` shouldBe `object`
                it.createdBy shouldBe command.contributorId
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `Given a statement create command, when subject cannot be found, it throws an exception`() {
        val command = createStatementCommand()

        every { thingRepository.findById(command.subjectId) } returns Optional.empty()

        shouldThrow<StatementSubjectNotFound> { service.create(command) }

        verify(exactly = 1) { thingRepository.findById(command.subjectId) }
    }

    @Test
    fun `Given a statement create command, when predicate cannot be found, it throws an exception`() {
        val command = createStatementCommand()
        val subject = createResource(command.subjectId)

        every { thingRepository.findById(command.subjectId) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId) } returns Optional.empty()

        shouldThrow<StatementPredicateNotFound> { service.create(command) }

        verify(exactly = 1) { thingRepository.findById(command.subjectId) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId) }
    }

    @Test
    fun `Given a statement create command, when object cannot be found, it throws an exception`() {
        val command = createStatementCommand()
        val subject = createResource(command.subjectId)
        val predicate = createPredicate(command.predicateId)

        every { thingRepository.findById(command.subjectId) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId) } returns Optional.of(predicate)
        every { thingRepository.findById(command.objectId) } returns Optional.empty()

        shouldThrow<StatementObjectNotFound> { service.create(command) }

        verify(exactly = 1) { thingRepository.findById(command.subjectId) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId) }
        verify(exactly = 1) { thingRepository.findById(command.objectId) }
    }

    @Test
    fun `Given a statement update command, when updating all properties, it returns success`() {
        val command = updateStatementCommand().copy(
            subjectId = ThingId("R321"),
            predicateId = ThingId("P321"),
            objectId = ThingId("L321"),
            modifiable = false
        )
        val statement = createStatement(command.statementId)
        val subject = createResource(command.subjectId!!)
        val predicate = createPredicate(command.predicateId!!)
        val `object` = createLiteral(command.objectId!!)

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)
        every { thingRepository.findById(command.subjectId!!) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId!!) } returns Optional.of(predicate)
        every { thingRepository.findById(command.objectId!!) } returns Optional.of(`object`)
        every { statementRepository.deleteByStatementId(command.statementId) } just runs
        every { statementRepository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
        verify(exactly = 1) { thingRepository.findById(command.subjectId!!) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
        verify(exactly = 1) { thingRepository.findById(command.objectId!!) }
        verify(exactly = 1) { statementRepository.deleteByStatementId(command.statementId) }
        verify(exactly = 1) {
            statementRepository.save(withArg {
                it.id shouldBe command.statementId
                it.subject shouldBe subject
                it.predicate shouldBe predicate
                it.`object` shouldBe `object`
                it.createdBy shouldBe statement.createdBy
                it.createdAt shouldBe statement.createdAt
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `Given a statement update command, when object literal does not change, it prevents the automatic literal deletion`() {
        val command = updateStatementCommand().copy(
            subjectId = ThingId("R321"),
            predicateId = ThingId("P321")
        )
        val subject = createResource(command.subjectId!!)
        val predicate = createPredicate(command.predicateId!!)
        val `object` = createLiteral(command.objectId!!)
        val statement = createStatement(command.statementId).copy(`object` = `object`)

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)
        every { thingRepository.findById(command.subjectId!!) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId!!) } returns Optional.of(predicate)
        every { statementRepository.deleteByStatementId(command.statementId) } just runs
        every { literalRepository.save(`object`) } just runs
        every { statementRepository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
        verify(exactly = 1) { thingRepository.findById(command.subjectId!!) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
        verify(exactly = 1) { statementRepository.deleteByStatementId(command.statementId) }
        verify(exactly = 1) { literalRepository.save(`object`) }
        verify(exactly = 1) {
            statementRepository.save(withArg {
                it.id shouldBe command.statementId
                it.subject shouldBe subject
                it.predicate shouldBe predicate
                it.`object` shouldBe `object`
                it.createdBy shouldBe statement.createdBy
                it.createdAt shouldBe statement.createdAt
                it.modifiable shouldBe statement.modifiable
            })
        }
    }

    @Test
    fun `Given a statement update command, when updating no properties, it does nothing`() {
        val id = StatementId("S123")
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateStatementUseCase.UpdateCommand(id, contributorId)

        service.update(command)
    }

    @Test
    fun `Given a statement update command, when inputs are identical to existing statement, it does nothing`() {
        val statement = createStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateStatementUseCase.UpdateCommand(
            statementId = statement.id,
            contributorId = contributorId,
            subjectId = statement.subject.id,
            predicateId = statement.predicate.id,
            objectId = statement.`object`.id,
            modifiable = statement.modifiable,
        )

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)

        service.update(command)

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
    }

    @Test
    fun `Given a statement update command, when statement cannot be found, it throws an exception`() {
        val command = updateStatementCommand()

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.empty()

        shouldThrow<StatementNotFound> { service.update(command) }

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
    }

    @Test
    fun `Given a statement update command, when subject cannot be found, it throws an exception`() {
        val command = updateStatementCommand().copy(
            subjectId = ThingId("R321")
        )
        val statement = createStatement(command.statementId)

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)
        every { thingRepository.findById(command.subjectId!!) } returns Optional.empty()

        shouldThrow<StatementSubjectNotFound> { service.update(command) }

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
        verify(exactly = 1) { thingRepository.findById(command.subjectId!!) }
    }

    @Test
    fun `Given a statement update command, when predicate cannot be found, it throws an exception`() {
        val command = updateStatementCommand().copy(
            subjectId = ThingId("R321"),
            predicateId = ThingId("P321")
        )
        val statement = createStatement(command.statementId)
        val subject = createResource(command.subjectId!!)

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)
        every { thingRepository.findById(command.subjectId!!) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId!!) } returns Optional.empty()

        shouldThrow<StatementPredicateNotFound> { service.update(command) }

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
        verify(exactly = 1) { thingRepository.findById(command.subjectId!!) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
    }

    @Test
    fun `Given a statement update command, when object cannot be found, it throws an exception`() {
        val command = updateStatementCommand().copy(
            subjectId = ThingId("R321"),
            predicateId = ThingId("P321"),
            objectId = ThingId("L321")
        )
        val statement = createStatement(command.statementId)
        val subject = createResource(command.subjectId!!)
        val predicate = createPredicate(command.predicateId!!)

        every { statementRepository.findByStatementId(command.statementId) } returns Optional.of(statement)
        every { thingRepository.findById(command.subjectId!!) } returns Optional.of(subject)
        every { predicateRepository.findById(command.predicateId!!) } returns Optional.of(predicate)
        every { thingRepository.findById(command.objectId!!) } returns Optional.empty()

        shouldThrow<StatementObjectNotFound> { service.update(command) }

        verify(exactly = 1) { statementRepository.findByStatementId(command.statementId) }
        verify(exactly = 1) { thingRepository.findById(command.subjectId!!) }
        verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
        verify(exactly = 1) { thingRepository.findById(command.objectId!!) }
    }

    @Test
    fun `Given a statement, when deleting, it deletes the statement from the repository`() {
        val id = StatementId("S123")

        every { statementRepository.deleteByStatementId(id) } just runs

        service.deleteById(id)

        verify(exactly = 1) { statementRepository.deleteByStatementId(id) }
    }

    @Test
    fun `Given several statements, when deleting, it deletes the statements from the repository`() {
        val ids = setOf(StatementId("S123"), StatementId("S124"))

        every { statementRepository.deleteByStatementIds(ids) } just runs

        service.deleteAllById(ids)

        verify(exactly = 1) { statementRepository.deleteByStatementIds(ids) }
    }
}
