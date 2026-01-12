package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.adapter.input.rest.ContributorIdentifierController.CreateContributorIdentifierRequest
import org.orkg.community.adapter.input.rest.ContributorIdentifierControllerUnitTest.TestController
import org.orkg.community.adapter.input.rest.mapping.ContributorIdentifierRepresentationAdapter
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.ContributorIdentifierAlreadyExists
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.input.ContributorIdentifierUseCases
import org.orkg.community.testing.asciidoc.allowedContributorIdentifierValues
import org.orkg.community.testing.fixtures.configuration.CommunityControllerUnitTestConfiguration
import org.orkg.community.testing.fixtures.contributorIdentifierResponseFields
import org.orkg.community.testing.fixtures.createContributorIdentifier
import org.orkg.contenttypes.domain.InvalidIdentifier
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectContributorIdentifier
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.enumValues
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@ContextConfiguration(
    classes = [ContributorIdentifierController::class, TestController::class, CommunityControllerUnitTestConfiguration::class]
)
@WebMvcTest(controllers = [ContributorIdentifierController::class, TestController::class])
internal class ContributorIdentifierControllerUnitTest : MockMvcBaseTest("contributor-identifiers") {
    @MockkBean
    private lateinit var contributorIdentifierUseCases: ContributorIdentifierUseCases

    @Test
    @DisplayName("Given a contributor identifier, when serialized, it returns the correct result")
    fun findById() {
        documentedGetRequestTo("/open-api-doc-test")
            .perform()
            .andExpect(status().isOk)
            .andExpectContributorIdentifier()
            .andDocument {
                responseFields<ContributorIdentifierRepresentation>(contributorIdentifierResponseFields())
            }
    }

    @Test
    @DisplayName("Given several contributor identifiers, when fetched by contributor id, then status is 200 OK and contributor identifiers are returned")
    fun findAllByContributorId() {
        val contributorId = ContributorId(MockUserId.USER)

        every { contributorIdentifierUseCases.findAllByContributorId(contributorId, any()) } returns pageOf(createContributorIdentifier())

        documentedGetRequestTo("/api/contributors/{id}/identifiers", contributorId)
            .perform()
            .andExpect(status().isOk)
            .andExpectContributorIdentifier("$.content[*]")
            .andDocument {
                summary("Listing contributor identifiers")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<contributor-identifiers,contributor identifiers>>.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the contributor."),
                )
                pagedQueryParameters()
                pagedResponseFields<ContributorIdentifierRepresentation>(contributorIdentifierResponseFields())
            }

        verify(exactly = 1) { contributorIdentifierUseCases.findAllByContributorId(contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contributor identifier create request, when service succeeds, then status is 201 CREATED")
    fun create() {
        val contributorId = ContributorId(MockUserId.USER)

        every { contributorIdentifierUseCases.create(any()) } returns createContributorIdentifier()

        documentedPostRequestTo("/api/contributors/{id}/identifiers", contributorId)
            .content(createContributorIdentifierRequest())
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/contributors/$contributorId/identifiers")))
            .andDocument {
                summary("Creating contributor identifiers")
                description(
                    """
                    A `POST` request assigns a new identifier to the currently logged in contributor.
                    The response will be `201 Created` when successful.
                    The updated set of contributor identifiers can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the contributor."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated set of identifiers can be fetched from."),
                )
                requestFields<CreateContributorIdentifierRequest>(
                    fieldWithPath("type").description("The type of the identifier. Either of $allowedContributorIdentifierValues.").type("enum").enumValues(ContributorIdentifier.Type.entries.map { it.id }),
                    fieldWithPath("value").description("The value of the identifier."),
                )
                throws(ContributorNotFound::class, ContributorIdentifierAlreadyExists::class, InvalidIdentifier::class)
            }

        verify(exactly = 1) { contributorIdentifierUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contributor identifier, when deleted by id, then status is 204 NO CONTENT")
    fun deleteByContributorIdAndValue() {
        val contributorId = ContributorId(MockUserId.USER)
        val value = "0000-0001-5109-3700"

        every { contributorIdentifierUseCases.deleteByContributorIdAndValue(contributorId, value) } just runs

        // TODO: For unknown reasons, delete requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedDeleteRequestTo("/api/contributors/{id}/identifiers?value=$value", contributorId)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Deleting contributor identifiers")
                description(
                    """
                    A `DELETE` request removes an existing identifier of the currently logged in contributor.
                    The response will be `204 No Content` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the contributor."),
                )
                queryParameters(
                    parameterWithName("value").description("The value of the identifier."),
                )
            }

        verify(exactly = 1) { contributorIdentifierUseCases.deleteByContributorIdAndValue(contributorId, value) }
    }

    @TestComponent
    @RestController
    internal class TestController : ContributorIdentifierRepresentationAdapter {
        @GetMapping("/open-api-doc-test")
        fun findById(): ContributorIdentifierRepresentation =
            createContributorIdentifier().toContributorIdentifierRepresentation()
    }

    private fun createContributorIdentifierRequest() =
        CreateContributorIdentifierRequest(
            type = ContributorIdentifier.Type.ORCID,
            value = "0000-0001-5109-3700"
        )
}
