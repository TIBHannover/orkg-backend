package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.input.LegacyStatisticsUseCases
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@ContextConfiguration(classes = [LegacyStatsController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [LegacyStatsController::class])
internal class LegacyStatsControllerUnitTest : MockMvcBaseTest("stats") {
    @MockkBean
    private lateinit var statisticsService: LegacyStatisticsUseCases

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @Test
    fun `When retrieving stats about observatories and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id, 1, 0)
        every {
            statisticsService.findAllObservatoryStats(any())
        } returns pageOf(response)

        get("/api/stats/observatories")
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { statisticsService.findAllObservatoryStats(any()) }
    }

    @Test
    fun `When retrieving stats about a single observatory and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id, 1, 0)
        every {
            statisticsService.findObservatoryStatsById(id)
        } returns response

        get("/api/stats/observatories/{id}", id)
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) { statisticsService.findObservatoryStatsById(id) }
    }
}
