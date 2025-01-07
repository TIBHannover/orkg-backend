package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.ComparisonControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class ComparisonControllerExceptionUnitTest : RestDocsTest("comparisons") {

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

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/comparison-already-published")
        fun comparisonAlreadyPublished(@RequestParam id: ThingId) {
            throw ComparisonAlreadyPublished(id)
        }
    }
}
