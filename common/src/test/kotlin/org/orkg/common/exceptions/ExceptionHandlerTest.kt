package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [ExceptionHandler::class, ExceptionHandlerTest.TestController::class])
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

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/errors/simple")
        fun simpleMessageWithCause(): Nothing = throw FakeSimpleExceptionWithoutCause()
    }

    internal class FakeSimpleExceptionWithoutCause : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = "Something went terribly wrong!",
        cause = null,
    )
}
