package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.input.RetrieveLegacyStatisticsUseCase
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [LegacyStatsController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [LegacyStatsController::class])
internal class LegacyStatsControllerUnitTest : MockMvcBaseTest("stats") {

    @MockkBean
    private lateinit var statisticsService: RetrieveLegacyStatisticsUseCase

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @Test
    fun `When retrieving stats about observatories and service succeeds, then status is 200 OK and observatory statistics are returned`() {
        val id = ObservatoryId(UUID.randomUUID())
        val response = ObservatoryStats(id, 1, 0)
        every {
            statisticsService.findAllObservatoryStats(any())
        } returns pageOf(response)

        get("/api/stats/observatories")
            .perform()
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

        get("/api/stats/observatories/{id}", id)
            .perform()
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

        documentedGetRequestTo("/api/stats/research-fields/{id}", id)
            .param("includeSubfields", "false")
            .perform()
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
                    queryParameters(
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

        get("/api/stats/research-fields/{id}", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/stats/research-fields/$id"))

        verify(exactly = 1) { statisticsService.findResearchFieldStatsById(id, false) }
    }
}
