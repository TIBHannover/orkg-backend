package org.orkg.common.exceptions

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import java.time.OffsetDateTime
import javax.servlet.http.HttpServletRequest
import org.neo4j.driver.exceptions.Neo4jException
import org.orkg.common.toSnakeCase
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.WebUtils

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ) = buildBadRequestResponse(ex, request.requestURI) {
        ex.bindingResult.fieldErrors.map {
            FieldValidationError(field = it.field.toSnakeCase(), message = it?.defaultMessage)
        }
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> =
        when (val cause = ex.cause) {
            is UnrecognizedPropertyException -> {
                val payload = MessageErrorResponse(
                    status = BAD_REQUEST.value(),
                    error = BAD_REQUEST.reasonPhrase,
                    message = """Unknown field "${cause.fieldPath}".""",
                    path = request.requestURI,
                )
                ResponseEntity(payload, BAD_REQUEST)
            }
            is MismatchedInputException -> {
                val payload = MessageErrorResponse(
                    status = BAD_REQUEST.value(),
                    error = BAD_REQUEST.reasonPhrase,
                    message = """Field "${cause.fieldPath}" is either missing, "null", of invalid type, or contains "null" values.""",
                    path = request.requestURI,
                )
                ResponseEntity(payload, BAD_REQUEST)
            }
            is JsonParseException -> {
                val payload = MessageErrorResponse(
                    status = BAD_REQUEST.value(),
                    error = BAD_REQUEST.reasonPhrase,
                    message = cause.originalMessage,
                    path = request.requestURI,
                )
                ResponseEntity(payload, BAD_REQUEST)
            }
            else -> super.handleHttpMessageNotReadable(ex, headers, status, request)
        }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        if (status == INTERNAL_SERVER_ERROR) {
            logException(ex, request)
        }
        val payload = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            path = request.requestURI
        )
        return ResponseEntity(payload, status)
    }

    @ExceptionHandler(PropertyValidationException::class)
    fun handlePropertyValidationException(
        ex: PropertyValidationException,
        request: WebRequest
    ) = buildBadRequestResponse(ex, request.requestURI) {
            // Use a list so that it is compatible to the javax.validation responses
            listOf(
                FieldValidationError(field = ex.property, message = ex.message)
            )
        }

    @ExceptionHandler(ForbiddenOperationException::class)
    fun handleForbiddenOperationException(
        ex: ForbiddenOperationException,
        request: WebRequest
    ) = buildForbiddenResponse(ex, request.requestURI) {
            // Use a list so that it is compatible to the javax.validation responses
            listOf(
                FieldValidationError(field = ex.property, message = ex.message)
            )
        }

    @ExceptionHandler(LoggedMessageException::class)
    fun handleLoggedMessageException(
        ex: LoggedMessageException,
        request: WebRequest
    ): ResponseEntity<Any> {
        logException(ex, request)
        return handleSimpleMessageException(ex, request)
    }

    @ExceptionHandler(SimpleMessageException::class)
    fun handleSimpleMessageException(
        ex: SimpleMessageException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val payload = MessageErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            path = request.requestURI,
            message = ex.message
        )
        return ResponseEntity(payload, ex.status)
    }

    /**
     * Handles exceptions when spring was unable to map a request
     * parameter to domain object due to a parsing error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<Any?>? {
        val payload = MessageErrorResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            path = request.requestURI,
            message = ex.rootCause?.message
        )
        return ResponseEntity(payload, BAD_REQUEST)
    }

    @ExceptionHandler(value = [RuntimeException::class, Neo4jException::class])
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<Any> {
        logException(ex, request)
        val payload = ErrorResponse(
            status = INTERNAL_SERVER_ERROR.value(),
            error = INTERNAL_SERVER_ERROR.reasonPhrase,
            path = request.requestURI,
        )
        return ResponseEntity(payload, INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val payload = ErrorResponse(
            status = FORBIDDEN.value(),
            error = FORBIDDEN.reasonPhrase,
            path = request.requestURI,
        )
        return ResponseEntity(payload, FORBIDDEN)
    }

    fun <T> buildBadRequestResponse(
        exception: T,
        path: String,
        block: (ex: T) -> List<FieldValidationError>
    ): ResponseEntity<Any> {
        val errors = block(exception)
        val payload = ValidationFailureResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            errors = errors,
            path = path
        )
        return ResponseEntity.badRequest().body(payload)
    }

    fun <T> buildForbiddenResponse(
        exception: T,
        path: String,
        block: (ex: T) -> List<FieldValidationError>
    ): ResponseEntity<Any> {
        val errors = block(exception)
        val payload = ValidationFailureResponse(
            status = FORBIDDEN.value(),
            error = FORBIDDEN.reasonPhrase,
            errors = errors,
            path = path
        )
        return ResponseEntity(payload, FORBIDDEN)
    }

    private fun logException(throwable: Throwable, request: WebRequest) {
        val message = buildString {
            append("Request: ")
            append(request.requestURI)
            append(request.parameterMap.toParameterString())
            append(", Headers: ")
            append(request.headerMap)

            if (request is ServletWebRequest) {
                val servletRequest: HttpServletRequest = request.request
                insert(0, " ")
                insert(0, servletRequest.method)
                val nativeRequest = WebUtils.getNativeRequest(servletRequest, ContentCachingRequestWrapper::class.java)

                if (nativeRequest != null) {
                    append(", Payload: ")
                    append(String(nativeRequest.contentAsByteArray))
                }
            }
        }

        logger.error(message, throwable)
    }

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val path: String,
        val timestamp: OffsetDateTime = OffsetDateTime.now()
    )

    /**
     * Helper class to collect the field validation error meta-data.
     */
    data class FieldValidationError(
        val field: String,
        val message: String?
    )

    /**
     * Helper class to create a validation response containing all validation errors.
     */
    data class ValidationFailureResponse(
        val status: Int,
        val error: String,
        val path: String,
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
        val errors: List<FieldValidationError>
    )

    data class MessageErrorResponse(
        val status: Int,
        val error: String,
        val path: String,
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
        val message: String?
    )
}

val WebRequest.requestURI: String
    get() = when (this) {
        is ServletWebRequest -> request.requestURI
        else -> contextPath // most likely this is empty
    }

private val WebRequest.headerMap: Map<String, String?>
    get() = headerNames.asSequence().associateWith { getHeader(it) }

private fun <K, V> Map<K, Array<V>>.toParameterString() = when {
    entries.isNotEmpty() -> entries.joinToString(separator = "&", prefix = "?") { "${it.key}=${it.value.joinToString(separator = ",")}" }
    else -> String()
}

private val JsonMappingException.fieldPath: String get() =
    path.joinToString(prefix = "$", separator = "") {
        with(it) {
            when {
                fieldName != null -> ".$fieldName"
                index >= 0 -> "[$index]"
                else -> ".?"
            }
        }
    }
