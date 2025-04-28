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
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.ContributorIdentifierControllerUnitTest.TestController
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.adapter.input.rest.mapping.ContributorIdentifierRepresentationAdapter
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.input.ContributorIdentifierUseCases
import org.orkg.community.testing.asciidoc.allowedContributorIdentifierValues
import org.orkg.community.testing.fixtures.createContributorIdentifier
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectContributorIdentifier
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@ContextConfiguration(
    classes = [
        ContributorIdentifierController::class,
        TestController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        CommunityJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [ContributorIdentifierController::class, TestController::class])
internal class ContributorIdentifierControllerUnitTest : MockMvcBaseTest("contributor-identifiers") {
    @MockkBean
    private lateinit var contributorIdentifierUseCases: ContributorIdentifierUseCases

    @Test
    @DisplayName("Given a contributor identifier, when serialized, it returns the correct result")
    fun getSingle() {
        documentedGetRequestTo("/contributor-identifier")
            .perform()
            .andExpect(status().isOk)
            .andExpectContributorIdentifier()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("type").description("The type of the identifier. Either of $allowedContributorIdentifierValues."),
                        fieldWithPath("value").description("The value of the identifier."),
                        timestampFieldWithPath("created_at", "the identifier was added"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    @DisplayName("Given several contributor identifiers, when fetched by contributor id, then status is 200 OK and contributor identifiers are returned")
    fun getPaged() {
        val contributorId = ContributorId(MockUserId.USER)

        every { contributorIdentifierUseCases.findAllByContributorId(contributorId, any()) } returns pageOf(createContributorIdentifier())

        documentedGetRequestTo("/api/contributors/{id}/identifiers", contributorId)
            .perform()
            .andExpect(status().isOk)
            .andExpectContributorIdentifier("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contributor.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contributor.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated set of identifiers can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("type").description("The type of the identifier. Either of $allowedContributorIdentifierValues."),
                        fieldWithPath("value").description("The value of the identifier."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributorIdentifierUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contributor identifier, when deleted by id, then status is 204 NO CONTENT")
    fun delete() {
        val contributorId = ContributorId(MockUserId.USER)
        val request = deleteContributorIdentifierRequest()

        every { contributorIdentifierUseCases.delete(contributorId, request.value) } just runs

        documentedDeleteRequestTo("/api/contributors/{id}/identifiers", contributorId)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contributor.")
                    ),
                    requestFields(
                        fieldWithPath("value").description("The value of the identifier."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributorIdentifierUseCases.delete(contributorId, request.value) }
    }

    @TestComponent
    @RestController
    internal class TestController : ContributorIdentifierRepresentationAdapter {
        @GetMapping("/contributor-identifier")
        fun getSingle(): ContributorIdentifierRepresentation =
            createContributorIdentifier().toContributorIdentifierRepresentation()
    }

    private fun createContributorIdentifierRequest() =
        ContributorIdentifierController.CreateContributorIdentifierRequest(
            type = ContributorIdentifier.Type.ORCID,
            value = "0000-0001-5109-3700"
        )

    private fun deleteContributorIdentifierRequest() =
        ContributorIdentifierController.DeleteContributorIdentifierRequest("0000-0001-5109-3700")
}
