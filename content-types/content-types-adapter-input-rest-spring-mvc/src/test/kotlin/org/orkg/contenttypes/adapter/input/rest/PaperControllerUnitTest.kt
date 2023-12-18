package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreateContributionRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ListDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.LiteralDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.PaperContentsDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.PredicateDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ResourceDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.StatementObjectDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.UpdatePaperRequest
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.graph.domain.DOIServiceUnavailable
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [PaperController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [PaperController::class])
@DisplayName("Given a Paper controller")
internal class PaperControllerUnitTest : RestDocsTest("papers") {

    @MockkBean
    private lateinit var paperService: PaperUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(paperService, contributionService)
    }

    @Test
    @DisplayName("Given a paper, when it is fetched by id and service succeeds, then status is 200 OK and paper is returned")
    fun getSingle() {
        val paper = createDummyPaper()
        every { paperService.findById(paper.id) } returns Optional.of(paper)

        documentedGetRequestTo("/api/papers/{id}", paper.id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPaper()
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
                        fieldWithPath("visibility").description("""Visibility of the paper. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this paper.").optional()
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
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

        documentedGetRequestTo("/api/papers")
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("research_field").description("Optional filter for research field id.").optional(),
                        parameterWithName("title").description("Optional filter for the title of the paper. Uses exact matching.").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "listed", "featured", "unlisted" or "deleted".""").optional(),
                        parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created this paper.").optional(),
                        parameterWithName("research_field").description("Optional filter for research field id.").optional(),
                        parameterWithName("include_subfields").description("Optional flag for whether subfields are included in the search or not.").optional(),
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    @Test
    fun `Given several papers, when they are fetched by visibility and research field id, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val researchFieldId = papers.first().researchFields.first().id
        every {
            paperService.findAllByResearchFieldAndVisibility(
                researchFieldId = researchFieldId,
                visibility = VisibilityFilter.ALL_LISTED,
                includeSubfields = true,
                pageable = any()
            )
        } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/papers?research_field=$researchFieldId&visibility=ALL_LISTED&include_subfields=true")
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) {
            paperService.findAllByResearchFieldAndVisibility(
                researchFieldId = researchFieldId,
                visibility = VisibilityFilter.ALL_LISTED,
                includeSubfields = true,
                pageable = any()
            )
        }
    }

    @Test
    @DisplayName("Given a paper, when contributors are fetched, then status 200 OK and contributors are returned")
    fun getContributors() {
        val id = ThingId("R8186")
        val contributors = listOf(ContributorId(UUID.fromString("0a56acb7-cd97-4277-9c9b-9b3089bde45f")))
        every { paperService.findAllContributorsByPaperId(id, any()) } returns PageImpl(contributors, PageRequest.of(0, 5), 1)

        documentedGetRequestTo("/api/papers/{id}/contributors", id)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
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
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/contributors"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper, when publishing, then status 204 NO CONTENT")
    fun publish() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )

        every { paperService.publish(any()) } just runs

        documentedPostRequestTo("/api/papers/{id}/publish", id)
            .content(request)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("api/papers/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the paper to publish.")
                    ),
                    requestFields(
                        fieldWithPath("subject").description("The subject of the paper."),
                        fieldWithPath("description").description("The description of the paper."),
                        fieldWithPath("authors").description("The list of authors that originally contributed to the paper."),
                        fieldWithPath("authors[].id").description("The ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].name").description("The name of the author."),
                        fieldWithPath("authors[].identifiers").description("The unique identifiers of the author."),
                        fieldWithPath("authors[].identifiers.orcid").type("String").description("The ORCID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.google_scholar").type("String").description("The Google Scholar ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.research_gate").type("String").description("The ResearchGate ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.linked_in").type("String").description("The LinkedIn ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.wikidata").type("String").description("The Wikidata ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.web_of_science").type("String").description("The Web of Science id of the author. (optional)").optional(),
                        fieldWithPath("authors[].homepage").description("The homepage of the author. (optional)").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper, when publishing but service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )
        val exception = PaperNotFound(id)

        every { paperService.publish(any()) } throws exception

        post("/api/papers/$id/publish")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper, when publishing but service reports doi service unavailable, then status is 503 SERVICE UNAVAILABLE`() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )
        val exception = DOIServiceUnavailable(500, "Internal error")

        every { paperService.publish(any()) } throws exception

        post("/api/papers/$id/publish")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .perform()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper create request, when service succeeds, it creates and returns the paper")
    fun create() {
        val id = ThingId("R123")
        every { paperService.create(any()) } returns id

        documentedPostRequestTo("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the paper."),
                        fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to."),
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
                        fieldWithPath("contents").description("Definition of the contents of the paper."),
                        fieldWithPath("contents.resources").description("Definition of resources that need to be created."),
                        fieldWithPath("contents.resources.*.label").description("The label of the resource."),
                        fieldWithPath("contents.resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("contents.literals").description("Definition of literals that need to be created."),
                        fieldWithPath("contents.literals.*.label").description("The value of the literal."),
                        fieldWithPath("contents.literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("contents.predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("contents.predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("contents.predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("contents.lists").description("Definition of lists that need to be created."),
                        fieldWithPath("contents.lists.*.label").description("The label of the list."),
                        fieldWithPath("contents.lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("contents.contributions").description("List of definitions of contribution that need to be created."),
                        fieldWithPath("contents.contributions[].label").description("Label of the contribution."),
                        fieldWithPath("contents.contributions[].classes").description("The classes of the contribution resource."),
                        subsectionWithPath("contents.contributions[].statements").description("Recursive map of statements contained within the contribution."),
                        fieldWithPath("contents.contributions[].statements.*[].id").description("The ID of the object of the statement."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of "unknown", "manual" or "automatic".""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneResearchFieldAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneOrganizationAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneObservatoryAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val exception = ThingNotDefined("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports author not found, then status is 404 NOT FOUND`() {
        val exception = AuthorNotFound(ThingId("R123"))
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val exception = InvalidTempId("invalid")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with title, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withTitle("paper title")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with identifier, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withIdentifier("paper title")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to "0000-1111-2222-3333")
            )
        )
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAPredicate("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val exception = InvalidStatementSubject("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val exception = ThingNotFound("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports empty contributions, then status is 400 BAD REQUEST`() {
        val exception = EmptyContribution(0)
        every { paperService.create(any()) } throws exception

        post("/api/papers", createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper update request, when service succeeds, it updates the paper")
    fun update() {
        val id = ThingId("R123")
        every { paperService.update(any()) } just runs

        documentedPutRequestTo("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the paper. (optional)"),
                        fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to. (optional)"),
                        fieldWithPath("identifiers").description("The unique identifiers of the paper. (optional)"),
                        fieldWithPath("identifiers.doi").description("The DOI of the paper. (optional)").optional(),
                        fieldWithPath("publication_info").description("The publication info of the paper. (optional)").optional(),
                        fieldWithPath("publication_info.published_month").description("The month in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_year").description("The year in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_in").description("The venue where the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.url").description("The URL to the original paper. (optional)").optional(),
                        fieldWithPath("authors").description("The list of authors that originally contributed to the paper."),
                        fieldWithPath("authors[].id").description("The ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].name").description("The name of the author."),
                        fieldWithPath("authors[].identifiers").description("The unique identifiers of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.orcid").description("The ORCID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.google_scholar").type("String").description("The Google Scholar ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.research_gate").type("String").description("The ResearchGate ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.linked_in").type("String").description("The LinkedIn ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.wikidata").type("String").description("The Wikidata ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.web_of_science").type("String").description("The Web of Science id of the author. (optional)").optional(),
                        fieldWithPath("authors[].homepage").description("The homepage of the author. (optional)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to. (optional)").optional()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.update(any()) }
    }
    
    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper already exists with title, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = PaperAlreadyExists.withTitle("paper title")
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper already exists with identifier, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = PaperAlreadyExists.withIdentifier("paper title")
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to "0000-1111-2222-3333")
            )
        )
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports author not found, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = AuthorNotFound(ThingId("R123"))
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneResearchFieldAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneOrganizationAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneObservatoryAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound(id)
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contribution request, when service succeeds, it creates and returns the contribution")
    fun createContribution() {
        val paperId = ThingId("R3541")
        val contributionId = ThingId("R123")
        every { paperService.createContribution(any()) } returns contributionId

        documentedPostRequestTo("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/contributions/$contributionId")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created resource can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of "unknown", "manual" or "automatic". (default: "unknown")""").optional(),
                        fieldWithPath("resources").description("Definition of resources that need to be created."),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created."),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("contribution").description("List of definitions of contribution that need to be created."),
                        fieldWithPath("contribution.label").description("Label of the contribution."),
                        fieldWithPath("contribution.classes").description("The classes of the contribution resource."),
                        subsectionWithPath("contribution.statements").description("Recursive map of statements contained within the contribution."),
                        fieldWithPath("contribution.statements.*[].id").description("The ID of the object of the statement.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidTempId("invalid")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = PaperNotFound(ThingId("R123"))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingNotDefined("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = ThingNotFound("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAPredicate("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidStatementSubject("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports empty contribution, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = EmptyContribution()
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/$paperId/contributions", createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    private fun createPaperRequest() =
        CreatePaperRequest(
            title = "example paper",
            researchFields = listOf(ThingId("R12")),
            identifiers = mapOf("doi" to "10.48550 / arXiv.2304.05327"),
            publicationInfo = PublicationInfoDTO(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = URI.create("https://www.example.org")
            ),
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
                    identifiers = mapOf("orcid" to "0000-1111-2222-3333"),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = mapOf("orcid" to "1111-2222-3333-4444"),
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
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            ),
            contents = PaperContentsDTO(
                resources = mapOf(
                    "#temp1" to ResourceDefinitionDTO(
                        label = "MOTO",
                        classes = setOf(ThingId("Result"))
                    )
                ),
                literals = mapOf(
                    "#temp2" to LiteralDefinitionDTO(
                        label = "0.1",
                        dataType = "xsd:decimal"
                    )
                ),
                predicates = mapOf(
                    "#temp3" to PredicateDefinitionDTO(
                        label = "hasResult",
                        description = "has result"
                    )
                ),
                lists = mapOf(
                    "#temp4" to ListDefinitionDTO(
                        label = "list",
                        elements = listOf("#temp1", "C123")
                    )
                ),
                contributions = listOf(
                    ContributionDTO(
                        label = "Contribution 1",
                        classes = setOf(ThingId("C123")),
                        statements = mapOf(
                            "P32" to listOf(
                                StatementObjectDefinitionDTO(
                                    id = "R3003",
                                    statements = null
                                )
                            ),
                            "HAS_EVALUATION" to listOf(
                                StatementObjectDefinitionDTO(
                                    id = "#temp1",
                                    statements = null
                                ),
                                StatementObjectDefinitionDTO(
                                    id = "R3004",
                                    statements = mapOf(
                                        "#temp3" to listOf(
                                            StatementObjectDefinitionDTO(
                                                id = "R3003",
                                                statements = null
                                            ),
                                            StatementObjectDefinitionDTO(
                                                id = "#temp2",
                                                statements = null
                                            ),
                                            StatementObjectDefinitionDTO(
                                                id = "#temp4",
                                                statements = null
                                            )
                                        ),
                                        "P32" to listOf(
                                            StatementObjectDefinitionDTO(
                                                id = "#temp2",
                                                statements = null
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )

    private fun updatePaperRequest() =
        UpdatePaperRequest(
            title = "example paper",
            researchFields = listOf(ThingId("R14")),
            identifiers = mapOf("doi" to "10.48550/arXiv.2304.05327"),
            publicationInfo = PublicationInfoDTO(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = URI.create("https://www.example.org")
            ),
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
                    identifiers = mapOf("orcid" to "0000-1111-2222-3333"),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = mapOf("orcid" to "1111-2222-3333-4444"),
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
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            )
        )

    private fun createContributionRequest() =
        CreateContributionRequest(
            resources = mapOf(
                "#temp1" to ResourceDefinitionDTO(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to LiteralDefinitionDTO(
                    label = "0.1",
                    dataType = "xsd:decimal"
                )
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinitionDTO(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinitionDTO(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            contribution = ContributionDTO(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    "P32" to listOf(
                        StatementObjectDefinitionDTO(
                            id = "R3003",
                            statements = null
                        )
                    ),
                    "HAS_EVALUATION" to listOf(
                        StatementObjectDefinitionDTO(
                            id = "#temp1",
                            statements = null
                        ),
                        StatementObjectDefinitionDTO(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    StatementObjectDefinitionDTO(
                                        id = "R3003",
                                        statements = null
                                    ),
                                    StatementObjectDefinitionDTO(
                                        id = "#temp2",
                                        statements = null
                                    ),
                                    StatementObjectDefinitionDTO(
                                        id = "#temp4",
                                        statements = null
                                    )
                                ),
                                "P32" to listOf(
                                    StatementObjectDefinitionDTO(
                                        id = "#temp2",
                                        statements = null
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
}
