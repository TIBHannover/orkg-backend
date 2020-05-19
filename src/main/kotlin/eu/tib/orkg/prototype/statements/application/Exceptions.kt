package eu.tib.orkg.prototype.statements.application

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFound : RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class LiteralNotFound : RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class ClassNotFound : java.lang.RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class PredicateNotFound(predicate: String) : RuntimeException("Predicate $predicate is not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryNotFound : RuntimeException("Observatory not found")

/**
 * Base class for custom property validation.
 */
abstract class PropertyValidationException(open val property: String, override val message: String) : RuntimeException()

/**
 * Exception indicating that a property was blank when it was not supposed to be.
 */
class PropertyIsBlank(override val property: String) : PropertyValidationException(property, "must not be blank")
