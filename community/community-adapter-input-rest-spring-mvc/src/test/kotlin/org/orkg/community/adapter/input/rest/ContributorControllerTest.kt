package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import java.time.Clock
import java.util.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/** A regular expression to match ISO 8601 formatted dates. */
// If nanosecond part is rounded to zero, the last digit is removed. Fun bug. -----vvv
private const val ISO_8601_PATTERN = """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{1,8}\d?([+-]\d{2}:\d{2}|Z)$"""

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(classes = [ContributorController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ContributorController::class])
class ContributorControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userDetailsService: UserDetailsService

    @MockkBean
    private lateinit var retrieveContributor: RetrieveContributorUseCase

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `When ID is not found Then return 404 Not Found`() {
        val id = MockUserId.USER.let(::ContributorId)
        every { retrieveContributor.findById(id) } returns Optional.empty()

        mockMvc
            .perform(contributorRequest(id))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `When ID is found Then return contributor`() {
        val id = MockUserId.USER.let(::ContributorId)
        every { retrieveContributor.findById(id) } returns Optional.of(createContributor(id = id))

        mockMvc
            .perform(contributorRequest(id))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.joined_at").value(isISO8601()))
            .andExpect(MockMvcResultMatchers.header().string("Cache-Control", "max-age=300"))
    }

    private fun contributorRequest(id: ContributorId) =
        MockMvcRequestBuilders.get("/api/contributors/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")

    private fun isISO8601() = Matchers.matchesRegex(ISO_8601_PATTERN)
}
