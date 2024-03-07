package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplateInstance
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectTemplateInstance
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [TemplateInstanceController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [TemplateInstanceController::class])
@DisplayName("Given a Template Instance controller")
internal class TemplateInstanceControllerUnitTest : RestDocsTest("template-instances") {

    @MockkBean
    private lateinit var service: TemplateInstanceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(service, statementService, formattedLabelRepository, flags)
    }

    @Test
    @DisplayName("Given a template instance, when it is fetched by id and service succeeds, then status is 200 OK and template instance is returned")
    fun getSingle() {
        val templateId = ThingId("R132")
        val templateInstance = createDummyTemplateInstance()

        every { service.findById(templateId, templateInstance.root.id) } returns Optional.of(templateInstance)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        documentedGetRequestTo("/api/templates/{templateId}/instances/{id}", templateId, templateInstance.root.id)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplateInstance()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("templateId").description("The identifier of the template to fetch the statements for."),
                        parameterWithName("id").description("The identifier of the templated resource to retrieve.")
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

        verify(exactly = 1) { service.findById(templateId, templateInstance.root.id) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given a template instance, when it is fetched by id and service reports missing resource, then status is 404 NOT FOUND`() {
        val templateId = ThingId("R132")
        val id = ThingId("Missing")
        val exception = ResourceNotFound.withId(id)

        every { service.findById(templateId, id) } returns Optional.empty()

        get("/api/templates/$templateId/instances/$id")
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/templates/$templateId/instances/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findById(templateId, id) }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with no parameters, then status is 200 OK and template instances are returned")
    fun getPaged() {
        val templateId = ThingId("R132")
        val templateInstance = createDummyTemplateInstance()

        every { service.findAll(templateId, pageable = any()) } returns pageOf(templateInstance)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        documentedGetRequestTo("/api/templates/{templateId}/instances", templateId)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectTemplateInstance("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAll(templateId, pageable = any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    @DisplayName("Given several template instances, when they are fetched with all possible filtering parameters, then status is 200 OK and template instances are returned")
    fun getPagedWithParameters() {
        val templateId = ThingId("R132")
        val templateInstance = createDummyTemplateInstance()

        every { service.findAll(templateId, any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(templateInstance)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())

        documentedGetRequestTo("/api/templates/{templateId}/instances", templateId)
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
                    requestParameters(
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

        verify(exactly = 1) { service.findAll(templateId, any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }
}
