package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.TemplateBasedResourceSnapshotController.CreateTemplateBasedResourceSnapshotRequest
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateBasedResourceSnapshotV1
import org.orkg.contenttypes.input.TemplateBasedResourceSnapshotUseCases
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplateBasedResourceSnapshot
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        TemplateBasedResourceSnapshotController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        WebMvcConfiguration::class,
        FixedClockConfig::class
    ]
)
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
    fun getSingle() {
        val templateBasedResourceSnapshot = createTemplateBasedResourceSnapshotV1()
        every { templateBasedResourceSnapshotService.findById(templateBasedResourceSnapshot.id) } returns Optional.of(templateBasedResourceSnapshot)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/resources/{id}/snapshots/{snapshotId}", templateBasedResourceSnapshot.resourceId, templateBasedResourceSnapshot.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplateBasedResourceSnapshot()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier the resource the snapshot was created of."),
                        parameterWithName("snapshotId").description("The identifier of the snapshot.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the template based resource snapshot."),
                        timestampFieldWithPath("created_at", "the template based resource snapshot was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this template based resource snapshot."),
                        subsectionWithPath("data").description("The snapshot of the template instance.").optional(),
                        subsectionWithPath("resource_id").description("The id of the root resource of the template instance.").optional(),
                        subsectionWithPath("template_id").description("The id of the template that was used to create the snapshot.").optional(),
                        subsectionWithPath("handle").description("The persistent handle identifier of the snapshot. (optional)").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateBasedResourceSnapshotService.findById(templateBasedResourceSnapshot.id) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a template based resource snapshot, when it is fetched by id and service reports missing template based resource snapshot, then status is 404 NOT FOUND`() {
        val id = SnapshotId("Missing")
        val resourceId = ThingId("R123")
        val exception = TemplateBasedResourceSnapshotNotFound(id)
        every { templateBasedResourceSnapshotService.findById(id) } returns Optional.empty()

        get("/api/resources/{id}/snapshots/{snapshotId}", resourceId, id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/resources/$resourceId/snapshots/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
    }

    @Test
    @DisplayName("Given several template based resource snapshots, when filtering by several parameters, then status is 200 OK and template based resource snapshots are returned")
    fun getPagedWithParameters() {
        every {
            templateBasedResourceSnapshotService.findAllByResourceIdAndTemplateId(any(), any(), any())
        } returns pageOf(createTemplateBasedResourceSnapshotV1())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        val resourceId = ThingId("R123")
        val templateId = ThingId("R456")

        documentedGetRequestTo("/api/resources/{id}/snapshots", resourceId)
            .param("template_id", templateId.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateBasedResourceSnapshot("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier the resource the snapshot was created for."),
                    ),
                    queryParameters(
                        parameterWithName("template_id").description("The id of the template that was used to create the resource snapshot. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateBasedResourceSnapshotService.findAllByResourceIdAndTemplateId(
                pageable = any(),
                resourceId = resourceId,
                templateId = templateId,
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier the resource the snapshot was created of."),
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created TemplateBasedResourceSnapshot can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("template_id").description("The id of the template that will be used for subgraph exploration when creating the snapshot."),
                        fieldWithPath("register_handle").description("Whether to register a persistent https://handle.net/[Handle] identifier. (optional, default: true)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateBasedResourceSnapshotService.create(any()) }
    }

    private fun createTemplateBasedResourceSnapshotRequest() =
        CreateTemplateBasedResourceSnapshotRequest(
            templateId = ThingId("R456"),
            registerHandle = true
        )
}
