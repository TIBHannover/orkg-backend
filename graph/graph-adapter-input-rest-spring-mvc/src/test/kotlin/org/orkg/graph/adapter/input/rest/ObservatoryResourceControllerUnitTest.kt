package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [ObservatoryResourceController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [ObservatoryResourceController::class])
@DisplayName("Given an ObservatoryResourceController controller")
internal class ObservatoryResourceControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and papers list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val paperResource = createResource(
            observatoryId = id,
            classes = setOf(ThingId("Paper"))
        )
        every {
            resourceService.findAllPapersByObservatoryId(id, any())
        } returns PageImpl(listOf(paperResource))

        mockMvc.perform(get("/api/observatories/$id/papers"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findAllPapersByObservatoryId(id, any()) }
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and comparisons list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val comparisonResource = createResource(
            observatoryId = id,
            classes = setOf(ThingId("Comparison"))
        )
        every {
            resourceService.findAllComparisonsByObservatoryId(id, any())
        } returns PageImpl(listOf(comparisonResource))

        mockMvc.perform(get("/api/observatories/$id/comparisons"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findAllComparisonsByObservatoryId(id, any()) }
    }

    @Test
    fun `Given the observatory id, when service succeeds, then status is 200 OK and problems list is returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val problemResource = createResource(
            observatoryId = id,
            classes = setOf(ThingId("Problem"))
        )
        every {
            resourceService.findAllProblemsByObservatoryId(id, any())
        } returns PageImpl(listOf(problemResource))

        mockMvc.perform(get("/api/observatories/$id/problems"))
            .andExpect(status().isOk)

        verify(exactly = 1) { resourceService.findAllProblemsByObservatoryId(id, any()) }
    }
}
