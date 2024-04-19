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
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Stream
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectLiteratureList
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LiteratureListController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LiteratureListController::class])
@DisplayName("Given a LiteratureList controller")
internal class LiteratureListControllerUnitTest : RestDocsTest("literature-lists") {

    @MockkBean
    private lateinit var literatureListService: LiteratureListUseCases

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

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
                        fieldWithPath("versions.published").description("The list of published versions of the literature list."),
                        fieldWithPath("versions.published[].id").description("The id of the published version."),
                        fieldWithPath("versions.published[].label").description("The label of the published version."),
                        timestampFieldWithPath("versions.published[].created_at", "the published version was created"),
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
                        fieldWithPath("sections[].entries[].id").description("The id of the linked resource.").optional(),
                        fieldWithPath("sections[].entries[].label").description("The label of the linked resource.").optional(),
                        fieldWithPath("sections[].entries[].classes").description("The classes of the linked resource.").optional(),
                        fieldWithPath("sections[].heading").description("The heading of the text section.").optional(),
                        fieldWithPath("sections[].heading_size").description("The heading size of the text section.").optional(),
                        fieldWithPath("sections[].text").description("The text contents of the text section.").optional(),
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
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
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
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several literature lists, when filtering by several parameters, then status is 200 OK and literature lists are returned")
    fun getPagedWithParameters() {
        every {
            literatureListService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummyLiteratureList())

        val title = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
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
                        headerWithName("Location").description("The uri path where the updated resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the literature list."),
                        fieldWithPath("research_fields").description("The list of research fields the literature list will be assigned to."),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the literature list will be assigned to. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to. (optional)").optional(),
                        fieldWithPath("extraction_method").type("String").description("""The method used to extract the resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional, default: "UNKNOWN")""").optional(),
                        subsectionWithPath("sections").description("The list of updated of the literature list (optional). See <<literature-list-sections,literature list sections>> for more information."),
                    ).and(authorListFields("literature list"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { literatureListService.create(any()) }
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
                        headerWithName("Location").description("The uri path where the updated resource can be fetched from.")
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
                    identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = URI.create("http://example.org/author")
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
                    identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = URI.create("http://example.org/author")
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

    companion object {
        @JvmStatic
        fun literatureListSectionRequests(): Stream<Arguments> = Stream.of(
            Arguments.of(textSectionRequest()),
            Arguments.of(listSectionRequest())
        )

        @JvmStatic
        private fun textSectionRequest() =
            LiteratureListController.TextSectionRequest(
                heading = "heading",
                headingSize = 1,
                text = "text contents"
            )

        @JvmStatic
        private fun listSectionRequest() =
            LiteratureListController.ListSectionRequest(
                entries = listOf(ThingId("R123"), ThingId("R456"))
            )
    }
}
