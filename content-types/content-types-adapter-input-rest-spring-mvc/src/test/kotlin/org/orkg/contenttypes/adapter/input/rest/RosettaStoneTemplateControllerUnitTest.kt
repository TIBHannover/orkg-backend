package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneTemplateController.CreateRosettaStoneTemplateRequest
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneTemplateController.UpdateRosettaStoneTemplateRequest
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDataType
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingDynamicLabelPlaceholder
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
import org.orkg.contenttypes.domain.RosettaStoneTemplatePropertyNotModifiable
import org.orkg.contenttypes.domain.TooManyNewRosettaStoneTemplateLabelSections
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneTemplate
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.numberLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.otherLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.resourceTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.rosettaStoneTemplateResponseFields
import org.orkg.contenttypes.input.testing.fixtures.stringLiteralTemplatePropertyRequest
import org.orkg.contenttypes.input.testing.fixtures.untypedTemplatePropertyRequest
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRosettaStoneTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [RosettaStoneTemplateController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [RosettaStoneTemplateController::class])
internal class RosettaStoneTemplateControllerUnitTest : MockMvcBaseTest("rosetta-stone-templates") {
    @MockkBean
    private lateinit var templateService: RosettaStoneTemplateUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @DisplayName("Given a template, when it is fetched by id and service succeeds, then status is 200 OK and template is returned")
    fun findById() {
        val template = createRosettaStoneTemplate()
        every { templateService.findById(template.id) } returns Optional.of(template)

        documentedGetRequestTo("/api/rosetta-stone/templates/{id}", template.id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneTemplate()
            .andDocument {
                summary("Fetching rosetta stone templates")
                description(
                    """
                    A `GET` request provides information about a template.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the rosetta stone template to retrieve.")
                )
                responseFields<RosettaStoneTemplateRepresentation>(rosettaStoneTemplateResponseFields())
                throws(RosettaStoneTemplateNotFound::class)
            }

        verify(exactly = 1) { templateService.findById(template.id) }
    }

    @Test
    fun `Given a rosetta stone template, when it is fetched by id and service reports missing rosetta stone template, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { templateService.findById(id) } returns Optional.empty()

        get("/api/rosetta-stone/templates/{id}", id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:rosetta_stone_template_not_found")

        verify(exactly = 1) { templateService.findById(id) }
    }

    @Test
    @DisplayName("Given several rosetta stone templates, when they are fetched, then status is 200 OK and rosetta stone templates are returned")
    fun getPaged() {
        val template = createRosettaStoneTemplate()
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
    fun findAll() {
        val template = createRosettaStoneTemplate()
        val q = "example"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = template.createdBy
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")

        every {
            templateService.findAll(
                searchString = any(),
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                pageable = any()
            )
        } returns pageOf(template)

        documentedGetRequestTo("/api/rosetta-stone/templates")
            .param("q", q)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRosettaStoneTemplate("$.content[*]")
            .andDocument {
                summary("Listing rosetta stone templates")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<rosetta-stone-templates-fetch,rosetta stone templates>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("Optional filter for the rosetta stone template label.").optional(),
                    parameterWithName("exact").description("Optional flag for whether label matching should be exact. (default: false)").optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created the rosetta stone template.").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned rosetta stone template can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned rosetta stone template can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the rosetta stone template belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the rosetta stone template belongs to. (optional)").format("uuid").optional(),
                )
                pagedResponseFields<RosettaStoneTemplateRepresentation>(rosettaStoneTemplateResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            templateService.findAll(
                searchString = withArg<ExactSearchString> { it.input shouldBe "example" },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
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
            .andDocument {
                summary("Creating rosetta stone templates")
                description(
                    """
                    A `POST` request creates a new rosetta stone template with all the given parameters.
                    The response will be `201 Created` when successful.
                    The rosetta stone template (object) can be retrieved by following the URI in the `Location` header field.
                    
                    NOTE: The first property of a rosetta stone template defines the subject position of the statement and is required to have a path of `hasSubjectPosition`, must have a minimum cardinality of at least one and is not a literal template property.
                          All other properties define an object position, which must have a path of `hasObjectPosition`.
                          At least one property is required to create a new rosetta stone template.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created rosetta stone template can be fetched from.")
                )
                requestFields<CreateRosettaStoneTemplateRequest>(
                    fieldWithPath("label").description("The label of the rosetta stone template."),
                    fieldWithPath("description").description("The description of the rosetta stone template."),
                    fieldWithPath("formatted_label").description("The formatted label pattern of the rosetta stone template."),
                    fieldWithPath("example_usage").description("One or more example sentences that demonstrate the usage of the statement that this template models."),
                    subsectionWithPath("properties").description("""The list of properties of the rosetta stone template. The first property defines the subject position of the statement and is required to have a path of `hasSubjectPosition`, must have a minimum cardinality of at least one and is not a literal template property. All other properties define a object position, which must have a path of `hasObjectPosition`. At least one property is required to create a new rosetta stone template. See <<template-properties,template properties>> for more information."""),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone template belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone template belongs to."),
                )
                throws(
                    InvalidLabel::class,
                    InvalidDescription::class,
                    MissingDynamicLabelPlaceholder::class,
                    MissingSubjectPosition::class,
                    InvalidSubjectPositionPath::class,
                    InvalidSubjectPositionCardinality::class,
                    InvalidSubjectPositionType::class,
                    InvalidObjectPositionPath::class,
                    MissingPropertyPlaceholder::class,
                    InvalidMinCount::class,
                    InvalidMaxCount::class,
                    InvalidCardinality::class,
                    InvalidDataType::class,
                    InvalidRegexPattern::class,
                    InvalidBounds::class,
                    ClassNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                )
            }

        verify(exactly = 1) { templateService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone template update request, when service succeeds, it updates the rosetta stone template")
    fun update() {
        val id = ThingId("R123")
        every { templateService.update(any()) } just runs

        documentedPutRequestTo("/api/rosetta-stone/templates/{id}", id)
            .content(updateRosettaStoneTemplateRequest())
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/rosetta-stone/templates/$id")))
            .andDocument {
                summary("Updating rosetta stone templates")
                description(
                    """
                    A `PUT` request updates an existing rosetta stone template with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated rosetta stone template (object) can be retrieved by following the URI in the `Location` header field.
                    
                    NOTE: Only rosetta stone templates that have not been used to create a rosetta stone statement can be fully updated.
                          Otherwise, it is only possible to add new object positions and to insert a section for that specific object position properties into the formatted label.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the rosetta stone template.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated rosetta stone template can be fetched from.")
                )
                requestFields<UpdateRosettaStoneTemplateRequest>(
                    fieldWithPath("label").description("The updated label of the rosetta stone template. After the rosetta stone template has been used to instantiate a rosetta stone statement, it is no longer possible to update the label. (optional)").optional(),
                    fieldWithPath("description").description("The updated description of the rosetta stone template. After the rosetta stone template has been used to instantiate a rosetta stone statement, it is no longer possible to update the description. (optional)").optional(),
                    fieldWithPath("formatted_label").description("The updated formatted label pattern of the rosetta stone template. After the rosetta stone template has been used to instantiate a rosetta stone statement, is only possible to append sections to the formatted label. (optional)").optional(),
                    fieldWithPath("example_usage").description("One or more updated example sentences that demonstrate the usage of the statement that this template models. After the rosetta stone template has been used to instantiate a rosetta stone statement, is only possible to append text to the example usage. (optional)").optional(),
                    subsectionWithPath("properties").description("""The updated list of properties of the rosetta stone template. The first property defines the subject position of the statement and is required to have a path of `hasSubjectPosition`, must have a minimum cardinality of at least one and is not a literal template property. All other properties define a object position, which must have a path of `hasObjectPosition`. See <<template-properties,template properties>> for more information. After the rosetta stone template has been used to instantiate a rosetta stone statement, is only possible to append new object positions. (optional)""").optional(),
                    fieldWithPath("organizations[]").description("The updated list of IDs of the organizations the rosetta stone template belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The updated list of IDs of the observatories the rosetta stone template belongs to. (optional)").optional(),
                )
                throws(
                    RosettaStoneTemplateNotFound::class,
                    RosettaStoneTemplateNotModifiable::class,
                    RosettaStoneTemplateInUse::class,
                    InvalidLabel::class,
                    RosettaStoneTemplateInUse::class,
                    InvalidDescription::class,
                    MissingDynamicLabelPlaceholder::class,
                    RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties::class,
                    RosettaStoneTemplateLabelMustStartWithPreviousVersion::class,
                    TooManyNewRosettaStoneTemplateLabelSections::class,
                    NewRosettaStoneTemplateLabelSectionsMustBeOptional::class,
                    RosettaStoneTemplateLabelMustBeUpdated::class,
                    NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage::class,
                    RosettaStoneTemplatePropertyNotModifiable::class,
                    NewRosettaStoneTemplatePropertyMustBeOptional::class,
                    MissingSubjectPosition::class,
                    InvalidSubjectPositionPath::class,
                    InvalidSubjectPositionCardinality::class,
                    InvalidSubjectPositionType::class,
                    InvalidObjectPositionPath::class,
                    MissingPropertyPlaceholder::class,
                    InvalidMinCount::class,
                    InvalidMaxCount::class,
                    InvalidCardinality::class,
                    InvalidDataType::class,
                    InvalidRegexPattern::class,
                    InvalidBounds::class,
                    ClassNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                )
            }

        verify(exactly = 1) { templateService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a rosetta stone template, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun deleteById() {
        val id = ThingId("R123")
        every { templateService.deleteById(id, any()) } just runs

        documentedDeleteRequestTo("/api/rosetta-stone/templates/{id}", id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Deleting rosetta stone templates")
                description(
                    """
                    A `DELETE` request deletes a rosetta stone template.
                    The response will be `204 No Content` when successful.
                    
                    NOTE: A rosetta stone template can only be deleted when it is not used for any <<rosetta-stone-statements,rosetta stone statement>>.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the rosetta stone template to delete.")
                )
                throws(
                    RosettaStoneTemplateNotFound::class,
                    RosettaStoneTemplateNotModifiable::class,
                    RosettaStoneTemplateInUse::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class,
                )
            }

        verify(exactly = 1) { templateService.deleteById(id, ContributorId(MockUserId.USER)) }
    }

    private fun createRosettaStoneTemplateRequest() =
        CreateRosettaStoneTemplateRequest(
            label = "Dummy Rosetta Stone Template Label",
            description = "Some description about the Rosetta Stone Template",
            dynamicLabel = "{P32}",
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

    private fun updateRosettaStoneTemplateRequest() =
        UpdateRosettaStoneTemplateRequest(
            label = "Dummy Rosetta Stone Template Label",
            description = "Some description about the Rosetta Stone Template",
            dynamicLabel = "{P32}",
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
