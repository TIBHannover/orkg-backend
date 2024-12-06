package org.orkg.contenttypes.domain

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.testing.MockUserId
import org.orkg.testing.fixedClock

class RosettaStoneStatementServiceUnitTest {
    private val repository: RosettaStoneStatementRepository = mockk()
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val classService: ClassUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val listService: ListUseCases = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val classHierarchyRepository: ClassHierarchyRepository = mockk()

    private val service = RosettaStoneStatementService(
        repository,
        rosettaStoneTemplateService,
        resourceRepository,
        observatoryRepository,
        organizationRepository,
        thingRepository,
        classRepository,
        classService,
        resourceService,
        statementService,
        literalService,
        predicateService,
        statementRepository,
        listService,
        contributorRepository,
        classHierarchyRepository,
        fixedClock
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            repository,
            rosettaStoneTemplateService,
            resourceRepository,
            observatoryRepository,
            organizationRepository,
            thingRepository,
            classRepository,
            classService,
            resourceService,
            statementService,
            literalService,
            predicateService,
            statementRepository,
            contributorRepository,
            listService,
            classHierarchyRepository
        )
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting, it soft deletes the rosetta stone statement`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { repository.softDelete(statement.id, contributorId) } just runs

        service.softDelete(statement.id, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { repository.softDelete(statement.id, contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but statement does not exist, it does nothing`() {
        val statementId = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statementId) } returns Optional.empty()

        service.softDelete(statementId, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statementId) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but statement is not modifiable, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement().copy(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        assertThrows<RosettaStoneStatementNotModifiable> { service.softDelete(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but provided id is not the latest version, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val id = statement.versions.first().id

        every { repository.findByIdOrVersionId(id) } returns Optional.of(statement)

        assertThrows<CannotDeleteIndividualRosettaStoneStatementVersion> { service.softDelete(id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(id) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but existing statement is already deleted, it does nothing`() {
        val statement = createDummyRosettaStoneStatement().copy(visibility = Visibility.DELETED)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        service.softDelete(statement.id, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting, it deletes the rosetta stone statement`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = true)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { repository.isUsedAsObject(statement.id) } returns false
        every { repository.delete(statement.id) } just runs

        service.delete(statement.id, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
        verify(exactly = 1) { repository.isUsedAsObject(statement.id) }
        verify(exactly = 1) { repository.delete(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but statement does not exist, it does nothing`() {
        val statementId = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statementId) } returns Optional.empty()

        service.delete(statementId, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statementId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but statement is not modifiable, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement().copy(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        assertThrows<RosettaStoneStatementNotModifiable> { service.delete(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but provided id is not the latest version, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.versions.first().id) } returns Optional.of(statement)

        assertThrows<CannotDeleteIndividualRosettaStoneStatementVersion> {
            service.delete(statement.versions.first().id, contributorId)
        }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.versions.first().id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but contributor cannot be found, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.empty()

        assertThrows<ContributorNotFound> { service.delete(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but contributor is not a curator, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = false)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)

        assertThrows<NotACurator> { service.delete(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but statement version is used an object, it throws an exception`() {
        val statement = createDummyRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = true)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { repository.isUsedAsObject(statement.id) } returns true

        assertThrows<RosettaStoneStatementInUse> { service.delete(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
        verify(exactly = 1) { repository.isUsedAsObject(statement.id) }
    }
}
