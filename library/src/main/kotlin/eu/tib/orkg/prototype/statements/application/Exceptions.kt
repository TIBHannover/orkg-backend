package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.ForbiddenOperationException
import eu.tib.orkg.prototype.shared.LoggedMessageException
import eu.tib.orkg.prototype.shared.PropertyValidationException
import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import java.util.*
import org.springframework.http.HttpStatus

class ResourceNotFound private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.NOT_FOUND, message) {
    companion object {
        fun withId(id: ThingId) = withId(id.value)
        fun withId(id: String) = ResourceNotFound("""Resource "$id" not found.""")
        fun withDOI(doi: String) = ResourceNotFound("""Resource with DOI "$doi" not found.""")
        fun withLabel(label: String) = ResourceNotFound("""Resource with label "$label" not found.""")
    }
}

class LiteralNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Literal "$id" not found.""")
    constructor(id: ThingId) : this(id.value)
}

class ClassNotFound private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.NOT_FOUND, message) {
    companion object {
        fun withThingId(id: ThingId) = withId(id.value)
        fun withId(id: String) = ClassNotFound("""Class "$id" not found.""")
        fun withURI(uri: URI) = ClassNotFound("""Class with URI "$uri" not found.""")
    }
}

class PredicateNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Predicate "$id" not found.""")
    constructor(id: ThingId) : this(id.value)
}

class StatementNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Statement "$id" not found.""")
    constructor(id: StatementId) : this(id.value)
}

class ContributorNotFound(id: ContributorId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Contributor "$id" not found.""")

class ResearchFieldNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Research field "$id" not found.""")

class ResearchProblemNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Research problem "$id" not found.""")

class DatasetNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Dataset "$id" not found.""")

class ResourceCantBeDeleted(id: ThingId) :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Unable to delete resource "$id" because it is used in at least one statement.""")

class PredicateCantBeDeleted(id: ThingId) :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Unable to delete predicate "$id" because it is used in at least one statement.""")

class ClassNotAllowed(`class`: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class id "$`class`" is not allowed.""")

class ClassAlreadyExists(`class`: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class "$`class`" already exists.""")

class ResourceAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Resource "$id" already exists.""")

class PredicateAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Predicate "$id" already exists.""")

class InvalidClassCollection(ids: Iterable<ThingId>) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The collection of classes "$ids" contains one or more invalid classes.""")

class DuplicateURI(uri: URI, id: String) :
    PropertyValidationException("uri", """The URI <$uri> is already assigned to class with ID "$id".""")

class InvalidUUID(uuid: String, cause: Throwable?) :
    PropertyValidationException("id", """Value "$uuid" is not a valid UUID.""", cause)

class InvalidLabel : PropertyValidationException("label", "A label must not be blank or contain newlines.")

class InvalidURI : PropertyValidationException("uri", "The provided URI is not a valid URI.")

class UserNotFound : SimpleMessageException {
    constructor(userId: UUID) : super(HttpStatus.BAD_REQUEST, """User "$userId" not found.""")
    constructor(email: String) : super(HttpStatus.BAD_REQUEST, """User with email "$email" not found.""")
}

class OrcidNotValid(orcid: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The ORCID "$orcid" is not valid.""")

class OrphanOrcidValue(orcid: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The ORCID "$orcid" is not attached to any author.""")

class CannotResetURI(id: String) :
    ForbiddenOperationException("uri", """The class "$id" already has a URI. It is not allowed to change URIs.""")

class URIAlreadyInUse(uri: String) :
    ForbiddenOperationException("uri", """The URI <$uri> is already in use by another class.""")

class InvalidClassFilter(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The class "$id" cannot be included and excluded at the same time.""")

class StatementSubjectNotFound(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Subject "$id" not found.""")

class StatementPredicateNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Predicate "$id" not found.""")

class StatementObjectNotFound(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Object "$id" not found.""")

class DOIRegistrationError(doi: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Unable to register DOI "$doi".""")

class DOIServiceUnavailable : LoggedMessageException {
    constructor(cause: Throwable) : super(HttpStatus.SERVICE_UNAVAILABLE, """DOI service unavailable""", cause)
    constructor(responseMessage: String, errorResponse: String) :
        super(HttpStatus.SERVICE_UNAVAILABLE, """DOI service returned "$responseMessage" with error response: $errorResponse""")
}

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
    }
}

private fun formatParameters(vararg parameters: String) =
    setOf(*parameters).joinToString { "\"$it\"" }

/**
 * Exception indicating that a property was blank when it was not supposed to be.
 */
class PropertyIsBlank(override val property: String) : PropertyValidationException(property, "must not be blank")
