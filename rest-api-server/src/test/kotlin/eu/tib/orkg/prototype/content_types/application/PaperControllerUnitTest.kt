package eu.tib.orkg.prototype.content_types.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.content_types.api.PaperRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperUseCases
import eu.tib.orkg.prototype.content_types.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
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
    fun `Given several paper are being fetched, when no parameters are given, then status is 200 OK and papers are returned`() {
        val papers = listOf(createDummyPaperRepresentation())
        every { paperService.findAll(any()) } returns PageImpl(papers, PageRequest.of(0, 5), 1)

        mockMvc.get("/api/content-types/paper/")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { paperService.findAll(any()) }
    }

    private fun MockMvc.get(string: String) = perform(MockMvcRequestBuilders.get(string))

    private fun createDummyPaperRepresentation() = object : PaperRepresentation {
        override val id: ThingId = ThingId("R123")
        override val title: String = "Dummy Paper Title"
        override val researchFields: List<ThingId> = listOf(
            ThingId("R456"),
            ThingId("R789")
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
        override val authors: List<ThingId> = listOf(
            ThingId("147"),
            ThingId("258")
        )
        override val contributors: List<ContributorId> = listOf(
            ContributorId("71834d65-cde6-474c-8840-2b8df2f62ea8"),
            ContributorId("c4023d56-2e9a-47e5-ad2d-e7a3da827fe8")
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
        override val featured: Boolean = false
        override val unlisted: Boolean = false
        override val verified: Boolean = false
        override val deleted: Boolean = false
    }
}
