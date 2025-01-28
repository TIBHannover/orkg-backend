package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.MarkAsVerifiedUseCase
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.restdocs.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [PaperVerificationCommandController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [PaperVerificationCommandController::class])
internal class PaperVerificationControllerUnitTest : MockMvcBaseTest("papers") {

    @MockkBean
    private lateinit var service: MarkAsVerifiedUseCase

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
                put("/api/papers/{id}/metadata/verified", id.value)
                    .perform()
                    .andExpect(status().isNotFound)
                verify(exactly = 1) { service.markAsVerified(any()) }
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 204 No Content`() {
                every { service.markAsVerified(any()) } returns Unit
                put("/api/papers/{id}/metadata/verified", "R1")
                    .perform()
                    .andExpect(status().isNoContent)
                verify(exactly = 1) { service.markAsVerified(any()) }
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
                delete("/api/papers/{id}/metadata/verified", id.value)
                    .perform()
                    .andExpect(status().isNotFound)
                verify(exactly = 1) { service.markAsUnverified(any()) }
            }
        }

        @Nested
        @DisplayName("And it exists")
        inner class ResourceDoesExist {
            @Test
            @TestWithMockCurator
            fun `Then the controller returns 204 No Content`() {
                every { service.markAsUnverified(any()) } returns Unit
                delete("/api/papers/{id}/metadata/verified", "R1")
                    .perform()
                    .andExpect(status().isNoContent)
                verify(exactly = 1) { service.markAsUnverified(any()) }
            }
        }
    }
}
