package org.orkg.testing.spring

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.validationExceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private const val ERROR_ENDPOINT_PATH = "/error"

@WebMvcTest
@ContextConfiguration(classes = [ExceptionTestConfiguration::class, FixedClockConfig::class])
abstract class MockMvcExceptionBaseTest : MockMvcBaseTest("errors") {
    @MockkBean
    private lateinit var testController: TestController

    protected final fun <T : Throwable> get(
        throwable: T,
        urlTemplate: String = ERROR_ENDPOINT_PATH,
        vararg uriValues: Any,
    ): ResultActions =
        mockIfNeeded(throwable, urlTemplate) {
            get(urlTemplate, *uriValues).perform().andExpectErrorResponse()
        }

    protected final fun <T : Throwable> documentedGetRequestTo(
        throwable: T,
        urlTemplate: String = ERROR_ENDPOINT_PATH,
        vararg uriValues: Any,
    ): ResultActions =
        mockIfNeeded(throwable, urlTemplate) {
            documentedGetRequestTo(urlTemplate, *uriValues).perform().andExpectErrorResponse()
        }

    private inline fun <T : Throwable> mockIfNeeded(throwable: T, urlTemplate: String, block: () -> ResultActions): ResultActions {
        if (urlTemplate == ERROR_ENDPOINT_PATH) {
            every { testController.error() } throws throwable
        }

        val result = block()

        if (urlTemplate == ERROR_ENDPOINT_PATH) {
            verify(exactly = 1) { testController.error() }
        }

        return result
    }

    protected final fun ResultActions.andDocumentWithDefaultExceptionResponseFields() =
        andDo(documentationHandler.document(responseFields(exceptionResponseFields())))

    protected final fun ResultActions.andDocumentWithoutDetailExceptionResponseFields(): ResultActions {
        val fieldDescriptors = exceptionResponseFields().filter { it.path != "detail" && it.path != "message" }
        return andDo(documentationHandler.document(responseFields(fieldDescriptors)))
    }

    protected final fun ResultActions.andDocumentWithValidationExceptionResponseFields() =
        andDo(documentationHandler.document(responseFields(validationExceptionResponseFields())))

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping(ERROR_ENDPOINT_PATH)
        fun error(): Nothing = throw RuntimeException("If you see this message, the test has failed!")
    }

    companion object {
        fun ResultActions.andExpectErrorStatus(httpStatus: HttpStatus): ResultActions =
            andExpect(status().`is`(httpStatus.value())).andExpect(jsonPath("$.status", `is`(httpStatus.value())))

        fun ResultActions.andExpectType(type: String): ResultActions =
            andExpect(jsonPath("$.type", `is`(type)))

        fun ResultActions.andExpectTitle(title: String): ResultActions =
            andExpect(jsonPath("$.title", `is`(title)))

        fun ResultActions.andExpectDetail(detail: String): ResultActions =
            andExpect(jsonPath("$.detail", `is`(detail)))

        fun ResultActions.andExpectErrorResponse(instance: String = ERROR_ENDPOINT_PATH): ResultActions =
            andExpect(jsonPath("$.status").isNumber)
                .andExpect(jsonPath("$.type", `is`(notNullValue())))
                .andExpect(jsonPath("$.title", `is`(notNullValue())))
                .andExpect(jsonPath("$.instance", `is`(instance)))
                .andExpect(header().string("Content-Type", `is`(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                // legacy fields
                .andExpect(jsonPath("$.error", `is`(notNullValue())))
                .andExpect(jsonPath("$.path", `is`(instance)))
                .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }
}
