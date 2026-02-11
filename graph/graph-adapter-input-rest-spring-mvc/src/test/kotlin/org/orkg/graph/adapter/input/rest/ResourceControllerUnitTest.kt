package org.orkg.graph.adapter.input.rest

import com.epages.restdocs.apispec.ParameterType
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
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.adapter.input.rest.ResourceController.CreateResourceRequest
import org.orkg.graph.adapter.input.rest.ResourceController.UpdateResourceRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ResourceAlreadyExists
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.ResourceInUse
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ResourceNotModifiable
import org.orkg.graph.domain.RosettaStoneStatementResourceNotModifiable
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateResourceUseCase.CreateCommand
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.repeatable
import org.orkg.testing.spring.restdocs.type
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [ResourceController::class, GraphControllerUnitTestConfiguration::class])
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
    fun findById() {
        val resource = createResource()
        every { resourceService.findById(any()) } returns Optional.of(resource)
        every { statementService.countIncomingStatementsById(resource.id) } returns 23

        documentedGetRequestTo("/api/resources/{id}", resource.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDocument {
                summary("Fetching resources")
                description(
                    """
                    A `GET` request provides information about a resource.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the resource to retrieve."),
                )
                responseFields<ResourceRepresentation>(resourceResponseFields())
                throws(ResourceNotFound::class)
            }

        verify(exactly = 1) { resourceService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(resource.id) }
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
        every { resourceService.findTimelineByResourceId(id, any()) } throws ResourceNotFound(id)

        get("/api/resources/{id}/timeline", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:resource_not_found")

        verify(exactly = 1) { resourceService.findTimelineByResourceId(id, any()) }
    }

    @Test
    @TestWithMockUser
    fun `When creating a resource, and service reports invalid class collection, then status is 400 BAD REQUEST`() {
        val command = CreateResourceRequest(
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
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_class_collection")

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
    fun findAll() {
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
            .andDocument {
                summary("Listing resources")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<resources-fetch,resources>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").type(ParameterType.BOOLEAN).optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this resource. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("include").description("A comma-separated set of classes that the resource must have. (optional)").repeatable().optional(),
                    parameterWithName("exclude").description("A comma-separated set of classes that the resource must not have. (optional)").repeatable().optional(),
                    parameterWithName("base_class").description("The id of the base class that the resource has to be an instance of. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").format("uuid").optional(),
                )
                pagedResponseFields<ResourceRepresentation>(resourceResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) { resourceService.findAll(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource update command, when service succeeds, then status is 204 NO CONTENT")
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

        documentedPutRequestTo("/api/resources/{id}", resource.id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/resources/${resource.id}")))
            .andDocument {
                summary("Updating resources")
                description(
                    """
                    A `PUT` request updates a resource with the given parameters.
                    The response will be `204 NO CONTENT` when successful.
                    The updated resource can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. If the resource doesn't exist, the return status will be `404 NOT FOUND`.
                    2. If the resource is not modifiable, the return status will be `403 FORBIDDEN`.
                    3. If the target visibility is `FEATURED` or `UNLISTED` and the performing user is not a curator, the return status will be `403 FORBIDDEN`.
                    4. If the target visibility is `DELETED` the performing user is not the owner of the resource and not a curator, the return status will be `403 FORBIDDEN`.
                    5. If the target visibility is `DEFAULT` and the original visibility is `DELETED` and the performing user is not the owner of the resource and not a curator, the return status will be `403 FORBIDDEN`.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the resource.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated resource can be fetched from.")
                )
                requestFields<UpdateResourceRequest>(
                    fieldWithPath("label").description("The updated resource label. (optional)").optional(),
                    fieldWithPath("classes[]").description("The classes to which the resource belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                    fieldWithPath("visibility").description("""Visibility of the resource. Can be one of $allowedVisibilityValues. (optional)""").optional(),
                    fieldWithPath("organization_id").description("The updated ID of the organization the resource belongs to. (optional)").optional(),
                    fieldWithPath("observatory_id").description("The updated ID of the observatory the resource belongs to. (optional)").optional(),
                )
                throws(
                    ResourceNotFound::class,
                    ResourceNotModifiable::class,
                    RosettaStoneStatementResourceNotModifiable::class,
                    InvalidLabel::class,
                    ReservedClass::class,
                    InvalidClassCollection::class,
                    ObservatoryNotFound::class,
                    OrganizationNotFound::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class,
                    NotACurator::class,
                )
            }

        verify(exactly = 1) { resourceService.update(command) }
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
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_class_collection")

        verify(exactly = 1) { resourceService.update(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("When creating a resource, and service succeeds, then status is 201 CREATED")
    fun create() {
        val id = ThingId("R213")
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateResourceRequest(
            id = id,
            label = "foo",
            classes = setOf(Classes.dataset),
            extractionMethod = ExtractionMethod.MANUAL
        )

        every { resourceService.create(any()) } returns id
        every { contributorService.findById(any()) } returns Optional.of(createContributor(contributorId))

        documentedPostRequestTo("/api/resources")
            .content(command)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/resources/$id")))
            .andDocument {
                summary("Creating resources")
                description(
                    """
                    A `POST` request creates a new resource with a given label.
                    An optional set of classes can be provided.
                    The response will be `201 Created` when successful.
                    The resource can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the created resource can be fetched from."),
                )
                requestFields<CreateResourceRequest>(
                    fieldWithPath("id").description("The id for the resource. (optional)").optional(),
                    fieldWithPath("label").description("The resource label."),
                    fieldWithPath("classes[]").type("Array").description("The classes of the resource. (optional)").optional(),
                    fieldWithPath("extraction_method").type("Enum").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: `UNKNOWN`)""").optional()
                )
                throws(InvalidLabel::class, ReservedClass::class, InvalidClassCollection::class, ResourceAlreadyExists::class)
            }

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
    }

    @Test
    @TestWithMockUser
    @DisplayName("When deleting a resource, and service succeeds, then status is 204 NO CONTENT")
    fun deleteById() {
        val id = ThingId("R213")

        every { resourceService.delete(id, any()) } just runs

        documentedDeleteRequestTo("/api/resources/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Deleting resources")
                description(
                    """
                    A `DELETE` request with the id of the resource to delete.
                    The response will be `204 NO CONTENT` when successful.
                    
                    [NOTE]
                    ====
                    1. If the resource doesn't exist, the return status will be `404 NOT FOUND`.
                    2. If the resource is not modifiable, the return status will be `403 FORBIDDEN`.
                    3. If the resource is used as an object in a statement, the return status will be `403 FORBIDDEN`.
                    4. If the performing user is not the creator of the resource and does not have the curator role, the return status will be `403 FORBIDDEN`.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the resource.")
                )
                throws(
                    ResourceNotFound::class,
                    ResourceNotModifiable::class,
                    ResourceInUse::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class
                )
            }

        verify(exactly = 1) { resourceService.delete(id, ContributorId(MockUserId.USER)) }
    }
}
