package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.*
import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.*
import org.neo4j.ogm.annotation.*
import org.neo4j.ogm.annotation.typeconversion.*

@RelationshipEntity(type = "HAS_VALUE_OF")
class Neo4jStatementWithLiteral(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @StartNode
    @JsonIgnore
    var subject: Neo4jResource? = null

    @EndNode
    @JsonIgnore
    var `object`: Neo4jLiteral? = null

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    var predicateId: PredicateId? = null

    constructor(
        id: Long? = null,
        subject: Neo4jResource,
        `object`: Neo4jLiteral,
        predicateId: PredicateId
    ) : this(id) {
        this.subject = subject
        this.`object` = `object`
        this.predicateId = predicateId
    }
}
