package eu.tib.orkg.prototype.statements.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import io.mockk.every
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [PaperVerificationCommandController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given a Resource")
class ResourceVerificationControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var resourceAdapter: LoadResourcePort

    @MockkBean
    private lateinit var service: MarkAsVerifiedUseCase

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Nested
    @DisplayName("When it is marked as verified")
    inner class WhenMarkedVerified {
        @Nested
        @DisplayName("And it does not exist")
        inner class ResourceDoesNotExist {
            @Test
            fun `Then the controller returns 404 Not Found`() {
                val id = ResourceId("unknown")
                every { service.markAsVerified(any()) } throws ResourceNotFound.withId(id)
                mockMvc.perform(markVerifiedRequest(id.value)).andExpect(status().isNotFound)
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
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
            fun `Then the controller returns 404 Not Found`() {
                val id = ResourceId("unknown")
                every { service.markAsUnverified(any()) } throws ResourceNotFound.withId(id)
                mockMvc.perform(markUnverifiedRequest(id.value)).andExpect(status().isNotFound)
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
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
