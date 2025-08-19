package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.exceptions.ErrorControllerUnitTest.ErrorResponseCustomizersTestConfiguration
import org.orkg.common.exceptions.ErrorControllerUnitTest.TestController
import org.orkg.common.exceptions.ErrorResponseCustomizer.Companion.errorResponseCustomizer
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
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
import java.net.URI

@WebMvcTest
@ContextConfiguration(
    classes = [
        TestController::class,
        CommonSpringConfig::class,
        ExceptionTestConfiguration::class,
        FixedClockConfig::class,
        ErrorResponseCustomizersTestConfiguration::class,
    ]
)
@Suppress("serial")
internal class ErrorControllerUnitTest : MockMvcBaseTest("errors") {
    @Test
    fun `Given an exception that extends ErrorResponse, it formats the exception correctly`() {
        get("/extends-error-response")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type", `is`("about:blank")))
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.title", `is`("Bad Request")))
            .andExpect(jsonPath("$.detail", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.instance", `is`("/extends-error-response")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.path", `is`("/extends-error-response")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun `Given an exception that does not extend ErrorResponse, then status is 500 internal server error and detail is not present`() {
        get("/not-extends-error-response")
            .perform()
            .andExpect(status().isInternalServerError)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type", `is`("about:blank")))
            .andExpect(jsonPath("$.status", `is`(500)))
            .andExpect(jsonPath("$.title", `is`("Internal Server Error")))
            .andExpect(jsonPath("$.detail").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.instance", `is`("/not-extends-error-response")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Internal Server Error")))
            .andExpect(jsonPath("$.message").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.path", `is`("/not-extends-error-response")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun `Given an exception, when error response is modified by an exception customizer (matched by super class), it returns the correct result`() {
        get("/specific-exception")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(header().string("X-Extra", `is`("Header Value")))
            .andExpect(jsonPath("$.type", `is`("custom:type")))
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.title", `is`("Custom title")))
            .andExpect(jsonPath("$.detail", `is`("Custom detail")))
            .andExpect(jsonPath("$.instance", `is`("/specific-exception")))
            .andExpect(jsonPath("$.extra-property", `is`("some value")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Custom title")))
            .andExpect(jsonPath("$.message", `is`("Custom detail")))
            .andExpect(jsonPath("$.path", `is`("/specific-exception")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun `Given an exception, when error response is modified by an matched exception customizer (matched by exact class), it returns the correct result`() {
        get("/super-specific-exception")
            .perform()
            .andExpect(status().isInternalServerError)
            .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type", `is`("about:blank")))
            .andExpect(jsonPath("$.status", `is`(500)))
            .andExpect(jsonPath("$.title", `is`("Not 'Custom title'")))
            .andExpect(jsonPath("$.detail").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.instance", `is`("/super-specific-exception")))
            // legacy fields
            .andExpect(jsonPath("$.error", `is`("Not 'Custom title'")))
            .andExpect(jsonPath("$.message").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.path", `is`("/super-specific-exception")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun `Given an exception, when exception defines NO_CONTENT as response status code, it returns nothing`() {
        get("/no-content-exception")
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(MockMvcResultMatchers.content().string(""))
    }

    internal class ExtendsErrorResonse : ResponseStatusException(BAD_REQUEST, "Something went terribly wrong!", null)

    internal class NotExtendsErrorResonse(override val message: String) : Exception(message)

    internal open class BaseException(override val message: String) : Exception(message)

    internal open class SpecificException(override val message: String) : BaseException(message)

    internal class SuperSpecificException(override val message: String) : SpecificException(message)

    internal class NoContentException : ResponseStatusException(NO_CONTENT)

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/extends-error-response")
        fun extendsErrorResponse(): Nothing = throw ExtendsErrorResonse()

        @GetMapping("/not-extends-error-response")
        fun notExtendsErrorResponse(): Nothing = throw NotExtendsErrorResonse("Not Extends Error Resonse")

        @GetMapping("/specific-exception")
        fun specificException(): Nothing = throw SpecificException("Specific Exception")

        @GetMapping("/super-specific-exception")
        fun superSpecificException(): Nothing = throw SuperSpecificException("Super Specific Exception")

        @GetMapping("/no-content-exception")
        fun noContentException(): Nothing = throw NoContentException()
    }

    @TestConfiguration
    internal class ErrorResponseCustomizersTestConfiguration {
        @Bean
        fun baseExceptionCustomizer() =
            errorResponseCustomizer<BaseException> { _, problemDetail, headers ->
                headers["X-Extra"] = "Header Value"
                problemDetail.type = URI.create("custom:type")
                problemDetail.status = HttpStatus.BAD_REQUEST.value()
                problemDetail.title = "Custom title"
                problemDetail.detail = "Custom detail"
                problemDetail.setProperty("extra-property", "some value")
            }

        @Bean
        fun superSpecificExceptionCustomizer() =
            errorResponseCustomizer<SuperSpecificException> { _, problemDetail, _ ->
                problemDetail.title = "Not 'Custom title'"
            }
    }
}
