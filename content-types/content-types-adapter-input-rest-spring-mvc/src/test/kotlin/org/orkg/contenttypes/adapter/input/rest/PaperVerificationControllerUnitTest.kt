package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.MarkAsVerifiedUseCase
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(classes = [PaperVerificationCommandController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [PaperVerificationCommandController::class])
internal class PaperVerificationControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var userDetailsService: UserDetailsService

    @MockkBean
    private lateinit var service: MarkAsVerifiedUseCase

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Nested
    @DisplayName("When it is marked as verified")
    inner class WhenMarkedVerified {
        @Nested
        @DisplayName("And it does not exist")
        inner class ResourceDoesNotExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 404 Not Found`() {
                val id = ThingId("unknown")
                every { service.markAsVerified(any()) } throws ResourceNotFound.withId(id)
                mockMvc.perform(markVerifiedRequest(id.value)).andExpect(status().isNotFound)
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 204 No Content`() {
                every { service.markAsVerified(any()) } returns Unit
                mockMvc.perform(markVerifiedRequest("R1")).andExpect(status().isNoContent)
            }
        }
    }

    @Nested
    @DisplayName("When it is marked as unverified")
    inner class WhenMarkedUnverified {
        @Nested
        @DisplayName("And it does not exist")
        inner class ResourceDoesNotExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 404 Not Found`() {
                val id = ThingId("unknown")
                every { service.markAsUnverified(any()) } throws ResourceNotFound.withId(id)
                mockMvc.perform(markUnverifiedRequest(id.value)).andExpect(status().isNotFound)
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 204 No Content`() {
                every { service.markAsUnverified(any()) } returns Unit
                mockMvc.perform(markUnverifiedRequest("R1")).andExpect(status().isNoContent)
            }
        }
    }

    private fun markVerifiedRequest(id: String): MockHttpServletRequestBuilder =
        put("/api/papers/{id}/metadata/verified", id)
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")

    private fun markUnverifiedRequest(id: String): MockHttpServletRequestBuilder =
        delete("/api/papers/{id}/metadata/verified", id)
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
}
