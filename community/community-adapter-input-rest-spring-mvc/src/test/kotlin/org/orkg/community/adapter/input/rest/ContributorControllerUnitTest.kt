package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.IsIso8601DateTimeString
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectContributor
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import orkg.orkg.community.testing.fixtures.contributorResponseFields
import java.util.Optional

@ContextConfiguration(
    classes = [
        ContributorController::class,
        CommonJacksonModule::class,
        CommunityJacksonModule::class,
        ExceptionTestConfiguration::class,
        FixedClockConfig::class
    ]
)
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
            .andExpect(status().isNotFound)

        verify(exactly = 1) { retrieveContributor.findById(id) }
    }

    @Test
    @DisplayName("When ID is found Then return contributor")
    fun getSingle() {
        val id = ContributorId(MockUserId.USER)
        every { retrieveContributor.findById(id) } returns Optional.of(createContributor(id = id))

        documentedGetRequestTo("/api/contributors/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectContributor()
            .andExpect(jsonPath("$.joined_at").value(IsIso8601DateTimeString()))
            .andExpect(header().string("Cache-Control", "max-age=300"))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contributor.")
                    ),
                    responseFields(contributorResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

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
}
