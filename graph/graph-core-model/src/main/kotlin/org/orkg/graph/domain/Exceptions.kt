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
    constructor(id: String) : super(
        HttpStatus.NOT_FOUND,
        """Resource "$id" not found.""",
        properties = mapOf("resource_id" to id),
    )
    constructor(id: ThingId) : this(id.value)
}

class LiteralNotFound : SimpleMessageException {
    constructor(id: String) : super(
        HttpStatus.NOT_FOUND,
        """Literal "$id" not found.""",
        properties = mapOf("literal_id" to id),
    )
    constructor(id: ThingId) : this(id.value)
}

class ClassNotFound private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.NOT_FOUND, message, properties = properties) {
    companion object {
        fun withThingId(id: ThingId) = withId(id.value)

        fun withId(id: String) = ClassNotFound("""Class "$id" not found.""", mapOf("class_id" to id))

        fun withURI(uri: ParsedIRI) = ClassNotFound("""Class with URI "$uri" not found.""", mapOf("class_uri" to uri.toString()))
    }
}

class PredicateNotFound : SimpleMessageException {
    constructor(id: String) : super(
        HttpStatus.NOT_FOUND,
        """Predicate "$id" not found.""",
        properties = mapOf("predicate_id" to id),
    )
    constructor(id: ThingId) : this(id.value)
}

class StatementNotFound : SimpleMessageException {
    constructor(id: String) : super(
        HttpStatus.NOT_FOUND,
        """Statement "$id" not found.""",
        properties = mapOf("statement_id" to id),
    )
    constructor(id: StatementId) : this(id.value)
}

class ThingNotFound : SimpleMessageException {
    constructor(id: String) : super(
        HttpStatus.NOT_FOUND,
        """Thing "$id" not found.""",
        properties = mapOf("thing_id" to id),
    )
    constructor(id: ThingId) : this(id.value)
}

class ExternalResourceNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(
            HttpStatus.NOT_FOUND,
            """External resource "$id" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "resource_id" to id,
                "ontology_id" to ontologyId,
            ),
        )
    constructor(ontologyId: String, uri: ParsedIRI) :
        super(
            HttpStatus.NOT_FOUND,
            """External resource "$uri" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "resource_uri" to uri.toString(),
                "ontology_id" to ontologyId,
            ),
        )
}

class ExternalPredicateNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(
            HttpStatus.NOT_FOUND,
            """External predicate "$id" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "predicate_id" to id,
                "ontology_id" to ontologyId,
            ),
        )
    constructor(ontologyId: String, uri: ParsedIRI) :
        super(
            HttpStatus.NOT_FOUND,
            """External predicate "$uri" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "predicate_uri" to uri.toString(),
                "ontology_id" to ontologyId,
            ),
        )
}

class ExternalClassNotFound : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(
            HttpStatus.NOT_FOUND,
            """External class "$id" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "class_id" to id,
                "ontology_id" to ontologyId,
            ),
        )
    constructor(ontologyId: String, uri: ParsedIRI) :
        super(
            HttpStatus.NOT_FOUND,
            """External class "$uri" for ontology "$ontologyId" not found.""",
            properties = mapOf(
                "class_uri" to uri.toString(),
                "ontology_id" to ontologyId,
            ),
        )
}

class ExternalEntityIsNotAResource : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(
            HttpStatus.FORBIDDEN,
            """Entity "$id" for ontology "$ontologyId" is not a resource.""",
            type = createProblemURI("external_entity_is_not_a_resource"),
            properties = mapOf(
                "entity_id" to id,
                "ontology_id" to ontologyId,
            ),
        )
    constructor(ontologyId: String, uri: ParsedIRI) :
        super(
            HttpStatus.FORBIDDEN,
            """Entity "$uri" for ontology "$ontologyId" is not a resource.""",
            type = createProblemURI("external_entity_is_not_a_resource"),
            properties = mapOf(
                "entity_uri" to uri.toString(),
                "ontology_id" to ontologyId,
            ),
        )
}

class ExternalEntityIsNotAClass : SimpleMessageException {
    constructor(ontologyId: String, id: String) :
        super(
            HttpStatus.FORBIDDEN,
            """Entity "$id" for ontology "$ontologyId" is not a class.""",
            type = createProblemURI("external_entity_is_not_a_class"),
            properties = mapOf(
                "entity_id" to id,
                "ontology_id" to ontologyId,
            ),
        )
    constructor(ontologyId: String, uri: ParsedIRI) :
        super(
            HttpStatus.FORBIDDEN,
            """Entity "$uri" for ontology "$ontologyId" is not a class.""",
            type = createProblemURI("external_entity_is_not_a_class"),
            properties = mapOf(
                "entity_uri" to uri.toString(),
                "ontology_id" to ontologyId,
            ),
        )
}

class ListNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """List "$id" not found.""",
        properties = mapOf("list_id" to id),
    )

class ResourceInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete resource "$id" because it is used in at least one statement.""",
        properties = mapOf("resource_id" to id),
    )

class PredicateInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete predicate "$id" because it is used in at least one statement.""",
        properties = mapOf("predicate_id" to id),
    )

class ListInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete list "$id" because it is used in at least one statement.""",
        properties = mapOf("list_id" to id),
    )

class ReservedClassId(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class id "$id" is reserved.""",
        properties = mapOf("class_id" to id),
    )

class ClassAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$id" already exists.""",
        properties = mapOf("class_id" to id),
    )

class ResourceAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Resource "$id" already exists.""",
        properties = mapOf("resource_id" to id),
    )

class PredicateAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Predicate "$id" already exists.""",
        properties = mapOf("predicate_id" to id),
    )

class LiteralAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Literal "$id" already exists.""",
        properties = mapOf("literal_id" to id),
    )

class ThingAlreadyExists(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """A thing with id "$id" already exists.""",
        properties = mapOf("thing_id" to id),
    )

class StatementAlreadyExists(id: StatementId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Statement already exists with id "$id".""",
        properties = mapOf("statement_id" to id),
    )

class ResourceNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Resource "$id" is not modifiable.""",
        properties = mapOf("resource_id" to id),
    )

class ClassNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Class "$id" is not modifiable.""",
        properties = mapOf("class_id" to id),
    )

class PredicateNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Predicate "$id" is not modifiable.""",
        properties = mapOf("predicate_id" to id),
    )

class LiteralNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literal "$id" is not modifiable.""",
        properties = mapOf("literal_id" to id),
    )

class StatementNotModifiable(id: StatementId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Statement "$id" is not modifiable.""",
        properties = mapOf("statement_id" to id),
    )

class ListNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """List "$id" is not modifiable.""",
        properties = mapOf("list_id" to id),
    )

class InvalidClassCollection(ids: Iterable<ThingId>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The collection of classes "$ids" contains one or more invalid classes.""",
        properties = mapOf("class_ids" to ids),
    )

class ReservedClass(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$id" is reserved and therefor cannot be set.""",
        properties = mapOf("class_id" to id),
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
        "A label must not be blank or contain newlines or NULL characters and must be at most $MAX_LABEL_LENGTH characters long."
    )

class InvalidDescription(val property: String = "description") :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference(property),
        "A description must not be blank or contain NULL characters and must be at most $MAX_LABEL_LENGTH characters long."
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
        """Subject "$id" not found.""",
        properties = mapOf("subject_id" to id),
    )

class StatementPredicateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Predicate "$id" not found.""",
        properties = mapOf("predicate_id" to id),
    )

class StatementObjectNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" not found.""",
        properties = mapOf("object_id" to id),
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
        """The class "$childId" cannot be a subclass of "$parentId".""",
        properties = mapOf(
            "class_id" to childId,
            "parent_class_id" to parentId,
        ),
    )

class ParentClassAlreadyExists(
    childId: ThingId,
    parentId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The class "$childId" already has a parent class ($parentId).""",
        properties = mapOf(
            "class_id" to childId,
            "parent_class_id" to parentId,
        ),
    )

class NeitherOwnerNorCurator private constructor(
    status: HttpStatus,
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(status, message, properties = properties) {
    constructor(ownerId: ContributorId, contributorId: ContributorId, thingId: ThingId) : this(
        status = HttpStatus.FORBIDDEN,
        message = "Contributor <$contributorId> does not own the entity to be deleted and is not a curator.",
        properties = mapOf(
            "owner_id" to ownerId,
            "contributor_id" to contributorId,
            "thing_id" to thingId,
        ),
    )

    companion object {
        fun cannotChangeVisibility(ownerId: ContributorId, contributorId: ContributorId, thingId: ThingId): NeitherOwnerNorCurator =
            NeitherOwnerNorCurator(
                status = HttpStatus.FORBIDDEN,
                message = """Insufficient permissions to change visibility of entity "$thingId".""",
                properties = mapOf(
                    "owner_id" to ownerId,
                    "contributor_id" to contributorId,
                    "thing_id" to thingId,
                ),
            )
    }
}

class NotACurator private constructor(
    status: HttpStatus,
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(status, message, properties = properties) {
    constructor(contributorId: ContributorId) : this(
        status = HttpStatus.FORBIDDEN,
        message = "Contributor <$contributorId> is not a curator.",
        properties = mapOf("contributor_id" to contributorId),
    )

    companion object {
        fun cannotChangeVerifiedStatus(contributorId: ContributorId): NotACurator =
            NotACurator(
                status = HttpStatus.FORBIDDEN,
                message = """Cannot change verified status: Contributor <$contributorId> is not a curator.""",
                properties = mapOf("contributor_id" to contributorId),
            )
    }
}
