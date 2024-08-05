package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaStoneTemplate
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.numberLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.otherLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.resourceTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.stringLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.untypedTemplatePropertyRequest
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRosettaStoneTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedDeleteRequestTo
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        RosettaStoneTemplateController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [RosettaStoneTemplateController::class])
@DisplayName("Given a Rosetta Stone Template controller")
internal class RosettaStoneTemplateControllerUnitTest : RestDocsTest("rosetta-stone-templates") {

    @MockkBean
    private lateinit var templateService: RosettaStoneTemplateUseCases

    @Test
    @DisplayName("Given a template, when it is fetched by id and service succeeds, then status is 200 OK and template is returned")
    fun getSingle() {
        val template = createDummyRosettaStoneTemplate()
        every { templateService.findById(template.id) } returns Optional.of(template)

        documentedGetRequestTo("/api/rosetta-stone/templates/{id}", template.id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneTemplate()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the rosetta stone template to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the rosetta stone template."),
                        fieldWithPath("label").description("The label of the rosetta stone template."),
                        fieldWithPath("description").description("The description of the rosetta stone template."),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the rosetta stone template."),
                        fieldWithPath("target_class").description("The target class of the rosetta stone template."),
                        fieldWithPath("example_usage").description("One or more example sentences that demonstrate the usage of the statement that this template models."),
                        subsectionWithPath("properties").description("The list of properties of the rosetta stone template. See <<template-properties,template properties>> for more information."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone template belongs to."),
                        timestampFieldWithPath("created_at", "the rosetta stone template resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this rosetta stone template."),
                        fieldWithPath("visibility").description("""Visibility of the rosetta stone template. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this rosetta stone template.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a rosetta stone template, when it is fetched by id and service reports missing rosetta stone template, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = RosettaStoneTemplateNotFound(id)
        every { templateService.findById(id) } returns Optional.empty()

        get("/api/rosetta-stone/templates/$id")
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/rosetta-stone/templates/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.findById(id) }
    }

    @Test
    @DisplayName("Given several rosetta stone templates, when they are fetched, then status is 200 OK and rosetta stone templates are returned")
    fun getPaged() {
        val template = createDummyRosettaStoneTemplate()
        every { templateService.findAll(pageable = any()) } returns pageOf(template)

        documentedGetRequestTo("/api/rosetta-stone/templates")
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaStoneTemplate("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.findAll(pageable = any()) }
    }

    @Test
    @DisplayName("Given several rosetta stone templates, when filtering by several parameters, then status is 200 OK and rosetta stone templates are returned")
    fun getPagedWithParameters() {
        val template = createDummyRosettaStoneTemplate()
        val createdBy = template.createdBy
        val q = "example"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED

        every {
            templateService.findAll(
                searchString = any(),
                visibility = visibility,
                createdBy = createdBy,
                pageable = any()
            )
        } returns pageOf(template)

        documentedGetRequestTo("/api/rosetta-stone/templates")
            .param("q", q)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaStoneTemplate("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("q").description("Optional filter for the rosetta stone template label.").optional(),
                        parameterWithName("exact").description("Optional flag for whether label matching should be exact. (default: false)").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED".""").optional(),
                        parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created the rosetta stone template.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateService.findAll(
                searchString = withArg<ExactSearchString> { it.input shouldBe "example" },
                visibility = visibility,
                createdBy = createdBy,
                pageable = any()
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone template create request, when service succeeds, it creates and returns the template")
    fun create() {
        val id = ThingId("R123")
        every { templateService.create(any()) } returns id

        documentedPostRequestTo("/api/rosetta-stone/templates")
            .content(createRosettaStoneTemplateRequest())
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/rosetta-stone/templates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created rosetta stone template  can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the rosetta stone template."),
                        fieldWithPath("description").description("The description of the rosetta stone template."),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the rosetta stone template."),
                        fieldWithPath("example_usage").description("One or more example sentences that demonstrate the usage of the statement that this template models."),
                        subsectionWithPath("properties").description("""The list of properties of the rosetta stone template. The first property defines the subject position of the statement and is required to have a path of `hasSubjectPosition`, must have a minimum cardinality of at least one and is not a literal template property. All other properties define a object position, which must have a path of `hasObjectPosition`. At least one property is required to create a new rosetta stone template. See <<template-properties,template properties>> for more information."""),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone template belongs to."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone template, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun delete() {
        val id = ThingId("R123")
        every { templateService.delete(id, any()) } just runs

        documentedDeleteRequestTo("/api/rosetta-stone/templates/{id}", id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the rosetta stone template to delete.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.delete(id, ContributorId(MockUserId.USER)) }
    }

    private fun createRosettaStoneTemplateRequest() =
        RosettaStoneTemplateController.CreateRosettaStoneTemplateRequest(
            label = "Dummy Rosetta Stone Template Label",
            description = "Some description about the Rosetta Stone Template",
            formattedLabel = "{P32}",
            exampleUsage = "example sentence of the statement",
            properties = listOf(
                untypedTemplatePropertyRequest(),
                stringLiteralTemplatePropertyRequest(),
                numberLiteralTemplatePropertyRequest(),
                otherLiteralTemplatePropertyRequest(),
                resourceTemplatePropertyRequest()
            ),
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            )
        )
}
