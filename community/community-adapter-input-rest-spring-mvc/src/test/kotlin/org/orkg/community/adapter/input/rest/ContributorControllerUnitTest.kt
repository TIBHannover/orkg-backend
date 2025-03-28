package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectContributor
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

/**
 * A regular expression to match ISO 8601 formatted dates.
 * If nanosecond part is rounded to zero, the last digit is removed. Fun bug. -----vvv
 */
private const val ISO_8601_PATTERN = """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{1,8}\d?([+-]\d{2}:\d{2}|Z)$"""

@ContextConfiguration(classes = [ContributorController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ContributorController::class])
internal class ContributorControllerUnitTest : MockMvcBaseTest("contributors") {
    @MockkBean
    private lateinit var retrieveContributor: RetrieveContributorUseCase

    @Test
    fun `When ID is not found Then return 404 Not Found`() {
        val id = MockUserId.USER.let(::ContributorId)
        every { retrieveContributor.findById(id) } returns Optional.empty()

        get("/api/contributors/{id}", id)
            .perform()
            .andExpect(MockMvcResultMatchers.status().isNotFound)

        verify(exactly = 1) { retrieveContributor.findById(id) }
    }

    @Test
    fun `When ID is found Then return contributor`() {
        val id = MockUserId.USER.let(::ContributorId)
        every { retrieveContributor.findById(id) } returns Optional.of(createContributor(id = id))

        get("/api/contributors/{id}", id)
            .perform()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.joined_at").value(isISO8601()))
            .andExpect(MockMvcResultMatchers.header().string("Cache-Control", "max-age=300"))

        verify(exactly = 1) { retrieveContributor.findById(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given several contributors, when they are fetched, then status is 200 OK and contributors are returned")
    fun getPaged() {
        every { retrieveContributor.findAll(any(), any()) } returns pageOf(createContributor())

        documentedGetRequestTo("/api/contributors")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributor("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            retrieveContributor.findAll(any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given several contributors, when filtering by several parameters, then status is 200 OK and contributors are returned")
    fun getPagedWithParameters() {
        every { retrieveContributor.findAll(any(), any()) } returns pageOf(createContributor())

        val q = "some"

        documentedGetRequestTo("/api/contributors")
            .param("q", q)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributor("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the display name of the contributor. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            retrieveContributor.findAll(
                pageable = any(),
                label = q,
            )
        }
    }

    private fun isISO8601() = Matchers.matchesRegex(ISO_8601_PATTERN)
}
