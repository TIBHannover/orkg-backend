package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [ExceptionHandler::class, ExceptionHandlerTest.TestController::class, CommonSpringConfig::class, FixedClockConfig::class])
internal class ExceptionHandlerTest : RestDocsTest("errors") {
    @Test
    fun simpleMessageException() {
        mockMvc.perform(documentedGetRequestTo("/errors/simple"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("Something went terribly wrong!")))
            .andExpect(jsonPath("$.path", `is`("/errors/simple")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
            .andDo(
                documentationHandler.document(
                    responseFields(
                        fieldWithPath("status").description("The HTTP status code of the error. This is equal to the status code of the request itself and MUST only be used for display purposes."),
                        fieldWithPath("error").description("The human-readable error description of the status code, e.g. \"Bad Request\" for code 400."),
                        fieldWithPath("message").description("A human-readable, and hopefully helpful message that explains the error."),
                        fieldWithPath("path").description("The path to which the request was made that caused the error."),
                        fieldWithPath("timestamp").description("The <<timestamp-representation,timestamp>> of when the error happened."),
                    )
                )
            )
    }

    @Test
    fun jsonMissingFieldException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonUnknownFieldException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"unknown": 1, "field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Unknown field "$.nested.unknown".""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonTypeMismatchException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": [], "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonTypeMismatchArrayException() {
        post("/errors/json")
            .content("""{"field": "abc", "array": [{"unknown": 1}] "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.array[0].field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonNullValueInCollectionException() {
        post("/errors/json")
            .content("""{"field": "abc", "nested": {"field": "def", "list": [null]}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.nested.list" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonNullValueException() {
        post("/errors/json")
            .content("""{"field": null, "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Field "$.field" is either missing, "null", of invalid type, or contains "null" values.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun jsonMalformedException() {
        post("/errors/json")
            .content("""{"field": "abc" "nested": {"field": "def", "list": []}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`(400)))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.message", `is`("""Unexpected character ('"' (code 34)): was expecting comma to separate Object entries""")))
            .andExpect(jsonPath("$.path", `is`("/errors/json")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun serviceUnavailable() {
        get("/errors/service-unavailable")
            .perform()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.status", `is`(503)))
            .andExpect(jsonPath("$.error", `is`("Service Unavailable")))
            .andExpect(jsonPath("$.message", `is`("""Service unavailable.""")))
            .andExpect(jsonPath("$.path", `is`("/errors/service-unavailable")))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

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
        @GetMapping("/errors/simple")
        fun simpleMessageWithCause(): Nothing = throw FakeSimpleExceptionWithoutCause()

        @PostMapping("/errors/json")
        fun json(@RequestBody request: JsonRequest): Nothing = throw NotImplementedError()

        @GetMapping("/errors/service-unavailable")
        fun serviceUnavailable(): Nothing = throw ServiceUnavailable.create("TEST", 500, "irrelevant")

        @GetMapping("/errors/malformed-media-type-capability")
        fun malformedMediaTypeCapability(@RequestParam name: String, @RequestParam value: String): Nothing =
            throw MalformedMediaTypeCapability(name, value)

        @GetMapping("/errors/handle-http-media-type-not-acceptable", produces = ["application/json", "application/xml"])
        fun handleHttpMediaTypeNotAcceptable() = "response"

        @PostMapping("/errors/http-media-type-not-supported", consumes = ["application/json", "application/xml"])
        fun httpMediaTypeNotSupported(@RequestBody body: String): Nothing = throw NotImplementedError()

        @Suppress("ArrayInDataClass")
        data class JsonRequest(
            val field: String,
            val nested: Nested,
            val array: Array<Nested>
        ) {
            data class Nested(
                val field: String,
                val list: List<String>
            )
        }
    }

    internal class FakeSimpleExceptionWithoutCause : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = "Something went terribly wrong!",
        cause = null,
    )
}
