package eu.tib.orkg.prototype.core.rest

import eu.tib.orkg.prototype.auth.rest.UserRegistrationException
import eu.tib.orkg.prototype.shared.ForbiddenOperationException
import eu.tib.orkg.prototype.shared.LoggedMessageException
import eu.tib.orkg.prototype.shared.PropertyValidationException
import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.toSnakeCase
import java.time.OffsetDateTime
import javax.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

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

    @ExceptionHandler(PropertyValidationException::class)
    fun handlePropertyValidationException(
        ex: PropertyValidationException,
        request: HttpServletRequest
    ) = buildBadRequestResponse(ex, request.requestURI) {
            // Use a list so that it is compatible to the javax.validation responses
            listOf(
                FieldValidationError(field = ex.property, message = ex.message)
            )
        }

    @ExceptionHandler(ForbiddenOperationException::class)
    fun handleForbiddenOperationException(
        ex: ForbiddenOperationException,
        request: HttpServletRequest
    ) = buildForbiddenResponse(ex, request.requestURI) {
            // Use a list so that it is compatible to the javax.validation responses
            listOf(
                FieldValidationError(field = ex.property, message = ex.message)
            )
        }

    @ExceptionHandler(UserRegistrationException::class)
    fun handleUserRegistrationException(
        ex: UserRegistrationException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val payload = MessageErrorResponse(
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            path = request.requestURI,
            message = ex.message
        )
        return ResponseEntity(payload, ex.status)
    }

    @ExceptionHandler(LoggedMessageException::class)
    fun handleLoggedMessageException(
        ex: LoggedMessageException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        ex.printStackTrace()
        return handleSimpleMessageException(ex, request)
    }

    @ExceptionHandler(SimpleMessageException::class)
    fun handleSimpleMessageException(
        ex: SimpleMessageException,
        request: HttpServletRequest
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

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val payload = ErrorResponse(
            status = INTERNAL_SERVER_ERROR.value(),
            error = INTERNAL_SERVER_ERROR.reasonPhrase,
            path = request.requestURI,
        )
        ex.printStackTrace()
        return ResponseEntity(payload, INTERNAL_SERVER_ERROR)
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

private val WebRequest.requestURI: String
    get() = when (this) {
        is ServletWebRequest -> request.requestURI
        else -> contextPath // most likely this is empty
    }
