package org.orkg.graph.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ForbiddenOperationException
import org.orkg.common.exceptions.PropertyValidationException
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.createProblemURI
import org.orkg.common.exceptions.jsonFieldPathToJsonPointerReference
import org.springframework.http.HttpStatus

class ResourceNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Resource "$id" not found.""")
    constructor(id: ThingId) : this(id.value)
}

class LiteralNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Literal "$id" not found.""")
    constructor(id: ThingId) : this(id.value)
}

class ClassNotFound private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.NOT_FOUND, message, properties = properties) {
    companion object {
        fun withThingId(id: ThingId) = withId(id.value)

        fun withId(id: String) = ClassNotFound("""Class "$id" not found.""", mapOf("id" to id))

        fun withURI(uri: ParsedIRI) = ClassNotFound("""Class with URI "$uri" not found.""", mapOf("uri" to uri.toString()))
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

class ExternalResourceNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(HttpStatus.NOT_FOUND, """External resource "$id" for ontology "$ontologyId" not found.""")
    constructor(ontologyId: String, uri: ParsedIRI) :
        this(ontologyId, uri.toString())
}

class ExternalPredicateNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(HttpStatus.NOT_FOUND, """External predicate "$id" for ontology "$ontologyId" not found.""")
    constructor(ontologyId: String, uri: ParsedIRI) :
        this(ontologyId, uri.toString())
}

class ExternalClassNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(HttpStatus.NOT_FOUND, """External class "$id" for ontology "$ontologyId" not found.""")
    constructor(ontologyId: String, uri: ParsedIRI) :
        this(ontologyId, uri.toString())
}

class ListNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """List "$id" not found."""
    )

class ResourceInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete resource "$id" because it is used in at least one statement."""
    )

class PredicateInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete predicate "$id" because it is used in at least one statement."""
    )

class ListInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete list "$id" because it is used in at least one statement."""
    )

class ReservedClassId(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class id "$id" is reserved."""
    )

class ClassAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$id" already exists."""
    )

class ResourceAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Resource "$id" already exists."""
    )

class PredicateAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Predicate "$id" already exists."""
    )

class LiteralAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Literal "$id" already exists."""
    )

class ThingAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """A thing with id "$id" already exists."""
    )

class StatementAlreadyExists(id: StatementId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Statement already exists with id "$id"."""
    )

class ResourceNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Resource "$id" is not modifiable."""
    )

class ClassNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Class "$id" is not modifiable."""
    )

class PredicateNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Predicate "$id" is not modifiable."""
    )

class LiteralNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literal "$id" is not modifiable."""
    )

class StatementNotModifiable(id: StatementId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Statement "$id" is not modifiable."""
    )

class ListNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """List "$id" is not modifiable."""
    )

class InvalidClassCollection(ids: Iterable<ThingId>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The collection of classes "$ids" contains one or more invalid classes."""
    )

class ReservedClass(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$id" is reserved and therefor cannot be set."""
    )

class URIAlreadyInUse(
    uri: ParsedIRI,
    id: ThingId,
) : PropertyValidationException(
        jsonFieldPathToJsonPointerReference("uri"),
        """The URI <$uri> is already assigned to class with ID "$id".""",
        type = createProblemURI("uri_already_in_use"),
    )

class URINotAbsolute(uri: ParsedIRI) :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference("uri"),
        """The URI <$uri> is not absolute.""",
        type = createProblemURI("uri_not_absolute"),
    )

class InvalidLabel(val property: String = "label") :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference(property),
        "A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."
    )

class InvalidDescription(val property: String = "description") :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference(property),
        "A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long."
    )

class InvalidLiteralLabel : PropertyValidationException {
    constructor() :
        super(jsonFieldPathToJsonPointerReference("label"), "A literal must be at most $MAX_LABEL_LENGTH characters long.")
    constructor(label: String, datatype: String) :
        super(jsonFieldPathToJsonPointerReference("label"), """Literal value "$label" is not a valid "$datatype".""")
}

class InvalidLiteralDatatype :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference("datatype"),
        "A literal datatype must be a URI or a \"xsd:\"-prefixed type."
    )

class CannotResetURI(id: ThingId) :
    ForbiddenOperationException(
        jsonFieldPathToJsonPointerReference("uri"),
        """The class "$id" already has a URI. It is not allowed to change URIs.""",
        type = createProblemURI("cannot_reset_uri")
    )

class StatementSubjectNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Subject "$id" not found."""
    )

class StatementPredicateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Predicate "$id" not found."""
    )

class StatementObjectNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" not found."""
    )

class InvalidStatement private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun isListElementStatement() = InvalidStatement(
            "A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists."
        )

        fun subjectMustNotBeLiteral() = InvalidStatement(
            "Subject must not be a literal."
        )

        fun includesRosettaStoneStatementResource() = InvalidStatement(
            "A rosetta stone statement resource cannot be managed using statements endpoint. Please see the documentation on how to manage rosetta stone statements."
        )
    }
}

class StatementInUse private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, null) {
    companion object {
        fun usedInList() = StatementInUse(
            "A statement cannot be deleted when it is used in a list. Please see the documentation on how to manage lists."
        )
    }
}

class ListElementNotFound :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference("elements"),
        "All elements inside the list have to exist."
    )

class InvalidSubclassRelation(
    childId: ThingId,
    parentId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The class "$childId" cannot be a subclass of "$parentId"."""
    )

class ParentClassAlreadyExists(
    childId: ThingId,
    parentId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The class "$childId" already has a parent class ($parentId)."""
    )

class EmptyChildIds : SimpleMessageException(HttpStatus.BAD_REQUEST, "The provided list of child classes is empty.")

class ParentClassAlreadyHasChildren(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The class "$id" already has a child classes."""
    )

class NeitherOwnerNorCurator private constructor(
    status: HttpStatus,
    override val message: String,
) : SimpleMessageException(status, message) {
    constructor(contributorId: ContributorId) : this(
        status = HttpStatus.FORBIDDEN,
        message = "Contributor <$contributorId> does not own the entity to be deleted and is not a curator."
    )

    companion object {
        fun cannotChangeVisibility(id: ThingId): NeitherOwnerNorCurator =
            NeitherOwnerNorCurator(
                status = HttpStatus.FORBIDDEN,
                message = """Insufficient permissions to change visibility of entity "$id"."""
            )
    }
}

class NotACurator private constructor(
    status: HttpStatus,
    override val message: String,
) : SimpleMessageException(status, message) {
    constructor(contributorId: ContributorId) : this(
        status = HttpStatus.FORBIDDEN,
        message = "Contributor <$contributorId> is not a curator."
    )

    companion object {
        fun cannotChangeVerifiedStatus(contributorId: ContributorId): NotACurator =
            NotACurator(
                status = HttpStatus.FORBIDDEN,
                message = """Cannot change verified status: Contributor <$contributorId> is not a curator."""
            )
    }
}
