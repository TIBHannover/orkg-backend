package eu.tib.orkg.prototype.contenttypes

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.contenttypes.application.CONTRIBUTION_JSON_V2
import eu.tib.orkg.prototype.contenttypes.application.ContributionController
import eu.tib.orkg.prototype.contenttypes.application.ContributionNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.pageOf
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.testing.andExpectPage
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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

@ContextConfiguration(classes = [ContributionController::class, ExceptionHandler::class])
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
        every { contributionService.findById(contribution.id) } returns contribution

        documentedGetRequestTo("/api/contributions/{id}", contribution.id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the contribution to retrieve.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the contribution."),
                        fieldWithPath("label").description("The label of the contribution."),
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
        every { contributionService.findById(id) } throws exception

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
        properties = mapOf(
            ThingId("R456") to listOf(
                ThingId("R789"),
                ThingId("R147")
            )
        ),
        visibility = Visibility.DEFAULT
    )
}
