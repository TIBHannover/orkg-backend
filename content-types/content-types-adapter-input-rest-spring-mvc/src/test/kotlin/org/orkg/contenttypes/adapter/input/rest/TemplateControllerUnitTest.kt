package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.PatternSyntaxException
import java.util.stream.Stream
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateNumberLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateOtherLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateResourcePropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateStringLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateUntypedPropertyCommand
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.numberLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.otherLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.resourceTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.stringLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.untypedTemplatePropertyRequest
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.ResearchProblemNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        TemplateController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class
    ]
)
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
                        fieldWithPath("target_class.id").description("The id of the target class."),
                        fieldWithPath("target_class.label").description("The label of the target class."),
                        fieldWithPath("target_class.uri").description("The uri of the target class. (optional)").optional(),
                        fieldWithPath("target_class._class").description("Indicates which type of entity was returned. Always has the value `class_ref`."),
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
                        subsectionWithPath("properties").description("The list of properties of the template. See <<template-properties,template properties>> for more information."),
                        fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the template resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
                        timestampFieldWithPath("created_at", "the template resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this template."),
                        fieldWithPath("visibility").description("""Visibility of the template. Can be one of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this template.").optional(),
                        fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `template`."),
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

        get("/api/templates/{id}", id)
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
        every {
            templateService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummyTemplate())

        documentedGetRequestTo("/api/templates")
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplate("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several templates, when filtering by several parameters, then status is 200 OK and templates are returned")
    fun getPagedWithParameters() {
        val template = createDummyTemplate()
        every { templateService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(template)

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val researchProblemId = ThingId("R789")
        val targetClass = ThingId("targetClass")

        documentedGetRequestTo("/api/templates")
            .param("q", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("research_problem", researchProblemId.value)
            .param("target_class", targetClass.value)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplate("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label of the template. (optional)."),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED". (optional)"""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created the template. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned template can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned template can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the template belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the template belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("research_problem").description("Filter for related research problem id. (optional)"),
                        parameterWithName("target_class").description("Filter for the target class. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                researchProblem = researchProblemId,
                targetClass = targetClass
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a template create request, when service succeeds, it creates and returns the template")
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
                        headerWithName("Location").description("The uri path where the newly created template can be fetched from.")
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
                        subsectionWithPath("properties").description("The list of properties of the template. See <<template-properties,template properties>> for more information."),
                        fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the template resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional)""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports target class, or datatype or class range not found, then status is 404 NOT FOUND`() {
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports template already exists for class, then status is 400 BAD REQUEST`() {
        val exception = TemplateAlreadyExistsForClass(ThingId("R123"), ThingId("R456"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports related research field not found, then status is 404 NOT FOUND`() {
        val exception = ResearchFieldNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports related research problem not found, then status is 404 NOT FOUND`() {
        val exception = ResearchProblemNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports related predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound(ThingId("R22"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports invalid min count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMinCount(-1)
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports invalid max count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMaxCount(-1)
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports invalid cardinality, then status is 400 BAD REQUEST`() {
        val exception = InvalidCardinality(5, 1)
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports invalid pattern, then status is 400 BAD REQUEST`() {
        val exception = InvalidRegexPattern("\\", Exception("Invalid regex pattern"))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports path predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound("P123")
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports organization not found, then status is 404 NOT FOUND`() {
        val exception = OrganizationNotFound(OrganizationId(UUID.randomUUID()))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    fun `Given a template create request, when service reports observatory not found, then status is 404 NOT FOUND`() {
        val exception = ObservatoryNotFound(ObservatoryId(UUID.randomUUID()))
        every { templateService.create(any()) } throws exception

        post("/api/templates")
            .content(objectMapper.writeValueAsString(createTemplateRequest()))
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
    @DisplayName("Given a template update request, when service succeeds, it updates the template")
    fun update() {
        val id = ThingId("R123")
        every { templateService.update(any()) } just runs

        documentedPutRequestTo("/api/templates/{id}", id)
            .content(updateTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/templates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated template can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the template. (optional)"),
                        fieldWithPath("description").description("The description of the template. (optional)"),
                        fieldWithPath("formatted_label").description("The formatted label pattern of the template.").optional(),
                        fieldWithPath("target_class").description("The target class of the template. (optional)"),
                        fieldWithPath("relations").description("The related resources of the template. This is used for suggestions. (optional)"),
                        fieldWithPath("relations.research_fields[]").description("The list of research fields the template relates to."),
                        fieldWithPath("relations.research_problems[]").description("The list of research problems the template relates to."),
                        fieldWithPath("relations.predicate").description("The predicate the template relates to."),
                        subsectionWithPath("properties").description("The list of updated properties of the template (optional). See <<template-properties,template properties>> for more information."),
                        fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to. (optional)"),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to. (optional)"),
                        fieldWithPath("extraction_method").description("""The updated method used to extract the template resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional)""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { templateService.update(any()) }
    }

    private inline fun <reified T : CreateCommand> createProperty(
        request: TemplatePropertyRequest,
        additionalRequestFieldDescriptors: List<FieldDescriptor> = emptyList()
    ) {
        val templateId = ThingId("R3541")
        val id = ThingId("R123")
        every { templateService.createTemplateProperty(any()) } returns id

        documentedPostRequestTo("/api/templates/{id}/properties", templateId)
            .content(request)
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
                        fieldWithPath("placeholder").description("The placeholder of the property. (optional)"),
                        fieldWithPath("description").description("The description of the property. (optional)"),
                        fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality. (optional)").optional(),
                        fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count. (optional)").optional(),
                        fieldWithPath("path").description("The predicate id for the path of the property."),
                    ).and(additionalRequestFieldDescriptors)
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateService.createTemplateProperty(withArg {
                it.shouldBeInstanceOf<T>()
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an untyped template property create request, when service succeeds, it creates the template property")
    fun createUntypedProperty() = createProperty<CreateUntypedPropertyCommand>(untypedTemplatePropertyRequest())

    @Test
    @TestWithMockUser
    @DisplayName("Given a string literal template property create request, when service succeeds, it creates the template property")
    fun createStringLiteralProperty() = createProperty<CreateStringLiteralPropertyCommand>(
        stringLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("pattern").description("The pattern (regular expression) of the property. (optional)").optional(),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be "String".""")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a number literal template property create request, when service succeeds, it creates the template property")
    fun createNumberLiteralProperty() = createProperty<CreateNumberLiteralPropertyCommand>(
        numberLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("min_inclusive").description("The minimum value (inclusive) that the number can have (optional).").optional(),
            fieldWithPath("max_inclusive").description("The maximum value (inclusive) that the number can have (optional).").optional(),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be either of "Integer", "Decimal" or "Float".""")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a literal template property create request, when service succeeds, it creates the template property")
    fun createOtherLiteralProperty() = createProperty<CreateOtherLiteralPropertyCommand>(
        otherLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("datatype").type("String").description("The class id of the datatype of the property, indicating a literal property.")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource template property create request, when service succeeds, it creates the template property")
    fun createResourceProperty() = createProperty<CreateResourcePropertyCommand>(
        resourceTemplatePropertyRequest(),
        listOf(
            fieldWithPath("class").type("String").description("The class id of the range of the property, indicating a resource property.")
        )
    )

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports datatype or class range not found, then status is 404 NOT FOUND`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports invalid min count, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMinCount(-1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports invalid max count, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMaxCount(-1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports invalid cardinality, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidCardinality(5, 1)
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports invalid pattern, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidRegexPattern("\\", PatternSyntaxException("Invalid regex pattern", "\\", 1))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports path predicate not found, then status is 404 NOT FOUND`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = PredicateNotFound("P123")
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
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
    fun `Given a template property create request, when service reports template is closed, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest
    ) {
        val templateId = ThingId("R123")
        val exception = TemplateClosed(ThingId("P123"))
        every { templateService.createTemplateProperty(any()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(objectMapper.writeValueAsString(request))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/properties"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { templateService.createTemplateProperty(any()) }
    }

    private inline fun <reified T : UpdateCommand> updateProperty(
        request: TemplatePropertyRequest,
        additionalRequestFieldDescriptors: List<FieldDescriptor> = emptyList()
    ) {
        val templateId = ThingId("R3541")
        val id = ThingId("R123")
        every { templateService.updateTemplateProperty(any()) } just runs

        documentedPutRequestTo("/api/templates/{templateId}/properties/{propertyId}", templateId, id)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/templates/$templateId")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated template can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the property."),
                        fieldWithPath("placeholder").description("The placeholder of the property."),
                        fieldWithPath("description").description("The description of the property."),
                        fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality.").optional(),
                        fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count.").optional(),
                        fieldWithPath("path").description("The predicate id for the path of the property."),
                    ).and(additionalRequestFieldDescriptors)
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            templateService.updateTemplateProperty(withArg {
                it.shouldBeInstanceOf<T>()
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an untyped template property update request, when service succeeds, it updates the template property")
    fun updateUntypedProperty() = updateProperty<UpdateUntypedPropertyCommand>(untypedTemplatePropertyRequest())

    @Test
    @TestWithMockUser
    @DisplayName("Given a string literal template property update request, when service succeeds, it updates the template property")
    fun updateStringLiteralProperty() = updateProperty<UpdateStringLiteralPropertyCommand>(
        stringLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("pattern").description("The pattern (regular expression) of the property."),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be "String".""")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a number literal template property update request, when service succeeds, it updates the template property")
    fun updateNumberLiteralProperty() = updateProperty<UpdateNumberLiteralPropertyCommand>(
        numberLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("min_inclusive").description("The minimum value (inclusive) that the number can have."),
            fieldWithPath("max_inclusive").description("The maximum value (inclusive) that the number can have."),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be either of "Integer", "Decimal" or "Float".""")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a literal template property update request, when service succeeds, it updates the template property")
    fun updateOtherLiteralProperty() = updateProperty<UpdateOtherLiteralPropertyCommand>(
        otherLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("datatype").type("String").description("The class id of the datatype of the property, indicating a literal property.")
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource template property update request, when service succeeds, it updates the template property")
    fun updateResourceProperty() = updateProperty<UpdateResourcePropertyCommand>(
        resourceTemplatePropertyRequest(),
        listOf(
            fieldWithPath("class").type("String").description("The class id of the range of the property, indicating a resource property.")
        )
    )

    private fun createTemplateRequest() =
        TemplateController.CreateTemplateRequest(
            label = "Dummy Template Label",
            description = "Some description about the template",
            formattedLabel = "{P32}",
            targetClass = ThingId("targetClass"),
            relations = TemplateController.TemplateRelationsDTO(
                researchFields = listOf(ThingId("R20")),
                researchProblems = listOf(ThingId("R21")),
                predicate = ThingId("P22")
            ),
            properties = listOf(
                untypedTemplatePropertyRequest(),
                stringLiteralTemplatePropertyRequest(),
                numberLiteralTemplatePropertyRequest(),
                otherLiteralTemplatePropertyRequest(),
                resourceTemplatePropertyRequest()
            ),
            isClosed = true,
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            )
        )

    private fun updateTemplateRequest() =
        TemplateController.UpdateTemplateRequest(
            label = "Dummy Template Label",
            description = "Some description about the template",
            formattedLabel = "{P32}",
            targetClass = ThingId("targetClass"),
            relations = TemplateController.TemplateRelationsDTO(
                researchFields = listOf(ThingId("R20")),
                researchProblems = listOf(ThingId("R21")),
                predicate = ThingId("P22")
            ),
            properties = listOf(
                untypedTemplatePropertyRequest(),
                stringLiteralTemplatePropertyRequest(),
                numberLiteralTemplatePropertyRequest(),
                otherLiteralTemplatePropertyRequest(),
                resourceTemplatePropertyRequest()
            ),
            isClosed = true,
            observatories = listOf(
                ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
            ),
            organizations = listOf(
                OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )

    companion object {
        @JvmStatic
        fun templatePropertyRequests(): Stream<Arguments> = Stream.of(
            Arguments.of(untypedTemplatePropertyRequest()),
            Arguments.of(stringLiteralTemplatePropertyRequest()),
            Arguments.of(numberLiteralTemplatePropertyRequest()),
            Arguments.of(otherLiteralTemplatePropertyRequest()),
            Arguments.of(resourceTemplatePropertyRequest())
        )
    }
}
