package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.UpdateResourceUseCase
import eu.tib.orkg.prototype.statements.application.InvalidClassCollection
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.ResourceUsedInStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

class ResourceServiceUnitTests {

    private val comparisonRepository: ComparisonRepository = mockk()
    private val contributionRepository: ContributionRepository = mockk()
    private val visualizationRepository: VisualizationRepository = mockk()
    private val smartReviewRepository: SmartReviewRepository = mockk()
    private val repository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val service = ResourceService(
        comparisonRepository,
        contributionRepository,
        visualizationRepository,
        smartReviewRepository,
        repository,
        statementRepository,
        classRepository
    )

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is found, it returns success`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.of(resource)
        every { statementRepository.findTimelineByResourceId(id, pageable) } returns Page.empty()

        service.findTimelineByResourceId(id, pageable)

        verify(exactly = 1) { statementRepository.findTimelineByResourceId(any(), any()) }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findTimelineByResourceId(id, pageable)
        }

        verify(exactly = 0) { statementRepository.findTimelineByResourceId(any(), any()) }
    }

    @Test
    fun `given all contributors for a resource are being retrieved, when the resource is found, it returns all creators`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.of(resource)
        every { statementRepository.findAllContributorsByResourceId(id, pageable) } returns Page.empty()

        service.findAllContributorsByResourceId(id, pageable)

        verify(exactly = 1) { statementRepository.findAllContributorsByResourceId(any(), any()) }
    }

    @Test
    fun `given all contributors for a resource are being retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findAllContributorsByResourceId(id, pageable)
        }

        verify(exactly = 0) { statementRepository.findAllContributorsByResourceId(any(), any()) }
    }

    @Test
    fun `given a resource is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockResource = createResource()

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns true

        shouldThrow<ResourceUsedInStatement> {
            service.delete(mockResource.id)
        }

        verify(exactly = 0) { repository.deleteById(any()) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, it gets deleted`() {
        val mockResource = createResource()

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns false
        every { repository.deleteById(mockResource.id) } returns Unit

        service.delete(mockResource.id)

        verify(exactly = 1) { repository.deleteById(mockResource.id) }
    }

    @Test
    fun `given a resource is being created, when it contains a missing class, an appropriate error is thrown`() {
        val classes = setOf(ThingId("DoesNotExist"))

        every { repository.nextIdentity() } returns ThingId("R1")
        every { classRepository.existsAll(classes) } returns false

        assertThrows<InvalidClassCollection> {
            service.create(
                CreateResourceUseCase.CreateCommand(
                    label = "irrelevant",
                    classes = classes
                )
            )
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `when a resource is being created, and it contains a reserved class, an appropriate error is thrown`() {
        val classes = setOf(Classes.list)

        every { repository.nextIdentity() } returns ThingId("R1")
        every { classRepository.existsAll(classes) } returns true

        assertThrows<InvalidClassCollection> {
            service.create(
                CreateResourceUseCase.CreateCommand(
                    label = "irrelevant",
                    classes = classes
                )
            )
        }

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `when a resource is being update, and it contains a reserved class, an appropriate error is thrown`() {
        val resource = createResource()
        val classes = setOf(Classes.list)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { classRepository.existsAll(classes) } returns true

        assertThrows<InvalidClassCollection> {
            service.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = resource.id,
                    classes = classes
                )
            )
        }

        verify(exactly = 0) { repository.save(any()) }
    }
}
