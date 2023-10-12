package eu.tib.orkg.prototype.statements.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.ContributorService
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.testing.annotations.UsesMocking
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [StatsController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [StatsController::class])
@DisplayName("Given a Stats controller")
@UsesMocking
class StatsControllerUnitTest : RestDocsTest("stats") {

    @MockkBean
    private lateinit var statisticsService: RetrieveStatisticsUseCase

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var contributorService: ContributorService

    @Test
    fun `When retrieving stats about observatories and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id, 1, 0)
        every {
            statisticsService.findAllObservatoryStats(any())
        } returns pageOf(response)

        mockMvc.perform(get("/api/stats/observatories"))
            .andExpect(status().isOk)

        verify(exactly = 1) { statisticsService.findAllObservatoryStats(any()) }
    }

    @Test
    fun `When retrieving stats about a single observatory and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id, 1, 0)
        every {
            statisticsService.findObservatoryStatsById(id)
        } returns response

        mockMvc.perform(get("/api/stats/observatories/$id"))
            .andExpect(status().isOk)

        verify(exactly = 1) { statisticsService.findObservatoryStatsById(id) }
    }

    @Test
    @DisplayName("When retrieving stats about a research field and service succeeds, then status is 200 OK and research field statistics are returned")
    fun researchFields() {
        val id = ThingId("R1")
        val response = ResearchFieldStats(
            id = id,
            papers = 25,
            comparisons = 5,
            total = 30
        )

        every { statisticsService.findResearchFieldStatsById(id, false) } returns response

        mockMvc.perform(documentedGetRequestTo("/api/stats/research-fields/{id}?includeSubfields={includeSubfields}", id, false))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.value))
            .andExpect(jsonPath("$.papers").value(response.papers))
            .andExpect(jsonPath("$.comparisons").value(response.comparisons))
            .andExpect(jsonPath("$.total").value(response.total))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research field.")
                    ),
                    requestParameters(
                        parameterWithName("includeSubfields").description("Whether or not to include subfields when calculating the statistics. Optional.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the research field."),
                        fieldWithPath("papers").description("The numbers of papers in that research field."),
                        fieldWithPath("comparisons").description("The numbers of comparisons in that research field."),
                        fieldWithPath("total").description("The total count of elements."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { statisticsService.findResearchFieldStatsById(id, false) }
    }

    @Test
    fun `When retrieving stats about a research field and service reports missing research field, then status is 404 NOT FOUND`() {
        val id = ThingId("R1")
        val exception = ResearchFieldNotFound(id)

        every { statisticsService.findResearchFieldStatsById(id, false) } throws exception

        mockMvc.perform(get("/api/stats/research-fields/{id}", id))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/stats/research-fields/$id"))

        verify(exactly = 1) { statisticsService.findResearchFieldStatsById(id, false) }
    }
}
