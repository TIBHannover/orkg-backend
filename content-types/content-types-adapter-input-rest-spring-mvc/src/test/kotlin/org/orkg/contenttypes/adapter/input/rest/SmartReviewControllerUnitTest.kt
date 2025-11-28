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
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.common.thingIdConstraint
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.CreateSmartReviewRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.PublishSmartReviewRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewComparisonSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewOntologySectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewPredicateSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewResourceSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewTextSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewVisualizationSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.UpdateSmartReviewRequest
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.PublishedSmartReviewContentNotFound
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
import org.orkg.contenttypes.domain.Visualization
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.domain.testing.fixtures.createVisualization
import org.orkg.contenttypes.input.CreateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.DeleteSmartReviewSectionUseCase
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.UpdateSmartReviewUseCase
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.contenttypes.input.testing.fixtures.comparisonResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.smartReviewResponseFields
import org.orkg.contenttypes.input.testing.fixtures.statementListResponseFields
import org.orkg.contenttypes.input.testing.fixtures.visualizationResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityFilterValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectComparison
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectSmartReview
import org.orkg.testing.andExpectStatementList
import org.orkg.testing.andExpectVisualization
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

@ContextConfiguration(classes = [SmartReviewController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [SmartReviewController::class])
internal class SmartReviewControllerUnitTest : MockMvcBaseTest("smart-reviews") {
    @MockkBean
    private lateinit var smartReviewService: SmartReviewUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Test
    @DisplayName("Given a smart review, when it is fetched by id and service succeeds, then status is 200 OK and smart review is returned")
    fun findById() {
        val smartReview = createSmartReview()
        every { smartReviewService.findById(smartReview.id) } returns Optional.of(smartReview)

        documentedGetRequestTo("/api/smart-reviews/{id}", smartReview.id)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectSmartReview()
            .andDocument {
                summary("Fetching smart reviews")
                description(
                    """
                    A `GET` request provides information about a smart review.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the smart review to retrieve."),
                )
                responseFields<SmartReviewRepresentation>(smartReviewResponseFields())
                throws(SmartReviewNotFound::class)
            }

        verify(exactly = 1) { smartReviewService.findById(smartReview.id) }
    }

    @Test
    @DisplayName("Given several smart reviews, when they are fetched, then status is 200 OK and smart reviews are returned")
    fun getPaged() {
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createSmartReview())

        documentedGetRequestTo("/api/smart-reviews")
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectSmartReview("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several smart reviews, when filtering by several parameters, then status is 200 OK and smart reviews are returned")
    fun findAll() {
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createSmartReview())

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

        documentedGetRequestTo("/api/smart-reviews")
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
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectSmartReview("$.content[*]")
            .andDocument {
                summary("Listing smart reviews")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<smart-reviews-fetch,smart reviews>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("title").description("A search term that must be contained in the title of the smart review. (optional)").optional(),
                    parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("visibility").description("""Optional filter for visibility. Either of $allowedVisibilityFilterValues.""").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this smart review. (optional)").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)").optional(),
                    parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("published").description("Filter for the publication status of the smart reviews. (optional)").optional(),
                    parameterWithName("sdg").description("Filter for the sustainable development goal that the smart review belongs to. (optional)").optional(),
                )
                pagedResponseFields<SmartReviewRepresentation>(smartReviewResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            smartReviewService.findAll(
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
    fun `Given several smart reviews, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/smart-reviews")
            .param("sort", "unknown")
            .accept(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a smart review create request, when service succeeds, it creates the smart review")
    fun create() {
        val id = ThingId("R123")
        every { smartReviewService.create(any<CreateSmartReviewUseCase.CreateCommand>()) } returns id

        documentedPostRequestTo("/api/smart-reviews")
            .content(createSmartReviewRequest())
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating smart reviews")
                description(
                    """
                    A `POST` request creates a new smart review with all the given parameters.
                    The response will be `201 Created` when successful.
                    The smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<CreateSmartReviewRequest>(
                    fieldWithPath("title").description("The title of the smart review."),
                    fieldWithPath("research_fields").description("The list of research fields the smart review will be assigned to."),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the smart review will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: `UNKNOWN`)""").optional(),
                    subsectionWithPath("sections").description("The list of sections of the smart review. See <<smart-review-sections,smart review sections>> for more information. (optional)").optional(),
                    fieldWithPath("references[]").description("The list of bibtex references of the smart review. (optional)").optional(),
                    *authorListFields("smart review").toTypedArray(),
                )
                throws(
                    InvalidLabel::class,
                    InvalidBibTeXReference::class,
                    OnlyOneResearchFieldAllowed::class,
                    ResearchFieldNotFound::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    SustainableDevelopmentGoalNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    ComparisonNotFound::class,
                    VisualizationNotFound::class,
                    ResourceNotFound::class,
                    PredicateNotFound::class,
                    OntologyEntityNotFound::class,
                    InvalidDescription::class,
                    InvalidSmartReviewTextSectionType::class,
                )
            }

        verify(exactly = 1) { smartReviewService.create(any<CreateSmartReviewUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison section create request, when service succeeds, it creates the comparison section")
    fun createSection_comparison() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = comparisonSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating comparison sections")
                description(
                    """
                    A `POST` request creates a new comparison section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewComparisonSectionRequest>(
                    fieldWithPath("heading").description("The heading of the comparison section."),
                    fieldWithPath("comparison").description("The id of the linked comparison. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    ComparisonNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison section create request, when service succeeds, it creates the comparison section at the specified index")
    fun createSectionAtIndex_comparison() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = comparisonSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating comparison sections")
                description(
                    """
                    A `POST` request creates a new comparison section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created comparison section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewComparisonSectionRequest>(
                    fieldWithPath("heading").description("The heading of the comparison section."),
                    fieldWithPath("comparison").description("The id of the linked comparison. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    ComparisonNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a visualization section create request, when service succeeds, it creates the visualization section")
    fun createSection_visualization() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = visualizationSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating visualization sections")
                description(
                    """
                    A `POST` request creates a new visualization section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewVisualizationSectionRequest>(
                    fieldWithPath("heading").description("The heading of the visualization section."),
                    fieldWithPath("visualization").description("The id of the linked visualization. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    VisualizationNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a visualization section create request, when service succeeds, it creates the visualization section at the specified index")
    fun createSectionAtIndex_visualization() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = visualizationSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating visualization sections")
                description(
                    """
                    A `POST` request creates a new visualization section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created visualization section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewVisualizationSectionRequest>(
                    fieldWithPath("heading").description("The heading of the visualization section."),
                    fieldWithPath("visualization").description("The id of the linked visualization. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    VisualizationNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource section create request, when service succeeds, it creates the resource section")
    fun createSection_resource() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = resourceSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating resource sections")
                description(
                    """
                    A `POST` request creates a new resource section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewResourceSectionRequest>(
                    fieldWithPath("heading").description("The heading of the resource section."),
                    fieldWithPath("resource").description("The id of the linked resource. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    ResourceNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource section create request, when service succeeds, it creates the resource section at the specified index")
    fun createSectionAtIndex_resource() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = resourceSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating resource sections")
                description(
                    """
                    A `POST` request creates a new resource section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created resource section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewResourceSectionRequest>(
                    fieldWithPath("heading").description("The heading of the resource section."),
                    fieldWithPath("resource").description("The id of the linked resource. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    ResourceNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate section create request, when service succeeds, it creates the predicate section")
    fun createSection_predicate() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = predicateSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating predicate sections")
                description(
                    """
                    A `POST` request creates a new predicate section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewPredicateSectionRequest>(
                    fieldWithPath("heading").description("The heading of the predicate section."),
                    fieldWithPath("predicate").description("The id of the linked predicate. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate section create request, when service succeeds, it creates the predicate section at the specified index")
    fun createSectionAtIndex_predicate() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = predicateSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating predicate sections")
                description(
                    """
                    A `POST` request creates a new predicate section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created predicate section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewPredicateSectionRequest>(
                    fieldWithPath("heading").description("The heading of the predicate section."),
                    fieldWithPath("predicate").description("The id of the linked predicate. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a ontology section create request, when service succeeds, it creates the ontology section")
    fun createSection_ontology() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = ontologySectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating ontology sections")
                description(
                    """
                    A `POST` request creates a new ontology section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewOntologySectionRequest>(
                    fieldWithPath("heading").description("The heading of the ontology section."),
                    fieldWithPath("entities[]").description("The id of the entities that should be shown in the ontology section."),
                    fieldWithPath("predicates[]").description("The ids of the predicates that should be shown in the ontology section."),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    OntologyEntityNotFound::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand>()
                    it.index shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a ontology section create request, when service succeeds, it creates the ontology section at the specified index")
    fun createSectionAtIndex_ontology() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val index = 5
        val request = ontologySectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating ontology sections")
                description(
                    """
                    A `POST` request creates a new ontology section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created ontology section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewOntologySectionRequest>(
                    fieldWithPath("heading").description("The heading of the ontology section."),
                    fieldWithPath("entities[]").description("The id of the entities that should be shown in the ontology section."),
                    fieldWithPath("predicates[]").description("The ids of the predicates that should be shown in the ontology section.")
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    OntologyEntityNotFound::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand>()
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
        val request = textSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateTextSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating text sections")
                description(
                    """
                    A `POST` request creates a new text section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewTextSectionRequest>(
                    fieldWithPath("heading").description("The heading of the text section."),
                    fieldWithPath("text").description("The text contents of the text section."),
                    fieldWithPath("class").description("The id of the class that indicates the type of the text section. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidSmartReviewTextSectionType::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateTextSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateTextSectionCommand>()
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
        val request = textSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateTextSectionCommand>()) } returns sectionId

        documentedPostRequestTo("/api/smart-reviews/{id}/sections/{index}", id, index)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Creating text sections")
                description(
                    """
                    A `POST` request creates a new text section and adds it to the specified smart review.
                    The response will be `201 Created` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("index").description("The insertion index the of the section. Otherwise, the created text section will be appended at the end of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewTextSectionRequest>(
                    fieldWithPath("heading").description("The heading of the text section."),
                    fieldWithPath("text").description("The text contents of the text section."),
                    fieldWithPath("class").description("The id of the class that indicates the type of the text section. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewNotModifiable::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidSmartReviewTextSectionType::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.create(
                withArg<CreateSmartReviewSectionUseCase.CreateTextSectionCommand> {
                    it.shouldBeInstanceOf<CreateSmartReviewSectionUseCase.CreateTextSectionCommand>()
                    it.index shouldBe index
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a smart review update request, when service succeeds, it updates the smart review")
    fun update() {
        val id = ThingId("R123")
        every { smartReviewService.update(any<UpdateSmartReviewUseCase.UpdateCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}", id)
            .content(updateSmartReviewRequest())
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating smart reviews")
                description(
                    """
                    A `PUT` request updates an existing smart review with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. All fields at the top level in the request can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    2. The same rules as for <<resources-edit,updating resources>> apply when updating the visibility of a smart review.
                    ====
                    
                    WARNING: Author names will not be updated if a resource id is specified for a given author.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the smart review."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<UpdateSmartReviewRequest>(
                    fieldWithPath("title").description("The title of the smart review. (optional)").optional(),
                    fieldWithPath("research_fields").description("The list of research fields the smart review will be assigned to. (optional)").optional(),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the smart review will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: `UNKNOWN`)""").optional(),
                    subsectionWithPath("sections").description("The list of updated sections of the smart review (optional). See <<smart-review-sections,smart review sections>> for more information.").optional(),
                    fieldWithPath("references[]").description("The list of updated bibtex references of the smart review. (optional)").optional(),
                    fieldWithPath("visibility").description("The updated visibility of the smart review. Can be one of $allowedVisibilityValues. (optional)").optional(),
                    *authorListFields("smart review", optional = true).toTypedArray(),
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    InvalidLabel::class,
                    InvalidBibTeXReference::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class,
                    OnlyOneResearchFieldAllowed::class,
                    ResearchFieldNotFound::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    SustainableDevelopmentGoalNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    ComparisonNotFound::class,
                    VisualizationNotFound::class,
                    ResourceNotFound::class,
                    PredicateNotFound::class,
                    OntologyEntityNotFound::class,
                    InvalidDescription::class,
                    InvalidSmartReviewTextSectionType::class,
                )
            }

        verify(exactly = 1) { smartReviewService.update(any<UpdateSmartReviewUseCase.UpdateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison section update request, when service succeeds, it updates the comparison section at the specified index")
    fun updateSection_comparison() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = comparisonSectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating comparison sections")
                description(
                    """
                    A `PUT` request updates an existing comparison section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewComparisonSectionRequest>(
                    fieldWithPath("heading").description("The heading of the comparison section."),
                    fieldWithPath("comparison").description("The id of the linked comparison."),
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    ComparisonNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a visualization section update request, when service succeeds, it updates the visualization section at the specified index")
    fun updateSection_visualization() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = visualizationSectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating visualization sections")
                description(
                    """
                    A `PUT` request updates an existing visualization section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewVisualizationSectionRequest>(
                    fieldWithPath("heading").description("The heading of the visualization section."),
                    fieldWithPath("visualization").description("The id of the linked visualization. (optional)").optional(),
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    VisualizationNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource section update request, when service succeeds, it updates the resource section at the specified index")
    fun updateSection_resource() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = resourceSectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating resource sections")
                description(
                    """
                    A `PUT` request updates an existing resource section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewResourceSectionRequest>(
                    fieldWithPath("heading").description("The heading of the resource section."),
                    fieldWithPath("resource").description("The id of the linked resource. (optional)").optional()
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    ResourceNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate section update request, when service succeeds, it updates the predicate section at the specified index")
    fun updateSection_predicate() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = predicateSectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating predicate sections")
                description(
                    """
                    A `PUT` request updates an existing predicate section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewPredicateSectionRequest>(
                    fieldWithPath("heading").description("The heading of the predicate section."),
                    fieldWithPath("predicate").description("The id of the linked predicate. (optional)").optional()
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a ontology section update request, when service succeeds, it updates the ontology section at the specified index")
    fun updateSection_ontology() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = ontologySectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating ontology sections")
                description(
                    """
                    A `PUT` request updates an existing ontology section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewOntologySectionRequest>(
                    fieldWithPath("heading").description("The heading of the ontology section."),
                    fieldWithPath("entities[]").description("The ids of the entities that should be shown in the ontology section."),
                    fieldWithPath("predicates[]").description("The ids of the predicates that should be shown in the ontology section."),
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    OntologyEntityNotFound::class,
                    PredicateNotFound::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section update request, when service succeeds, it updates the text section at the specified index")
    fun updateSection_text() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = textSectionRequest()

        every { smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand>()) } just runs

        documentedPutRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("Updating text sections")
                description(
                    """
                    A `PUT` request updates an existing text section with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                requestFields<SmartReviewTextSectionRequest>(
                    fieldWithPath("heading").description("The heading of the text section."),
                    fieldWithPath("text").description("The text contents of the text section."),
                    fieldWithPath("class").description("The id of the class that indicates the type of the text section. An absent value indicates no type. (optional)").optional(),
                )
                throws(
                    SmartReviewNotModifiable::class,
                    SmartReviewNotFound::class,
                    UnrelatedSmartReviewSection::class,
                    SmartReviewSectionTypeMismatch::class,
                    InvalidLabel::class,
                    InvalidDescription::class,
                    InvalidSmartReviewTextSectionType::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a smart review section delete request, when service succeeds, it returns status 204 NO CONTENT")
    fun deleteSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val command = DeleteSmartReviewSectionUseCase.DeleteCommand(
            id,
            sectionId,
            ContributorId(MockUserId.USER)
        )
        every { smartReviewService.delete(command) } just runs

        documentedDeleteRequestTo("/api/smart-reviews/{id}/sections/{sectionId}", id, sectionId)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))
            .andDocument {
                summary("´Deleting smart review sections")
                description(
                    """
                    A `DELETE` request deletes a smart review section by ID.
                    The response will be `204 No Content` when successful.
                    The updated smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the smart review the section belongs to."),
                    parameterWithName("sectionId").description("The id of the section."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated smart review can be fetched from."),
                )
                throws(SmartReviewNotModifiable::class, SmartReviewNotFound::class)
            }

        verify(exactly = 1) { smartReviewService.delete(command) }
    }

    @Test
    @DisplayName("Given a published smart review, when fetching its contents (resource), returns success")
    fun findPublishedContentById_statements() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")

        every { smartReviewService.findPublishedContentById(any(), any()) } returns Either.right(listOf(createStatement(subject = createResource(sectionId))))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/smart-reviews/{id}/published-contents/{contentId}", id, sectionId)
            .perform()
            .andExpect(status().isOk)
            .andExpectStatementList()
            .andDocument {
                summary("Fetching published smart review contents")
                description(
                    """
                    A `GET` request returns contents of an already published smart review, at the state of publishing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the published smart review."),
                    parameterWithName("contentId").description("The id of the resource to fetch.")
                )
                responseFields<StatementListRepresentation>(statementListResponseFields())
                throws(SmartReviewNotFound::class, PublishedSmartReviewContentNotFound::class)
            }

        verify(exactly = 1) { smartReviewService.findPublishedContentById(id, sectionId) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
    }

    @Test
    @DisplayName("Given a published smart review, when fetching its contents (comparison), returns success")
    fun findPublishedContentById_comparison() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")

        every { smartReviewService.findPublishedContentById(any(), any()) } returns Either.left(createComparison())

        documentedGetRequestTo("/api/smart-reviews/{id}/published-contents/{contentId}", id, sectionId)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparison()
            .andDocument {
                summary("Fetching published smart review contents")
                description(
                    """
                    A `GET` request returns contents of an already published smart review, at the state of publishing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the published smart review."),
                    parameterWithName("contentId").description("The id of the resource to fetch.")
                )
                responseFields<ComparisonRepresentation>(comparisonResponseFields())
                throws(SmartReviewNotFound::class, PublishedSmartReviewContentNotFound::class)
            }

        verify(exactly = 1) { smartReviewService.findPublishedContentById(id, sectionId) }
    }

    @Test
    @DisplayName("Given a published smart review, when fetching its contents (visualization), returns success")
    fun findPublishedContentById_visualization() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")

        every { smartReviewService.findPublishedContentById(any(), any()) } returns Either.left(createVisualization())

        documentedGetRequestTo("/api/smart-reviews/{id}/published-contents/{contentId}", id, sectionId)
            .perform()
            .andExpect(status().isOk)
            .andExpectVisualization()
            .andDocument {
                summary("Fetching published smart review contents")
                description(
                    """
                    A `GET` request returns contents of an already published smart review, at the state of publishing.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The id of the published smart review."),
                    parameterWithName("contentId").description("The id of the resource to fetch.")
                )
                responseFields<Visualization>(visualizationResponseFields())
                throws(SmartReviewNotFound::class, PublishedSmartReviewContentNotFound::class)
            }

        verify(exactly = 1) { smartReviewService.findPublishedContentById(id, sectionId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a smart review, when publishing, then status 204 NO CONTENT")
    fun publish() {
        val id = ThingId("R123")
        val changelog = "new papers added"
        val assignDOI = true
        val description = "review description"
        val request = mapOf(
            "changelog" to changelog,
            "assign_doi" to assignDOI,
            "description" to description
        )
        val smartReviewVersionId = ThingId("R456")

        every { smartReviewService.publish(any()) } returns smartReviewVersionId

        documentedPostRequestTo("/api/smart-reviews/{id}/publish", id)
            .content(request)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/smart-reviews/$smartReviewVersionId")))
            .andDocument {
                summary("Publishing smart reviews")
                description(
                    """
                    A `POST` request publishes an existing smart review with the given parameters.
                    In the process, a new smart review published resource is created and linked to the original smart review resource.
                    All statements containing the sections of the original smart review are archived in a separate database.
                    Optionally, a DOI can be assigned to the published smart review resource.
                    The response will be `201 Created` when successful.
                    The published smart review (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the smart review to publish.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the published smart review can be fetched from."),
                )
                requestFields<PublishSmartReviewRequest>(
                    fieldWithPath("changelog").description("The description of changes that have been made since the previous version."),
                    fieldWithPath("assign_doi").description("Whether to assign a new DOI for the smart review when publishing."),
                    fieldWithPath("description").description("The description of the contents of the smart review. This description is used for the DOI metadata. It will be ignored when `assign_doi` is set to `false`. (optional)").optional(),
                )
                throws(
                    SmartReviewNotFound::class,
                    SmartReviewAlreadyPublished::class,
                    InvalidDescription::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    InvalidLabel::class,
                    ServiceUnavailable::class,
                )
            }

        verify(exactly = 1) {
            smartReviewService.publish(
                withArg {
                    it.smartReviewId shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.changelog shouldBe changelog
                    it.assignDOI shouldBe assignDOI
                    it.description shouldBe description
                }
            )
        }
    }

    private fun createSmartReviewRequest() =
        CreateSmartReviewRequest(
            title = "Dummy Smart Review Label",
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
                comparisonSectionRequest(),
                visualizationSectionRequest(),
                resourceSectionRequest(),
                predicateSectionRequest(),
                ontologySectionRequest(),
                textSectionRequest()
            ),
            references = listOf(
                "reference 1",
                "reference 2"
            )
        )

    private fun updateSmartReviewRequest() =
        UpdateSmartReviewRequest(
            title = "Dummy Smart Review Label",
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
                comparisonSectionRequest(),
                visualizationSectionRequest(),
                resourceSectionRequest(),
                predicateSectionRequest(),
                ontologySectionRequest(),
                textSectionRequest()
            ),
            references = listOf(
                "updated reference 1",
                "updated reference 2"
            ),
            visibility = Visibility.FEATURED
        )

    private fun comparisonSectionRequest() =
        SmartReviewComparisonSectionRequest(
            heading = "comparison section heading",
            comparison = ThingId("comparisonId")
        )

    private fun visualizationSectionRequest() =
        SmartReviewVisualizationSectionRequest(
            heading = "visualization section heading",
            visualization = ThingId("visualizationId")
        )

    private fun resourceSectionRequest() =
        SmartReviewResourceSectionRequest(
            heading = "resource section heading",
            resource = ThingId("resourceId")
        )

    private fun predicateSectionRequest() =
        SmartReviewPredicateSectionRequest(
            heading = "predicate section heading",
            predicate = ThingId("predicateId")
        )

    private fun ontologySectionRequest() =
        SmartReviewOntologySectionRequest(
            heading = "ontology section heading",
            entities = listOf(ThingId("resourceId")),
            predicates = listOf(ThingId("predicateId"))
        )

    private fun textSectionRequest() =
        SmartReviewTextSectionRequest(
            heading = "text section heading",
            `class` = Classes.epilogue,
            text = "epilogue"
        )
}
