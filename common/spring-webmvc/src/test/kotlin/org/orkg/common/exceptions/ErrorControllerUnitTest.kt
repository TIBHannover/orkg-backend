package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.exceptions.ErrorControllerUnitTest.TestController
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@WebMvcTest
@ContextConfiguration(
    classes = [
        TestController::class,
        CommonSpringConfig::class,
        ExceptionTestConfiguration::class,
        FixedClockConfig::class,
    ]
)
@Suppress("serial")
internal class ErrorControllerUnitTest : MockMvcBaseTest("errors") {
    @Test
    fun `Given endpoint that returns application json, when an error is thrown, it returns application json regardless`() {
        get("/application-json")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type", `is`("about:blank")))
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.title", `is`("Bad Request")))
            .andExpect(jsonPath("$.detail", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.instance", `is`("/application-json")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.path", `is`("/application-json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun `Given an exception, when exception defines NO_CONTENT as response status code, it returns nothing`() {
        get("/no-content-exception")
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(MockMvcResultMatchers.content().string(""))
    }

    // Since we need to use workaround to get the ErrorController to work in a MockMvc test (see ExceptionTestConfiguration),
    // this test is NOT representative of what actually happens in the system in a production environment.
    @Test
    fun `Given an endpoint that does not return application json, when an error is thrown, it returns application json regardless`() {
        get("/text-html")
            .accept(MediaType.TEXT_HTML)
            .contentType(MediaType.TEXT_HTML)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type", `is`("about:blank")))
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.title", `is`("Bad Request")))
            .andExpect(jsonPath("$.detail", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.instance", `is`("/text-html")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.path", `is`("/text-html")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    internal class SomeErrorResponse : ResponseStatusException(BAD_REQUEST, "Something went terribly wrong!", null)

    internal class NoContentException : ResponseStatusException(NO_CONTENT)

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/application-json", produces = [MediaType.APPLICATION_JSON_VALUE])
        fun extendsErrorResponse(): Nothing = throw SomeErrorResponse()

        @GetMapping("/no-content-exception")
        fun noContentException(): Nothing = throw NoContentException()

        @GetMapping("/text-html", produces = [MediaType.TEXT_HTML_VALUE])
        fun textHtml(): Nothing = throw SomeErrorResponse()
    }
}
