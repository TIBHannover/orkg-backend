package eu.tib.orkg.prototype.contenttypes

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.application.PAPER_JSON_V2
import eu.tib.orkg.prototype.contenttypes.application.PaperController
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.LabeledObject
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.testing.andExpectPage
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import eu.tib.orkg.prototype.testing.spring.restdocs.timestampFieldWithPath
import io.mockk.every
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [PaperController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [PaperController::class])
@DisplayName("Given a Paper controller")
internal class PaperControllerUnitTest : RestDocsTest("papers") {

    @MockkBean
    private lateinit var paperService: PaperUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @Test
    @DisplayName("Given a paper, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned")
    fun getSingle() {
        val paper = createDummyPaper()
        every { paperService.findById(paper.id) } returns Optional.of(paper)

        mockMvc.perform(documentedGetRequestTo("/api/papers/{id}", paper.id, accept = PAPER_JSON_V2, contentType = PAPER_JSON_V2))
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the paper to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the paper."),
                        fieldWithPath("title").description("The title of the paper."),
                        fieldWithPath("research_fields").description("The list of research fields the paper is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("identifiers").description("The unique identifiers of the paper."),
                        fieldWithPath("identifiers.doi").description("The DOI of the paper. (optional)").optional(),
                        fieldWithPath("publication_info").description("The publication info of the paper.").optional(),
                        fieldWithPath("publication_info.published_month").description("The month in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_year").description("The year in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_in").description("The venue where the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.url").description("The URL to the original paper. (optional)").optional(),
                        fieldWithPath("authors").description("The list of authors that originally contributed to the paper."),
                        fieldWithPath("authors[].id").description("The ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].name").description("The name of the author."),
                        fieldWithPath("authors[].identifiers").description("The unique identifiers of the author."),
                        fieldWithPath("authors[].identifiers.orcid").description("The ORCID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.google_scholar").type("String").description("The Google Scholar ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.research_gate").type("String").description("The ResearchGate ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.linked_in").type("String").description("The LinkedIn ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.wikidata").type("String").description("The Wikidata ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.web_of_science").type("String").description("The Web of Science id of the author. (optional)").optional(),
                        fieldWithPath("authors[].homepage").description("The homepage of the author. (optional)").optional(),
                        fieldWithPath("contributions").description("The list of contributions of the paper."),
                        fieldWithPath("contributions[].id").description("The ID of the contribution."),
                        fieldWithPath("contributions[].label").description("The label of the contribution."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of "unknown", "manual" or "automatic"."""),
                        timestampFieldWithPath("created_at", "the paper resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this paper."),
                        fieldWithPath("verified").description("Determines if the paper was verified by a curator."),
                        fieldWithPath("visibility").description("""Visibility of the paper. Can be one of "default", "featured", "unlisted" or "deleted".""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.findById(paper.id) }
    }

    @Test
    fun `Given a paper, when it is fetched by id and service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = PaperNotFound(id)
        every { paperService.findById(id) } returns Optional.empty()

        get("/api/papers/$id")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findById(id) }
    }

    @Test
    @DisplayName("Given several papers, when they are fetched, then status is 200 OK and papers are returned")
    fun getPaged() {
        val papers = listOf(createDummyPaper())
        every { paperService.findAll(any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.perform(documentedGetRequestTo("/api/papers", accept = PAPER_JSON_V2, contentType = PAPER_JSON_V2))
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("research_field").description("Optional filter for research field id.").optional(),
                        parameterWithName("title").description("Optional filter for the title of the paper. Uses exact matching.").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "listed", "featured", "unlisted" or "deleted".""").optional(),
                        parameterWithName("created_by").description("Optional filter for research field id.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.findAll(any()) }
    }

    @Test
    fun `Given several papers, when they are fetched by doi, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val doi = papers.first().identifiers["doi"]!!
        every { paperService.findAllByDOI(doi, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/papers?doi=$doi")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByDOI(doi, any()) }
    }

    @Test
    fun `Given several papers, when they are fetched by title, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val title = papers.first().title
        every { paperService.findAllByTitle(title, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/papers?title=$title")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByTitle(title, any()) }
    }

    @Test
    fun `Given several papers, when they are fetched by visibility, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val visibility = VisibilityFilter.ALL_LISTED
        every { paperService.findAllByVisibility(visibility, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/papers?visibility=$visibility")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByVisibility(visibility, any()) }
    }

    @Test
    fun `Given several papers, when they are fetched by contributor id, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val contributorId = papers.first().createdBy
        every { paperService.findAllByContributor(contributorId, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/papers?created_by=$contributorId")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByContributor(contributorId, any()) }
    }

    @Test
    fun `Given several papers, when they are fetched but multiple query parameters are given, then status is 400 BAD REQUEST`() {
        val papers = listOf(createDummyPaper())
        val title = papers.first().title
        val contributorId = papers.first().createdBy
        val exception = TooManyParameters.atMostOneOf("doi", "title", "visibility", "created_by")

        get("/api/papers?title=$title&created_by=$contributorId")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    @Test
    @DisplayName("Given a paper, when contributors are fetched, then status 200 OK and contributors are returned")
    fun getContributors() {
        val id = ThingId("R8186")
        val contributors = listOf(ContributorId(UUID.randomUUID()))
        every { paperService.findAllContributorsByPaperId(id, any()) } returns PageImpl(contributors, PageRequest.of(0, 5), 1)

        mockMvc.perform(documentedGetRequestTo("/api/papers/{id}/contributors", id, accept = PAPER_JSON_V2, contentType = PAPER_JSON_V2))
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    fun `Given a paper, when contributors are fetched but paper is missing, then status 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound(id)
        every { paperService.findAllContributorsByPaperId(id, any()) } throws exception

        get("/api/papers/$id/contributors")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/contributors"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    private fun get(string: String) = mockMvc.perform(
        MockMvcRequestBuilders.get(string)
            .accept("application/vnd.orkg.paper.v2+json")
    )

    private fun createDummyPaper() = Paper(
        id = ThingId("R8186"),
        title = "Dummy Paper Title",
        researchFields = listOf(
            LabeledObject(
                id = ThingId("R456"),
                label = "Research Field 1"
            ),
            LabeledObject(
                id = ThingId("R789"),
                label = "Research Field 2"
            )
        ),
        identifiers = mapOf(
            "doi" to "10.1000/182"
        ),
        publicationInfo = PublicationInfo(
            publishedMonth = 4,
            publishedYear = 2023,
            publishedIn = "Fancy Conference",
            url = "https://example.org"
        ),
        authors = listOf(
            Author(
                id = ThingId("147"),
                name = "Josiah Stinkney Carberry",
                identifiers = mapOf(
                    "orcid" to "0000-0002-1825-0097"
                ),
                homepage = "https://example.org"
            ),
            Author(
                id = null,
                name = "Author 2",
                identifiers = emptyMap(),
                homepage = null
            )
        ),
        contributions = listOf(
            LabeledObject(
                id = ThingId("R258"),
                label = "Contribution 1"
            ),
            LabeledObject(
                id = ThingId("R396"),
                label = "Contribution 2"
            )
        ),
        observatories = listOf(
            ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
            ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
        ),
        organizations = listOf(
            OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
            OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
        ),
        extractionMethod = ExtractionMethod.UNKNOWN,
        createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
        createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
        visibility = Visibility.DEFAULT,
        verified = false
    )
}
