package eu.tib.orkg.prototype.content_types.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.content_types.api.AuthorRepresentation
import eu.tib.orkg.prototype.content_types.api.LabeledObjectRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperUseCases
import eu.tib.orkg.prototype.content_types.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.content_types.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
    fun `Given several papers are being fetched, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        every { paperService.findAll(any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAll(any()) }
    }

    @Test
    fun `Given several papers are being fetched by doi, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        val doi = papers.first().identifiers["doi"]!!
        every { paperService.findAllByDOI(doi, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/?doi=$doi")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByDOI(doi, any()) }
    }

    @Test
    fun `Given several papers are being fetched by title, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        val title = papers.first().title
        every { paperService.findAllByTitle(title, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/?title=$title")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByTitle(title, any()) }
    }

    @Test
    fun `Given several papers are being fetched by visibility, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        val visibility = papers.first().visibility
        every { paperService.findAllByVisibility(visibility, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/?visibility=$visibility")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByVisibility(visibility, any()) }
    }

    @Test
    fun `Given several papers are being fetched by contributor id, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        val contributorId = papers.first().createdBy
        every { paperService.findAllByContributor(contributorId, any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/?contributor=$contributorId")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAllByContributor(contributorId, any()) }
    }

    @Test
    fun `Given several papers are being fetched, when multiple query parameters are given, then status is 400 BAD REQUEST`() {
        val papers = listOf(createDummyPaperRepresentation())
        val title = papers.first().title
        val contributorId = papers.first().createdBy
        val exception = TooManyParameters.atMostOneOf("doi", "title", "visibility", "contributor")

        mockMvc.get("/api/content-types/paper/?title=$title&contributor=$contributorId")
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/content-types/paper/"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    private fun MockMvc.get(string: String) = perform(MockMvcRequestBuilders.get(string))

    private fun createDummyPaperRepresentation() = object : PaperRepresentation {
        override val id: ThingId = ThingId("R123")
        override val title: String = "Dummy Paper Title"
        override val researchFields: List<LabeledObjectRepresentation> = listOf(
            object : LabeledObjectRepresentation {
                override val id: ThingId = ThingId("R456")
                override val label: String = "Research Field 1"
            },
            object : LabeledObjectRepresentation {
                override val id: ThingId = ThingId("R789")
                override val label: String = "Research Field 2"
            }
        )
        override val identifiers: Map<String, String> = mapOf(
            "doi" to "10.1000/182"
        )
        override val publicationInfo = object : PublicationInfoRepresentation {
            override val publishedMonth: Int = 4
            override val publishedYear: Long = 2023
            override val publishedIn: String = "Fancy Conference"
            override val url: String = "https://example.org"
        }
        override val authors: List<AuthorRepresentation> = listOf(
            object : AuthorRepresentation {
                override val id: ThingId = ThingId("147")
                override val name: String = "Author 1"
                override val identifiers: Map<String, String> = mapOf(
                    "doi" to "10.1007/s00787-010-0130-8"
                )
                override val homepage: String = "https://example.org"
            },
            object : AuthorRepresentation {
                override val id: ThingId? = null
                override val name: String = "Author 2"
                override val identifiers: Map<String, String> = emptyMap()
                override val homepage: String? = null
            }
        )
        override val contributions: List<LabeledObjectRepresentation> = listOf(
            object : LabeledObjectRepresentation {
                override val id: ThingId = ThingId("R258")
                override val label: String = "Contribution 1"
            },
            object : LabeledObjectRepresentation {
                override val id: ThingId = ThingId("R396")
                override val label: String = "Contribution 2"
            }
        )
        override val observatories: List<ObservatoryId> = listOf(
            ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
            ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
        )
        override val organizations: List<OrganizationId> = listOf(
            OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
            OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
        )
        override val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
        override val createdAt: OffsetDateTime = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00")
        override val createdBy: ContributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        override val visibility: Visibility = Visibility.LISTED
        override val verified: Boolean = false
    }
}
