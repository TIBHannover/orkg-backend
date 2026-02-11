package org.orkg.statistics.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.statistics.adapter.input.rest.testing.fixtures.configuration.StatisticsControllerExceptionUnitTestConfiguration
import org.orkg.statistics.domain.GroupNotFound
import org.orkg.statistics.domain.InvalidParameterValue
import org.orkg.statistics.domain.MetricNotFound
import org.orkg.statistics.domain.TooManyParameterValues
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [StatisticsControllerExceptionUnitTestConfiguration::class])
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

    @Test
    fun invalidParameterValue() {
        val type = "orkg:problem:invalid_parameter_value"
        documentedGetRequestTo(InvalidParameterValue("param1", "invalid", IllegalArgumentException("invalid is not a valid value.")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid value "invalid" for parameter "param1".""")
            .andExpect(jsonPath("$.parameter_name").value("param1"))
            .andExpect(jsonPath("$.parameter_value").value("invalid"))
            .andExpect(jsonPath("$.reason").value("invalid is not a valid value."))
            .andDocument {
                responseFields<InvalidParameterValue>(
                    fieldWithPath("parameter_name").description("The name of the parameter."),
                    fieldWithPath("parameter_value").description("The value of the parameter."),
                    fieldWithPath("reason").description("The reason why the value is invalid."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
