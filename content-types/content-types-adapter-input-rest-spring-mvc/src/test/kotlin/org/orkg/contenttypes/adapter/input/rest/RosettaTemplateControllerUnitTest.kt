package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.RosettaTemplateNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyRosettaTemplate
import org.orkg.contenttypes.input.RosettaTemplateUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRosettaTemplate
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [RosettaTemplateController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [RosettaTemplateController::class])
@DisplayName("Given a Rosetta Template controller")
internal class RosettaTemplateControllerUnitTest : RestDocsTest("rosetta-templates") {

    @MockkBean
    private lateinit var templateService: RosettaTemplateUseCases

    @Test
    @DisplayName("Given a template, when it is fetched by id and service succeeds, then status is 200 OK and template is returned")
    fun getSingle() {
        val template = createDummyRosettaTemplate()
        every { templateService.findById(template.id) } returns Optional.of(template)

        documentedGetRequestTo("/api/rosetta/templates/{id}", template.id)
            .accept(ROSETTA_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaTemplate()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the rosetta template to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the rosetta template."),
                        fieldWithPath("label").description("The label of the rosetta template."),
                        fieldWithPath("description").description("The description of the rosetta template."),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the rosetta template."),
                        fieldWithPath("target_class").description("The target class of the rosetta template."),
                        PayloadDocumentation.subsectionWithPath("properties").description("The list of properties of the rosetta template. See <<template-properties,template properties>> for more information."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta template belongs to."),
                        timestampFieldWithPath("created_at", "the rosetta template resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this rosetta template."),
                        fieldWithPath("visibility").description("""Visibility of the rosetta template. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this rosetta template.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a rosetta template, when it is fetched by id and service reports missing rosetta template, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = RosettaTemplateNotFound(id)
        every { templateService.findById(id) } returns Optional.empty()

        get("/api/rosetta/templates/$id")
            .accept(ROSETTA_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/rosetta/templates/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.findById(id) }
    }

    @Test
    @DisplayName("Given several rosetta templates, when they are fetched, then status is 200 OK and rosetta templates are returned")
    fun getPaged() {
        val template = createDummyRosettaTemplate()
        every { templateService.findAll(pageable = any()) } returns pageOf(template)

        get("/api/rosetta/templates")
            .accept(ROSETTA_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaTemplate("$.content[*]")

        verify(exactly = 1) { templateService.findAll(pageable = any()) }
    }

    @Test
    fun `Given several rosetta templates, when they are fetched, then status is 200 OK and rosetta templates are returned`() {
        val template = createDummyRosettaTemplate()
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

        documentedGetRequestTo("/api/rosetta/templates")
            .param("q", q)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .accept(ROSETTA_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaTemplate("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("q").description("Optional filter for the rosetta template label.").optional(),
                        parameterWithName("exact").description("Optional flag for whether label matching should be exact. (default: false)").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED".""").optional(),
                        parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created the rosetta template.").optional()
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
}
