package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.IsIso8601DateTimeString
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.configuration.CommunityControllerUnitTestConfiguration
import org.orkg.community.testing.fixtures.contributorResponseFields
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectContributor
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [ContributorController::class, CommunityControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ContributorController::class])
internal class ContributorControllerUnitTest : MockMvcBaseTest("contributors") {
    @MockkBean
    private lateinit var retrieveContributor: RetrieveContributorUseCase

    @Test
    @DisplayName("Given a contributor, when fetched by id, and contributor is found, then status is 200 OK and contributor is returned")
    fun findById() {
        val id = ContributorId(MockUserId.USER)
        every { retrieveContributor.findById(id) } returns Optional.of(createContributor(id = id))

        documentedGetRequestTo("/api/contributors/{id}", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectContributor()
            .andExpect(jsonPath("$.joined_at").value(IsIso8601DateTimeString()))
            .andExpect(header().string("Cache-Control", "max-age=300"))
            .andDocument {
                summary("Fetching contributors")
                description(
                    """
                    Information about a specific contributor can be obtained by sending a `GET` request to the contributor endpoint:
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the contributor."),
                )
                responseFields<Contributor>(contributorResponseFields())
                throws(ContributorNotFound::class)
            }

        verify(exactly = 1) { retrieveContributor.findById(id) }
    }

    @Test
    fun `When id is not found, then status is 404 NOT FOUND`() {
        val id = MockUserId.USER.let(::ContributorId)
        every { retrieveContributor.findById(id) } returns Optional.empty()

        get("/api/contributors/{id}", id)
            .perform()
            .andExpect(status().isNotFound)

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
    fun findAll() {
        val q = "some"
        every { retrieveContributor.findAll(any(), any()) } returns pageOf(createContributor())

        documentedGetRequestTo("/api/contributors")
            .param("q", q)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributor("$.content[*]")
            .andDocument {
                summary("")
                description(
                    """
                    
                    """
                )
                summary("Listing contributors")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<contributor-fetch,contributors>>.

                    NOTE: This endpoint requires authentication.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the display name of the contributor. (optional)").optional(),
                )
                pagedResponseFields<Contributor>(contributorResponseFields())
            }

        verify(exactly = 1) {
            retrieveContributor.findAll(
                pageable = any(),
                label = q,
            )
        }
    }
}
