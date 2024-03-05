package org.orkg.graph.domain

import java.net.URI
import java.util.*
import kotlin.collections.List
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ForbiddenOperationException
import org.orkg.common.exceptions.PropertyValidationException
import org.orkg.common.exceptions.SimpleMessageException
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

class ThingNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Thing "$id" not found.""")
    constructor(id: ThingId) : this(id.value)
}

class ListNotFound(id: ThingId) : SimpleMessageException(HttpStatus.NOT_FOUND, """List "$id" not found.""")

class ContributorNotFound(id: ContributorId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Contributor "$id" not found.""")

class ResearchFieldNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Research field "$id" not found.""")

class ResearchProblemNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Research problem "$id" not found.""")

class DatasetNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Dataset "$id" not found.""")

class ResourceUsedInStatement(id: ThingId) :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Unable to delete resource "$id" because it is used in at least one statement.""")

class PredicateUsedInStatement(id: ThingId) :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Unable to delete predicate "$id" because it is used in at least one statement.""")

class ClassNotAllowed(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class id "$id" is not allowed.""")

class ClassAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class "$id" already exists.""")

class ResourceAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Resource "$id" already exists.""")

class PredicateAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Predicate "$id" already exists.""")

class LiteralAlreadyExists(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Literal "$id" already exists.""")

class ResourceNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Resource "$id" is not modifiable.""")

class ClassNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class "$id" is not modifiable.""")

class PredicateNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Predicate "$id" is not modifiable.""")

class LiteralNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Literal "$id" is not modifiable.""")

class StatementNotModifiable(id: StatementId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Statement "$id" is not modifiable.""")

class ListNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """List "$id" is not modifiable.""")

class InvalidClassCollection(ids: Iterable<ThingId>) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The collection of classes "$ids" contains one or more invalid classes.""")

class ReservedClass(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class "$id" is reserved and therefor cannot be set.""")

class URIAlreadyInUse(uri: URI, id: ThingId) :
    PropertyValidationException("uri", """The URI <$uri> is already assigned to class with ID "$id".""")

class InvalidLabel : PropertyValidationException("label", "A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long.")

class InvalidLiteralLabel : PropertyValidationException("label", "A literal must be at most $MAX_LABEL_LENGTH characters long.")

class InvalidLiteralDatatype : PropertyValidationException("datatype", "A literal datatype must be a URI or a \"xsd:\"-prefixed type")

class UserNotFound : SimpleMessageException {
    constructor(userId: UUID) : super(HttpStatus.BAD_REQUEST, """User "$userId" not found.""")
    constructor(email: String) : super(HttpStatus.BAD_REQUEST, """User with email "$email" not found.""")
}

class OrphanOrcidValue(orcid: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The ORCID "$orcid" is not attached to any author.""")

class CannotResetURI(id: ThingId) :
    ForbiddenOperationException("uri", """The class "$id" already has a URI. It is not allowed to change URIs.""")

class StatementSubjectNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Subject "$id" not found.""")

class StatementPredicateNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Predicate "$id" not found.""")

class StatementObjectNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Object "$id" not found.""")

class InvalidStatement private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun isListElementStatement() =
            InvalidStatement("A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists.")
    }
}

class ForbiddenStatementDeletion private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun usedInList() =
            ForbiddenStatementDeletion("A statement cannot be deleted when it is used in a list. Please see the documentation on how to manage lists.")
    }
}

class TooFewIDsError(ids: List<ThingId>) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Too few ids: At least two ids are required. Got only "${ids.size}".""")

class ListElementNotFound : PropertyValidationException("element", "All elements inside the list have to exist.")

class InvalidSubclassRelation(childId: ThingId, parentId: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The class "$childId" cannot be a subclass of "$parentId"."""")

class ParentClassAlreadyExists(childId: ThingId, parentId: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The class "$childId" already has a parent class ($parentId)."""")

class EmptyChildIds :
    SimpleMessageException(HttpStatus.BAD_REQUEST, "The provided list is empty.")

class ParentClassAlreadyHasChildren(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The class "$id" already has a child classes."""")

/**
 * Exception indicating that a property was blank when it was not supposed to be.
 */
class PropertyIsBlank(override val property: String) : PropertyValidationException(property, "must not be blank")
