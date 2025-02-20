package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.ComparisonExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
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
internal class ComparisonExceptionUnitTest : MockMvcBaseTest("comparisons") {
    @Test
    fun comparisonAlreadyPublished() {
        val id = "R123"

        get("/comparison-already-published")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-already-published"))
            .andExpect(jsonPath("$.message").value("""Comparison "$id" is already published."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonNotModifiable() {
        val id = "R123"

        get("/comparison-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonRelatedResourceNotModifiable() {
        val id = "R123"

        get("/comparison-related-resource-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-related-resource-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison related resource "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonRelatedFigureNotModifiable() {
        val id = "R123"

        get("/comparison-related-figure-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-related-figure-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison related figure "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/comparison-already-published")
        fun comparisonAlreadyPublished(
            @RequestParam id: ThingId,
        ): Unit = throw ComparisonAlreadyPublished(id)

        @GetMapping("/comparison-not-modifiable")
        fun comparisonNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw ComparisonNotModifiable(id)

        @GetMapping("/comparison-related-resource-not-modifiable")
        fun comparisonRelatedResourceNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw ComparisonRelatedResourceNotModifiable(id)

        @GetMapping("/comparison-related-figure-not-modifiable")
        fun comparisonRelatedFigureNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw ComparisonRelatedFigureNotModifiable(id)
    }
}
