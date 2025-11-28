package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.common.thingIdConstraint
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.CreateLiteratureListRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListListSectionRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListListSectionRequest.Entry
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListTextSectionRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.PublishLiteratureListRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.UpdateLiteratureListRequest
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.CreateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.CreateLiteratureListUseCase
import org.orkg.contenttypes.input.DeleteLiteratureListSectionUseCase
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.UpdateLiteratureListUseCase
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.literatureListResponseFields
import org.orkg.contenttypes.input.testing.fixtures.paperResponseFields
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityFilterValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectLiteratureList
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
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

@ContextConfiguration(classes = [LiteratureListController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [LiteratureListController::class])
internal class LiteratureListControllerUnitTest : MockMvcBaseTest("literature-lists") {
    @MockkBean
    private lateinit var literatureListService: LiteratureListUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a literature list, when it is fetched by id and service succeeds, then status is 200 OK and literature list is returned")
    fun findById() {
        val literatureList = createLiteratureList()
        every { literatureListService.findById(literatureList.id) } returns Optional.of(literatureList)

        documentedGetRequestTo("/api/literature-lists/{id}", literatureList.id)
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectLiteratureList()
            .andDocument {
                summary("Fetching literature lists")
                description(
                    """
                    A `GET` request provides information about a literature list.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the literature list to retrieve."),
                )
                responseFields<LiteratureListRepresentation>(literatureListResponseFields())
                throws(LiteratureListNotFound::class)
            }

        verify(exactly = 1) { literatureListService.findById(literatureList.id) }
    }

    @Test
    @DisplayName("Given several literature lists, when they are fetched, then status is 200 OK and literature lists are returned")
    fun getPaged() {
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createLiteratureList())

        documentedGetRequestTo("/api/literature-lists")
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteratureList("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several literature lists, when filtering by several parameters, then status is 200 OK and literature lists are returned")
    fun findAll() {
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createLiteratureList())

        val title = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val published = true
        val sdg = ThingId("SDG_1")

        documentedGetRequestTo("/api/literature-lists")
            .param("title", title)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("published", published.toString())
            .param("sdg", sdg.value)
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectLiteratureList("$.content[*]")
            .andDocument {
                summary("Listing literature lists")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<literature-lists-fetch,literature lists>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("title").description("A search term that must be contained in the title of the literature list. (optional)").optional(),
                    parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("visibility").description("""Optional filter for visibility. Either of $allowedVisibilityFilterValues.""").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this literature list. (optional)").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").optional(),
                    parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("published").description("Filter for the publication status of the literature lists. (optional)").optional(),
                    parameterWithName("sdg").description("Filter for the sustainable development goal that the literature list belongs to. (optional)").optional(),
                )
                pagedResponseFields<LiteratureListRepresentation>(literatureListResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            literatureListService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe title
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                published = published,
                sustainableDevelopmentGoal = sdg
            )
        }
    }

    @Test
    fun `Given several literature lists, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/literature-lists")
            .param("sort", "unknown")
            .accept(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list create request, when service succeeds, it creates the literature list")
    fun create() {
        val id = ThingId("R123")
        every { literatureListService.create(any<CreateLiteratureListUseCase.CreateCommand>()) } returns id

        documentedPostRequestTo("/api/literature-lists")
            .content(createLiteratureListRequest())
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Creating literature lists")
                description(
                    """
                    A `POST` request creates a new literature list with all the given parameters.
                    The response will be `201 Created` when successful.
                    The literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the literature list can be fetched from."),
                )
                requestFields<CreateLiteratureListRequest>(
                    fieldWithPath("title").description("The title of the literature list."),
                    fieldWithPath("research_fields").description("The list of research fields the literature list will be assigned to."),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the literature list will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: `UNKNOWN`)""").optional(),
                    subsectionWithPath("sections[]").description("A list of sections of the literature list (optional). See <<literature-list-sections,literature list sections>> for more information. (optional)").optional(),
                    *authorListFields("literature list").toTypedArray(),
                )
                throws(
                    InvalidLabel::class,
                    OnlyOneResearchFieldAllowed::class,
                    ResearchFieldNotFound::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    SustainableDevelopmentGoalNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    InvalidDescription::class,
                    ResourceNotFound::class,
                    InvalidListSectionEntry::class,
                    InvalidHeadingSize::class,
                )
            }

        verify(exactly = 1) { literatureListService.create(any<CreateLiteratureListUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section create request, when service succeeds, it creates the list section")
    fun createSection_list() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123")),
                Entry(ThingId("R456"))
            )
        )
        every { literatureListService.create(any<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/literature-lists/{id}/sections", id)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Creating list sections")
                description(
                    """
                    A `POST` request creates a new list section and adds it to the specified literature list.
                    The response will be `201 Created` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListListSectionRequest>(
                    fieldWithPath("entries").description("""The list entries that should be part of this section."""),
                    fieldWithPath("entries[].id").description("""The id of the linked resource. Every resource must either be an instance of "Paper", "Dataset" or "Software"."""),
                    fieldWithPath("entries[].description").type("String").description("""The description of the entry. (optional)""").optional(),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    InvalidDescription::class,
                    ResourceNotFound::class,
                    InvalidListSectionEntry::class,
                )
            }

        verify(exactly = 1) {
            literatureListService.create(
                withArg<CreateLiteratureListSectionUseCase.CreateListSectionCommand> {
                    it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section create request, when service succeeds, it creates the list section at the specified index")
    fun createSectionAtIndex_list() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123")),
                Entry(ThingId("R456"))
            )
        )
        every { literatureListService.create(any<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/literature-lists/{id}/sections/{index}", id, index)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Creating list sections")
                description(
                    """
                    A `POST` request creates a new list section and adds it to the specified literature list.
                    The response will be `201 Created` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created list section will be appended at the end of the literature list."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListListSectionRequest>(
                    fieldWithPath("entries").description("""The list entries that should be part of this section."""),
                    fieldWithPath("entries[].id").description("""The id of the linked resource. Every resource must either be an instance of "Paper", "Dataset" or "Software"."""),
                    fieldWithPath("entries[].description").type("String").description("""The description of the entry. (optional)""").optional(),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    InvalidDescription::class,
                    ResourceNotFound::class,
                    InvalidListSectionEntry::class,
                )
            }

        verify(exactly = 1) {
            literatureListService.create(
                withArg<CreateLiteratureListSectionUseCase.CreateListSectionCommand> {
                    it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section create request, when service succeeds, it creates the text section")
    fun createSection_text() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 2,
            text = "text contents"
        )
        every { literatureListService.create(any<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()) } returns sectionId

        post("/api/literature-lists/{id}/sections", id)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Creating text sections")
                description(
                    """
                    A `POST` request creates a new text section and adds it to the specified literature list.
                    The response will be `201 Created` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListTextSectionRequest>(
                    fieldWithPath("heading").description("""The heading of the text section."""),
                    fieldWithPath("heading_size").description("""The heading size of the text section."""),
                    fieldWithPath("text").description("""The text contents of the text section."""),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidHeadingSize::class,
                )
            }

        verify(exactly = 1) {
            literatureListService.create(
                withArg<CreateLiteratureListSectionUseCase.CreateTextSectionCommand> {
                    it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section create request, when service succeeds, it creates the text section at the specified index")
    fun createSectionAtIndex_text() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 2,
            text = "text contents"
        )
        every { literatureListService.create(any<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/literature-lists/{id}/sections/{index}", id, index)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Creating text sections")
                description(
                    """
                    A `POST` request creates a new text section and adds it to the specified literature list.
                    The response will be `201 Created` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created text section will be appended at the end of the literature list."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListTextSectionRequest>(
                    fieldWithPath("heading").description("""The heading of the text section."""),
                    fieldWithPath("heading_size").description("""The heading size of the text section."""),
                    fieldWithPath("text").description("""The text contents of the text section."""),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidHeadingSize::class,
                )
            }

        verify(exactly = 1) {
            literatureListService.create(
                withArg<CreateLiteratureListSectionUseCase.CreateTextSectionCommand> {
                    it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list update request, when service succeeds, it updates the literature list")
    fun update() {
        val id = ThingId("R123")
        every { literatureListService.update(any<UpdateLiteratureListUseCase.UpdateCommand>()) } just runs

        documentedPutRequestTo("/api/literature-lists/{id}", id)
            .content(updateLiteratureListRequest())
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Updating literature lists")
                description(
                    """
                    A `PUT` request updates an existing literature list with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. All fields at the top level in the request can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    2. The same rules as for <<resources-edit,updating resources>> apply when updating the visibility of a literature list.
                    ====
                    
                    WARNING: Author names will not be updated if a resource id is specified for a given author.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the literature list."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<UpdateLiteratureListRequest>(
                    fieldWithPath("title").description("The title of the literature list. (optional)").optional(),
                    fieldWithPath("research_fields").description("The list of research fields the literature list will be assigned to. (optional)").optional(),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the literature list will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: `UNKNOWN`)""").optional(),
                    subsectionWithPath("sections[]").description("The updated list of sections of the literature list (optional). See <<literature-list-sections,literature list sections>> for more information.").optional(),
                    fieldWithPath("visibility").description("The updated visibility of the literature list. Can be one of $allowedVisibilityValues. (optional)").optional(),
                    *authorListFields("literature list", optional = true).toTypedArray(),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    LiteratureListSectionTypeMismatch::class,
                    InvalidDescription::class,
                    ResourceNotFound::class,
                    InvalidListSectionEntry::class,
                    InvalidLabel::class,
                    InvalidHeadingSize::class,
                )
            }

        verify(exactly = 1) { literatureListService.update(any<UpdateLiteratureListUseCase.UpdateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section update request, when service succeeds, it updates the list section")
    fun updateSection_list() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = LiteratureListListSectionRequest(
            entries = listOf(Entry(ThingId("R123")), Entry(ThingId("R456")))
        )
        every { literatureListService.update(any<UpdateLiteratureListSectionUseCase.UpdateCommand>()) } just runs

        documentedPutRequestTo("/api/literature-lists/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Updating list sections")
                description(
                    """
                    A `PUT` request updates an existing list section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list the section belongs to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListListSectionRequest>(
                    fieldWithPath("entries").description("""The list of updated entries that should be part of this section."""),
                    fieldWithPath("entries[].id").description("""The id of the linked resource. Every resource must either be an instance of "Paper", "Dataset" or "Software"."""),
                    fieldWithPath("entries[].description").type("String").description("""The description of the entry. (optional)""").optional(),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    UnrelatedLiteratureListSection::class,
                    LiteratureListSectionTypeMismatch::class,
                    InvalidDescription::class,
                    ResourceNotFound::class,
                    InvalidListSectionEntry::class,
                )
            }

        verify(exactly = 1) { literatureListService.update(any<UpdateLiteratureListSectionUseCase.UpdateListSectionCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section update request, when service succeeds, it updates the text section")
    fun updateSection_text() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = LiteratureListTextSectionRequest(
            heading = "updated heading",
            headingSize = 3,
            text = "updated text contents"
        )
        every { literatureListService.update(any<UpdateLiteratureListSectionUseCase.UpdateCommand>()) } just runs

        documentedPutRequestTo("/api/literature-lists/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Updating text sections")
                description(
                    """
                    A `PUT` request updates an existing text section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list the section belongs to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                requestFields<LiteratureListTextSectionRequest>(
                    fieldWithPath("heading").description("""The updated heading of the text section."""),
                    fieldWithPath("heading_size").description("""The updated heading size of the text section."""),
                    fieldWithPath("text").description("""The updated text contents of the text section."""),
                )
                throws(
                    LiteratureListNotModifiable::class,
                    LiteratureListNotFound::class,
                    UnrelatedLiteratureListSection::class,
                    LiteratureListSectionTypeMismatch::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidHeadingSize::class,
                )
            }

        verify(exactly = 1) { literatureListService.update(any<UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list section delete request, when service succeeds, it returns status 204 NO CONTENT")
    fun deleteSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val command = DeleteLiteratureListSectionUseCase.DeleteCommand(
            id,
            sectionId,
            ContributorId(MockUserId.USER)
        )
        every { literatureListService.delete(command) } just runs

        documentedDeleteRequestTo("/api/literature-lists/{id}/sections/{sectionId}", id, sectionId)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDocument {
                summary("Deleting literature list sections")
                description(
                    """
                    A `DELETE` request deletes a literature list section by ID.
                    The response will be `204 No Content` when successful.
                    The updated literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the literature list the section belongs to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated literature list can be fetched from."),
                )
                throws(LiteratureListNotModifiable::class, LiteratureListNotFound::class)
            }

        verify(exactly = 1) { literatureListService.delete(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a published literature list, when fetching its contents (paper), it returns success")
    fun findPublishedContentById_paper() {
        val id = ThingId("R3541")
        val contentId = ThingId("R123")
        every { literatureListService.findPublishedContentById(any(), any()) } returns Either.left(createPaper())

        documentedGetRequestTo("/api/literature-lists/{id}/published-contents/{contentId}", id, contentId)
            .perform()
            .andExpect(status().isOk)
            .andExpectPaper()
            .andDocument {
                summary("Fetching published literature list contents")
                description(
                    """
                    A `GET` request returns contents of an already published literature list, at the state of publishing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the published literature list."),
                    parameterWithName("contentId").description("The id of the resource to fetch."),
                )
                responseFields<PaperRepresentation>(paperResponseFields())
                throws(LiteratureListNotFound::class, PublishedLiteratureListContentNotFound::class)
            }

        verify(exactly = 1) { literatureListService.findPublishedContentById(any(), any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a published literature list, when fetching its contents (resource), it returns success")
    fun findPublishedContentById_resource() {
        val id = ThingId("R3541")
        val contentId = ThingId("R123")
        every { literatureListService.findPublishedContentById(any(), any()) } returns Either.right(createResource(id = contentId))
        every { statementService.countIncomingStatementsById(contentId) } returns 0

        documentedGetRequestTo("/api/literature-lists/{id}/published-contents/{contentId}", id, contentId)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDocument {
                summary("Fetching published literature list contents")
                description(
                    """
                    A `GET` request returns contents of an already published literature list, at the state of publishing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the published literature list."),
                    parameterWithName("contentId").description("The id of the resource to fetch."),
                )
                responseFields<ResourceRepresentation>(resourceResponseFields())
                throws(LiteratureListNotFound::class, PublishedLiteratureListContentNotFound::class)
            }

        verify(exactly = 1) { literatureListService.findPublishedContentById(any(), any()) }
        verify(exactly = 1) { statementService.countIncomingStatementsById(contentId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list, when publishing, then status 204 NO CONTENT")
    fun publish() {
        val id = ThingId("R123")
        val changelog = "new papers added"
        val request = mapOf(
            "changelog" to changelog
        )
        val literatureListVersionId = ThingId("R456")

        every { literatureListService.publish(any()) } returns literatureListVersionId

        documentedPostRequestTo("/api/literature-lists/{id}/publish", id)
            .content(request)
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/literature-lists/$literatureListVersionId")))
            .andDocument {
                summary("Publishing literature lists")
                description(
                    """
                    A `POST` request publishes an existing literature list with the given parameters.
                    In the process, a new literature list published resource is created and linked to the original literature list resource.
                    All statements containing the sections of the original literature list are archived in a separate database.
                    The response will be `201 Created` when successful.
                    The published literature list (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the literature list to publish.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the published literature list can be fetched from.")
                )
                requestFields<PublishLiteratureListRequest>(
                    fieldWithPath("changelog").description("The description of changes that have been made since the previous version."),
                )
                throws(
                    LiteratureListNotFound::class,
                    LiteratureListAlreadyPublished::class,
                    InvalidDescription::class,
                )
            }

        verify(exactly = 1) {
            literatureListService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.changelog shouldBe changelog
                }
            )
        }
    }

    private fun createLiteratureListRequest() =
        CreateLiteratureListRequest(
            title = "Dummy Literature List Label",
            researchFields = listOf(ThingId("R14")),
            authors = listOf(
                AuthorRequest(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI.create("https://example.org/author")
                ),
                AuthorRequest(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(
                ThingId("SDG_3"),
                ThingId("SDG_4")
            ),
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            ),
            extractionMethod = ExtractionMethod.MANUAL,
            sections = listOf(
                textSectionRequest(),
                listSectionRequest()
            )
        )

    private fun updateLiteratureListRequest() =
        UpdateLiteratureListRequest(
            title = "Dummy Literature List Label",
            researchFields = listOf(ThingId("R14")),
            authors = listOf(
                AuthorRequest(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI.create("https://example.org/author")
                ),
                AuthorRequest(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(
                ThingId("SDG_3"),
                ThingId("SDG_4")
            ),
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            ),
            extractionMethod = ExtractionMethod.MANUAL,
            sections = listOf(
                textSectionRequest(),
                listSectionRequest()
            ),
            visibility = Visibility.FEATURED
        )

    private fun textSectionRequest() =
        LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 1,
            text = "text contents"
        )

    private fun listSectionRequest() =
        LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123"), "Example description of an entry"),
                Entry(ThingId("R456"))
            )
        )
}
