package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Contributor Controller")
@Transactional
internal class ContributorControllerTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var service: UserService

    @Test
    @DisplayName("When contributor is found Then returns contributor information")
    fun getById() {
        service.registerUser("some.user@example.org", "IRRELEVANT", "Some User")
        val id = service
            .findByEmail("some.user@example.org")
            .map(UserEntity::id)
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
        PayloadDocumentation.responseFields(
            fieldWithPath("id").description("The contributor ID."),
            fieldWithPath("display_name").description("The name of the contributor."),
            fieldWithPath("joined_at").description("The time the contributor joined the project (in ISO 8601 format)."),
            fieldWithPath("organization_id").description("The ID of the organization the contributor belongs to. All zeros if the contributor is not part of an organization."),
            fieldWithPath("observatory_id").description("The ID of the observatory the contributor belongs to. All zeros if the contributor has not joined an observatory."),
            fieldWithPath("gravatar_id").description("The ID of the contributor on https://gravatar.com/[Gravatar]. (Useful for generating profile pictures.)"),
            fieldWithPath("avatar_url").description("A URL to an avatar representing the user. Currently links to https://gravatar.com/[Gravatar].")
        )
}
