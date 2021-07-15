package eu.tib.orkg.prototype.core.rest

import eu.tib.orkg.prototype.statements.application.PropertyValidationException
import eu.tib.orkg.prototype.toSnakeCase
import java.time.OffsetDateTime
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ) = buildBadRequestResponse(ex) {
        ex.bindingResult.fieldErrors.map {
            FieldValidationError(field = it.field.toSnakeCase(), message = it?.defaultMessage)
        }
    }

    @ExceptionHandler(PropertyValidationException::class)
    fun handleIllegalArgumentException(ex: PropertyValidationException) =
        buildBadRequestResponse(ex) {
            // Use a list so that it is compatible to the javax.validation responses
            listOf(
                FieldValidationError(field = ex.property, message = ex.message)
            )
        }

    fun <T> buildBadRequestResponse(
        exception: T,
        block: (ex: T) -> List<FieldValidationError>
    ): ResponseEntity<Any> {
        val errors = block(exception)
        val payload = ValidationFailureResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            errors = errors
        )
        return ResponseEntity.badRequest().body(payload)
    }

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
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
        val status: Int,
        val error: String,
        val errors: List<FieldValidationError>
    )
}
