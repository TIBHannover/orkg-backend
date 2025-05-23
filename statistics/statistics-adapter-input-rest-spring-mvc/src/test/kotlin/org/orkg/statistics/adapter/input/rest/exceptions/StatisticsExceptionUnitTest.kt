package org.orkg.statistics.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.statistics.adapter.input.rest.exceptions.StatisticsExceptionUnitTest.TestController
import org.orkg.statistics.domain.GroupNotFound
import org.orkg.statistics.domain.MetricNotFound
import org.orkg.statistics.domain.TooManyParameterValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [TestController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class StatisticsExceptionUnitTest : MockMvcBaseTest("statistics") {
    @Test
    fun groupNotFound() {
        val id = "group1"

        get("/group-not-found")
            .param("id", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/group-not-found"))
            .andExpect(jsonPath("$.message").value("""Group "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun metricNotFound() {
        val group = "group1"
        val name = "metric1"

        get("/metric-not-found")
            .param("group", group)
            .param("name", name)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/metric-not-found"))
            .andExpect(jsonPath("$.message").value("""Metric "$group-$name" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tooManyParameterValues() {
        val name = "param1"

        get("/too-many-parameter-values")
            .param("name", name)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/too-many-parameter-values"))
            .andExpect(jsonPath("$.message").value("""Too many values for parameter "$name"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/group-not-found")
        fun groupNotFound(
            @RequestParam id: String,
        ): Unit = throw GroupNotFound(id)

        @GetMapping("/metric-not-found")
        fun metricNotFound(
            @RequestParam group: String,
            @RequestParam name: String,
        ): Unit = throw MetricNotFound(group, name)

        @GetMapping("/too-many-parameter-values")
        fun tooManyParameterValues(
            @RequestParam name: String,
        ): Unit = throw TooManyParameterValues(name)
    }
}
