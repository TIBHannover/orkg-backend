package org.orkg.contenttypes.adapter.input.rest

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
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplateInstance
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
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
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@ContextConfiguration(
    classes = [
        TemplateInstanceController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
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
    fun getSingle() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findById(id, templateInstance.root.id) } returns Optional.of(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/templates/{id}/instances/{instanceId}", id, templateInstance.root.id)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplateInstance()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the template to fetch the statements for."),
                        parameterWithName("instanceId").description("The identifier of the templated resource to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        subsectionWithPath("root").description("The resource representation of the root resource."),
                        subsectionWithPath("statements").description("Map of predicate id to list of embedded statement representations, where `root` is the subject."),
                        subsectionWithPath("statements.*[].thing").description("The thing representation of the object of the statement. Acts as the subject for statements defined in the `statements` object."),
                        timestampFieldWithPath("statements.*[].created_at", "the statement was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("statements.*[].created_by").description("The UUID of the user or service who created this statement."),
                        subsectionWithPath("statements.*[].statements").description("Map of predicate id to list of embedded statement representations, where `thing` is the subject.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findById(id, templateInstance.root.id) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a template instance, when it is fetched by id and service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R132")
        val instanceId = ThingId("Missing")
        val exception = ResourceNotFound.withId(instanceId)

        every { service.findById(id, instanceId) } returns Optional.empty()

        get("/api/templates/{id}/instances/{instanceId}", id, instanceId)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$id/instances/$instanceId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findById(id, instanceId) }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with no parameters, then status is 200 OK and template instances are returned")
    fun getPaged() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findAll(id, pageable = any()) } returns pageOf(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/templates/{id}/instances", id)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateInstance("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the template to fetch the statements for."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAll(id, pageable = any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with all possible filtering parameters, then status is 200 OK and template instances are returned")
    fun getPagedWithParameters() {
        val id = ThingId("R132")
        val templateInstance = createTemplateInstance()

        every { service.findAll(id, any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(templateInstance)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the template to fetch the statements for."),
                    ),
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)"),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED". (optional)"""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this template instance. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned template instance can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned template instance can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the template instance belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the template instance belongs to. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAll(id, any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the template."),
                        parameterWithName("instanceId").description("The identifier of the template instance."),
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated template instance can be fetched from.")
                    ),
                    requestFields(
                        subsectionWithPath("statements").description("Map of predicate ids to list of object ids that represent the statements of the template instance."),
                        fieldWithPath("resources").description("Definition of resources that need to be created."),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created."),
                        fieldWithPath("literals.*").description("Key value pairs of literal temp ids to literal values. The type will be automatically assigned based on the template."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("classes").description("Definition of classes that need to be created."),
                        fieldWithPath("classes.*.label").description("The label of the class."),
                        fieldWithPath("classes.*.uri").description("The uri of the class."),
                        fieldWithPath("extraction_method").description("""The method used to extract the template instance. Can be one of "unknown", "manual" or "automatic".""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.update(any()) }
    }

    private fun updateTemplateInstanceRequest() =
        TemplateInstanceController.UpdateTemplateInstanceRequest(
            statements = mapOf(
                Predicates.hasAuthor to listOf("#temp1", "#temp2", "#temp3"),
                Predicates.field to listOf("#temp4", "#temp5", "R123")
            ),
            resources = mapOf(
                "#temp1" to ResourceDefinitionDTO(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to "0.1"
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinitionDTO(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinitionDTO(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            classes = mapOf(
                "#temp5" to ClassDefinitionDTO(
                    label = "class",
                    uri = ParsedIRI("https://orkg.org/class/C1")
                )
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )
}
