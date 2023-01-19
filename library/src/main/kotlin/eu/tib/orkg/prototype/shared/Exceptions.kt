package eu.tib.orkg.prototype.shared

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
