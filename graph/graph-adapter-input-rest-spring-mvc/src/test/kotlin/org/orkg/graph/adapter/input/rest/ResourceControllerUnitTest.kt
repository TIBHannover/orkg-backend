package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateResourceUseCase.CreateCommand
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(
    classes = [
        ResourceController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
@WebMvcTest(controllers = [ResourceController::class])
internal class ResourceControllerUnitTest : MockMvcBaseTest("resources") {
    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun getSingle() {
        val resource = createResource()
        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { statementService.countIncomingStatementsById(resource.id) } returns 23

        documentedGetRequestTo("/api/resources/{id}", resource.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the resource to retrieve."),
                    ),
                    responseFields(resourceResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(resource.id) }
    }

    @Test
    fun `Given the contributors are requested, when service succeeds, then status is 200 OK and contributors are returned`() {
        val id = ThingId("R123")
        val contributorIds = listOf(
            ContributorId(MockUserId.USER),
            ContributorId(MockUserId.ADMIN)
        )
        val contributors = PageImpl(
            contributorIds,
            PageRequest.of(0, 25),
            contributorIds.size.toLong()
        )
        every { resourceService.findAllContributorsByResourceId(id, any()) } returns contributors

        get("/api/resources/{id}/contributors", id)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.page.number").value(0)) // page number
            .andExpect(jsonPath("$.page.total_elements").value(2))

        verify(exactly = 1) { resourceService.findAllContributorsByResourceId(id, any()) }
    }

    @Test
    fun `Given the contributors are requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findAllContributorsByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        get("/api/resources/{id}/contributors", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/contributors"))

        verify(exactly = 1) { resourceService.findAllContributorsByResourceId(id, any()) }
    }

    @Test
    fun `Given a timeline is requested, when service succeeds, then status is 200 OK and timeline is returned`() {
        val id = ThingId("R123")
        val resourceContributors = listOf(
            ResourceContributor(ContributorId(MockUserId.USER), OffsetDateTime.now(clock)),
            ResourceContributor(ContributorId(MockUserId.ADMIN), OffsetDateTime.now(clock))
        )
        val timeline = PageImpl(
            resourceContributors,
            PageRequest.of(0, 25),
            resourceContributors.size.toLong()
        )
        every { resourceService.findTimelineByResourceId(id, any()) } returns timeline

        get("/api/resources/{id}/timeline", id)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", Matchers.hasSize<Int>(2)))
            .andExpect(jsonPath("$.page.number").value(0)) // page number
            .andExpect(jsonPath("$.page.total_elements").value(2))

        verify(exactly = 1) { resourceService.findTimelineByResourceId(id, any()) }
    }

    @Test
    fun `Given a timeline is requested, when service reports missing resource, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        every { resourceService.findTimelineByResourceId(id, any()) } throws ResourceNotFound.withId(id)

        get("/api/resources/{id}/timeline", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Resource "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id/timeline"))

        verify(exactly = 1) { resourceService.findTimelineByResourceId(id, any()) }
    }

    @Test
    @TestWithMockUser
    fun `When creating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val command = ResourceController.CreateResourceRequest(
            id = null,
            label = "irrelevant",
            classes = setOf(Classes.list)
        )
        val exception = InvalidClassCollection(command.classes)

        every { contributorService.findById(any()) } returns Optional.of(
            createContributor(id = ContributorId(MockUserId.USER))
        )
        every { resourceService.create(any<CreateCommand>()) } throws exception

        post("/api/resources")
            .content(command)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources"))

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { resourceService.create(any<CreateCommand>()) }
    }

    @Test
    @DisplayName("Given several resources, when filtering by no parameters, then status is 200 OK and resources are returned")
    fun getPaged() {
        every { resourceService.findAll(any()) } returns pageOf(createResource())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/resources")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findAll(any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several resources, when they are fetched with all possible filtering parameters, then status is 200 OK and resources are returned")
    fun getPagedWithParameters() {
        every { resourceService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(createResource())
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)
        val includeClasses = setOf(ThingId("Include1"), ThingId("Include2"))
        val excludeClasses = setOf(ThingId("Exclude1"), ThingId("Exclude2"))
        val baseClass = ThingId("BaseClass")
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")

        documentedGetRequestTo("/api/resources")
            .param("q", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .param("include", includeClasses.joinToString(separator = ","))
            .param("exclude", excludeClasses.joinToString(separator = ","))
            .param("base_class", baseClass.value)
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)"),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED". (optional)"""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this resource. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("include").description("A comma-separated set of classes that the resource must have. (optional)"),
                        parameterWithName("exclude").description("A comma-separated set of classes that the resource must not have. (optional)"),
                        parameterWithName("base_class").description("The id of the base class that the resource has to be an instance of. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
                baseClass = baseClass,
                observatoryId = observatoryId,
                organizationId = organizationId
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given several resources, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { resourceService.findAll(any()) } throws exception

        get("/api/resources")
            .param("sort", "unknown")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources"))

        verify(exactly = 1) { resourceService.findAll(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource update command, when service succeeds, it returns status 200 OK")
    fun update() {
        val resource = createResource(classes = setOf(Classes.data), label = "foo")
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            label = resource.label,
            classes = resource.classes,
            extractionMethod = resource.extractionMethod,
            visibility = resource.visibility,
            observatoryId = resource.observatoryId,
            organizationId = resource.organizationId,
        )
        val request = mapOf(
            "label" to resource.label,
            "classes" to resource.classes,
            "extraction_method" to resource.extractionMethod,
            "visibility" to resource.visibility,
            "observatory_id" to resource.observatoryId,
            "organization_id" to resource.organizationId
        )

        every { resourceService.update(command) } just runs
        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { statementService.countIncomingStatementsById(resource.id) } returns 0

        documentedPutRequestTo("/api/resources/{id}", resource.id)
            .content(request)
            .perform()
            .andExpect(status().isOk)
            .andExpect(header().string("Location", endsWith("/api/resources/${resource.id}")))
            .andExpectResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the resource.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The updated resource label. (optional)").optional(),
                        fieldWithPath("classes").description("The classes to which the resource belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                        fieldWithPath("visibility").description("""Visibility of the resource. Can be one of $allowedVisibilityValues. (optional)""").optional(),
                        fieldWithPath("organization_id").description("The updated ID of the organization the resource belongs to. (optional)").optional(),
                        fieldWithPath("observatory_id").description("The updated ID of the observatory the resource belongs to. (optional)").optional(),
                    ),
                    responseFields(resourceResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.update(command) }
        verify(exactly = 1) { resourceService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(resource.id) }
    }

    @Test
    @TestWithMockUser
    fun `When updating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val id = ThingId("R213")
        val command = UpdateResourceUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            classes = setOf(Classes.list)
        )
        val exception = InvalidClassCollection(command.classes!!)

        every { resourceService.update(command) } throws exception

        put("/api/resources/{id}", id)
            .content(command)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/resources/$id"))

        verify(exactly = 1) { resourceService.update(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When creating a resource, and service succeeds, then status is 201 CREATED and resource is returned")
    fun create() {
        val id = ThingId("R213")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ResourceController.CreateResourceRequest(
            id = id,
            label = "foo",
            classes = setOf(Classes.dataset),
            extractionMethod = ExtractionMethod.MANUAL
        )
        val resource = createResource(
            id = id,
            classes = command.classes,
            label = command.label,
            extractionMethod = command.extractionMethod
        )

        every { resourceService.create(any()) } returns id
        every { contributorService.findById(any()) } returns Optional.of(createContributor(contributorId))
        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { statementService.countIncomingStatementsById(id) } returns 0

        documentedPostRequestTo("/api/resources")
            .content(command)
            .perform()
            .andExpect(status().isCreated)
            .andExpectResource()
            .andExpect(header().string("Location", endsWith("api/resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the created resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("id").description("The id for the resource. (optional)"),
                        fieldWithPath("label").description("The resource label."),
                        fieldWithPath("classes").type("Array").description("The classes of the resource. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: "UNKNOWN")""").optional()
                    ),
                    responseFields(resourceResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            resourceService.create(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe contributorId
                    it.label shouldBe command.label
                    it.classes shouldBe command.classes
                    it.extractionMethod shouldBe command.extractionMethod
                }
            )
        }
        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { resourceService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When deleting a resource, and service succeeds, then status is 204 NO CONTENT")
    fun delete() {
        val id = ThingId("R213")

        every { resourceService.delete(id, any()) } just runs

        documentedDeleteRequestTo("/api/resources/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.delete(id, ContributorId(MockUserId.USER)) }
    }
}
