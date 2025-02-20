package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.adapter.input.rest.exceptions.ContributorExceptionUnitTest.TestController
import org.orkg.community.domain.ContributorAlreadyExists
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
internal class ContributorExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun contributorAlreadyExists() {
        val id = ContributorId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")

        get("/contributor-already-exists")
            .param("id", id.value.toString())
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/contributor-already-exists"))
            .andExpect(jsonPath("$.message").value("""Contributor "$id" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/contributor-already-exists")
        fun contributorAlreadyExists(
            @RequestParam id: ContributorId,
        ): Unit = throw ContributorAlreadyExists(id)
    }
}
