package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListListSectionRequest.Entry
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.CreateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.DeleteLiteratureListSectionUseCase
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectLiteratureList
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedDeleteRequestTo
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LiteratureListController::class, ExceptionHandler::class, CommonJacksonModule::class, ContentTypeJacksonModule::class, WebMvcConfiguration::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LiteratureListController::class])
@DisplayName("Given a LiteratureList controller")
internal class LiteratureListControllerUnitTest : RestDocsTest("literature-lists") {

    @MockkBean
    private lateinit var literatureListService: LiteratureListUseCases

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literatureListService, contributionService)
    }

    @Test
    @DisplayName("Given a literature list, when it is fetched by id and service succeeds, then status is 200 OK and literature list is returned")
    fun getSingle() {
        val literatureList = createDummyLiteratureList()
        every { literatureListService.findById(literatureList.id) } returns Optional.of(literatureList)

        documentedGetRequestTo("/api/literature-lists/{id}", literatureList.id)
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectLiteratureList()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the literature list to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the literature list."),
                        fieldWithPath("title").description("The title of the literature list."),
                        fieldWithPath("research_fields").description("The list of research fields the literature list is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("versions.head").description("The head version of the literature list."),
                        fieldWithPath("versions.head.id").description("The id of the head version."),
                        fieldWithPath("versions.head.label").description("The label of the head version."),
                        timestampFieldWithPath("versions.head.created_at", "the head version was created"),
                        fieldWithPath("versions.head.created_by").type("String").description("The UUID of the user or service who created the literature list version."),
                        fieldWithPath("versions.published").description("The list of published versions of the literature list."),
                        fieldWithPath("versions.published[].id").description("The id of the published version."),
                        fieldWithPath("versions.published[].label").description("The label of the published version."),
                        timestampFieldWithPath("versions.published[].created_at", "the published version was created"),
                        fieldWithPath("versions.published[].created_by").type("String").description("The UUID of the user or service who created the literature list version."),
                        fieldWithPath("versions.published[].changelog").description("The changelog of the published version."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the literature list resource. Can be one of "unknown", "manual" or "automatic"."""),
                        timestampFieldWithPath("created_at", "the literature list resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this literature list."),
                        fieldWithPath("visibility").description("""Visibility of the literature list. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this literature list.").optional(),
                        fieldWithPath("published").description("Whether the literature is published or not."),
                        fieldWithPath("sections").description("The list of sections of the literature list."),
                        fieldWithPath("sections[].id").description("The id of the section."),
                        fieldWithPath("sections[].type").description("""The type of the section. Either of "text" or "list"."""),
                        fieldWithPath("sections[].entries").description("The linked resources of a list section.").optional(),
                        fieldWithPath("sections[].entries[].value").description("The linked resource of the entry.").optional(),
                        fieldWithPath("sections[].entries[].value.id").description("The id of the linked resource.").optional(),
                        fieldWithPath("sections[].entries[].value.label").description("The label of the linked resource.").optional(),
                        fieldWithPath("sections[].entries[].value.classes").description("The classes of the linked resource.").optional(),
                        fieldWithPath("sections[].entries[].value._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`."),
                        fieldWithPath("sections[].entries[].description").description("The description of the entry.").optional(),
                        fieldWithPath("sections[].heading").description("The heading of the text section.").optional(),
                        fieldWithPath("sections[].heading_size").description("The heading size of the text section.").optional(),
                        fieldWithPath("sections[].text").description("The text contents of the text section.").optional(),
                        subsectionWithPath("acknowledgements").description("A key-value map of contributor ids to an estimated contribution percentage."),
                        fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `literature-list`."),
                    ).and(authorListFields("literature list"))
                        .and(sustainableDevelopmentGoalsFields("literature list"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.findById(literatureList.id) }
    }

    @Test
    @DisplayName("Given several literature lists, when they are fetched, then status is 200 OK and literature lists are returned")
    fun getPaged() {
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummyLiteratureList())

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
    fun getPagedWithParameters() {
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummyLiteratureList())

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
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("title").description("A search term that must be contained in the title of the literature list. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this literature list. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("published").description("Filter for the publication status of the literature lists. (optional)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the literature list belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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

        mockMvc.perform(get("/api/literature-lists?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/literature-lists"))

        verify(exactly = 1) {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list create request, when service succeeds, it creates the literature list")
    fun create() {
        val id = ThingId("R123")
        every { literatureListService.create(any()) } returns id

        documentedPostRequestTo("/api/literature-lists")
            .content(createLiteratureListRequest())
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the literature list."),
                        fieldWithPath("research_fields").description("The list of research fields the literature list will be assigned to. (optional)").optional(),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the literature list will be assigned to. (optional)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional, default: "UNKNOWN")""").optional(),
                        subsectionWithPath("sections").description("The list of updated sections of the literature list (optional). See <<literature-list-sections,literature list sections>> for more information. (optional)").optional(),
                    ).and(authorListFields("literature list"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section create request, when service succeeds, it creates the list section")
    fun createListSection() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val request = LiteratureListController.LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123")),
                Entry(ThingId("R456"))
            )
        )
        every { literatureListService.createSection(any()) } returns id

        MockMvcRequestBuilders.post("/api/literature-lists/{literatureListId}/sections", literatureListId)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))

        verify(exactly = 1) {
            literatureListService.createSection(withArg {
                it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()
                it.index shouldBe null
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section create request, when service succeeds, it creates the list section at the specified index")
    fun createListSectionAtIndex() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val index = 5
        val request = LiteratureListController.LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123")),
                Entry(ThingId("R456"))
            )
        )
        every { literatureListService.createSection(any()) } returns id

        documentedPostRequestTo("/api/literature-lists/{literatureListId}/sections/{index}", literatureListId, index)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the literature list to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created list section will be appended at the end of the literature list. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("entries").description("""The list entries that should be part of this section."""),
                        fieldWithPath("entries[].id").description("""The id of the linked resource. Every resource must either be an instance of "Paper", "Dataset" or "Software"."""),
                        fieldWithPath("entries[].description").type("String").description("""The description of the entry. (optional)""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            literatureListService.createSection(withArg {
                it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateListSectionCommand>()
                it.index shouldBe index
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section create request, when service succeeds, it creates the text section")
    fun createTextSection() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val request = LiteratureListController.LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 2,
            text = "text contents"
        )
        every { literatureListService.createSection(any()) } returns id

        MockMvcRequestBuilders.post("/api/literature-lists/{literatureListId}/sections", literatureListId)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))

        verify(exactly = 1) {
            literatureListService.createSection(withArg {
                it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()
                it.index shouldBe null
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section create request, when service succeeds, it creates the text section at the specified index")
    fun createTextSectionAtIndex() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val index = 5
        val request = LiteratureListController.LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 2,
            text = "text contents"
        )
        every { literatureListService.createSection(any()) } returns id

        documentedPostRequestTo("/api/literature-lists/{literatureListId}/sections/{index}", literatureListId, index)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the literature list to which the new section should be appended to."),
                        parameterWithName("index").description("The insertion index the of the section. Otherwise, the created text section will be appended at the end of the literature list. (optional)")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("""The heading of the text section."""),
                        fieldWithPath("heading_size").description("""The heading size of the text section."""),
                        fieldWithPath("text").description("""The text contents of the text section.""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            literatureListService.createSection(withArg {
                it.shouldBeInstanceOf<CreateLiteratureListSectionUseCase.CreateTextSectionCommand>()
                it.index shouldBe index
            })
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list update request, when service succeeds, it updates the literature list")
    fun update() {
        val id = ThingId("R123")
        every { literatureListService.update(any()) } just runs

        documentedPutRequestTo("/api/literature-lists/{id}", id)
            .content(updateLiteratureListRequest())
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the literature list. (optional)"),
                        fieldWithPath("research_fields").description("The list of research fields the literature list will be assigned to. (optional)"),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the literature list will be assigned to. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional, default: "UNKNOWN")""").optional(),
                        subsectionWithPath("sections").description("The list of updated sections of the literature list (optional). See <<literature-list-sections,literature list sections>> for more information."),
                    ).and(authorListFields("literature list"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a list section update request, when service succeeds, it updates the list section")
    fun updateListSection() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val request = LiteratureListController.LiteratureListListSectionRequest(
            entries = listOf(Entry(ThingId("R123")), Entry(ThingId("R456")))
        )
        every { literatureListService.updateSection(any()) } just runs

        documentedPutRequestTo("/api/literature-lists/{literatureListId}/sections/{sectionId}", literatureListId, id)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the literature list the section belongs to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("entries").description("""The list of updated entries that should be part of this section."""),
                        fieldWithPath("entries[].id").description("""The id of the linked resource. Every resource must either be an instance of "Paper", "Dataset" or "Software"."""),
                        fieldWithPath("entries[].description").type("String").description("""The description of the entry. (optional)""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.updateSection(any<UpdateLiteratureListSectionUseCase.UpdateListSectionCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a published literature list, when fetching its contents, returns success")
    fun findPublishedContentById() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        every { literatureListService.findPublishedContentById(any(), any()) } returns Either.left(createDummyPaper())

        documentedGetRequestTo("/api/literature-lists/{literatureListId}/published-contents/{contentId}", literatureListId, id)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPaper()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the published literature list."),
                        parameterWithName("contentId").description("The id of the resource to fetch.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.findPublishedContentById(any(), any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a text section update request, when service succeeds, it updates the text section")
    fun updateTextSection() {
        val literatureListId = ThingId("R3541")
        val id = ThingId("R123")
        val request = LiteratureListController.LiteratureListTextSectionRequest(
            heading = "updated heading",
            headingSize = 3,
            text = "updated text contents"
        )
        every { literatureListService.updateSection(any()) } just runs

        documentedPutRequestTo("/api/literature-lists/{literatureListId}/sections/{sectionId}", literatureListId, id)
            .content(request)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the literature list the section belongs to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated literature list can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("heading").description("""The updated heading of the text section."""),
                        fieldWithPath("heading_size").description("""The updated heading size of the text section."""),
                        fieldWithPath("text").description("""The updated text contents of the text section.""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.updateSection(any<UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a literature list section delete request, when service succeeds, it returns status 204 NO CONTENT")
    fun deleteSection() {
        val literatureListId = ThingId("R3541")
        val sectionId = ThingId("R123")
        val command = DeleteLiteratureListSectionUseCase.DeleteCommand(
            literatureListId, sectionId, ContributorId(MockUserId.USER)
        )
        every { literatureListService.deleteSection(command) } just runs

        documentedDeleteRequestTo("/api/literature-lists/{literatureListId}/sections/{sectionId}", literatureListId, sectionId)
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/literature-lists/$literatureListId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("literatureListId").description("The id of the literature list the section belongs to."),
                        parameterWithName("sectionId").description("The id of the section.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.deleteSection(command) }
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
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("api/literature-lists/$literatureListVersionId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the literature list to publish.")
                    ),
                    requestFields(
                        fieldWithPath("changelog").description("The description of changes that have been made since the previous version."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
        LiteratureListController.CreateLiteratureListRequest(
            title = "Dummy Literature List Label",
            researchFields = listOf(ThingId("R14")),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("http://example.org/author")
                ),
                AuthorDTO(
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
        LiteratureListController.UpdateLiteratureListRequest(
            title = "Dummy Literature List Label",
            researchFields = listOf(ThingId("R14")),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("http://example.org/author")
                ),
                AuthorDTO(
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

    private fun textSectionRequest() =
        LiteratureListController.LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 1,
            text = "text contents"
        )

    private fun listSectionRequest() =
        LiteratureListController.LiteratureListListSectionRequest(
            entries = listOf(
                Entry(ThingId("R123"), "Example description of an entry"),
                Entry(ThingId("R456"))
            )
        )
}
