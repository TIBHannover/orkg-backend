package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.net.URI
import java.util.UUID
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
class ObservatoryNotFound(id: UUID) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrganizationNotFound(id: UUID) : RuntimeException("""Organization "$id" not found""")

@ResponseStatus(HttpStatus.FORBIDDEN)
class ResourceCantBeDeleted(id: ResourceId) : RuntimeException("Unable to delete Resource $id because it is used in at least one statement")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ClassNotAllowed(`class`: String) : RuntimeException("This class id ($`class`) is not allowed")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ClassAlreadyExists(`class`: String) : RuntimeException("The class with the id ($`class`) already exists")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DuplicateURI(uri: URI, id: String) :
    PropertyValidationException("uri", "The URI <$uri> is already assigned to class with ID $id.")

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFound(userId: String) : RuntimeException("""User $userId not found""")

/**
 * Base class for custom property validation.
 */
abstract class PropertyValidationException(open val property: String, override val message: String) : RuntimeException()

/**
 * Exception indicating that a property was blank when it was not supposed to be.
 */
class PropertyIsBlank(override val property: String) : PropertyValidationException(property, "must not be blank")
