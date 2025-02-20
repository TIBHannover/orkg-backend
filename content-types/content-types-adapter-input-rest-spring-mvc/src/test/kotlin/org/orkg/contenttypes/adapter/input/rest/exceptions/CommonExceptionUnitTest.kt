package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.CommonExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
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
internal class CommonExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun invalidMonth() {
        get("/invalid-month")
            .param("month", "0")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-month"))
            .andExpect(jsonPath("$.message").value("""Invalid month "0". Must be in range [1..12]."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun sustainableDevelopmentGoalNotFound() {
        get("/sustainable-development-goal-not-found")
            .param("sdgId", "SDG1")
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/sustainable-development-goal-not-found"))
            .andExpect(jsonPath("$.message").value("""Sustainable Development Goal "SDG1" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidBibTeXReference() {
        val reference = "not bibtex"

        get("/invalid-bibtex-reference")
            .param("reference", reference)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-bibtex-reference"))
            .andExpect(jsonPath("$.message").value("""Invalid BibTeX reference "$reference"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/invalid-month")
        fun invalidMonth(
            @RequestParam month: Int,
        ): Unit = throw InvalidMonth(month)

        @GetMapping("/sustainable-development-goal-not-found")
        fun sustainableDevelopmentGoalNotFound(
            @RequestParam sdgId: ThingId,
        ): Unit = throw SustainableDevelopmentGoalNotFound(sdgId)

        @GetMapping("/invalid-bibtex-reference")
        fun invalidBibTeXReference(
            @RequestParam reference: String,
        ): Unit = throw InvalidBibTeXReference(reference)
    }
}
