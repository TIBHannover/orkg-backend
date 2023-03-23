package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.statements.testing.createResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

class ResourceServiceUnitTests {

    private val comparisonRepository: ComparisonRepository = mockk()
    private val contributionRepository: ContributionRepository = mockk()
    private val visualizationRepository: VisualizationRepository = mockk()
    private val smartReviewRepository: SmartReviewRepository = mockk()
    private val repository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val templateRepository: TemplateRepository = mockk()
    private val flags: FeatureFlagService = mockk()
    private val classRepository: ClassRepository = mockk()

    private val service = ResourceService(
        comparisonRepository,
        contributionRepository,
        visualizationRepository,
        smartReviewRepository,
        repository,
        statementRepository,
        templateRepository,
        flags,
        classRepository
    )

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is found, it returns success`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findByResourceId(id) } returns Optional.of(resource)
        every { statementRepository.findTimelineByResourceId(id, pageable) } returns Page.empty()

        service.findTimelineByResourceId(id, pageable)

        verify(exactly = 1) { statementRepository.findTimelineByResourceId(any(), any()) }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findByResourceId(id) } returns Optional.empty()

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

        every { repository.findByResourceId(id) } returns Optional.of(resource)
        every { statementRepository.findAllContributorsByResourceId(id, pageable) } returns Page.empty()

        service.findAllContributorsByResourceId(id, pageable)

        verify(exactly = 1) { statementRepository.findAllContributorsByResourceId(any(), any()) }
    }

    @Test
    fun `given all contributors for a resource are being retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findByResourceId(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findAllContributorsByResourceId(id, pageable)
        }

        verify(exactly = 0) { statementRepository.findAllContributorsByResourceId(any(), any()) }
    }
}
