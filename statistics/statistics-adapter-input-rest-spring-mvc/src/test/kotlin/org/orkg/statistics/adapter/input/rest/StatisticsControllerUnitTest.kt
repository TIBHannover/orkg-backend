package org.orkg.statistics.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.statistics.adapter.input.rest.mapping.MetricRepresentationAdapter.MetricResponseFormat
import org.orkg.statistics.domain.GroupNotFound
import org.orkg.statistics.domain.MetricNotFound
import org.orkg.statistics.domain.TooManyParameterValues
import org.orkg.statistics.input.StatisticsUseCases
import org.orkg.statistics.testing.fixtures.createMetrics
import org.orkg.statistics.testing.fixtures.createSimpleMetric
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.enumValues
import org.orkg.testing.spring.restdocs.wildcard
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap

val allowedMetricResponseFormatValues =
    MetricResponseFormat.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

@ContextConfiguration(
    classes = [StatisticsController::class, ExceptionTestConfiguration::class, CommonJacksonModule::class, FixedClockConfig::class]
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
            .andDocument {
                summary("Listing groups")
                description(
                    """
                    A `GET` request provides a list of all available metric groups.
                    """
                )
                mapResponseFields<EndpointReference>(
                    "The id of the entry",
                    fieldWithPath("href").description("The URI of the endpoint, which can be used to fetch a additional information about the entity."),
                )
            }

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
            .andDocument {
                summary("Listing metrics of groups")
                description(
                    """
                    A `GET` request provides a list of all available metrics of a given group.
                    """
                )
                pathParameters(
                    parameterWithName("group").description("The name of the group of metrics.")
                )
                mapResponseFields<EndpointReference>(
                    "The id of the entry.",
                    fieldWithPath("href").description("The URI of the endpoint, which can be used to fetch a additional information about the entity."),
                )
                throws(GroupNotFound::class)
            }

        verify(exactly = 1) { service.findAllMetricsByGroup(group) }
    }

    private inline fun <reified T> findMetricByGroupAndName(
        responseFormat: MetricResponseFormat,
        responseFields: List<FieldDescriptor>,
    ): ResultActions {
        val metric = createSimpleMetric()

        every { service.findMetricByGroupAndName(metric.group, metric.name) } returns metric

        val result = documentedGetRequestTo("/api/statistics/{group}/{name}", metric.group, metric.name)
            .param("filter", "2")
            .param("response_format", responseFormat.name)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Fetching metrics")
                description(
                    """
                    A `GET` request provides information about a given metric within a group.
                    
                    [NOTE]
                    ====
                    The values of some metrics are cached and therefore might not reflect the real value of the metric at that point in time.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("group").description("The name of the group of metrics."),
                    parameterWithName("name").description("The name of the metric."),
                )
                queryParameters(
                    parameterWithName("response_format").description("The response format of the metric. Either of $allowedMetricResponseFormatValues. (optional, default: `${MetricResponseFormat.DEFAULT}`)").enumValues(MetricResponseFormat::class).optional(),
                    parameterWithName("parameters").description("Filter parameters specific for each metric.").wildcard(mapOf("filter" to "2")).optional(),
                    parameterWithName("filter").ignored(),
                )
                responseFields(T::class, responseFields)
                throws(GroupNotFound::class, MetricNotFound::class, TooManyParameterValues::class)
            }

        verify(exactly = 1) { service.findMetricByGroupAndName(metric.group, metric.name) }

        return result
    }

    @Test
    @DisplayName("Given a metric, when fetched (deafult), then status is 200 OK and metric is returned")
    fun findMetricByGroupAndName_default() {
        findMetricByGroupAndName<DefaultMetricRepresentation>(
            responseFormat = MetricResponseFormat.DEFAULT,
            responseFields = listOf(
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
            .andExpect(jsonPath("$.name").value("metric1"))
            .andExpect(jsonPath("$.description").value("Description of the metric."))
            .andExpect(jsonPath("$.group").value("group1"))
            .andExpect(jsonPath("$.value").value(1))
    }

    @Test
    @DisplayName("Given a metric, when fetched (slim), then status is 200 OK and metric is returned")
    fun findMetricByGroupAndName_slim() {
        findMetricByGroupAndName<SlimMetricRepresentation>(
            responseFormat = MetricResponseFormat.SLIM,
            responseFields = listOf(
                fieldWithPath("value").description("The value of the metric.")
            ),
        )
            .andExpect(jsonPath("$.value").value(1))
    }

    @Test
    fun `Given a metric, when fetched with slim reponse format, then status is 200 OK and metric is returned`() {
        val metric = createSimpleMetric()

        every { service.findMetricByGroupAndName(metric.group, metric.name) } returns metric

        documentedGetRequestTo("/api/statistics/{group}/{name}", metric.group, metric.name)
            .param("response_format", MetricResponseFormat.SLIM.name)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.value").value(metric.value(LinkedMultiValueMap()).toString()))

        verify(exactly = 1) { service.findMetricByGroupAndName(metric.group, metric.name) }
    }
}
