package eu.tib.orkg.prototype.content_types.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.content_types.api.ContributionRepresentation
import eu.tib.orkg.prototype.content_types.api.ContributionUseCases
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
    private lateinit var objectMapper: ObjectMapper

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
    fun `Given a contribution, when is fetched by id and service succeeds, then status is 200 OK and contribution is returned`() {
        val contribution = createDummyContributionRepresentation()
        every { contributionService.findById(contribution.id) } returns contribution

        get("/api/content-types/contribution/${contribution.id}")
            .andExpect(status().isOk)

        verify(exactly = 1) { contributionService.findById(contribution.id) }
    }

    @Test
    fun `Given a contribution, when is fetched by id and service reports missing contribution, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = ContributionNotFound(id)
        every { contributionService.findById(id) } throws exception

        get("/api/content-types/contribution/$id")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/content-types/contribution/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { contributionService.findById(id) }
    }

    private fun get(string: String) = mockMvc.perform(MockMvcRequestBuilders.get(string))

    private fun createDummyContributionRepresentation() = object : ContributionRepresentation {
        override val id: ThingId = ThingId("R123")
        override val label: String = "Dummy Contribution Label"
        override val properties: Map<ThingId, List<ThingId>> = mapOf(
            ThingId("R456") to listOf(
                ThingId("R789"),
                ThingId("R147")
            )
        )
        override val visibility: Visibility = Visibility.DEFAULT
    }
}
