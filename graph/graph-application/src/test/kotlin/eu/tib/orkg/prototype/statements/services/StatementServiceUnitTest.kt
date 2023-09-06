package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.auth.domain.UserService
import eu.tib.orkg.prototype.auth.testing.fixtures.createAdminUser
import eu.tib.orkg.prototype.auth.testing.fixtures.createUser
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.UpdateStatementUseCase
import eu.tib.orkg.prototype.statements.application.ForbiddenStatementDeletion
import eu.tib.orkg.prototype.statements.application.ForbiddenStatementSubject
import eu.tib.orkg.prototype.statements.application.ThingNotFound
import eu.tib.orkg.prototype.statements.application.UnmodifiableStatement
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.OwnershipInfo
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.confirmVerified
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort

class StatementServiceUnitTest : DescribeSpec({

    val statementRepository: StatementRepository = mockk()
    val userService: UserService = mockk()
    val literalRepository: LiteralRepository = mockk()
    val thingRepository: ThingRepository = mockk()

    val service = StatementService(
        thingRepository,
        predicateService = mockk(),
        statementRepository,
        literalRepository
    )

    afterEach {
        // Confirm all calls. This is a protection against false-positive test results.
        confirmVerified(statementRepository, userService, literalRepository)
    }

    context("creating a statement") {
        context("with a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")

                every { thingRepository.findByThingId(listId) } returns Optional.of(
                    createResource().copy(
                        id = listId,
                        label = "irrelevant",
                        classes = setOf(Classes.list)
                    )
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<ForbiddenStatementSubject> {
                        service.create(
                            ContributorId(UUID.randomUUID()),
                            subject = listId,
                            predicate = Predicates.hasListElement,
                            `object` = ThingId("R1")
                        )
                    }
                }
            }
        }
    }

    context("creating a statement (fast)") {
        context("with a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")

                every { thingRepository.findByThingId(listId) } returns Optional.of(
                    createResource().copy(
                        id = listId,
                        label = "irrelevant",
                        classes = setOf(Classes.list)
                    )
                )

                withContext(Dispatchers.IO) {
                    shouldThrow<ForbiddenStatementSubject> {
                        service.add(
                            ContributorId(UUID.randomUUID()),
                            subject = listId,
                            predicate = Predicates.hasListElement,
                            `object` = ThingId("R1")
                        )
                    }
                }
            }
        }
    }

    context("updating a statement") {
        context("that has a list as a subject and hasListElement as a predicate") {
            it("throws an error") {
                val listId = ThingId("L1")
                val id = StatementId("S1")
                val command = UpdateStatementUseCase.UpdateCommand(
                    statementId = id
                )
                val existingStatement = GeneralStatement(
                    id = id,
                    subject = createResource().copy(
                        id = listId,
                        classes = setOf(Classes.list)
                    ),
                    predicate = createPredicate(id = Predicates.hasListElement.value),
                    `object` = createResource(),
                    createdBy = ContributorId(UUID.randomUUID()),
                    createdAt = OffsetDateTime.now()
                )

                every { statementRepository.findByStatementId(id) } returns Optional.of(existingStatement)

                withContext(Dispatchers.IO) {
                    shouldThrow<UnmodifiableStatement> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 0) { statementRepository.save(any()) }
            }
        }
        context("to a list subject and hasListElement as a predicate") {
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
                    createdAt = OffsetDateTime.now()
                )
                val list = createResource().copy(
                    id = listId,
                    classes = setOf(Classes.list)
                )

                every { statementRepository.findByStatementId(id) } returns Optional.of(existingStatement)
                every { thingRepository.findByThingId(listId) } returns Optional.of(list)

                withContext(Dispatchers.IO) {
                    shouldThrow<ForbiddenStatementSubject> {
                        service.update(command)
                    }
                }

                verify(exactly = 1) { statementRepository.findByStatementId(id) }
                verify(exactly = 1) { thingRepository.findByThingId(listId) }
                verify(exactly = 0) { statementRepository.save(any()) }
            }
        }
    }

    context("deleting a single statement") {
        // Disabled because functionality has temporarily been removed
        xcontext("statement is owned by contributor") {
            val subject = createResource()
            val predicate = createPredicate()
            val `object` = createResource()
            val statementId = StatementId("S_EXISTS")
            val contributorId = randomContributorId()
            val statement = createStatement(subject, predicate, `object`)
                .copy(id = statementId, createdBy = contributorId)
            val user = createUser(contributorId.value)
            every { statementRepository.findByStatementId(statementId) } returns Optional.of(statement)

            it("deletes the statement") {
                every { userService.findById(contributorId.value) } returns Optional.of(user)
                every { statementRepository.deleteByStatementId(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementId, contributorId)
                }

                verify(exactly = 1) {
                    statementRepository.findByStatementId(statementId)
                    userService.findById(contributorId.value)
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
            val statement = createStatement(subject, predicate, `object`)
                .copy(id = statementId, createdBy = randomContributorId())
            every { statementRepository.findByStatementId(statementId) } returns Optional.of(statement)
            // Safeguard for broken test assumption
            contributorId.value shouldNotBe statement.createdBy

            it("deletes the statement if the user is a curator") {
                val user = createAdminUser(contributorId.value)
                every { userService.findById(contributorId.value) } returns Optional.of(user)
                every { statementRepository.deleteByStatementId(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementId, contributorId)
                }

                verify(exactly = 1) { statementRepository.findByStatementId(statementId) }
                verify(exactly = 1) { userService.findById(contributorId.value) }
                verify(exactly = 1) { statementRepository.deleteByStatementId(statementId) }
            }
        }
        context("with a list as a subject and hasListElement as a predicate") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(
                subject = createResource().copy(
                    classes = setOf(ThingId("List"))
                ),
                predicate = createPredicate(
                    id = Predicates.hasListElement.value
                ),
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
                verify(exactly = 0) { statementRepository.deleteByStatementId(any()) }
            }
        }
        context("with a list as a subject and does not has hasListElement as a predicate") {
            val id = StatementId("S1")
            val fakeStatement = createStatement(
                subject = createResource().copy(
                    classes = setOf(ThingId("List"))
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
    }
    context("deleting multiple statements") {
        // Disabled because functionality has temporarily been removed
        xcontext("all statements are owned by contributor") {
            val statementIds = (1..4).map { StatementId("S$it") }.toSet()
            val contributorId = randomContributorId()
            val user = createUser(contributorId.value)
            val fakeResult = statementIds.map { OwnershipInfo(it, contributorId) }.toSet()
            every { statementRepository.determineOwnership(statementIds) } returns fakeResult

            it("deletes the statements") {
                every { userService.findById(contributorId.value) } returns Optional.of(user)
                every { statementRepository.deleteByStatementIds(any()) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(statementIds, contributorId)
                }

                verify(exactly = 1) {
                    statementRepository.determineOwnership(statementIds)
                    userService.findById(contributorId.value)
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
                val user = createUser(contributorId.value)
                every { userService.findById(contributorId.value) } returns Optional.of(user)

                withContext(Dispatchers.IO) {
//                    service.delete(allStatementIds, contributorId)
                }

                verify(exactly = 1) { statementRepository.determineOwnership(allStatementIds) }
                verify(exactly = 1) { userService.findById(contributorId.value) }
            }

            it("deletes all statements if the user is a curator") {
                val user = createAdminUser(contributorId.value)
                every { userService.findById(contributorId.value) } returns Optional.of(user)
                every { statementRepository.deleteByStatementIds(allStatementIds) } just Runs

                withContext(Dispatchers.IO) {
//                    service.delete(allStatementIds, contributorId)
                }

                verify(exactly = 1) { statementRepository.determineOwnership(allStatementIds) }
                verify(exactly = 1) { userService.findById(contributorId.value) }
                verify(exactly = 1) { statementRepository.deleteByStatementIds(allStatementIds) }
            }
        }
        context("where at least one statement has a list as a subject and hasListElement as a predicate") {
            val ids = (1..4).map { StatementId("S$it") }.toSet()
            val fakeStatements = ids.map {
                createStatement(
                    subject = createResource().copy(
                        classes = setOf(ThingId("List"))
                    ),
                    predicate = createPredicate(
                        id = Predicates.hasListElement.value
                    ),
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
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}

internal fun randomContributorId() = ContributorId(UUID.randomUUID())
