package org.orkg.community.adapter.input.rest

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.domain.User
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Contributor Controller")
@Transactional
internal class ContributorControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: AuthUseCase

    @Autowired
    private lateinit var repository: UserRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("When contributor is found Then returns contributor information")
    fun getById() {
        service.registerUser("some.user@example.org", "IRRELEVANT", "Some User")
        val id = service
            .findByEmail("some.user@example.org")
            .map(User::id)
            .orElseThrow { IllegalStateException("Test setup broken! Should find the user created!") }

        mockMvc
            .perform(getRequestTo("/api/contributors/$id"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    snippet,
                    contributorListResponseFields()
                )
            )
    }

    private fun contributorListResponseFields() =
        responseFields(
            fieldWithPath("id").description("The contributor ID."),
            fieldWithPath("display_name").description("The name of the contributor."),
            fieldWithPath("joined_at").description("The time the contributor joined the project (in ISO 8601 format)."),
            fieldWithPath("organization_id").description("The ID of the organization the contributor belongs to. All zeros if the contributor is not part of an organization."),
            fieldWithPath("observatory_id").description("The ID of the observatory the contributor belongs to. All zeros if the contributor has not joined an observatory."),
            fieldWithPath("gravatar_id").description("The ID of the contributor on https://gravatar.com/[Gravatar]. (Useful for generating profile pictures.)"),
            fieldWithPath("avatar_url").description("A URL to an avatar representing the user. Currently links to https://gravatar.com/[Gravatar].")
        )
}
