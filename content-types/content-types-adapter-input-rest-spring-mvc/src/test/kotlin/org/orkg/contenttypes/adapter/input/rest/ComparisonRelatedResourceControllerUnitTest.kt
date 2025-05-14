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
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedResource
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectComparisonRelatedResource
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
        ComparisonRelatedResourceController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [ComparisonRelatedResourceController::class])
internal class ComparisonRelatedResourceControllerUnitTest : MockMvcBaseTest("comparison-related-resources") {
    @MockkBean
    private lateinit var comparisonRelatedResourceService: ComparisonRelatedResourceUseCases

    @Test
    @DisplayName("Given a comparison related resource, when fetched by id, then status is 200 OK and comparison related resource is returned")
    fun getSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = createComparisonRelatedResource()

        every {
            comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResource.id)
        } returns Optional.of(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResource.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related resource."),
                        fieldWithPath("label").description("The title of label comparison related resource."),
                        fieldWithPath("image").description("The url for the image of the comparison related resource."),
                        fieldWithPath("url").description("The url of the comparison related resource."),
                        fieldWithPath("description").description("The description of the comparison related resource."),
                        timestampFieldWithPath("created_at", "the comparison related resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResource.id) }
    }

    @Test
    fun `Given a comparison related resource, when fetched by id but service reports missing comparison related resource, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResourceId = ThingId("R1435")
        val exception = ComparisonRelatedResourceNotFound(comparisonRelatedResourceId)

        every {
            comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResourceId)
        } returns Optional.empty()

        get("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResourceId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-resources/$comparisonRelatedResourceId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResourceId) }
    }

    @Test
    @DisplayName("Given several comparison related resources, when fetched, then status is 200 OK and comparison related resources are returned")
    fun getPaged() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = listOf(createComparisonRelatedResource())

        every { comparisonRelatedResourceService.findAllByComparisonId(comparisonId, any()) } returns pageOf(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{id}/related-resources", comparisonId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedResourceService.findAllByComparisonId(comparisonId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource request, when service succeeds, it creates and returns the comparison related resource")
    fun create() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonRelatedResourceService.create(any()) } returns id

        documentedPostRequestTo("/api/comparisons/{id}/related-resources", comparisonId)
            .content(createComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-resources/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The comparison to attach the comparison related resource to.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created comparison related resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related resource."),
                        fieldWithPath("image").description("The url to the image of the comparison related resource. (optional)"),
                        fieldWithPath("url").description("The url of the comparison related resource. (optional)"),
                        fieldWithPath("description").description("The description of the comparison related resource. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedResourceService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison related resource request, when service reports missing comparison, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R100")
        val exception = ComparisonNotFound(comparisonId)
        every { comparisonRelatedResourceService.create(any()) } throws exception

        post("/api/comparisons/$comparisonId/related-resources")
            .content(createComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-resources"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonRelatedResourceService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource request, when service succeeds, it updates the comparison related resource")
    fun update() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonRelatedResourceService.update(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedFigureId}", comparisonId, id)
            .content(updateComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison related resource can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The id of the comparison the comparison related resource belongs to."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related resource to update.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related resource. (optional)"),
                        fieldWithPath("image").description("The url to the image of the comparison related resource."),
                        fieldWithPath("url").description("The url of the comparison related resource."),
                        fieldWithPath("description").description("The description of the comparison related resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonRelatedResourceService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun delete() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResourceId = ThingId("R456")

        every {
            comparisonRelatedResourceService.delete(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = any()
            )
        } just runs

        documentedDeleteRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResourceId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to delete.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison can be fetched from.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonRelatedResourceService.delete(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = ContributorId(MockUserId.USER)
            )
        }
    }

    private fun createComparisonRelatedResourceRequest() =
        ComparisonRelatedResourceController.CreateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )

    private fun updateComparisonRelatedResourceRequest() =
        ComparisonRelatedResourceController.UpdateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )
}
