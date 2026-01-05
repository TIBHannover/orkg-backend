package org.orkg.contenttypes.adapter.input.rest

import com.epages.restdocs.apispec.ParameterType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.adapter.input.rest.TemplateInstanceController.UpdateTemplateInstanceRequest
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MismatchedDataType
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIsNotAClass
import org.orkg.contenttypes.domain.ObjectIsNotAList
import org.orkg.contenttypes.domain.ObjectIsNotALiteral
import org.orkg.contenttypes.domain.ObjectIsNotAPredicate
import org.orkg.contenttypes.domain.ObjectMustNotBeALiteral
import org.orkg.contenttypes.domain.ResourceIsNotAnInstanceOfTargetClass
import org.orkg.contenttypes.domain.TemplateNotApplicable
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateClassRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateListRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreatePredicateRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.mapOfCreateResourceRequestPartRequestFields
import org.orkg.contenttypes.input.testing.fixtures.templateInstanceResponseFields
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplateInstance
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@ContextConfiguration(classes = [TemplateInstanceController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [TemplateInstanceController::class])
internal class TemplateInstanceControllerUnitTest : MockMvcBaseTest("template-instances") {
    @MockkBean
    private lateinit var service: TemplateInstanceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a template instance, when it is fetched by id and service succeeds, then status is 200 OK and template instance is returned")
    fun findByTemplateIdAndResourceId() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findById(id, templateInstance.root.id) } returns Optional.of(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/templates/{id}/instances/{instanceId}", id, templateInstance.root.id)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplateInstance()
            .andDocument {
                summary("Fetching template isntances")
                description(
                    """
                    A `GET` request provides information about a template instance.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the template to fetch the statements for."),
                    parameterWithName("instanceId").description("The identifier of the templated resource to retrieve."),
                )
                responseFields<TemplateInstanceRepresentation>(templateInstanceResponseFields())
                throws(
                    TemplateNotFound::class,
                    TemplateNotApplicable::class,
                    ResourceNotFound::class,
                )
            }

        verify(exactly = 1) { service.findById(id, templateInstance.root.id) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a template instance, when it is fetched by id and service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R132")
        val instanceId = ThingId("Missing")

        every { service.findById(id, instanceId) } returns Optional.empty()

        get("/api/templates/{id}/instances/{instanceId}", id, instanceId)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:resource_not_found")

        verify(exactly = 1) { service.findById(id, instanceId) }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with no parameters, then status is 200 OK and template instances are returned")
    fun getPaged() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findAll(id, pageable = any()) } returns pageOf(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/templates/{id}/instances", id)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateInstance("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAll(id, pageable = any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with all possible filtering parameters, then status is 200 OK and template instances are returned")
    fun findAllByTemplateId() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findAll(id, any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())

        documentedGetRequestTo("/api/templates/{id}/instances", id)
            .param("q", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateInstance("$.content[*]")
            .andDocument {
                summary("Listing template instances")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<template-instances-fetch,template instances>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the template to fetch the statements for."),
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").type(ParameterType.BOOLEAN).optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this template instance. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned template instance can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned template instance can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the template instance belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the template instance belongs to. (optional)").format("uuid").optional(),
                )
                pagedResponseFields<TemplateInstanceRepresentation>(templateInstanceResponseFields())
                throws(TemplateNotFound::class, UnknownSortingProperty::class)
            }

        verify(exactly = 1) { service.findAll(id, any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a template instance update request, when service succeeds, it updates the template instance")
    fun update() {
        val id = ThingId("R123")
        val instanceId = ThingId("R456")

        every { service.update(any()) } just runs

        documentedPutRequestTo("/api/templates/{id}/instances/{instanceId}", id, instanceId)
            .content(updateTemplateInstanceRequest())
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/templates/$id/instances/$instanceId")))
            .andDocument {
                summary("Updating template instances")
                description(
                    """
                    A `PUT` request updates an existing template instance with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated template instance (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the template."),
                    parameterWithName("instanceId").description("The identifier of the template instance."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated template instance can be fetched from."),
                )
                requestFields<UpdateTemplateInstanceRequest>(
                    fieldWithPath("statements").description("Map of predicate ids to list of object ids that represent the statements of the template instance."),
                    fieldWithPath("statements.*").description("A predicate id"),
                    fieldWithPath("statements.*[]").description("A list of thing ids or temp ids representing the objects of a statement."),
                    *mapOfCreateResourceRequestPartRequestFields().toTypedArray(),
                    fieldWithPath("literals").description("A key-value map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("literals.*").description("The value of the literal. The type will be automatically assigned based on the template."),
                    *mapOfCreatePredicateRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateListRequestPartRequestFields().toTypedArray(),
                    *mapOfCreateClassRequestPartRequestFields().toTypedArray(),
                    fieldWithPath("extraction_method").description("""The method used to extract the template instance. Can be one of $allowedExtractionMethodValues. (optional)""").optional()
                )
                throws(
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    TemplateNotFound::class,
                    ResourceNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    UnknownTemplateProperties::class,
                    MissingPropertyValues::class,
                    TooManyPropertyValues::class,
                    ObjectIsNotAClass::class,
                    ObjectIsNotAPredicate::class,
                    ObjectIsNotAList::class,
                    ObjectMustNotBeALiteral::class,
                    ResourceIsNotAnInstanceOfTargetClass::class,
                    ObjectIsNotALiteral::class,
                    InvalidLiteral::class,
                    MismatchedDataType::class,
                    LabelDoesNotMatchPattern::class,
                    NumberTooLow::class,
                    NumberTooHigh::class,
                )
            }

        verify(exactly = 1) { service.update(any()) }
    }

    private fun updateTemplateInstanceRequest() =
        UpdateTemplateInstanceRequest(
            statements = mapOf(
                Predicates.hasAuthor to listOf("#temp1", "#temp2", "#temp3"),
                Predicates.field to listOf("#temp4", "#temp5", "R123")
            ),
            resources = mapOf(
                "#temp1" to CreateResourceRequestPart(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to "0.1"
            ),
            predicates = mapOf(
                "#temp3" to CreatePredicateRequestPart(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to CreateListRequestPart(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to CreateClassRequestPart(
                    label = "class",
                    uri = ParsedIRI.create("https://orkg.org/class/C1")
                )
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )
}
