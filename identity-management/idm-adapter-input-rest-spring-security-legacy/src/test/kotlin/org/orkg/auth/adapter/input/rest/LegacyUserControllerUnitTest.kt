package org.orkg.auth.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.adapter.input.rest.LegacyUserController.UserDetails
import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.ContributorId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        LegacyUserController::class,
        WebMvcConfiguration::class,
        FixedClockConfig::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        CommonDocumentationContextProvider::class,
    ]
)
@WebMvcTest(controllers = [LegacyUserController::class])
internal class LegacyUserControllerUnitTest : MockMvcBaseTest("users") {
    @MockkBean
    private lateinit var contributorRepository: ContributorRepository

    @Test
    @TestWithMockUser
    @DisplayName("Given a user, when fetching its user data, then status is 200 OK and user data is returned")
    fun fetchUserData() {
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(id = contributorId, name = "Test User")

        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)

        documentedGetRequestTo("/api/user")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(contributorId.toString()))
            .andExpect(jsonPath("$.email").value("user@example.org"))
            .andExpect(jsonPath("$.display_name").value("Test User"))
            .andExpect(jsonPath("$.created_at").value("2023-10-06T10:37:17.055493Z"))
            .andExpect(jsonPath("$.organization_id", `is`(nullValue())))
            .andExpect(jsonPath("$.observatory_id", `is`(nullValue())))
            .andExpect(jsonPath("$.is_curation_allowed").value(false))
            .andDocument {
                summary("Fetching user data")
                description(
                    """
                    A `GET` request returns information about the currently logged-in user.
                    An authentication token needs to be provided.
                    """
                )
                responseFields<UserDetails>(
                    fieldWithPath("id").description("The id of the user."),
                    fieldWithPath("email").description("The email of the user."),
                    fieldWithPath("display_name").description("The display name of the user."),
                    timestampFieldWithPath("created_at", "the user created their account"),
                    fieldWithPath("organization_id").type("string").description("The id of the organization the user belongs to. (optional)").optional(),
                    fieldWithPath("observatory_id").type("string").description("The id of the observatory the user belongs to. (optional)").optional(),
                    fieldWithPath("is_curation_allowed").description("Whether the user is allowed to curate content."),
                )
                throws(ContributorNotFound::class)
            }

        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }
}
