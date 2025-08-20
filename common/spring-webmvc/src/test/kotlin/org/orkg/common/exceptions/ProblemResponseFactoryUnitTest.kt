package org.orkg.common.exceptions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ErrorResponseCustomizer.Companion.errorResponseCustomizer
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@Suppress("serial")
internal class ProblemResponseFactoryUnitTest : MockkBaseTest {
    private val errorResponseCustomizers: List<ErrorResponseCustomizer<*>> = listOf(
        errorResponseCustomizer<BaseException> { _, problemDetail, headers ->
            headers["X-Extra"] = "Header Value"
            problemDetail.type = URI.create("custom:type")
            problemDetail.status = HttpStatus.BAD_REQUEST.value()
            problemDetail.title = "Custom title"
            problemDetail.detail = "Custom detail"
            problemDetail.setProperty("extra-property", "some value")
        },
        errorResponseCustomizer<SuperSpecificException> { _, problemDetail, _ ->
            problemDetail.title = "Not 'Custom title'"
        }
    )
    private val messageSource: MessageSource = mockk()

    private val problemResponseFactory = ProblemResponseFactory(errorResponseCustomizers, messageSource)

    @Test
    fun `Given an exception that extends ErrorResponse, it formats the exception correctly`() {
        every { messageSource.getMessage(any(), any(), any(), any()) } returns null

        val result = problemResponseFactory.createProblemResponse(
            exception = ExtendsErrorResponse(),
            status = HttpStatus.BAD_REQUEST,
            instance = "/path/to/endpoint"
        )

        result.problemDetail.asClue {
            it.type shouldBe URI.create("about:blank")
            it.status shouldBe 400
            it.title shouldBe "Bad Request"
            it.detail shouldBe "Something went terribly wrong!"
            it.instance shouldBe URI.create("/path/to/endpoint")
            it.properties shouldBe null
        }
        result.httpHeaders shouldBe HttpHeaders()

        verify(exactly = 4) { messageSource.getMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `Given an exception that does not extend ErrorResponse, then status is 500 internal server error and detail is not present`() {
        val result = problemResponseFactory.createProblemResponse(
            exception = NotExtendsErrorResponse("Not Extends Error Response"),
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            instance = "/path/to/endpoint"
        )

        result.problemDetail.asClue {
            it.type shouldBe URI.create("about:blank")
            it.status shouldBe 500
            it.title shouldBe "Internal Server Error"
            it.detail shouldBe null
            it.instance shouldBe URI.create("/path/to/endpoint")
            it.properties shouldBe null
        }
        result.httpHeaders shouldBe HttpHeaders()
    }

    @Test
    fun `Given an exception, when error response is modified by an exception customizer (matched by super class), it returns the correct result`() {
        val result = problemResponseFactory.createProblemResponse(
            exception = SpecificException("Specific Exception"),
            status = HttpStatus.BAD_REQUEST,
            instance = "/path/to/endpoint"
        )

        result.problemDetail.asClue {
            it.type shouldBe URI.create("custom:type")
            it.status shouldBe 400
            it.title shouldBe "Custom title"
            it.detail shouldBe "Custom detail"
            it.instance shouldBe URI.create("/path/to/endpoint")
            it.properties shouldBe mapOf("extra-property" to "some value")
        }
        result.httpHeaders shouldBe HttpHeaders().apply {
            set("X-Extra", "Header Value")
        }
    }

    @Test
    fun `Given an exception, when error response is modified by an matched exception customizer (matched by exact class), it returns the correct result`() {
        val result = problemResponseFactory.createProblemResponse(
            exception = SuperSpecificException("Super Specific Exception"),
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            instance = "/path/to/endpoint"
        )

        result.problemDetail.asClue {
            it.type shouldBe URI.create("about:blank")
            it.status shouldBe 500
            it.title shouldBe "Not 'Custom title'"
            it.detail shouldBe null
            it.instance shouldBe URI.create("/path/to/endpoint")
            it.properties shouldBe null
        }
        result.httpHeaders shouldBe HttpHeaders()
    }

    internal class ExtendsErrorResponse : ResponseStatusException(BAD_REQUEST, "Something went terribly wrong!", null)

    internal class NotExtendsErrorResponse(override val message: String) : Exception(message)

    internal open class BaseException(override val message: String) : Exception(message)

    internal open class SpecificException(override val message: String) : BaseException(message)

    internal class SuperSpecificException(override val message: String) : SpecificException(message)
}
