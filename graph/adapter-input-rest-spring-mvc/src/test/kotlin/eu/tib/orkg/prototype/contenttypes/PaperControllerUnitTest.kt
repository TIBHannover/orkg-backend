package eu.tib.orkg.prototype.contenttypes

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
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
import io.mockk.every
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [PaperController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [PaperController::class])
@DisplayName("Given a Paper controller")
internal class PaperControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var paperService: PaperUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given a paper, when it is fetched by id and service succeeds, then status is 200 OK and contribution is returned`() {
        val contribution = createDummyPaper()
        every { paperService.findById(contribution.id) } returns contribution

        get("/api/content-types/papers/${contribution.id}")
            .andExpect(status().isOk)

        verify(exactly = 1) { paperService.findById(contribution.id) }
    }

    @Test
    fun `Given a paper, when it is fetched by id and service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = PaperNotFound(id)
        every { paperService.findById(id) } throws exception

        get("/api/content-types/papers/$id")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/content-types/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findById(id) }
    }

    @Test
    fun `Given several papers, when they are fetched, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        every { paperService.findAll(any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/content-types/papers/")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAll(any()) }
    }

    @Test
    fun `Given several papers, when they are fetched by doi, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaper())
        val doi = papers.first().identifiers["doi"]!!
        every { paperService.findAllByDOI(doi, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        get("/api/content-types/papers/?doi=$doi")
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

        get("/api/content-types/papers/?title=$title")
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

        get("/api/content-types/papers/?visibility=$visibility")
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

        get("/api/content-types/papers/?created_by=$contributorId")
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

        get("/api/content-types/papers/?title=$title&created_by=$contributorId")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/content-types/papers/"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    @Test
    fun `Given a paper, when contributors are fetched, then status 200 OK and contributors are returned`() {
        val id = ThingId("R123")
        val contributors = listOf(ContributorId(UUID.randomUUID()))
        every { paperService.findAllContributorsByPaperId(id, any()) } returns PageImpl(contributors, PageRequest.of(0, 5), 1)

        get("/api/content-types/papers/$id/contributors")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    fun `Given a paper, when contributors are fetched but paper is missing, then status 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound(id)
        every { paperService.findAllContributorsByPaperId(id, any()) } throws exception

        get("/api/content-types/papers/$id/contributors")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/content-types/papers/$id/contributors"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    private fun get(string: String) = mockMvc.perform(MockMvcRequestBuilders.get(string))

    private fun createDummyPaper() = Paper(
        id = ThingId("R123"),
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
                name = "Author 1",
                identifiers = mapOf(
                    "doi" to "10.1007/s00787-010-0130-8"
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
