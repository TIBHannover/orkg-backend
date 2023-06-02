package eu.tib.orkg.prototype.statements.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [StatsController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [StatsController::class])
@DisplayName("Given a Stats controller")
class StatsControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var statisticsService: RetrieveStatisticsUseCase

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: ContributorService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `When retrieving stats about observatories and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id.value.toString(), 1, 0)
        every {
            statisticsService.getObservatoriesPapersAndComparisonsCount(any())
        } returns pageOf(response)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stats/observatories"))
            .andExpect(MockMvcResultMatchers.status().isOk)

        verify(exactly = 1) { statisticsService.getObservatoriesPapersAndComparisonsCount(any()) }
    }
}
