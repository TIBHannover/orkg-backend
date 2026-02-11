package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.contenttypes.adapter.input.rest.TemplateBasedResourceSnapshotController.CreateTemplateBasedResourceSnapshotRequest
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.contenttypes.domain.TemplateInstanceNotFound
import org.orkg.contenttypes.domain.TemplateNotApplicable
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateBasedResourceSnapshotV1
import org.orkg.contenttypes.input.TemplateBasedResourceSnapshotUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.templateBasedResourceSnapshotResponseFields
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplateBasedResourceSnapshot
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [TemplateBasedResourceSnapshotController::class, ContentTypeControllerUnitTestConfiguration::class])
@TestPropertySource(
    properties = ["orkg.snapshots.resources.url-templates.frontend=https://orkg.org/resource/{id}/snapshots/{snapshotId}"]
)
@WebMvcTest(controllers = [TemplateBasedResourceSnapshotController::class])
internal class TemplateBasedResourceSnapshotControllerUnitTest : MockMvcBaseTest("template-based-resource-snapshots") {
    @MockkBean
    private lateinit var templateBasedResourceSnapshotService: TemplateBasedResourceSnapshotUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a template based resource snapshot, when it is fetched by id and service succeeds, then status is 200 OK and template based resource snapshot is returned")
    fun findById() {
        val templateBasedResourceSnapshot = createTemplateBasedResourceSnapshotV1()
        every { templateBasedResourceSnapshotService.findById(templateBasedResourceSnapshot.id) } returns Optional.of(templateBasedResourceSnapshot)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/resources/{id}/snapshots/{snapshotId}", templateBasedResourceSnapshot.resourceId, templateBasedResourceSnapshot.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplateBasedResourceSnapshot()
            .andDocument {
                summary("Fetching template based resource snapshots")
                description(
                    """
                    A `GET` request returns a template based resource snapshot.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier the resource the snapshot was created of."),
                    parameterWithName("snapshotId").description("The identifier of the snapshot."),
                )
                responseFields<TemplateBasedResourceSnapshotRepresentation>(templateBasedResourceSnapshotResponseFields())
                throws(TemplateBasedResourceSnapshotNotFound::class)
            }

        verify(exactly = 1) { templateBasedResourceSnapshotService.findById(templateBasedResourceSnapshot.id) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a template based resource snapshot, when it is fetched by id and service reports missing template based resource snapshot, then status is 404 NOT FOUND`() {
        val id = SnapshotId("Missing")
        val resourceId = ThingId("R123")
        every { templateBasedResourceSnapshotService.findById(id) } returns Optional.empty()

        get("/api/resources/{id}/snapshots/{snapshotId}", resourceId, id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_based_resource_snapshot_not_found")

        verify(exactly = 1) { templateBasedResourceSnapshotService.findById(id) }
    }

    @Test
    fun `Given a template based resource snapshot, when it is fetched by id with html accept header, it returns a redirect to the frontend`() {
        val id = ThingId("R123")
        val snapshotId = SnapshotId("1a2b3c")

        get("/api/resources/{id}/snapshots/{snapshotId}", id, snapshotId)
            .accept(TEXT_HTML_VALUE)
            .perform()
            .andExpect(status().isPermanentRedirect)
            .andExpect(header().string("Location", "https://orkg.org/resource/$id/snapshots/$snapshotId"))
    }

    @Test
    @DisplayName("Given several template based resource snapshots, when they are fetched, then status is 200 OK and template based resource snapshot are returned")
    fun getPaged() {
        val resourceId = ThingId("R123")
        every {
            templateBasedResourceSnapshotService.findAllByResourceId(any(), any())
        } returns pageOf(createTemplateBasedResourceSnapshotV1())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/resources/{id}/snapshots", resourceId)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateBasedResourceSnapshot("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateBasedResourceSnapshotService.findAllByResourceId(any(), any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several template based resource snapshots, when filtering by several parameters, then status is 200 OK and template based resource snapshots are returned")
    fun findAll() {
        every {
            templateBasedResourceSnapshotService.findAllByResourceIdAndTemplateId(any(), any(), any())
        } returns pageOf(createTemplateBasedResourceSnapshotV1())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        val resourceId = ThingId("R123")
        val templateId = ThingId("R456")

        documentedGetRequestTo("/api/resources/{id}/snapshots", resourceId)
            .param("template_id", templateId.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateBasedResourceSnapshot("$.content[*]")
            .andDocument {
                summary("Fetching template based resource snapshots")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<template-based-resource-snapshots-fetch,template based resource snapshots>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier the resource the snapshot was created for."),
                )
                pagedQueryParameters(
                    parameterWithName("template_id").description("The id of the template that was used to create the resource snapshot. (optional)").optional(),
                )
                pagedResponseFields<TemplateBasedResourceSnapshotRepresentation>(templateBasedResourceSnapshotResponseFields())
            }

        verify(exactly = 1) {
            templateBasedResourceSnapshotService.findAllByResourceIdAndTemplateId(
                pageable = any(),
                resourceId = resourceId,
                templateId = templateId,
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a template based resource snapshot create request, when service succeeds, it creates and returns the template based resource snapshot")
    fun create() {
        val id = SnapshotId("a1b2c3")
        val resourceId = ThingId("R123")
        every { templateBasedResourceSnapshotService.create(any()) } returns id

        documentedPostRequestTo("/api/resources/{id}/snapshots", resourceId)
            .content(createTemplateBasedResourceSnapshotRequest())
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/resources/$resourceId/snapshots/$id")))
            .andDocument {
                summary("Creating template based resource snapshots")
                description(
                    """
                    A `POST` request creates a new template based resource snapshot.
                    The response will be `201 Created` when successful.
                    The template based resource snapshot (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier the resource the snapshot was created of."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created TemplateBasedResourceSnapshot can be fetched from."),
                )
                requestFields<CreateTemplateBasedResourceSnapshotRequest>(
                    fieldWithPath("template_id").description("The id of the template that will be used for subgraph exploration when creating the snapshot."),
                    fieldWithPath("register_handle").description("Whether to register a persistent https://handle.net/[Handle] identifier. (optional, default: true)").optional(),
                )
                throws(
                    ResourceNotFound::class,
                    TemplateNotFound::class,
                    TemplateNotApplicable::class,
                    TemplateInstanceNotFound::class,
                    ServiceUnavailable::class,
                )
            }

        verify(exactly = 1) { templateBasedResourceSnapshotService.create(any()) }
    }

    private fun createTemplateBasedResourceSnapshotRequest() =
        CreateTemplateBasedResourceSnapshotRequest(
            templateId = ThingId("R456"),
            registerHandle = true
        )
}
