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
import org.orkg.contenttypes.adapter.input.rest.ComparisonRelatedResourceController.CreateComparisonRelatedResourceRequest
import org.orkg.contenttypes.adapter.input.rest.ComparisonRelatedResourceController.UpdateComparisonRelatedResourceRequest
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedResource
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
import org.orkg.contenttypes.input.testing.fixtures.comparisonRelatedResourceResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.graph.domain.InvalidLabel
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectComparisonRelatedResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [ComparisonRelatedResourceController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ComparisonRelatedResourceController::class])
internal class ComparisonRelatedResourceControllerUnitTest : MockMvcBaseTest("comparison-related-resources") {
    @MockkBean
    private lateinit var comparisonRelatedResourceService: ComparisonRelatedResourceUseCases

    @Test
    @DisplayName("Given a comparison related resource, when fetched by id, then status is 200 OK and comparison related resource is returned")
    fun findByIdAndComparisonId() {
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
            .andDocument {
                summary("Fetching comparison related resources")
                description(
                    """
                    A `GET` request provides information about a comparison related resource.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison."),
                    parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to retrieve."),
                )
                responseFields<ComparisonRelatedResourceRepresentation>(comparisonRelatedResourceResponseFields())
            }

        verify(exactly = 1) { comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResource.id) }
    }

    @Test
    fun `Given a comparison related resource, when fetched by id but service reports missing comparison related resource, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResourceId = ThingId("R1435")

        every {
            comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResourceId)
        } returns Optional.empty()

        get("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResourceId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:comparison_related_resource_not_found")

        verify(exactly = 1) { comparisonRelatedResourceService.findByIdAndComparisonId(comparisonId, comparisonRelatedResourceId) }
    }

    @Test
    @DisplayName("Given several comparison related resources, when fetched, then status is 200 OK and comparison related resources are returned")
    fun findAllByComparisonId() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = listOf(createComparisonRelatedResource())

        every { comparisonRelatedResourceService.findAllByComparisonId(comparisonId, any()) } returns pageOf(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{id}/related-resources", comparisonId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedResource("$.content[*]")
            .andDocument {
                summary("Listing comparison related resources")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<comparisons-related-resource-fetch,comparison related resources>>.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison."),
                )
                pagedQueryParameters()
                pagedResponseFields<ComparisonRelatedResourceRepresentation>(comparisonRelatedResourceResponseFields())
            }

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
            .andDocument {
                summary("Creating comparison related resources")
                description(
                    """
                    A `POST` request creates a new comparison related resource with all the given parameters.
                    The response will be `201 Created` when successful.
                    The comparison related resource (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The comparison to attach the comparison related resource to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created comparison related resource can be fetched from."),
                )
                requestFields<CreateComparisonRelatedResourceRequest>(
                    fieldWithPath("label").description("The label of the comparison related resource."),
                    fieldWithPath("image").description("The url to the image of the comparison related resource. (optional)").optional(),
                    fieldWithPath("url").description("The url of the comparison related resource. (optional)").optional(),
                    fieldWithPath("description").description("The description of the comparison related resource. (optional)").optional(),
                )
                throws(InvalidLabel::class, ComparisonNotFound::class)
            }

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
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:comparison_not_found")

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
            .andDocument {
                summary("Updating comparison related resources")
                description(
                    """
                    A `PUT` request updates an existing comparison related resource with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated comparison related resource (object) can be retrieved by following the URI in the `Location` header field.
                    
                    NOTE: Top level fields that were mandatory when creating the comparison related resource can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated comparison related resource can be fetched from."),
                )
                pathParameters(
                    parameterWithName("id").description("The id of the comparison the comparison related resource belongs to."),
                    parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related resource to update."),
                )
                requestFields<UpdateComparisonRelatedResourceRequest>(
                    fieldWithPath("label").description("The label of the comparison related resource. (optional)").optional(),
                    fieldWithPath("image").description("The url to the image of the comparison related resource. (optional)").optional(),
                    fieldWithPath("url").description("The url of the comparison related resource. (optional)").optional(),
                    fieldWithPath("description").description("The description of the comparison related resource. (optional)").optional(),
                )
                throws(
                    ComparisonRelatedResourceNotModifiable::class,
                    ComparisonNotFound::class,
                    InvalidLabel::class,
                    ComparisonRelatedResourceNotFound::class
                )
            }

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
            .andDocument {
                summary("Deleting comparison related resources")
                description(
                    """
                    A `DELETE` request deletes a comparison related resource by ID.
                    The response will be `204 No Content` when successful.
                    The updated comparison (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the comparison."),
                    parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to delete."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated comparison can be fetched from."),
                )
                throws(ComparisonRelatedResourceNotFound::class, ComparisonRelatedResourceNotModifiable::class)
            }

        verify(exactly = 1) {
            comparisonRelatedResourceService.delete(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = ContributorId(MockUserId.USER)
            )
        }
    }

    private fun createComparisonRelatedResourceRequest() =
        CreateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )

    private fun updateComparisonRelatedResourceRequest() =
        UpdateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )
}
