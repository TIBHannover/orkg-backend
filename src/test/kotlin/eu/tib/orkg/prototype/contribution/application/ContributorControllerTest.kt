package eu.tib.orkg.prototype.contribution.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.application.ContributorController
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import io.mockk.every
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.provenance.contributors.application.ports.input.RetrieveContributorUseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [ContributorController::class])
@AuthorizationServerUnitTestWorkaround
class ContributorControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var retrieveContributor: RetrieveContributorUseCase

    @BeforeEach
    fun setup() {
        mockMvc = webAppContextSetup(context).build()
    }

    @Test
    fun `When ID is not found Then return 404 Not Found`() {
        val id = ContributorId(UUID.randomUUID())
        every { retrieveContributor.byId(id) } returns Optional.empty()

        mockMvc
            .perform(contributorRequest(id))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `When ID is found Then return contributor`() {
        val id = ContributorId(UUID.randomUUID())
        every { retrieveContributor.byId(id) } returns Optional.of(Contributor(id, "Some Name", OffsetDateTime.now()))

        mockMvc
            .perform(contributorRequest(id))
            .andExpect(status().isOk)
    }

    private fun contributorRequest(id: ContributorId) =
        get("/api/contributors/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
}
