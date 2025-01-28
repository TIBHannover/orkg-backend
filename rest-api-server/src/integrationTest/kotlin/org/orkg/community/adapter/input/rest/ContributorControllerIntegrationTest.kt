package org.orkg.community.adapter.input.rest

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.spring.restdocs.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
internal class ContributorControllerIntegrationTest : MockMvcBaseTest("contributors") {

    @Autowired
    private lateinit var repository: ContributorRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("When contributor is found Then returns contributor information")
    fun getById() {
        val contributor = createContributor()
        repository.save(contributor)

        documentedGetRequestTo("/api/contributors/{id}", contributor.id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contributor.")
                    ),
                    contributorListResponseFields()
                )
            )
            .andDo(generateDefaultDocSnippets())
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
