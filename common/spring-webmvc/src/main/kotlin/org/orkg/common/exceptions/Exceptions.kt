package org.orkg.common.exceptions

import org.orkg.common.toSnakeCase
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import org.springframework.validation.FieldError as SpringFieldError

fun createProblemURI(type: String): URI = URI.create("orkg:problem:$type")

/**
 * Base class for custom property validation.
 */
abstract class PropertyValidationException(
    property: String,
    override val message: String,
    override val cause: Throwable? = null,
    status: HttpStatus = HttpStatus.BAD_REQUEST,
    type: URI? = null,
) : SimpleMessageException(
        status = status,
        message = null,
        cause = cause,
        properties = mapOf("errors" to listOf(FieldError(message, property, message, property))),
        type = type,
    )

abstract class ForbiddenOperationException(
    property: String,
    message: String,
    type: URI? = null,
) : PropertyValidationException(
        property = property,
        message = message,
        status = HttpStatus.FORBIDDEN,
        type = type,
    )

abstract class SimpleMessageException(
    status: HttpStatus,
    override val message: String?,
    override val cause: Throwable? = null,
    properties: Map<String, Any?> = emptyMap(),
    type: URI? = null,
) : ResponseStatusException(status, message, cause) {
    init {
        if (type != null) {
            body.type = type
        } else {
            val simpleName = this::class.simpleName
            if (simpleName != null) {
                body.type = createProblemURI(simpleName.toSnakeCase())
            }
        }
        properties.forEach { property, value -> body.setProperty(property, value) }
    }
}

abstract class LoggedMessageException(
    status: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null,
) : SimpleMessageException(status, message, cause)

class MissingParameter private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun requiresAtLeastOneOf(parameter: String, vararg parameters: String) = MissingParameter(
            "Missing parameter: At least one parameter out of ${formatParameters(parameter, *parameters)} is required."
        )
    }
}

class TooManyParameters private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun requiresExactlyOneOf(first: String, second: String, vararg parameters: String) = TooManyParameters(
            "Too many parameters: Only exactly one out of ${formatParameters(first, second, *parameters)} is allowed."
        )

        fun atMostOneOf(first: String, second: String, vararg parameters: String) = TooManyParameters(
            "Too many parameters: At most one out of ${formatParameters(first, second, *parameters)} is allowed."
        )
    }
}

class UnknownParameter(parameter: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unknown parameter "$parameter"."""
    )

class UnknownSortingProperty(property: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unknown sorting property "$property"."""
    )

private fun formatParameters(vararg parameters: String) =
    setOf(*parameters).joinToString { "\"$it\"" }

class InvalidUUID(
    uuid: String,
    cause: Throwable?,
) : PropertyValidationException(
        property = jsonFieldPathToJsonPointerReference("id"),
        message = """Value "$uuid" is not a valid UUID.""",
        cause = cause,
        type = createProblemURI("invalid_uuid"),
    )

class Forbidden : SimpleMessageException(HttpStatus.FORBIDDEN, """Forbidden.""")

class Unauthorized : LoggedMessageException(HttpStatus.UNAUTHORIZED, """Unauthorized.""")

class ServiceUnavailable private constructor(
    val internalMessage: String,
    cause: Throwable? = null,
) : LoggedMessageException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable.", cause) {
    companion object {
        fun create(serviceName: String, status: Int, responseMessage: String) = ServiceUnavailable(
            internalMessage = """$serviceName service returned status $status with error response: "$responseMessage"."""
        )

        fun create(serviceName: String, cause: Throwable) = ServiceUnavailable(
            internalMessage = """$serviceName service threw an exception.""",
            cause = cause
        )
    }
}

class MalformedMediaTypeCapability(
    name: String,
    value: String,
    cause: Throwable? = null,
) : SimpleMessageException(
        status = HttpStatus.NOT_ACCEPTABLE,
        message = """Malformed value "$value" for media type capability "$name".""",
        cause = cause
    )

data class FieldError(
    val detail: String?,
    val pointer: String,
    @Deprecated("To be removed")
    val message: String?,
    @Deprecated("To be removed")
    val field: String,
) {
    companion object {
        fun from(fieldError: SpringFieldError) =
            FieldError(
                detail = fieldError.defaultMessage,
                pointer = jsonFieldPathToJsonPointerReference(fieldError.field.toSnakeCase()),
                message = fieldError.defaultMessage,
                field = fieldError.field.toSnakeCase(),
            )
    }
}
