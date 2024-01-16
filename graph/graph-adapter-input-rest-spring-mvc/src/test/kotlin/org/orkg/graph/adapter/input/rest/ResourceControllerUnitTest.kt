package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateResourceUseCase.CreateCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ResourceController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ResourceController::class])
@DisplayName("Given a Resource controller")
internal class ResourceControllerUnitTest : RestDocsTest("resources") {

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @Suppress("unused") // required by ResourceController but not used in the test (yet)
    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun getSingle() {
        val resource = createResource()
        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { statementService.countStatementsAboutResource(resource.id) } returns 23
        every { flags.isFormattedLabelsEnabled() } returns false

        documentedGetRequestTo("/api/resources/{id}", resource.id)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the resource."),
                        fieldWithPath("label").description("The label of the resource. It is intended to be read by humans and should be used for displaying the resource."),
                        fieldWithPath("formatted_label").description("The formatted label of the resource.").ignored(),
                        fieldWithPath("classes").description("The set of classes of which this resources is an instance of."),
                        fieldWithPath("shared").description("The number of statements that have this resource in their object position."),
                        fieldWithPath("featured").description("Determine if the resource is featured. Defaults to `false`."),
                        fieldWithPath("unlisted").description("Determine if the resource is unlisted. Defaults to `false`."),
                        fieldWithPath("verified").description("Determine if the resource is verified. Defaults to `false`."),
                        fieldWithPath("extraction_method").description("Determines how the resource was created. Can be one of $allowedExtractionMethodValues."),
                        fieldWithPath("observatory_id").description("The UUID of the observatory to which this resource belongs."),
                        fieldWithPath("organization_id").description("The UUID of the organization to which this resource belongs."),
                        timestampFieldWithPath("created_at", "the resource was created"),
                        fieldWithPath("created_by").description("The UUID of the user or service who created this resource."),
                        fieldWithPath("_class").description("An indicator which type of entity was returned. Always has the value `resource`."),
                    )
                )
            )
    }

    @Test
    fun `Given the contributors are requested, when service succeeds, then status is 200 OK and contributors are returned`() {
        val id = ThingId("R123")
        val contributorIds = listOf(
            ContributorId(UUID.randomUUID()),
            ContributorId(UUID.randomUUID())
        )
        val contributors = PageImpl(
            contributorIds,
            PageRequest.of(0, 25),
            contributorIds.size.toLong()
        )
        every { resourceService.findAllContributorsByResourceId(id, any()) } returns contributors

        mockMvc.perform(get("/api/resources/$id/contributors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun `Given the contributors are requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findAllContributorsByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        mockMvc.perform(get("/api/resources/$id/contributors"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/contributors"))
    }

    @Test
    fun `Given a timeline is requested, when service succeeds, then status is 200 OK and timeline is returned`() {
        val id = ThingId("R123")
        val resourceContributors = listOf(
            UUID.randomUUID() to OffsetDateTime.now(clock),
            UUID.randomUUID() to OffsetDateTime.now(clock)
        ).map {
            ResourceContributor(it.first.toString(), it.second.format(ISO_DATE_TIME))
        }
        val timeline = PageImpl(
            resourceContributors,
            PageRequest.of(0, 25),
            resourceContributors.size.toLong()
        )
        every { resourceService.findTimelineByResourceId(id, any()) } returns timeline

        mockMvc.perform(get("/api/resources/$id/timeline"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    fun `Given a timeline is requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findTimelineByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        mockMvc.perform(get("/api/resources/$id/timeline"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/timeline"))
    }

    @Test
    @TestWithMockUser
    fun `When creating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val command = CreateResourceRequest(
            id = null,
            label = "irrelevant",
            classes = setOf(ThingId("List"))
        )
        val exception = InvalidClassCollection(command.classes)

        every { contributorService.findById(any()) } returns Optional.of(
            Contributor(
                id = ContributorId(UUID.randomUUID()),
                name = "irrelevant",
                joinedAt = OffsetDateTime.now(clock)
            )
        )
        every { resourceService.create(any<CreateCommand>()) } throws exception

        mockMvc.performPost("/api/resources/", command)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/"))
    }

    @Test
    @TestWithMockUser
    fun `When updating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            classes = setOf(ThingId("List"))
        )
        val exception = InvalidClassCollection(command.classes!!)

        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { resourceService.update(command) } throws exception

        mockMvc.performPut("/api/resources/${resource.id}", command)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/${resource.id}"))
    }

    @Test
    @DisplayName("Given several resources, when they are fetched, then status is 200 OK and resources are returned")
    fun getPaged() {
        every { resourceService.findAll(any()) } returns pageOf(createResource())
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        documentedGetRequestTo("/api/resources/")
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").optional(),
                        parameterWithName("visibility").description("""Filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED". (optional)""").optional(),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this paper. (optional)").optional(),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                        parameterWithName("include").description("Filter for a set of classes that the resource must have. (optional)").optional(),
                        parameterWithName("exclude").description("Filter for a set of classes that the resource must not have. (optional)").optional(),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").optional(),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findAll(any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given several resources, when filtering by several parameters, then status is 200 OK and resources are returned`() {
        every { resourceService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createResource())
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val includeClasses = setOf(ThingId("Include1"), ThingId("Include2"))
        val excludeClasses = setOf(ThingId("Exclude1"), ThingId("Exclude2"))
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())

        get("/api/resources/" +
            "?q={label}" +
            "&exact={exact}" +
            "&visibility={visibility}" +
            "&created_by={created_by}" +
            "&created_at_start={created_at_start}" +
            "&created_at_end={created_at_end}" +
            "&include={include}" +
            "&exclude={exclude}" +
            "&observatory_id={observatoryId}" +
            "&organization_id={organizationId}",
            label,
            exact,
            visibility,
            createdBy,
            createdAtStart.format(ISO_OFFSET_DATE_TIME),
            createdAtEnd.format(ISO_OFFSET_DATE_TIME),
            includeClasses.joinToString(separator = ","),
            excludeClasses.joinToString(separator = ","),
            observatoryId,
            organizationId
        )
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")

        verify(exactly = 1) {
            resourceService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                includeClasses = includeClasses,
                excludeClasses = excludeClasses,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given several resources, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { resourceService.findAll(any()) } throws exception

        mockMvc.perform(get("/api/resources/?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/"))
    }

    private fun MockMvc.performPost(urlTemplate: String, request: Any): ResultActions = perform(
        post(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(request))
    )

    private fun MockMvc.performPut(urlTemplate: String, request: Any): ResultActions = perform(
        put(urlTemplate)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(request))
    )
}
