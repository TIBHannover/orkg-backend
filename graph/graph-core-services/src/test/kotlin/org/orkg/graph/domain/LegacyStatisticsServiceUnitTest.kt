package org.orkg.graph.domain

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
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.fixedClock

internal class LegacyStatisticsServiceUnitTest {

    private val legacyStatisticsRepository: LegacyStatisticsRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val service = LegacyStatisticsService(
        legacyStatisticsRepository,
        contributorRepository,
        observatoryRepository,
        organizationRepository,
        resourceRepository,
        fixedClock,
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            legacyStatisticsRepository,
            contributorRepository,
            observatoryRepository,
            organizationRepository,
            resourceRepository
        )
    }

    @Test
    fun `given a research field id, when fetching the research field stats, it returns success`() {
        val id = ThingId("R11")
        val researchField = createResource(id = id, classes = setOf(Classes.researchField))
        val stats = ResearchFieldStats(
            id = id,
            papers = 30,
            comparisons = 5,
            total = 30
        )

        every { resourceRepository.findById(id) } returns Optional.of(researchField)
        every { legacyStatisticsRepository.findResearchFieldStatsById(id, any()) } returns Optional.of(stats)

        service.findResearchFieldStatsById(id, false)

        verify(exactly = 1) {
            resourceRepository.findById(id)
            legacyStatisticsRepository.findResearchFieldStatsById(id, any())
        }
    }

    @Test
    fun `given a research field id, when research field resource does not exist, then it returns an appropriate error`() {
        val id = ThingId("R11")

        every { resourceRepository.findById(id) } returns Optional.empty()

        assertThrows<ResearchFieldNotFound> { service.findResearchFieldStatsById(id, false) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 0) { legacyStatisticsRepository.findResearchFieldStatsById(id, any()) }
    }

    @Test
    fun `given a research field id, when underlying resource is not a research field, then it returns an appropriate error`() {
        val id = ThingId("R11")
        val notAResearchField = createResource(id = id)

        every { resourceRepository.findById(id) } returns Optional.of(notAResearchField)

        assertThrows<ResearchFieldNotFound> { service.findResearchFieldStatsById(id, false) }

        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 0) { legacyStatisticsRepository.findResearchFieldStatsById(id, any()) }
    }
}
