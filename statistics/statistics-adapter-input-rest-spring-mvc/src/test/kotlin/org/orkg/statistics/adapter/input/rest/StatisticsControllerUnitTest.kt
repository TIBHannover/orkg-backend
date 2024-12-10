package org.orkg.statistics.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.statistics.domain.SimpleMetric
import org.orkg.statistics.input.RetrieveStatisticsUseCase
import org.orkg.statistics.testing.fixtures.createDummyMetrics
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [StatisticsController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [StatisticsController::class])
internal class StatisticsControllerUnitTest : RestDocsTest("statistics") {

    @MockkBean
    private lateinit var service: RetrieveStatisticsUseCase

    @Test
    @DisplayName("Given several metrics, when fetching all groups, then status is 200 OK and groups are returned")
    fun findAllGroups() {
        val groups = listOf("group1", "group2")

        every { service.findAllGroups() } returns groups

        documentedGetRequestTo("/api/statistics")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.group1.href").value(endsWith("/api/statistics/group1")))
            .andExpect(jsonPath("$.group2.href").value(endsWith("/api/statistics/group2")))
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("*").description("The name of the group."),
                        fieldWithPath("*.href").description("The URI of the group, which can be used to fetch a list of available metrics that are associated with this group.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllGroups() }
    }

    @Test
    @DisplayName("Given several metrics, when fetching the metrics of a group, then status is 200 OK and metrics are returned")
    fun findAllMetricsByGroup() {
        val group = "group1"
        val metrics = createDummyMetrics().filter { it.group == group }

        every { service.findAllMetricsByGroup(group) } returns metrics

        documentedGetRequestTo("/api/statistics/{group}", group)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.metric1.href").value(endsWith("/api/statistics/$group/metric1")))
            .andExpect(jsonPath("$.metric2.href").value(endsWith("/api/statistics/$group/metric2")))
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("*").description("The name of the metric."),
                        fieldWithPath("*.href").description("The URI of the metric, which can be used to fetch information about the metric and its current value.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllMetricsByGroup(group) }
    }

    @Test
    @DisplayName("Given a metric, when fetched, then status is 200 OK and metric is returned")
    fun findMetricByGroupAndName() {
        val metric = SimpleMetric(
            name = "metric1",
            description = "Description of the metric.",
            group = "group1",
            supplier = { 1 }
        )

        every { service.findMetricByGroupAndName(metric.group, metric.name) } returns metric

        documentedGetRequestTo("/api/statistics/{group}/{name}", metric.group, metric.name)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(metric.name))
            .andExpect(jsonPath("$.description").value(metric.description))
            .andExpect(jsonPath("$.group").value(metric.group))
            .andExpect(jsonPath("$.value").value(metric.value().toString()))
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("name").description("The name of the metric."),
                        fieldWithPath("description").description("The description of the metric."),
                        fieldWithPath("group").description("The group of the metric."),
                        fieldWithPath("value").description("The value of the metric.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findMetricByGroupAndName(metric.group, metric.name) }
    }
}
