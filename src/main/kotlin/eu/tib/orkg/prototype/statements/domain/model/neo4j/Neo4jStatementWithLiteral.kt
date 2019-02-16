package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.*
import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.*
import org.neo4j.ogm.annotation.*
import org.neo4j.ogm.annotation.typeconversion.*

@RelationshipEntity(type = "HAS_VALUE_OF")
data class Neo4jStatementWithLiteral(
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

    @Property("statement_id")
    @Required
    @Convert(StatementIdGraphAttributeConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    var predicateId: PredicateId? = null

    constructor(statementId: StatementId, subject: Neo4jResource, predicateId: PredicateId, `object`: Neo4jLiteral) :
        this(null) {
        this.statementId = statementId
        this.subject = subject
        this.predicateId = predicateId
        this.`object` = `object`
    }
}
