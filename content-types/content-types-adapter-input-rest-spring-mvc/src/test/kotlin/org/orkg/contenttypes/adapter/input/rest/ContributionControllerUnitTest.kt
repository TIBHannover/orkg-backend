package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.Contribution
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.graph.domain.Visibility
import org.orkg.testing.andExpectContribution
import org.orkg.testing.andExpectPage
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ContributionController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [ContributionController::class])
@DisplayName("Given a Contribution controller")
internal class ContributionControllerUnitTest : RestDocsTest("contributions") {

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

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
                        fieldWithPath("visibility").description("""Visibility of the contribution. Can be one of "default", "featured", "unlisted" or "deleted".""")
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

        get("/api/contributions/$id")
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

    private fun get(string: String) = mockMvc.perform(
        MockMvcRequestBuilders.get(string)
            .accept("application/vnd.orkg.contribution.v2+json")
    )

    private fun createDummyContribution() = Contribution(
        id = ThingId("R8199"),
        label = "ORKG System",
        classes = setOf(ThingId("C123")),
        properties = mapOf(
            ThingId("R456") to listOf(
                ThingId("R789"),
                ThingId("R147")
            )
        ),
        visibility = Visibility.DEFAULT
    )
}
