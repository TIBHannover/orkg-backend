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
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class StatisticsExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun groupNotFound() {
        documentedGetRequestTo(GroupNotFound("group1"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:group_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Group "group1" not found.""")
            .andExpect(jsonPath("$.group_name").value("group1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("group_name").description("The name of the group."),
                    )
                )
            )
    }

    @Test
    fun metricNotFound() {
        documentedGetRequestTo(MetricNotFound("group1", "metric1"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:metric_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Metric "group1-metric1" not found.""")
            .andExpect(jsonPath("$.group_name").value("group1"))
            .andExpect(jsonPath("$.metric_name").value("metric1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("group_name").description("The name of the group."),
                        fieldWithPath("metric_name").description("The name of the metric."),
                    )
                )
            )
    }

    @Test
    fun tooManyParameterValues() {
        documentedGetRequestTo(TooManyParameterValues("param1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameter_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for parameter "param1".""")
            .andExpect(jsonPath("$.parameter_name").value("param1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("parameter_name").description("The name of the parameter."),
                    )
                )
            )
    }
}
