package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.OwnershipInfo
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort

internal class StatementServiceUnitTest : MockkDescribeSpec({

    val statementRepository: StatementRepository = mockk()
    val literalRepository: LiteralRepository = mockk()
    val thingRepository: ThingRepository = mockk()
    val predicateRepository: PredicateRepository = mockk()

    val service = StatementService(
        thingRepository,
        predicateRepository,
        statementRepository,
        literalRepository,
        fixedClock,
    )

    describe("creating a statement") {
        context("with a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")

                every { thingRepository.findByThingId(listId) } returns Optional.of(
                    createResource(
                        id = listId,
                        label = "irrelevant",
                        classes = setOf(Classes.list)
                    )
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.create(
                            ContributorId(UUID.randomUUID()),
                            subject = listId,
                            predicate = Predicates.hasListElement,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(listId) }
            }
        }
        context("with a rosetta stone statement as a subject") {
            it("throws an error") {
                val subjectId = ThingId("L1")

                every { thingRepository.findByThingId(subjectId) } returns Optional.of(
                    createResource(
                        id = subjectId,
                        classes = setOf(Classes.rosettaStoneStatement)
                    )
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.create(
                            ContributorId(UUID.randomUUID()),
                            subject = subjectId,
                            predicate = Predicates.description,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(subjectId) }
            }
        }
        context("with a literal as a subject") {
            it("throws an error") {
                val subjectId = ThingId("L1")

                every { thingRepository.findByThingId(subjectId) } returns Optional.of(
                    createLiteral(subjectId)
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.create(
                            ContributorId(UUID.randomUUID()),
                            subject = subjectId,
                            predicate = Predicates.description,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(subjectId) }
            }
        }
    }

    describe("creating a statement (fast)") {
        context("with a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")

                every { thingRepository.findByThingId(listId) } returns Optional.of(
                    createResource(
                        id = listId,
                        label = "irrelevant",
                        classes = setOf(Classes.list)
                    )
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.add(
                            ContributorId(UUID.randomUUID()),
                            subject = listId,
                            predicate = Predicates.hasListElement,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(listId) }
            }
        }
        context("with a rosetta stone statement as a subject") {
            it("throws an error") {
                val subjectId = ThingId("R123")

                every { thingRepository.findByThingId(subjectId) } returns Optional.of(
                    createResource(subjectId, classes = setOf(Classes.rosettaStoneStatement))
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.add(
                            ContributorId(UUID.randomUUID()),
                            subject = subjectId,
                            predicate = Predicates.description,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(subjectId) }
            }
        }
        context("with a literal as a subject") {
            it("throws an error") {
                val subjectId = ThingId("L1")

                every { thingRepository.findByThingId(subjectId) } returns Optional.of(
                    createLiteral(subjectId)
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.add(
                            ContributorId(UUID.randomUUID()),
                            subject = subjectId,
                            predicate = Predicates.description,
                            `object` = ThingId("R1")
                        )
                    }
                }

                verify(exactly = 1) { thingRepository.findByThingId(subjectId) }
            }
        }
    }

    describe("updating a statement") {
        context("with a non-literal object") {
            it("successfully updates the statement") {
                val id = StatementId("S1")
                val statement = createStatement()
                val newSubject = createResource(ThingId("newSubject"))
                val newPredicate = createPredicate(ThingId("newPredicate"))
                val newObject = createResource(ThingId("newObject"))
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    subjectId = newSubject.id,
                    predicateId = newPredicate.id,
                    objectId = newObject.id
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)
                every { thingRepository.findByThingId(command.subjectId!!) } returns Optional.of(newSubject)
                every { predicateRepository.findById(command.predicateId!!) } returns Optional.of(newPredicate)
                every { thingRepository.findByThingId(command.objectId!!) } returns Optional.of(newObject)
                every { statementRepository.deleteByStatementId(command.statementId) } just runs
                every { statementRepository.save(any()) } just runs

                withContext(Dispatchers.IO) {
                    service.update(command)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { thingRepository.findByThingId(command.subjectId!!) }
                verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
                verify(exactly = 1) { thingRepository.findByThingId(command.objectId!!) }
                verify(exactly = 1) { statementRepository.deleteByStatementId(command.statementId) }
                verify(exactly = 1) {
                    statementRepository.save(withArg {
                        it.id shouldBe id
                        it.subject shouldBe newSubject
                        it.predicate shouldBe newPredicate
                        it.`object` shouldBe newObject
                        it.createdBy shouldBe statement.createdBy
                        it.createdAt shouldBe statement.createdAt
                        it.modifiable shouldBe statement.modifiable
                        it.index shouldBe statement.index
                    })
                }
            }
        }
        context("with a literal object") {
            it("successfully updates the statement") {
                val id = StatementId("S1")
                val statement = createStatement(`object` = createLiteral())
                val newSubject = createResource(ThingId("newSubject"))
                val newPredicate = createPredicate(ThingId("newPredicate"))
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    subjectId = newSubject.id,
                    predicateId = newPredicate.id,
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)
                every { thingRepository.findByThingId(command.subjectId!!) } returns Optional.of(newSubject)
                every { predicateRepository.findById(command.predicateId!!) } returns Optional.of(newPredicate)
                every { statementRepository.deleteByStatementId(command.statementId) } just runs
                every { statementRepository.save(any()) } just runs
                every { literalRepository.save(any()) } just runs

                withContext(Dispatchers.IO) {
                    service.update(command)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { thingRepository.findByThingId(command.subjectId!!) }
                verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
                verify(exactly = 1) { statementRepository.deleteByStatementId(command.statementId) }
                verify(exactly = 1) {
                    statementRepository.save(withArg {
                        it.id shouldBe id
                        it.subject shouldBe newSubject
                        it.predicate shouldBe newPredicate
                        it.`object` shouldBe statement.`object`
                        it.createdBy shouldBe statement.createdBy
                        it.createdAt shouldBe statement.createdAt
                        it.modifiable shouldBe statement.modifiable
                        it.index shouldBe statement.index
                    })
                }
                verify(exactly = 1) { literalRepository.save(statement.`object` as Literal) }
            }
        }
        context("without any changes") {
            it("does nothing") {
                val id = StatementId("S1")
                val statement = createStatement()
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    subjectId = statement.subject.id,
                    predicateId = statement.predicate.id,
                    objectId = statement.`object`.id
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)

                withContext(Dispatchers.IO) {
                    service.update(command)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
            }
        }
        context("that does not exist") {
            it("throws an error") {
                val id = StatementId("S1")
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id
                )
                every { statementRepository.findByStatementId(id) } returns Optional.empty()

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementNotFound> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
            }
        }
        context("that is non-modifiable") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(id, modifiable = false)
            val command = UpdateStatementUseCase.UpdateCommand(id)

            it("throws an exception") {
                every { statementRepository.findByStatementId(id) } returns Optional.of(fakeStatement)

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementNotModifiable> { service.update(command) }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(any()) }
            }
        }
        context("that has a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")
                val id = StatementId("S1")
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id
                )
                val existingStatement = GeneralStatement(
                    id = id,
                    subject = createResource(
                        id = listId,
                        classes = setOf(Classes.list)
                    ),
                    predicate = createPredicate(Predicates.hasListElement),
                    `object` = createResource(),
                    createdBy = ContributorId(UUID.randomUUID()),
                    createdAt = OffsetDateTime.now(fixedClock)
                )

                every { statementRepository.findByStatementId(id) } returns Optional.of(existingStatement)

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
            }
        }
        context("with a non-existing subject entity") {
            it("throws an error") {
                val id = StatementId("S1")
                val statement = createStatement()
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    subjectId = ThingId("missing")
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)
                every { thingRepository.findByThingId(command.subjectId!!) } returns Optional.empty()

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementSubjectNotFound> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { thingRepository.findByThingId(command.subjectId!!) }
            }
        }
        context("with a list resource as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")
                val id = StatementId("S1")
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    subjectId = listId,
                    predicateId = Predicates.hasListElement
                )
                val existingStatement = GeneralStatement(
                    id = id,
                    subject = createResource(),
                    predicate = createPredicate(),
                    `object` = createResource(),
                    createdBy = ContributorId(UUID.randomUUID()),
                    createdAt = OffsetDateTime.now(fixedClock)
                )
                val list = createResource(
                    id = listId,
                    classes = setOf(Classes.list)
                )

                every { statementRepository.findByStatementId(id) } returns Optional.of(existingStatement)
                every { thingRepository.findByThingId(listId) } returns Optional.of(list)

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) {
                    statementRepository.findByStatementId(id)
                    thingRepository.findByThingId(listId)
                }
            }
        }
        context("with a rosetta stone statement as a subject") {
            it("throws an error") {
                val id = StatementId("S1")
                val fakeStatement = createStatement(id)
                val subjectId = ThingId("L1")

                every { statementRepository.findByStatementId(id) } returns Optional.of(fakeStatement)
                every { thingRepository.findByThingId(subjectId) } returns Optional.of(
                    createResource(subjectId, classes = setOf(Classes.rosettaStoneStatement))
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<InvalidStatement> {
                        service.update(
                            UpdateStatementUseCase.UpdateCommand(
                                statementId = id,
                                subjectId = subjectId,
                                predicateId = Predicates.description,
                                objectId = ThingId("R1")
                            )
                        )
                    }
                }

                verify(exactly = 1) {
                    statementRepository.findByStatementId(id)
                    thingRepository.findByThingId(subjectId)
                }
            }
        }
        context("with a non-existing predicate") {
            it("throws an error") {
                val id = StatementId("S1")
                val statement = createStatement()
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    predicateId = ThingId("missing")
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)
                every { predicateRepository.findById(command.predicateId!!) } returns Optional.empty()

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementPredicateNotFound> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { predicateRepository.findById(command.predicateId!!) }
            }
        }
        context("with a non-existing object entity") {
            it("throws an error") {
                val id = StatementId("S1")
                val statement = createStatement()
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id,
                    objectId = ThingId("missing")
                )
                every { statementRepository.findByStatementId(id) } returns Optional.of(statement)
                every { thingRepository.findByThingId(command.objectId!!) } returns Optional.empty()

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementObjectNotFound> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { thingRepository.findByThingId(command.objectId!!) }
            }
        }
    }

    describe("deleting a single statement") {
        // Disabled because functionality has temporarily been removed
        xcontext("statement is owned by contributor") {
            val subject = createResource()
            val predicate = createPredicate()
            val `object` = createResource()
            val statementId = StatementId("S_EXISTS")
            val contributorId = randomContributorId()
            val statement = createStatement(id = statementId,
                subject = subject,
                predicate = predicate,
                `object` = `object`,
                createdBy = contributorId
            )
            every { statementRepository.findByStatementId(statementId) } returns Optional.of(statement)

            it("deletes the statement") {
                every { statementRepository.deleteByStatementId(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementId, contributorId)
                }

                verify(exactly = 1) {
                    statementRepository.findByStatementId(statementId)
                    statementRepository.deleteByStatementId(statementId)
                }
            }
        }
        // Disabled because functionality has temporarily been removed
        xcontext("statement is not owned by contributor") {
            val subject = createResource()
            val predicate = createPredicate()
            val `object` = createResource()
            val statementId = StatementId("S_EXISTS")
            val contributorId = randomContributorId()
            val statement = createStatement(
                id = statementId,
                subject = subject,
                predicate = predicate,
                `object` = `object`,
                createdBy = randomContributorId()
            )
            every { statementRepository.findByStatementId(statementId) } returns Optional.of(statement)
            // Safeguard for broken test assumption
            contributorId.value shouldNotBe statement.createdBy

            it("deletes the statement if the user is a curator") {
                every { statementRepository.deleteByStatementId(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementId, contributorId)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(statementId) }
                verify(exactly = 1) { statementRepository.deleteByStatementId(statementId) }
            }
        }
        context("with a list as a subject and hasListElement as a predicate") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(
                subject = createResource(
                    classes = setOf(Classes.list)
                ),
                predicate = createPredicate(Predicates.hasListElement),
                `object` = createResource()
            )

            it("throws an error") {
                every { statementRepository.findByStatementId(id) } returns Optional.of(fakeStatement)

                withContext(Dispatchers.IO) {
                    shouldThrow<ForbiddenStatementDeletion> {
                        service.delete(id)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(any()) }
            }
        }
        context("with a list as a subject and does not has hasListElement as a predicate") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(
                subject = createResource(
                    classes = setOf(Classes.list)
                ),
                `object` = createResource()
            )

            it("deletes the statement") {
                every { statementRepository.findByStatementId(id) } returns Optional.of(fakeStatement)
                every { statementRepository.deleteByStatementId(id) } just runs

                withContext(Dispatchers.IO) {
                    service.delete(id)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(any()) }
                verify(exactly = 1) { statementRepository.deleteByStatementId(any()) }
            }
        }
        context("that is non-modifiable") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(id, modifiable = false)

            it("throws an exception") {
                every { statementRepository.findByStatementId(id) } returns Optional.of(fakeStatement)

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementNotModifiable> { service.delete(id) }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(any()) }
            }
        }
    }
    describe("deleting multiple statements") {
        // Disabled because functionality has temporarily been removed
        xcontext("all statements are owned by contributor") {
            val statementIds = (1..4).map { StatementId("S$it") }.toSet()
            val contributorId = randomContributorId()
            val fakeResult = statementIds.map { OwnershipInfo(it, contributorId) }.toSet()
            every { statementRepository.determineOwnership(statementIds) } returns fakeResult

            it("deletes the statements") {
                every { statementRepository.deleteByStatementIds(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementIds, contributorId)
                }

                verify(exactly = 1) {
                    statementRepository.determineOwnership(statementIds)
                    statementRepository.deleteByStatementIds(statementIds)
                }
            }
        }
        // Disabled because functionality has temporarily been removed
        xcontext("at least one statement is not owned by the contributor") {
            val ownedStatementIds = (1..4).map { StatementId("S$it") }.toSet()
            val contributorId = randomContributorId()
            val fakeResult = ownedStatementIds.map { OwnershipInfo(it, contributorId) }
                .plus(OwnershipInfo(StatementId("S_other"), randomContributorId()))
                .toSet()
            val allStatementIds = fakeResult.map(OwnershipInfo::statementId).toSet()
            every { statementRepository.determineOwnership(allStatementIds) } returns fakeResult

            it("deletes no statements, but does not complain") {
                withContext(Dispatchers.IO) {
//                    service.delete(allStatementIds, contributorId)
                }

                verify(exactly = 1) { statementRepository.determineOwnership(allStatementIds) }
            }

            it("deletes all statements if the user is a curator") {
                every { statementRepository.deleteByStatementIds(allStatementIds) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(allStatementIds, contributorId)
                }

                verify(exactly = 1) { statementRepository.determineOwnership(allStatementIds) }
                verify(exactly = 1) { statementRepository.deleteByStatementIds(allStatementIds) }
            }
        }
        context("where at least one statement has a list as a subject and hasListElement as a predicate") {
            val ids = (1..4).map { StatementId("S$it") }.toSet()
            val fakeStatements = ids.map {
                createStatement(
                    subject = createResource(
                        classes = setOf(Classes.list)
                    ),
                    predicate = createPredicate(Predicates.hasListElement),
                    `object` = createResource()
                )
            }

            it("throws an error") {
                every { statementRepository.findAllByStatementIdIn(ids, any()) } returns PageImpl(fakeStatements)

                withContext(Dispatchers.IO) {
                    shouldThrow<ForbiddenStatementDeletion> {
                        service.delete(ids)
                    }
                }

                verify(exactly = 1) { statementRepository.findAllByStatementIdIn(ids, any()) }
                verify(exactly = 0) { statementRepository.deleteByStatementIds(any()) }
            }
        }
        context("where at least one statement is non-modifiable") {
            val ids = (1..4).map { StatementId("S$it") }.toSet()
            val fakeStatements = ids.map { createStatement(it, modifiable = false) }

            it("throws an error") {
                every { statementRepository.findAllByStatementIdIn(ids, any()) } returns PageImpl(fakeStatements)

                withContext(Dispatchers.IO) {
                    shouldThrow<StatementNotModifiable> {
                        service.delete(ids)
                    }
                }

                verify(exactly = 1) { statementRepository.findAllByStatementIdIn(ids, any()) }
            }
        }
    }
    describe("fetching statements as a bundle") {
        context("by thing id") {
            context("when thing does not exist") {
                val id = ThingId("DoesNotExist")
                val configuration = BundleConfiguration.firstLevelConf()

                it("throws an exception") {
                    every { thingRepository.findByThingId(id) } returns Optional.empty()

                    assertThrows<ThingNotFound> {
                        service.fetchAsBundle(id, configuration, false, Sort.unsorted())
                    }

                    verify(exactly = 1) { thingRepository.findByThingId(id) }
                }
            }
        }
    }
})

internal fun randomContributorId() = ContributorId(UUID.randomUUID())
