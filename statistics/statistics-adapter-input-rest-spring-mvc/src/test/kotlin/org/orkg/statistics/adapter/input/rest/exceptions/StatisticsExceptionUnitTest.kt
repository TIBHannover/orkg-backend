package org.orkg.statistics.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.statistics.domain.GroupNotFound
import org.orkg.statistics.domain.MetricNotFound
import org.orkg.statistics.domain.TooManyParameterValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class StatisticsExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun groupNotFound() {
        val type = "orkg:problem:group_not_found"
        documentedGetRequestTo(GroupNotFound("group1"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Group "group1" not found.""")
            .andExpect(jsonPath("$.group_name").value("group1"))
            .andDocument {
                responseFields<GroupNotFound>(
                    fieldWithPath("group_name").description("The name of the group."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun metricNotFound() {
        val type = "orkg:problem:metric_not_found"
        documentedGetRequestTo(MetricNotFound("group1", "metric1"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Metric "group1-metric1" not found.""")
            .andExpect(jsonPath("$.group_name").value("group1"))
            .andExpect(jsonPath("$.metric_name").value("metric1"))
            .andDocument {
                responseFields<MetricNotFound>(
                    fieldWithPath("group_name").description("The name of the group."),
                    fieldWithPath("metric_name").description("The name of the metric."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyParameterValues() {
        val type = "orkg:problem:too_many_parameter_values"
        documentedGetRequestTo(TooManyParameterValues("param1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for parameter "param1".""")
            .andExpect(jsonPath("$.parameter_name").value("param1"))
            .andDocument {
                responseFields<TooManyParameterValues>(
                    fieldWithPath("parameter_name").description("The name of the parameter."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
