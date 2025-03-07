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
import org.orkg.statistics.domain.SingleValueParameterSpec
import org.orkg.statistics.input.StatisticsUseCases
import org.orkg.statistics.testing.fixtures.createMetrics
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap

@ContextConfiguration(
    classes = [StatisticsController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class]
)
@WebMvcTest(controllers = [StatisticsController::class])
internal class StatisticsControllerUnitTest : MockMvcBaseTest("statistics") {
    @MockkBean
    private lateinit var service: StatisticsUseCases

    @Test
    @DisplayName("Given several metrics, when fetching all groups, then status is 200 OK and groups are returned")
    fun findAllGroups() {
        val groups = listOf("group1", "group2")

        every { service.findAllGroups() } returns groups

        documentedGetRequestTo("/api/statistics")
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
        val metrics = createMetrics().filter { it.group == group }

        every { service.findAllMetricsByGroup(group) } returns metrics

        documentedGetRequestTo("/api/statistics/{group}", group)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.metric1.href").value(endsWith("/api/statistics/$group/metric1")))
            .andExpect(jsonPath("$.metric2.href").value(endsWith("/api/statistics/$group/metric2")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("group").description("The name of the group of metrics.")
                    ),
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
        val intParameterSpec = SingleValueParameterSpec(
            name = "parameter1",
            description = "Description of the parameter.",
            type = Int::class,
            values = listOf(0, 1, 2, 3),
            parser = { it.toInt() }
        )
        val metric = SimpleMetric(
            name = "metric1",
            description = "Description of the metric.",
            group = "group1",
            parameterSpecs = mapOf("filter" to intParameterSpec),
            supplier = { 1 }
        )

        every { service.findMetricByGroupAndName(metric.group, metric.name) } returns metric

        documentedGetRequestTo("/api/statistics/{group}/{name}", metric.group, metric.name)
            .param("filter", "5")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(metric.name))
            .andExpect(jsonPath("$.description").value(metric.description))
            .andExpect(jsonPath("$.group").value(metric.group))
            .andExpect(jsonPath("$.value").value(metric.value(LinkedMultiValueMap()).toString()))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("group").description("The name of the group of metrics."),
                        parameterWithName("name").description("The name of the metric.")
                    ),
                    responseFields(
                        fieldWithPath("name").description("The name of the metric."),
                        fieldWithPath("description").description("The description of the metric."),
                        fieldWithPath("group").description("The group of the metric."),
                        fieldWithPath("value").description("The value of the metric."),
                        subsectionWithPath("parameters[]").description("A list of optional query parameters."),
                        fieldWithPath("parameters[].id").description("The id of the parameter, which doubles as the query parameter name."),
                        fieldWithPath("parameters[].name").description("The name of the parameter."),
                        fieldWithPath("parameters[].description").description("The description of the parameter."),
                        fieldWithPath("parameters[].type").description("The type of the parameter."),
                        fieldWithPath("parameters[].multivalued").description("Whether the parameter accepts multiple values at once."),
                        fieldWithPath("parameters[].values[]").description("A list of possible values for the parameter. (optional)").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findMetricByGroupAndName(metric.group, metric.name) }
    }
}
