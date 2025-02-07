package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.exceptions.MediaTypeCapabilitiesExceptionUnitTest.TestController
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [ExceptionHandler::class, TestController::class, CommonSpringConfig::class, FixedClockConfig::class])
internal class MediaTypeCapabilitiesExceptionUnitTest : MockMvcBaseTest("errors") {

    @Test
    fun malformedMediaTypeCapability() {
        val name = "formatted-label"
        val value = "true"

        get("/errors/malformed-media-type-capability")
            .param("name", name)
            .param("value", value)
            .perform()
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_ACCEPTABLE.value()))
            .andExpect(jsonPath("$.error", `is`("Not Acceptable")))
            .andExpect(jsonPath("$.path").value("/errors/malformed-media-type-capability"))
            .andExpect(jsonPath("$.message").value("""Malformed value "$value" for media type capability "$name"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun handleHttpMediaTypeNotAcceptable() {
        get("/errors/handle-http-media-type-not-acceptable")
            .accept("application/unsupported+json")
            .perform()
            .andExpect(status().isNotAcceptable)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_ACCEPTABLE.value()))
            .andExpect(jsonPath("$.error", `is`("Not Acceptable")))
            .andExpect(jsonPath("$.path").value("/errors/handle-http-media-type-not-acceptable"))
            .andExpect(jsonPath("$.message").value("""Unsupported response media type. Please check the 'Accept' header for a list of supported media types."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
            .andExpect(header().string("Accept", "application/json, application/xml"))
    }

    @Test
    fun httpMediaTypeNotSupported() {
        post("/errors/http-media-type-not-supported")
            .content("request")
            .contentType("text/plain")
            .perform()
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.status").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
            .andExpect(jsonPath("$.error", `is`("Unsupported Media Type")))
            .andExpect(jsonPath("$.path").value("/errors/http-media-type-not-supported"))
            .andExpect(jsonPath("$.message").value("""Unsupported request media type. Please check the 'Accept' header for a list of supported media types."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
            .andExpect(header().string("Accept", "application/json, application/xml"))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/errors/malformed-media-type-capability")
        fun malformedMediaTypeCapability(@RequestParam name: String, @RequestParam value: String): Nothing =
            throw MalformedMediaTypeCapability(name, value)

        @GetMapping("/errors/handle-http-media-type-not-acceptable", produces = ["application/json", "application/xml"])
        fun handleHttpMediaTypeNotAcceptable() = "response"

        @PostMapping("/errors/http-media-type-not-supported", consumes = ["application/json", "application/xml"])
        fun httpMediaTypeNotSupported(@RequestBody body: String): Nothing = throw NotImplementedError()
    }
}
