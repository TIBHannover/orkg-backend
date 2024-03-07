package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.util.*
import java.util.regex.PatternSyntaxException
import java.util.stream.Stream
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.ResearchProblemNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
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
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [TemplateController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [TemplateController::class])
@DisplayName("Given a Template controller")
internal class TemplateControllerUnitTest : RestDocsTest("templates") {

    @MockkBean
    private lateinit var templateService: TemplateUseCases

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
                        fieldWithPath("properties[].placeholder").description("The placeholder of the property."),
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
                        fieldWithPath("visibility").description("""Visibility of the template. Can be one of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED"."""),
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
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED".""").optional(),
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

    @Test
    @TestWithMockUser
    @DisplayName("Given a template request, when service succeeds, it creates and returns the template")
    fun create() {
        val id = ThingId("R123")
        every { templateService.create(any()) } returns id

        documentedPostRequestTo("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/templates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the template."),
                        fieldWithPath("description").description("The description of the template."),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the template. (optional)").optional(),
                        fieldWithPath("target_class").description("The target class of the template."),
                        fieldWithPath("relations").description("The related resources of the template. This is used for suggestions."),
                        fieldWithPath("relations.research_fields[]").description("The list of research fields the template relates to."),
                        fieldWithPath("relations.research_problems[]").description("The list of research problems the template relates to."),
                        fieldWithPath("relations.predicate").description("The predicate the template relates to."),
                        fieldWithPath("properties[]").description("The property descriptions of the template. They can either be literal properties or resource properties. This is denoted by the `class` (resource) and `datatype` (literal) properties."),
                        fieldWithPath("properties[].label").description("The label of the property."),
                        fieldWithPath("properties[].placeholder").description("The placeholder of the property."),
                        fieldWithPath("properties[].min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality. (optional)").optional(),
                        fieldWithPath("properties[].max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count. (optional)").optional(),
                        fieldWithPath("properties[].pattern").description("The pattern (regular expression) of the property. (optional)").optional(),
                        fieldWithPath("properties[].path").description("The predicate id for the path of the property."),
                        fieldWithPath("properties[].class").description("The class id of the range of the property, indicating a resource property. Mutually exclusive with `datatype`.").optional(),
                        fieldWithPath("properties[].datatype").description("The class id of the datatype of the property, indicating a literal property. Mutually exclusive with `class`.").optional(),
                        fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports target class, or datatype or class range not found, then status is 404 NOT FOUND`() {
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports template already exists for class, then status is 400 BAD REQUEST`() {
        val exception = TemplateAlreadyExistsForClass(ThingId("R123"), ThingId("R456"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports related research field not found, then status is 404 NOT FOUND`() {
        val exception = ResearchFieldNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports related research problem not found, then status is 404 NOT FOUND`() {
        val exception = ResearchProblemNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports related predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports invalid min count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMinCount(-1)
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports invalid max count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMaxCount(-1)
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports invalid cardinality, then status is 400 BAD REQUEST`() {
        val exception = InvalidCardinality(5, 1)
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports invalid pattern, then status is 400 BAD REQUEST`() {
        val exception = InvalidRegexPattern("\\", Exception("Invalid regex pattern"))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports path predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound("P123")
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports organization not found, then status is 404 NOT FOUND`() {
        val exception = OrganizationNotFound(OrganizationId(UUID.randomUUID()))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template request, when service reports observatory not found, then status is 404 NOT FOUND`() {
        val exception = ObservatoryNotFound(ObservatoryId(UUID.randomUUID()))
        every { templateService.create(any()) } throws exception

        post("/api/templates", createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a template property request, when service succeeds, it creates the template property")
    fun createProperty() {
        val templateId = ThingId("R3541")
        val id = ThingId("R123")
        every { templateService.createTemplateProperty(any()) } returns id

        documentedPostRequestTo("/api/templates/{templateId}/properties", templateId)
            .content(createLiteralTemplatePropertyRequest())
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/templates/$templateId")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated template can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the property."),
                        fieldWithPath("placeholder").description("The placeholder of the property."),
                        fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality. (optional)").optional(),
                        fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count. (optional)").optional(),
                        fieldWithPath("pattern").description("The pattern (regular expression) of the property. (optional)").optional(),
                        fieldWithPath("path").description("The predicate id for the path of the property."),
                        fieldWithPath("class").type("String").description("The class id of the range of the property, indicating a resource property. Mutually exclusive with `datatype`.").optional(),
                        fieldWithPath("datatype").type("String").description("The class id of the datatype of the property, indicating a literal property. Mutually exclusive with `class`.").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports datatype or class range not found, then status is 404 NOT FOUND`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports invalid min count, then status is 400 BAD REQUEST`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMinCount(-1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports invalid max count, then status is 400 BAD REQUEST`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMaxCount(-1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports invalid cardinality, then status is 400 BAD REQUEST`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidCardinality(5, 1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports invalid pattern, then status is 400 BAD REQUEST`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidRegexPattern("\\", PatternSyntaxException("Invalid regex pattern", "\\", 1))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports path predicate not found, then status is 404 NOT FOUND`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = PredicateNotFound("P123")
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property request, when service reports template is closed, then status is 400 BAD REQUEST`(
        request: TemplateController.CreateTemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = TemplateClosed(ThingId("P123"))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/$templateId/properties", request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    private fun createTemplateRequest() =
        TemplateController.CreateTemplateRequest(
            label = "Dummy Template Label",
            description = "Some description about the template",
            formattedLabel = "{P32}",
            targetClass = ThingId("targetClass"),
            relations = TemplateController.CreateTemplateRequest.TemplateRelationsDTO(
                researchFields = listOf(ThingId("R20")),
                researchProblems = listOf(ThingId("R21")),
                predicate = ThingId("P22")
            ),
            properties = listOf(
                createLiteralTemplatePropertyRequest(),
                createResourceTemplatePropertyRequest()
            ),
            isClosed = true,
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            )
        )

    companion object {
        @JvmStatic
        fun templatePropertyRequests(): Stream<Arguments> = Stream.of(
            Arguments.of(createLiteralTemplatePropertyRequest()),
            Arguments.of(createResourceTemplatePropertyRequest())
        )

        @JvmStatic
        private fun createLiteralTemplatePropertyRequest() =
            TemplateController.CreateLiteralPropertyRequest(
                label = "literal property label",
                placeholder = "literal property placeholder",
                minCount = 1,
                maxCount = 2,
                pattern = """\d+""",
                path = ThingId("P24"),
                datatype = ThingId("C25"),
            )

        @JvmStatic
        private fun createResourceTemplatePropertyRequest() =
            TemplateController.CreateResourcePropertyRequest(
                label = "resource property label",
                placeholder = "resource property placeholder",
                minCount = 3,
                maxCount = 4,
                pattern = """\w+""",
                path = ThingId("P27"),
                `class` = ThingId("C28"),
            )
    }
}
