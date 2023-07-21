package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.auth.domain.Role
import eu.tib.orkg.prototype.auth.domain.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.createUser
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.OwnershipInfo
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatementServiceTest : DescribeSpec({

    val statementRepository: StatementRepository = mockk()
    val userService: UserService = mockk()
    val literalRepository: LiteralRepository = mockk()

    @Suppress("UNUSED_VARIABLE")
    val service = StatementService(
        thingRepository = mockk(),
        predicateService = mockk(),
        statementRepository,
        literalRepository
    )

    afterEach {
        // Confirm all calls. This is a protection against false-positive test results.
        confirmVerified(statementRepository, userService, literalRepository)
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
            val user = createUser(contributorId.value).toUser()
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
                val user = createUser(contributorId.value)
                    .toUser()
                    .copy(roles = setOf(Role("ROLE_ADMIN")))
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
    }
    context("deleting multiple statements") {
        // Disabled because functionality has temporarily been removed
        xcontext("all statements are owned by contributor") {
            val statementIds = (1..4).map { StatementId("S$it") }.toSet()
            val contributorId = randomContributorId()
            val user = createUser(contributorId.value).toUser()
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
                val user = createUser(contributorId.value).toUser()
                every { userService.findById(contributorId.value) } returns Optional.of(user)

                withContext(Dispatchers.IO) {
//                    service.delete(allStatementIds, contributorId)
                }

                verify(exactly = 1) { statementRepository.determineOwnership(allStatementIds) }
                verify(exactly = 1) { userService.findById(contributorId.value) }
            }

            it("deletes all statements if the user is a curator") {
                val user = createUser(contributorId.value).toUser()
                    .copy(roles = setOf(Role("ROLE_ADMIN")))
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
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}

internal fun randomContributorId() = ContributorId(UUID.randomUUID())
