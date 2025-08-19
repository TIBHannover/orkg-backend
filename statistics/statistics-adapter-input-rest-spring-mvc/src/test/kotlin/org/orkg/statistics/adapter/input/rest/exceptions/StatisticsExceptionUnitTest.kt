package org.orkg.statistics.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.statistics.domain.GroupNotFound
import org.orkg.statistics.domain.MetricNotFound
import org.orkg.statistics.domain.TooManyParameterValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

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
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun metricNotFound() {
        documentedGetRequestTo(MetricNotFound("group1", "metric1"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:metric_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Metric "group1-metric1" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyParameterValues() {
        documentedGetRequestTo(TooManyParameterValues("param1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameter_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for parameter "param1".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
