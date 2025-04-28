package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.adapter.input.rest.exceptions.ContributorIdentifierExceptionUnitTest.TestController
import org.orkg.community.domain.ContributorIdentifierAlreadyExists
import org.orkg.community.domain.UnknownIdentifierType
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
internal class ContributorIdentifierExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun unknownIdentifierType() {
        val type = "doi"

        get("/unknown-identifier-type")
            .param("type", type)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/unknown-identifier-type"))
            .andExpect(jsonPath("$.message").value("""Unknown identifier type "$type"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun contributorIdentifierAlreadyExists() {
        val contributorId = "9d791767-245b-46e1-b260-2c00fb34efda"
        val value = "identifier"

        get("/contributor-identifier-already-exists")
            .param("contributorId", contributorId)
            .param("value", value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/contributor-identifier-already-exists"))
            .andExpect(jsonPath("$.message").value("""Identifier "$value" for contributor "$contributorId" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/unknown-identifier-type")
        fun unknownIdentifierType(
            @RequestParam type: String,
        ): Unit = throw UnknownIdentifierType(type)

        @GetMapping("/contributor-identifier-already-exists")
        fun contributorIdentifierAlreadyExists(
            @RequestParam contributorId: ContributorId,
            @RequestParam value: String,
        ): Unit = throw ContributorIdentifierAlreadyExists(contributorId, value)
    }
}
