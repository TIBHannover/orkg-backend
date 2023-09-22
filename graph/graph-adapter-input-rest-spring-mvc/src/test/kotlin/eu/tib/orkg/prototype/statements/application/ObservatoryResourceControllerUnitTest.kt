package eu.tib.orkg.prototype.statements.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.ContributorService
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
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
    private lateinit var userRepository: UserRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: ContributorService

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var templateRepository: TemplateRepository

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
        val paperResource = createResource().copy(
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
        val comparisonResource = createResource().copy(
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
        val problemResource = createResource().copy(
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
