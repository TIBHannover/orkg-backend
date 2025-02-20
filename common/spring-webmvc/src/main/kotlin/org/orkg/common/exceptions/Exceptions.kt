package org.orkg.common.exceptions

import org.springframework.http.HttpStatus

/**
 * Base class for custom property validation.
 */
abstract class PropertyValidationException(
    open val property: String,
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(cause)

abstract class ForbiddenOperationException(
    property: String,
    message: String,
) : PropertyValidationException(
        property,
        message
    )

abstract class SimpleMessageException(
    open val status: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

abstract class LoggedMessageException(
    override val status: HttpStatus,
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
        property = "id",
        message = """Value "$uuid" is not a valid UUID.""",
        cause = cause
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
