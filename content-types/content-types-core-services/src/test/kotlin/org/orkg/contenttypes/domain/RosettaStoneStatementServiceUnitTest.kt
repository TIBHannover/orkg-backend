package org.orkg.contenttypes.domain

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.testing.MockUserId
import java.util.Optional

internal class RosettaStoneStatementServiceUnitTest : MockkBaseTest {
    private val repository: RosettaStoneStatementRepository = mockk()
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()
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
        unsafeClassUseCases,
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
        unsafePredicateUseCases,
        statementRepository,
        listService,
        contributorRepository,
        classHierarchyRepository,
        fixedClock
    )

    @Test
    fun `Given a rosetta stone statement, when soft deleting, it soft deletes the rosetta stone statement`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { repository.softDelete(statement.id, contributorId) } just runs

        service.softDeleteById(statement.id, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { repository.softDelete(statement.id, contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but statement does not exist, it does nothing`() {
        val statementId = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statementId) } returns Optional.empty()

        service.softDeleteById(statementId, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statementId) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but statement is not modifiable, it throws an exception`() {
        val statement = createRosettaStoneStatement().copy(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        assertThrows<RosettaStoneStatementNotModifiable> { service.softDeleteById(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but provided id is not the latest version, it throws an exception`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val id = statement.versions.first().id

        every { repository.findByIdOrVersionId(id) } returns Optional.of(statement)

        assertThrows<CannotDeleteIndividualRosettaStoneStatementVersion> { service.softDeleteById(id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(id) }
    }

    @Test
    fun `Given a rosetta stone statement, when soft deleting but existing statement is already deleted, it does nothing`() {
        val statement = createRosettaStoneStatement().copy(visibility = Visibility.DELETED)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        service.softDeleteById(statement.id, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting, it deletes the rosetta stone statement`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = true)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { repository.isUsedAsObject(statement.id) } returns false
        every { repository.delete(statement.id) } just runs

        service.deleteById(statement.id, contributorId)

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

        service.deleteById(statementId, contributorId)

        verify(exactly = 1) { repository.findByIdOrVersionId(statementId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but statement is not modifiable, it throws an exception`() {
        val statement = createRosettaStoneStatement().copy(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)

        assertThrows<RosettaStoneStatementNotModifiable> { service.deleteById(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but provided id is not the latest version, it throws an exception`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.versions.first().id) } returns Optional.of(statement)

        assertThrows<CannotDeleteIndividualRosettaStoneStatementVersion> {
            service.deleteById(statement.versions.first().id, contributorId)
        }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.versions.first().id) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but contributor cannot be found, it throws an exception`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.empty()

        assertThrows<ContributorNotFound> { service.deleteById(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but contributor is not a curator, it throws an exception`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = false)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)

        assertThrows<NotACurator> { service.deleteById(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a rosetta stone statement, when deleting but statement version is used an object, it throws an exception`() {
        val statement = createRosettaStoneStatement()
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = true)

        every { repository.findByIdOrVersionId(statement.id) } returns Optional.of(statement)
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { repository.isUsedAsObject(statement.id) } returns true

        assertThrows<RosettaStoneStatementInUse> { service.deleteById(statement.id, contributorId) }

        verify(exactly = 1) { repository.findByIdOrVersionId(statement.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
        verify(exactly = 1) { repository.isUsedAsObject(statement.id) }
    }
}
