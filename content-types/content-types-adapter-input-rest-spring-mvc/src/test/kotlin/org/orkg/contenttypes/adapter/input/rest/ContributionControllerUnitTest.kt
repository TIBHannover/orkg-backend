package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyContribution
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectContribution
import org.orkg.testing.andExpectPage
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ContributionController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ContributionController::class])
internal class ContributionControllerUnitTest : RestDocsTest("contributions") {

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Test
    @DisplayName("Given a contribution, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned")
    fun getSingle() {
        val contribution = createDummyContribution()
        every { contributionService.findById(contribution.id) } returns Optional.of(contribution)

        documentedGetRequestTo("/api/contributions/{id}", contribution.id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectContribution()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contribution to retrieve.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the contribution."),
                        fieldWithPath("label").description("The label of the contribution."),
                        fieldWithPath("classes").description("The classes of the contribution resource."),
                        subsectionWithPath("properties").description("A map of predicate ids to lists of thing ids, that represent the statements that this contribution consists of."),
                        fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
                        timestampFieldWithPath("created_at", "the contribution resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this contribution."),
                        fieldWithPath("visibility").description("""Visibility of the contribution. Can be one of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this contribution.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributionService.findById(contribution.id) }
    }

    @Test
    fun `Given a contribution, when it is fetched by id and service reports missing contribution, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = ContributionNotFound(id)
        every { contributionService.findById(id) } returns Optional.empty()

        mockMvc.perform(get("/api/contributions/{id}", id).accept("application/vnd.orkg.contribution.v2+json"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/contributions/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { contributionService.findById(id) }
    }

    @Test
    @DisplayName("Given several contributions, then status is 200 OK and contributions are returned")
    fun getPaged() {
        val contribution = createDummyContribution()
        every { contributionService.findAll(any()) } returns pageOf(contribution)

        documentedGetRequestTo("/api/contributions")
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContribution("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributionService.findAll(any()) }
    }
}
