package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFound(id: String? = null) : RuntimeException("Resource $id not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class LiteralNotFound(id: String? = null) : RuntimeException("Literal $id not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ClassNotFound(id: String? = null) : RuntimeException("Class $id not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class PredicateNotFound(predicate: String) : RuntimeException("Predicate $predicate is not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class StatementNotFound(statementId: String) : PropertyValidationException("ids", "Statement $statementId is not found")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ContributorNotFound(id: ContributorId) : RuntimeException("""Contributor $id not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryNotFound(id: ObservatoryId) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryURLNotFound(id: String) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrganizationNotFound(id: String) : RuntimeException("""Organization "$id" not found""") {
    constructor(id: OrganizationId) : this(id.toString())
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResearchFieldNotFound(id: ResourceId) : RuntimeException("""Research field "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResearchProblemNotFound(id: ResourceId) : RuntimeException("""Research problem "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class DatasetNotFound(id: ResourceId) : RuntimeException("""Dataset "$id" not found""")

@ResponseStatus(HttpStatus.FORBIDDEN)
class ResourceCantBeDeleted(id: ResourceId) :
    RuntimeException("Unable to delete Resource $id because it is used in at least one statement")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ClassNotAllowed(`class`: String) : RuntimeException("This class id ($`class`) is not allowed")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ClassAlreadyExists(`class`: String) : RuntimeException("The class with the id ($`class`) already exists")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidClassCollection(ids: Iterable<ClassId>) :
    RuntimeException("The collection of classes $ids contains one or more missing classes")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class DuplicateURI(uri: URI, id: String) :
    PropertyValidationException("uri", "The URI <$uri> is already assigned to class with ID $id.")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidUUID(uuid: String, cause: Throwable?) :
    PropertyValidationException("id", "Value \"$uuid\" is not a valid UUID.", cause)

class InvalidLabel : PropertyValidationException("label", "A label must not be blank or contain newlines.")

class InvalidURI : PropertyValidationException("uri", "The provided URI is not a valid URI.")

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFound(userId: String) : RuntimeException("""User $userId not found""")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class OrcidNotValid(orcid: String) : RuntimeException("ORCID value ($orcid) is not valid identifier")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class OrphanOrcidValue(orcid: String) : RuntimeException("ORCID value ($orcid) is not attached to any author!")

class CannotResetURI(id: String) :
    ForbiddenOperationException("uri", "The class $id already has a URI. It is not allowed to change URIs.")

class URIAlreadyInUse(uri: String) :
    ForbiddenOperationException("uri", "The URI <$uri> is already in use by another class.")

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

/**
 * Exception indicating that a property was blank when it was not supposed to be.
 */
class PropertyIsBlank(override val property: String) : PropertyValidationException(property, "must not be blank")
