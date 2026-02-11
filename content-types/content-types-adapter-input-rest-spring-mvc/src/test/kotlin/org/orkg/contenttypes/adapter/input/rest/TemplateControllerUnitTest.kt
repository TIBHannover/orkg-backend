package org.orkg.contenttypes.adapter.input.rest

import com.epages.restdocs.apispec.ParameterType
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
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
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.TemplateController.CreateTemplateRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.UpdateTemplateRequest
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDataType
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.ResearchProblemNotFound
import org.orkg.contenttypes.domain.TemplateAlreadyExistsForClass
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.UnrelatedTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateNumberLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateOtherLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateResourcePropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateStringLiteralPropertyCommand
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase.CreateUntypedPropertyCommand
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateNumberLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateOtherLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateResourcePropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateStringLiteralPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase.UpdateUntypedPropertyCommand
import org.orkg.contenttypes.input.UpdateTemplateUseCase
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.numberLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.otherLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.resourceTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.stringLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.templateResponseFields
import org.orkg.contenttypes.input.testing.fixtures.untypedTemplatePropertyRequest
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID
import java.util.regex.PatternSyntaxException
import java.util.stream.Stream
import kotlin.reflect.KClass

@ContextConfiguration(classes = [TemplateController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [TemplateController::class])
internal class TemplateControllerUnitTest : MockMvcBaseTest("templates") {
    @MockkBean
    private lateinit var templateService: TemplateUseCases

    @Test
    @DisplayName("Given a template, when it is fetched by id and service succeeds, then status is 200 OK and template is returned")
    fun findById() {
        val template = createTemplate()
        every { templateService.findById(template.id) } returns Optional.of(template)

        documentedGetRequestTo("/api/templates/{id}", template.id)
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplate()
            .andDocument {
                summary("Fetching templates")
                description(
                    """
                    A `GET` request provides information about a template.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the template to retrieve."),
                )
                responseFields<TemplateRepresentation>(templateResponseFields())
                throws(TemplateNotFound::class)
            }

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a template, when it is fetched by id and service reports missing template, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { templateService.findById(id) } returns Optional.empty()

        get("/api/templates/{id}", id)
            .accept(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_not_found")

        verify(exactly = 1) { templateService.findById(id) }
    }

    @Test
    @DisplayName("Given several templates, when they are fetched, then status is 200 OK and templates are returned")
    fun getPaged() {
        every {
            templateService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createTemplate())

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
    fun findAll() {
        val template = createTemplate()
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
            .andDocument {
                summary("Listing templates")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<templates-fetch,templates>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label of the template. (optional).").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").type(ParameterType.BOOLEAN).optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created the template. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned template can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned template can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the template belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the template belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").type(ParameterType.BOOLEAN).optional(),
                    parameterWithName("research_problem").description("Filter for related research problem id. (optional)").optional(),
                    parameterWithName("target_class").description("Filter for the target class. (optional)").optional(),
                )
                pagedResponseFields<TemplateRepresentation>(templateResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } returns id

        documentedPostRequestTo("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/templates/$id")))
            .andDocument {
                summary("Creating templates")
                description(
                    """
                    A `POST` request creates a new template with all the given parameters.
                    The response will be `201 Created` when successful.
                    The template (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created template can be fetched from."),
                )
                requestFields<CreateTemplateRequest>(
                    fieldWithPath("label").description("The label of the template."),
                    fieldWithPath("description").description("The description of the template. (optional)").optional(),
                    fieldWithPath("formatted_label").description("The formatted label pattern of the template. (optional)").optional(),
                    fieldWithPath("target_class").description("The id of target class of the template."),
                    fieldWithPath("relations").description("The related resources of the template. This is used for suggestions."),
                    fieldWithPath("relations.research_fields[]").description("The list of research fields the template relates to."),
                    fieldWithPath("relations.research_problems[]").description("The list of research problems the template relates to."),
                    fieldWithPath("relations.predicate").description("The predicate the template relates to. (optional)").optional(),
                    subsectionWithPath("properties").description("The list of properties of the template. See <<template-properties,template properties>> for more information."),
                    fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
                    fieldWithPath("extraction_method").description("""The method used to extract the template resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional()
                )
                throws(
                    InvalidLabel::class,
                    InvalidDescription::class,
                    ClassNotFound::class,
                    TemplateAlreadyExistsForClass::class,
                    ResearchFieldNotFound::class,
                    ResearchProblemNotFound::class,
                    PredicateNotFound::class,
                    InvalidMinCount::class,
                    InvalidMaxCount::class,
                    InvalidCardinality::class,
                    InvalidDataType::class,
                    InvalidRegexPattern::class,
                    InvalidBounds::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                )
            }

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports target class, or datatype or class range not found, then status is 404 NOT FOUND`() {
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports template already exists for class, then status is 400 BAD REQUEST`() {
        val exception = TemplateAlreadyExistsForClass(ThingId("R123"), ThingId("R456"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_already_exists_for_class")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports related research field not found, then status is 404 NOT FOUND`() {
        val exception = ResearchFieldNotFound(ThingId("R22"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:research_field_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports related research problem not found, then status is 404 NOT FOUND`() {
        val exception = ResearchProblemNotFound(ThingId("R22"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:research_problem_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports related predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound(ThingId("R22"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports invalid min count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMinCount(-1)
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_min_count")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports invalid max count, then status is 400 BAD REQUEST`() {
        val exception = InvalidMaxCount(-1)
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_max_count")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports invalid cardinality, then status is 400 BAD REQUEST`() {
        val exception = InvalidCardinality(5, 1)
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_cardinality")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports invalid pattern, then status is 400 BAD REQUEST`() {
        val exception = InvalidRegexPattern("\\", Exception("Invalid regex pattern"))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_regex_pattern")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports path predicate not found, then status is 404 NOT FOUND`() {
        val exception = PredicateNotFound("P123")
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports organization not found, then status is 404 NOT FOUND`() {
        val exception = OrganizationNotFound(OrganizationId(UUID.randomUUID()))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a template create request, when service reports observatory not found, then status is 404 NOT FOUND`() {
        val exception = ObservatoryNotFound(ObservatoryId(UUID.randomUUID()))
        every { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) } throws exception

        post("/api/templates")
            .content(createTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplateUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a template update request, when service succeeds, it updates the template")
    fun update() {
        val id = ThingId("R123")
        every { templateService.update(any<UpdateTemplateUseCase.UpdateCommand>()) } just runs

        documentedPutRequestTo("/api/templates/{id}", id)
            .content(updateTemplateRequest())
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/templates/$id")))
            .andDocument {
                summary("Updating templates")
                description(
                    """
                    A `PUT` request updates an existing template with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated template (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. All fields at the top level in the request can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    2. The same rules as for <<resources-edit,updating resources>> apply when updating the visibility of a template.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the template."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated template can be fetched from."),
                )
                requestFields<UpdateTemplateRequest>(
                    fieldWithPath("label").description("The label of the template. (optional)").optional(),
                    fieldWithPath("description").description("The description of the template. (optional)").optional(),
                    fieldWithPath("formatted_label").description("The formatted label pattern of the template. (optional)").optional(),
                    fieldWithPath("target_class").description("The id of target class of the template. (optional)").optional(),
                    fieldWithPath("relations").description("The related resources of the template. This is used for suggestions. (optional)").optional(),
                    fieldWithPath("relations.research_fields[]").description("The list of research fields the template relates to."),
                    fieldWithPath("relations.research_problems[]").description("The list of research problems the template relates to."),
                    fieldWithPath("relations.predicate").description("The predicate the template relates to. (optional)").optional(),
                    subsectionWithPath("properties").description("The list of updated properties of the template (optional). See <<template-properties,template properties>> for more information. (optional)").optional(),
                    fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified. (optional) (optional)").optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").description("""The updated method used to extract the template resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                    fieldWithPath("visibility").description("The updated visibility of the template. Can be one of $allowedVisibilityValues. (optional)").optional()
                )
                throws(
                    TemplateNotFound::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class,
                    ClassNotFound::class,
                    TemplateAlreadyExistsForClass::class,
                    ResearchFieldNotFound::class,
                    ResearchProblemNotFound::class,
                    PredicateNotFound::class,
                    InvalidMinCount::class,
                    InvalidMaxCount::class,
                    InvalidCardinality::class,
                    InvalidDataType::class,
                    InvalidRegexPattern::class,
                    InvalidBounds::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                )
            }

        verify(exactly = 1) { templateService.update(any<UpdateTemplateUseCase.UpdateCommand>()) }
    }

    private inline fun <reified T : CreateTemplatePropertyUseCase.CreateCommand> createProperty(
        description: String,
        request: TemplatePropertyRequest,
        additionalRequestFieldDescriptors: List<FieldDescriptor> = emptyList(),
        additionalThrowables: Set<KClass<out Throwable>> = emptySet(),
    ) {
        val templateId = ThingId("R3541")
        val id = ThingId("R123")
        every { templateService.create(any<T>()) } returns id

        documentedPostRequestTo("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/templates/$templateId")))
            .andDocument {
                summary("Creating template properties")
                description(description)
                pathParameters(
                    parameterWithName("id").description("The identifier of the template."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated template can be fetched from."),
                )
                requestFields(
                    request::class,
                    fieldWithPath("label").description("The label of the property."),
                    fieldWithPath("placeholder").description("The placeholder of the property. (optional)").optional(),
                    fieldWithPath("description").description("The description of the property. (optional)").optional(),
                    fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality. (optional)").optional(),
                    fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count. (optional)").optional(),
                    fieldWithPath("path").description("The predicate id for the path of the property."),
                    *additionalRequestFieldDescriptors.toTypedArray(),
                )
                throws(
                    TemplateNotFound::class,
                    TemplateClosed::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidMinCount::class,
                    InvalidMaxCount::class,
                    InvalidCardinality::class,
                    PredicateNotFound::class,
                    *additionalThrowables.toTypedArray(),
                )
            }

        verify(exactly = 1) {
            templateService.create(
                withArg<T> {
                    it.shouldBeInstanceOf<T>()
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an untyped template property create request, when service succeeds, it creates the template property")
    fun createProperty_untyped() = createProperty<CreateUntypedPropertyCommand>(
        """
        A `POST` request creates a new template property without any type constraints.
        The response will be `201 Created` when successful.
        The updated template (object) can be retrieved by following the URI in the `Location` header field.
        """,
        untypedTemplatePropertyRequest(),
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a string literal template property create request, when service succeeds, it creates the template property")
    fun createProperty_stringLiteral() = createProperty<CreateStringLiteralPropertyCommand>(
        """
        A `POST` request creates a new string template property, with an optional `pattern` constraint.
        The response will be `201 Created` when successful.
        The updated template (object) can be retrieved by following the URI in the `Location` header field.
        """,
        stringLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("pattern").description("The pattern (regular expression) of the property. (optional)").optional(),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be "String".""")
        ),
        setOf(
            InvalidDataType::class,
            InvalidRegexPattern::class,
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a number literal template property create request, when service succeeds, it creates the template property")
    fun createProperty_numberLiteral() = createProperty<CreateNumberLiteralPropertyCommand>(
        """
        A `POST` request creates a new number template property, with optional boundary constraints.
        The response will be `201 Created` when successful.
        The updated template (object) can be retrieved by following the URI in the `Location` header field.
        """,
        numberLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("min_inclusive").description("The minimum value (inclusive) that the number can have (optional).").optional(),
            fieldWithPath("max_inclusive").description("The maximum value (inclusive) that the number can have (optional).").optional(),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be either of "Integer", "Decimal" or "Float".""")
        ),
        setOf(
            InvalidDataType::class,
            InvalidBounds::class,
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a literal template property create request, when service succeeds, it creates the template property")
    fun createProperty_otherLiteral() = createProperty<CreateOtherLiteralPropertyCommand>(
        """
        A `POST` request creates a new literal template property for the given datatype, without any additional constraints.
        The response will be `201 Created` when successful.
        The updated template (object) can be retrieved by following the URI in the `Location` header field.
        """,
        otherLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("datatype").type("String").description("The class id of the datatype of the property, indicating a literal property.")
        ),
        setOf(
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource template property create request, when service succeeds, it creates the template property")
    fun createProperty_resource() = createProperty<CreateResourcePropertyCommand>(
        """
        A `POST` request creates a new resource template property for the given datatype, without any additional constraints.
        The response will be `201 Created` when successful.
        The updated template (object) can be retrieved by following the URI in the `Location` header field.
        """,
        resourceTemplatePropertyRequest(),
        listOf(
            fieldWithPath("class").type("String").description("The class id of the range of the property, indicating a resource property.")
        ),
        setOf(
            ClassNotFound::class,
        )
    )

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports datatype or class range not found, then status is 404 NOT FOUND`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = ClassNotFound.withThingId(ThingId("invalid"))
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports invalid min count, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMinCount(-1)
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_min_count")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports invalid max count, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidMaxCount(-1)
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_max_count")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports invalid cardinality, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidCardinality(5, 1)
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_cardinality")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports invalid pattern, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = InvalidRegexPattern("\\", PatternSyntaxException("Invalid regex pattern", "\\", 1))
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_regex_pattern")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports path predicate not found, then status is 404 NOT FOUND`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = PredicateNotFound("P123")
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    @ParameterizedTest
    @MethodSource("templatePropertyRequests")
    @TestWithMockUser
    fun `Given a template property create request, when service reports template is closed, then status is 400 BAD REQUEST`(
        request: TemplatePropertyRequest,
    ) {
        val templateId = ThingId("R123")
        val exception = TemplateClosed(ThingId("P123"))
        every { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) } throws exception

        post("/api/templates/{id}/properties", templateId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_closed")

        verify(exactly = 1) { templateService.create(any<CreateTemplatePropertyUseCase.CreateCommand>()) }
    }

    private inline fun <reified T : UpdateTemplatePropertyUseCase.UpdateCommand> updateProperty(
        description: String,
        request: TemplatePropertyRequest,
        additionalRequestFieldDescriptors: List<FieldDescriptor> = emptyList(),
        additionalThrowables: Set<KClass<out Throwable>> = emptySet(),
    ) {
        val id = ThingId("R3541")
        val propertyId = ThingId("R123")
        every { templateService.update(any<T>()) } just runs

        documentedPutRequestTo("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(request)
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/templates/$id")))
            .andDocument {
                summary("Updating template properties")
                description(description)
                pathParameters(
                    parameterWithName("id").description("The identifier of the template that the property belongs to."),
                    parameterWithName("propertyId").description("The identifier of the template property.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated template can be fetched from.")
                )
                requestFields(
                    request::class,
                    fieldWithPath("label").description("The label of the property."),
                    fieldWithPath("placeholder").description("The placeholder of the property."),
                    fieldWithPath("description").description("The description of the property."),
                    fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality.").optional(),
                    fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count.").optional(),
                    fieldWithPath("path").description("The predicate id for the path of the property."),
                    *additionalRequestFieldDescriptors.toTypedArray(),
                )
                throws(
                    TemplateNotFound::class,
                    TemplateClosed::class,
                    UnrelatedTemplateProperty::class,
                    *additionalThrowables.toTypedArray(),
                )
            }

        verify(exactly = 1) {
            templateService.update(
                withArg<T> {
                    it.shouldBeInstanceOf<T>()
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an untyped template property update request, when service succeeds, it updates the template property")
    fun updateProperty_untyped() = updateProperty<UpdateUntypedPropertyCommand>(
        """
        A `PUT` request updates an existing template property of any type with all the given parameters and converts it to an untyped template property if necessary.
        If the previous template property had a type constraint, it will be removed in the process.
        The response will be `204 No Content` when successful.
        The updated template property (object) can be retrieved by following the URI in the `Location` header field.
        
        NOTE: All fields at the top level in the request can be omitted or `null`, except for optional fields, meaning that the corresponding fields should not be updated.
        """,
        untypedTemplatePropertyRequest(),
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a string literal template property update request, when service succeeds, it updates the template property")
    fun updateProperty_stringLiteral() = updateProperty<UpdateStringLiteralPropertyCommand>(
        """
        A `PUT` request updates an existing template property of any type with all the given parameters and converts it to a string literal template property if necessary.
        The response will be `204 No Content` when successful.
        The updated template property (object) can be retrieved by following the URI in the `Location` header field.
        
        NOTE: All fields at the top level in the request can be omitted or `null`, except for optional fields, meaning that the corresponding fields should not be updated.
        """,
        stringLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("pattern").description("The pattern (regular expression) of the property."),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be "String".""")
        ),
        setOf(
            InvalidDataType::class,
            InvalidRegexPattern::class,
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a number literal template property update request, when service succeeds, it updates the template property")
    fun updateProperty_numberLiteral() = updateProperty<UpdateNumberLiteralPropertyCommand>(
        """
        A `PUT` request updates an existing template property of any type with all the given parameters and converts it to a number literal template property if necessary.
        The response will be `204 No Content` when successful.
        The updated template property (object) can be retrieved by following the URI in the `Location` header field.
        
        NOTE: All fields at the top level in the request can be omitted or `null`, except for optional fields, meaning that the corresponding fields should not be updated.
        """,
        numberLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("min_inclusive").description("The minimum value (inclusive) that the number can have."),
            fieldWithPath("max_inclusive").description("The maximum value (inclusive) that the number can have."),
            fieldWithPath("datatype").type("String").description("""The class id of the datatype of the property. Must be either of "Integer", "Decimal" or "Float".""")
        ),
        setOf(
            InvalidDataType::class,
            InvalidBounds::class,
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a literal template property update request, when service succeeds, it updates the template property")
    fun updateProperty_otherLiteral() = updateProperty<UpdateOtherLiteralPropertyCommand>(
        """
        A `PUT` request updates an existing template property of any type with all the given parameters and converts it to a literal template property if necessary.
        The response will be `204 No Content` when successful.
        The updated template property (object) can be retrieved by following the URI in the `Location` header field.
        
        NOTE: All fields at the top level in the request can be omitted or `null`, except for optional fields, meaning that the corresponding fields should not be updated.
        """,
        otherLiteralTemplatePropertyRequest(),
        listOf(
            fieldWithPath("datatype").type("String").description("The class id of the datatype of the property, indicating a literal property.")
        ),
        setOf(
            ClassNotFound::class,
        )
    )

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource template property update request, when service succeeds, it updates the template property")
    fun updateProperty_resource() = updateProperty<UpdateResourcePropertyCommand>(
        """
        A `PUT` request updates an existing template property of any type with all the given parameters and converts it to a resource template property if necessary.
        The response will be `204 No Content` when successful.
        The updated template property (object) can be retrieved by following the URI in the `Location` header field.
        
        NOTE: All fields at the top level in the request can be omitted or `null`, except for optional fields, meaning that the corresponding fields should not be updated.
        """,
        resourceTemplatePropertyRequest(),
        listOf(
            fieldWithPath("class").type("String").description("The class id of the range of the property, indicating a resource property.")
        ),
        setOf(
            ClassNotFound::class,
        )
    )

    private fun createTemplateRequest() =
        CreateTemplateRequest(
            label = "Dummy Template Label",
            description = "Some description about the template",
            formattedLabel = "{P32}",
            targetClass = ThingId("targetClass"),
            relations = TemplateController.TemplateRelationsRequestPart(
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
        UpdateTemplateRequest(
            label = "Dummy Template Label",
            description = "Some description about the template",
            formattedLabel = "{P32}",
            targetClass = ThingId("targetClass"),
            relations = TemplateController.TemplateRelationsRequestPart(
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
            extractionMethod = ExtractionMethod.MANUAL,
            visibility = Visibility.FEATURED
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
