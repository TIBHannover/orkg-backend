package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import org.orkg.contenttypes.testing.fixtures.createDummyTemplate
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [TemplateController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [TemplateController::class])
@DisplayName("Given a Template controller")
internal class TemplateControllerUnitTest : RestDocsTest("templates") {

    @MockkBean
    private lateinit var templateService: TemplateUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @Test
    @DisplayName("Given a template, when it is fetched by id and service succeeds, then status is 200 OK and template is returned")
    fun getSingle() {
        val template = createDummyTemplate()
        every { templateService.findById(template.id) } returns Optional.of(template)

        documentedGetRequestTo("/api/templates/{id}", template.id)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplate()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the template to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the template."),
                        fieldWithPath("label").description("The label of the template."),
                        fieldWithPath("description").description("The description of the template."),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the template."),
                        fieldWithPath("target_class").description("The target class of the template."),
                        fieldWithPath("relations").description("The relations class of the template. Used for suggestions."),
                        fieldWithPath("relations.research_fields[]").description("The research fields that this template relates to."),
                        fieldWithPath("relations.research_fields[].id").description("The id of the research field that this template relates to."),
                        fieldWithPath("relations.research_fields[].label").description("The label of the research field that this template relates to."),
                        fieldWithPath("relations.research_problems[]").description("The research problems that this template relates to."),
                        fieldWithPath("relations.research_problems[].id").description("The id of the research problem that this template relates to."),
                        fieldWithPath("relations.research_problems[].label").description("The label of the research problem that this template relates to."),
                        fieldWithPath("relations.predicate").description("The predicate that this template relates to. (optional)").optional(),
                        fieldWithPath("relations.predicate.id").description("The id of the predicate that this template relates to.").optional(),
                        fieldWithPath("relations.predicate.label").description("The label of the predicate that this template relates to.").optional(),
                        fieldWithPath("properties").description("The properties of the template."),
                        fieldWithPath("properties[].id").description("The id of the property."),
                        fieldWithPath("properties[].label").description("The label of the property."),
                        fieldWithPath("properties[].order").description("The order of the property."),
                        fieldWithPath("properties[].min_count").description("The minimum cardinality of the property."),
                        fieldWithPath("properties[].max_count").description("The maximum cardinality of the property."),
                        fieldWithPath("properties[].pattern").description("The pattern (regex) of the property."),
                        fieldWithPath("properties[].path").description("The predicate path of the property."),
                        fieldWithPath("properties[].path.id").description("The id of the predicate."),
                        fieldWithPath("properties[].path.label").description("The label of the predicate."),
                        fieldWithPath("properties[].datatype").description("The data type of the property, if the property is a literal property.").optional(),
                        timestampFieldWithPath("properties[].created_at", "the property was created."),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("properties[].created_by").description("The UUID of the user or service who created this property."),
                        fieldWithPath("properties[].datatype.id").description("The id of the data type.").optional(),
                        fieldWithPath("properties[].datatype.label").description("The label of the data type.").optional(),
                        fieldWithPath("properties[].class").description("The class range of the property, if the property is a literal property.").optional(),
                        fieldWithPath("properties[].class.id").description("The id of the class.").optional(),
                        fieldWithPath("properties[].class.label").description("The label of the class.").optional(),
                        fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
                        timestampFieldWithPath("created_at", "the template resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this template."),
                        fieldWithPath("visibility").description("""Visibility of the template. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this template.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a template, when it is fetched by id and service reports missing template, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = TemplateNotFound(id)
        every { templateService.findById(id) } returns Optional.empty()

        get("/api/templates/$id")
            .accept(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.findById(id) }
    }

    @Test
    @DisplayName("Given several templates, when they are fetched, then status is 200 OK and templates are returned")
    fun getPaged() {
        val template = createDummyTemplate()
        every { templateService.findAll(pageable = any()) } returns pageOf(template)

        documentedGetRequestTo("/api/templates")
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplate("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("q").description("Optional filter for the template label.").optional(),
                        parameterWithName("exact").description("Optional flag for whether label matching should be exact. (default: false)").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "listed", "featured", "unlisted" or "deleted".""").optional(),
                        parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created the template.").optional(),
                        parameterWithName("research_field").description("Optional filter for related research field id.").optional(),
                        parameterWithName("research_problem").description("Optional filter for related research problem id.").optional(),
                        parameterWithName("target_class").description("Optional filter for the target class.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.findAll(pageable = any()) }
    }

    @Test
    fun `Given several templates, when they are fetched, then status is 200 OK and templates are returned`() {
        val template = createDummyTemplate()
        val createdBy = template.createdBy
        every {
            templateService.findAll(
                searchString = any(),
                visibility = VisibilityFilter.ALL_LISTED,
                createdBy = createdBy,
                researchField = ThingId("R11"),
                researchProblem = ThingId("R12"),
                targetClass = ThingId("C123"),
                pageable = any()
            )
        } returns pageOf(template)

        get("/api/templates?q=example&exact=true&visibility=ALL_LISTED&created_by=$createdBy&research_field=R11&research_problem=R12&target_class=C123")
            .accept(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplate("$.content[*]")

        verify(exactly = 1) {
            templateService.findAll(
                searchString = withArg<ExactSearchString> { it.input shouldBe "example" },
                visibility = VisibilityFilter.ALL_LISTED,
                createdBy = createdBy,
                researchField = ThingId("R11"),
                researchProblem = ThingId("R12"),
                targetClass = ThingId("C123"),
                pageable = any()
            )
        }
    }
}
