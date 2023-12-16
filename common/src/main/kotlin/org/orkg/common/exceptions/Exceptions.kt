package org.orkg.common.exceptions

import org.springframework.http.HttpStatus

/**
 * Base class for custom property validation.
 */
abstract class PropertyValidationException(
    open val property: String,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(cause)

abstract class ForbiddenOperationException(property: String, message: String) :
    PropertyValidationException(
        property,
        message
    )

abstract class SimpleMessageException(
    open val status: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

abstract class LoggedMessageException(
    override var status: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null
) : SimpleMessageException(status, message, cause)

class MissingParameter private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun requiresAll(parameter: String, vararg parameters: String) =
            MissingParameter("Missing parameters: All parameters out of ${formatParameters(parameter, *parameters)} are required.")
        fun requiresAtLeastOneOf(parameter: String, vararg parameters: String) =
            MissingParameter("Missing parameter: At least one parameter out of ${formatParameters(parameter, *parameters)} is required.")
    }
}

class TooManyParameters private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun requiresExactlyOneOf(first: String, second: String, vararg parameters: String) =
            TooManyParameters("Too many parameters: Only exactly one out of ${formatParameters(first, second, *parameters)} is allowed.")
        fun atMostOneOf(first: String, second: String, vararg parameters: String) =
            TooManyParameters("Too many parameters: At most one out of ${formatParameters(first, second, *parameters)} is allowed.")
    }
}

class UnknownSortingProperty(property: String) : SimpleMessageException(HttpStatus.BAD_REQUEST, """Unknown sorting property "$property".""")

private fun formatParameters(vararg parameters: String) =
    setOf(*parameters).joinToString { "\"$it\"" }

class InvalidUUID(uuid: String, cause: Throwable?) :
    PropertyValidationException("id", """Value "$uuid" is not a valid UUID.""", cause)

class Unauthorized :
    SimpleMessageException(HttpStatus.UNAUTHORIZED, """Unauthorized.""")
