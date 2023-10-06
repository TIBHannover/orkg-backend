package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.spi.ContributorRepository
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StatisticsServiceUnitTests {

    private val statsRepository: StatsRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val observatoryRepository: PostgresObservatoryRepository = mockk()
    private val organizationRepository: PostgresOrganizationRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val service = StatisticsService(
        statsRepository,
        userRepository,
        contributorRepository,
        observatoryRepository,
        organizationRepository,
        resourceRepository
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            statsRepository,
            userRepository,
            contributorRepository,
            observatoryRepository,
            organizationRepository,
            resourceRepository
        )
    }

    @Test
    fun `given a research field id, when fetching the research field stats, it returns success`() {
        val id = ThingId("R11")
        val researchField = createResource().copy(id = id, classes = setOf(Classes.researchField))
        val stats = ResearchFieldStats(
            id = id,
            papers = 30,
            comparisons = 5,
            total = 30
        )

        every { resourceRepository.findById(id) } returns Optional.of(researchField)
        every { statsRepository.findResearchFieldStatsById(id, any()) } returns Optional.of(stats)

        service.findResearchFieldStatsById(id, false)

        verify(exactly = 1) {
            resourceRepository.findById(id)
            statsRepository.findResearchFieldStatsById(id, any())
        }
    }

    @Test
    fun `given a research field id, when research field resource does not exist, then it returns an appropriate error`() {
        val id = ThingId("R11")

        every { resourceRepository.findById(id) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { service.findResearchFieldStatsById(id, false) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 0) { statsRepository.findResearchFieldStatsById(id, any()) }
    }

    @Test
    fun `given a research field id, when underlying resource is not a research field, then it returns an appropriate error`() {
        val id = ThingId("R11")
        val notAResearchField = createResource().copy(id = id)

        every { resourceRepository.findById(id) } returns Optional.of(notAResearchField)

        assertThrows<ResearchFieldNotFound> { service.findResearchFieldStatsById(id, false) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 0) { statsRepository.findResearchFieldStatsById(id, any()) }
    }
}
