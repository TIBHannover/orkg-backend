package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedFigure
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectComparisonRelatedFigure
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        ComparisonRelatedFigureController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [ComparisonRelatedFigureController::class])
internal class ComparisonRelatedFigureControllerUnitTest : MockMvcBaseTest("comparison-related-figures") {
    @MockkBean
    private lateinit var comparisonRelatedFigureService: ComparisonRelatedFigureUseCases

    @Test
    @DisplayName("Given a comparison related figure, when fetched by id, then status is 200 OK and comparison related figure is returned")
    fun getSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = createComparisonRelatedFigure()

        every {
            comparisonRelatedFigureService.findByIdAndComparisonId(comparisonId, comparisonRelatedFigure.id)
        } returns Optional.of(comparisonRelatedFigure)

        documentedGetRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigure.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedFigure()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related figure."),
                        fieldWithPath("label").description("The title of label comparison related figure."),
                        fieldWithPath("image").description("The url for the image of the comparison related figure."),
                        fieldWithPath("description").description("The description of the comparison related figure."),
                        timestampFieldWithPath("created_at", "the comparison related figure was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related figure.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedFigureService.findByIdAndComparisonId(comparisonId, comparisonRelatedFigure.id) }
    }

    @Test
    fun `Given a comparison related figure, when fetched by id but service reports missing comparison related figure, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigureId = ThingId("R1435")
        val exception = ComparisonRelatedFigureNotFound(comparisonRelatedFigureId)

        every {
            comparisonRelatedFigureService.findByIdAndComparisonId(comparisonId, comparisonRelatedFigureId)
        } returns Optional.empty()

        get("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigureId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-figures/$comparisonRelatedFigureId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonRelatedFigureService.findByIdAndComparisonId(comparisonId, comparisonRelatedFigureId) }
    }

    @Test
    @DisplayName("Given several comparison related figures, when fetched, then status is 200 OK and comparison related figures are returned")
    fun getPaged() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = listOf(createComparisonRelatedFigure())

        every { comparisonRelatedFigureService.findAllByComparisonId(comparisonId, any()) } returns pageOf(comparisonRelatedFigure)

        documentedGetRequestTo("/api/comparisons/{id}/related-figures", comparisonId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedFigure("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedFigureService.findAllByComparisonId(comparisonId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure request, when service succeeds, it creates and returns the comparison related figure")
    fun create() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonRelatedFigureService.create(any()) } returns id

        documentedPostRequestTo("/api/comparisons/{id}/related-figures", comparisonId)
            .content(createComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-figures/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The comparison to attach the comparison related figure to.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created comparison related figure can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related figure."),
                        fieldWithPath("image").description("The url to the image of the comparison related figure. (optional)"),
                        fieldWithPath("description").description("The description of the comparison related figure. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedFigureService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison related figure request, when service reports missing comparison, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R100")
        val exception = ComparisonNotFound(comparisonId)
        every { comparisonRelatedFigureService.create(any()) } throws exception

        post("/api/comparisons/$comparisonId/related-figures")
            .content(createComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-figures"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonRelatedFigureService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure request, when service succeeds, it updates the comparison related figure")
    fun update() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonRelatedFigureService.update(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, id)
            .content(updateComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-figures/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison related figure can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The id of the comparison the comparison related figure belongs to."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to update.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related figure. (optional)"),
                        fieldWithPath("image").description("The url to the image of the comparison related figure."),
                        fieldWithPath("description").description("The description of the comparison related figure.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedFigureService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun delete() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigureId = ThingId("R456")

        every {
            comparisonRelatedFigureService.delete(
                comparisonId = comparisonId,
                comparisonRelatedFigureId = comparisonRelatedFigureId,
                contributorId = any()
            )
        } just runs

        documentedDeleteRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigureId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to delete.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison can be fetched from.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonRelatedFigureService.delete(
                comparisonId = comparisonId,
                comparisonRelatedFigureId = comparisonRelatedFigureId,
                contributorId = ContributorId(MockUserId.USER)
            )
        }
    }

    private fun createComparisonRelatedFigureRequest() =
        ComparisonRelatedFigureController.CreateComparisonRelatedFigureRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            description = "comparison related resource description"
        )

    private fun updateComparisonRelatedFigureRequest() =
        ComparisonRelatedFigureController.UpdateComparisonRelatedFigureRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            description = "comparison related resource description"
        )
}
