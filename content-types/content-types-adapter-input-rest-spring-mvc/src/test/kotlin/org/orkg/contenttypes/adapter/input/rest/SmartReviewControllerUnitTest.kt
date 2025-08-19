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
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.CreateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.DeleteSmartReviewSectionUseCase
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.UpdateSmartReviewUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
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
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectSmartReview
import org.orkg.testing.andExpectStatementList
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@ContextConfiguration(
    classes = [
        SmartReviewController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        ContentTypeJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
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
    fun getSingle() {
        val smartReview = createSmartReview()
        every { smartReviewService.findById(smartReview.id) } returns Optional.of(smartReview)

        documentedGetRequestTo("/api/smart-reviews/{id}", smartReview.id)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectSmartReview()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the smart review to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the smart review."),
                        fieldWithPath("title").description("The title of the smart review."),
                        fieldWithPath("research_fields").description("The list of research fields the smart review is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("versions.head").description("The head version of the smart review."),
                        fieldWithPath("versions.head.id").description("The id of the head version."),
                        fieldWithPath("versions.head.label").description("The label of the head version."),
                        timestampFieldWithPath("versions.head.created_at", "the head version was created"),
                        fieldWithPath("versions.head.created_by").type("String").description("The UUID of the user or service who created the smart review version."),
                        fieldWithPath("versions.published").description("The list of published versions of the smart review."),
                        fieldWithPath("versions.published[].id").description("The id of the published version."),
                        fieldWithPath("versions.published[].label").description("The label of the published version."),
                        timestampFieldWithPath("versions.published[].created_at", "the published version was created"),
                        fieldWithPath("versions.published[].created_by").type("String").description("The UUID of the user or service who created the review version version."),
                        fieldWithPath("versions.published[].changelog").description("The changelog of the published version."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the smart review resource. Can be one of $allowedExtractionMethodValues."""),
                        timestampFieldWithPath("created_at", "the smart review resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this smart review."),
                        fieldWithPath("visibility").description("""Visibility of the smart review. Can be one of $allowedVisibilityValues."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this smart review.").optional(),
                        fieldWithPath("published").description("Whether the smart review is published or not."),
                        fieldWithPath("sections").description("The list of sections of the smart review."),
                        fieldWithPath("sections[].id").description("The id of the section."),
                        fieldWithPath("sections[].heading").description("The heading of the section.").optional(),
                        fieldWithPath("sections[].type").description("""The type of the section. Either of "text", "comparison", "visualization", "resource", "property" or "ontology"."""),
                        fieldWithPath("sections[].comparison").description("The linked comparison of a comparison section.").optional(),
                        fieldWithPath("sections[].comparison.id").description("The id of the linked comparison.").optional(),
                        fieldWithPath("sections[].comparison.label").description("The label of the linked comparison.").optional(),
                        fieldWithPath("sections[].comparison.classes").description("The classes of the linked comparison.").optional(),
                        fieldWithPath("sections[].comparison._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`.").optional(),
                        fieldWithPath("sections[].visualization").description("The linked visualization of a visualization section.").optional(),
                        fieldWithPath("sections[].visualization.id").description("The id of the linked visualization.").optional(),
                        fieldWithPath("sections[].visualization.label").description("The label of the linked visualization.").optional(),
                        fieldWithPath("sections[].visualization.classes").description("The classes of the linked visualization.").optional(),
                        fieldWithPath("sections[].visualization._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`.").optional(),
                        fieldWithPath("sections[].resource").description("The linked resource of a resource section.").optional(),
                        fieldWithPath("sections[].resource.id").description("The id of the linked resource.").optional(),
                        fieldWithPath("sections[].resource.label").description("The label of the linked resource.").optional(),
                        fieldWithPath("sections[].resource.classes").description("The classes of the linked resource.").optional(),
                        fieldWithPath("sections[].resource._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`.").optional(),
                        fieldWithPath("sections[].predicate").description("The linked resource of a predicate section.").optional(),
                        fieldWithPath("sections[].predicate.id").description("The id of the linked predicate.").optional(),
                        fieldWithPath("sections[].predicate.label").description("The label of the linked predicate.").optional(),
                        fieldWithPath("sections[].predicate._class").description("Indicates which type of entity was returned. Always has the value `predicate_ref`.").optional(),
                        fieldWithPath("sections[].entities").description("The entities that should be shown in the ontology section. They can either be a resource or a predicate.").optional(),
                        fieldWithPath("sections[].entities[].id").description("The id of the entity.").optional(),
                        fieldWithPath("sections[].entities[].label").description("The label of the entity.").optional(),
                        fieldWithPath("sections[].entities[].classes").description("The classes of the entity, if the entity is a resource.").optional(),
                        fieldWithPath("sections[].entities[]._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`.").optional(),
                        fieldWithPath("sections[].predicates").description("The predicates that should be shown in the ontology section.").optional(),
                        fieldWithPath("sections[].predicates[].id").description("The id of the predicate.").optional(),
                        fieldWithPath("sections[].predicates[].label").description("The label of the predicate.").optional(),
                        fieldWithPath("sections[].predicates[]._class").description("Indicates which type of entity was returned. Always has the value `predicate_ref`.").optional(),
                        fieldWithPath("sections[].text").description("The text contents of the text section.").optional(),
                        fieldWithPath("sections[].classes").description("The additional classes of the text section.").optional(),
                        fieldWithPath("references").description("The list of bibtex references of the smart review.").optional(),
                        subsectionWithPath("acknowledgements").description("A key-value map of contributor ids to an estimated contribution percentage."),
                        fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `smart-review`."),
                    ).and(authorListFields("smart review"))
                        .and(sustainableDevelopmentGoalsFields("smart review"))
                        .and(smartReviewIdentifierFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun getPagedWithParameters() {
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
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("title").description("A search term that must be contained in the title of the smart review. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of $allowedVisibilityFilterValues."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this smart review. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("published").description("Filter for the publication status of the smart reviews. (optional)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the smart review belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    @DisplayName("Given a published smart review, when fetching its contents, returns success")
    fun findPublishedContentById() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")

        every { smartReviewService.findPublishedContentById(any(), any()) } returns Either.right(listOf(createStatement(subject = createResource(sectionId))))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/smart-reviews/{id}/published-contents/{contentId}", id, sectionId)
            .perform()
            .andExpect(status().isOk)
            .andExpectStatementList()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the published smart review."),
                        parameterWithName("contentId").description("The id of the resource to fetch.")
                    ),
                    responseFields(
                        fieldWithPath("_class").description("Indicates which type of entity was returned."),
                        subsectionWithPath("statements[]").description("The list of first-level <<statements-fetch,statements>>.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { smartReviewService.findPublishedContentById(id, sectionId) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
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
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the smart review."),
                        fieldWithPath("research_fields").description("The list of research fields the smart review will be assigned to."),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the smart review will be assigned to. (optional)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: "UNKNOWN")""").optional(),
                        subsectionWithPath("sections").description("The list of sections of the smart review. See <<smart-review-sections,smart review sections>> for more information. (optional)").optional(),
                        fieldWithPath("references[]").description("The list of bibtex references of the smart review. (optional)").optional(),
                    ).and(authorListFields("smart review"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { smartReviewService.create(any<CreateSmartReviewUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison section create request, when service succeeds, it creates the comparison section")
    fun createComparisonSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = comparisonSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createComparisonSectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created comparison section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the comparison section."),
                        fieldWithPath("comparison").description("The id of the linked comparison. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun createVisualizationSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = visualizationSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createVisualizationSectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created visualization section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the visualization section."),
                        fieldWithPath("visualization").description("The id of the linked visualization. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun createResourceSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = resourceSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateResourceSectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createResourceSectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created resource section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the resource section."),
                        fieldWithPath("resource").description("The id of the linked resource. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun createPredicateSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = predicateSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createPredicateSectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created predicate section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the predicate section."),
                        fieldWithPath("predicate").description("The id of the linked predicate. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun createOntologySection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = ontologySectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateOntologySectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createOntologySectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created ontology section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the ontology section."),
                        fieldWithPath("entities[]").description("The id of the entities that should be shown in the ontology section."),
                        fieldWithPath("predicates[]").description("The ids of the predicates that should be shown in the ontology section.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
    fun createTextSection() {
        val id = ThingId("R3541")
        val sectionId = ThingId("R123")
        val request = textSectionRequest()

        every { smartReviewService.create(any<CreateSmartReviewSectionUseCase.CreateTextSectionCommand>()) } returns sectionId

        post("/api/smart-reviews/{id}/sections", id)
            .content(request)
            .accept(SMART_REVIEW_SECTION_JSON_V1)
            .contentType(SMART_REVIEW_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/smart-reviews/$id")))

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
    fun createTextSectionAtIndex() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created text section will be appended at the end of the smart review. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The heading of the text section."),
                        fieldWithPath("text").description("The text contents of the text section."),
                        fieldWithPath("class").description("The id of the class that indicates the type of the text section. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the smart review.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the smart review. (optional)"),
                        fieldWithPath("research_fields").description("The list of research fields the smart review will be assigned to. (optional)"),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the smart review will be assigned to. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of $allowedExtractionMethodValues. (optional, default: "UNKNOWN")""").optional(),
                        subsectionWithPath("sections").description("The list of updated sections of the smart review (optional). See <<smart-review-sections,smart review sections>> for more information."),
                        fieldWithPath("references[]").description("The list of updated bibtex references of the smart review."),
                        fieldWithPath("visibility").description("The updated visibility of the smart review. Can be one of $allowedVisibilityValues. (optional)").optional(),
                    ).and(authorListFields("smart review"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { smartReviewService.update(any<UpdateSmartReviewUseCase.UpdateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison section update request, when service succeeds, it updates the comparison section at the specified index")
    fun updateComparisonSection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the comparison section."),
                        fieldWithPath("comparison").description("The updated id of the linked comparison.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a visualization section update request, when service succeeds, it updates the visualization section at the specified index")
    fun updateVisualizationSection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the visualization section."),
                        fieldWithPath("visualization").description("The updated id of the linked visualization.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a resource section update request, when service succeeds, it updates the resource section at the specified index")
    fun updateResourceSection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the resource section."),
                        fieldWithPath("resource").description("The updated id of the linked resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate section update request, when service succeeds, it updates the predicate section at the specified index")
    fun updatePredicateSection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the predicate section."),
                        fieldWithPath("predicate").description("The updated id of the linked predicate. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a ontology section update request, when service succeeds, it updates the ontology section at the specified index")
    fun updateOntologySection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the ontology section."),
                        fieldWithPath("entities[]").description("The updated id of the entities that should be shown in the ontology section."),
                        fieldWithPath("predicates[]").description("The updated ids of the predicates that should be shown in the ontology section.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.update(any<UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand>())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section update request, when service succeeds, it updates the text section at the specified index")
    fun updateTextSection() {
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review to which the new section should be appended to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("The updated heading of the text section."),
                        fieldWithPath("text").description("The updated text contents of the text section."),
                        fieldWithPath("class").description("The updated id of the class that indicates the type of the text section. An absent value indicates no type."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The id of the smart review the section belongs to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated smart review can be fetched from.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { smartReviewService.delete(command) }
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
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the smart review to publish.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the published smart review can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("changelog").description("The description of changes that have been made since the previous version."),
                        fieldWithPath("assign_doi").description("Whether to assign a new DOI for the smart review when publishing."),
                        fieldWithPath("description").description("The description of the contents of the smart review. This description is used for the DOI metadata. It will be ignored when `assign_doi` is set to `false`. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
        SmartReviewController.CreateSmartReviewRequest(
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
        SmartReviewController.UpdateSmartReviewRequest(
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
        SmartReviewController.SmartReviewComparisonSectionRequest(
            heading = "comparison section heading",
            comparison = ThingId("comparisonId")
        )

    private fun visualizationSectionRequest() =
        SmartReviewController.SmartReviewVisualizationSection(
            heading = "visualization section heading",
            visualization = ThingId("visualizationId")
        )

    private fun resourceSectionRequest() =
        SmartReviewController.SmartReviewResourceSectionRequest(
            heading = "resource section heading",
            resource = ThingId("resourceId")
        )

    private fun predicateSectionRequest() =
        SmartReviewController.SmartReviewPredicateSectionRequest(
            heading = "predicate section heading",
            predicate = ThingId("predicateId")
        )

    private fun ontologySectionRequest() =
        SmartReviewController.SmartReviewOntologySectionRequest(
            heading = "ontology section heading",
            entities = listOf(ThingId("resourceId")),
            predicates = listOf(ThingId("predicateId"))
        )

    private fun textSectionRequest() =
        SmartReviewController.SmartReviewTextSectionRequest(
            heading = "text section heading",
            `class` = Classes.epilogue,
            text = "epilogue"
        )
}
