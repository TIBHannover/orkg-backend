package eu.tib.orkg.prototype.contenttypes

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.contenttypes.application.ContributionController
import eu.tib.orkg.prototype.contenttypes.application.ContributionNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [ContributionController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [ContributionController::class])
@DisplayName("Given a Contribution controller")
internal class ContributionControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given a contribution, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned`() {
        val contribution = createDummyContribution()
        every { contributionService.findById(contribution.id) } returns contribution

        get("/api/contributions/${contribution.id}")
            .andExpect(status().isOk)

        verify(exactly = 1) { contributionService.findById(contribution.id) }
    }

    @Test
    fun `Given a contribution, when it is fetched by id and service reports missing contribution, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = ContributionNotFound(id)
        every { contributionService.findById(id) } throws exception

        get("/api/contributions/$id")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/contributions/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { contributionService.findById(id) }
    }

    private fun get(string: String) = mockMvc.perform(
        MockMvcRequestBuilders.get(string)
            .accept("application/vnd.orkg.contribution.v2+json")
    )

    private fun createDummyContribution() = Contribution(
        id = ThingId("R123"),
        label = "Dummy Contribution Label",
        properties = mapOf(
            ThingId("R456") to listOf(
                ThingId("R789"),
                ThingId("R147")
            )
        ),
        visibility = Visibility.DEFAULT
    )
}
