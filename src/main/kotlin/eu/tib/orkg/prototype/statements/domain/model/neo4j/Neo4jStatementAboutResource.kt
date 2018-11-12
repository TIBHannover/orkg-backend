package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.neo4j.ogm.annotation.*

@RelationshipEntity(type = "RELATES_TO")
data class Neo4jStatementAboutResource(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @StartNode
    var subject: Neo4jResource? = null

    @EndNode
    var `object`: Neo4jResource? = null

    @Property("predicate_id")
    @Required
    var predicateId: Long? = null

    constructor(
        id: Long? = null,
        subject: Neo4jResource,
        `object`: Neo4jResource,
        predicateId: Long?
    ) : this(id) {
        this.subject = subject
        this.`object` = `object`
        this.predicateId = predicateId
    }

    fun toStatement(): Statement {
        val subjectId = subject?.id
        val objectId = `object`?.id
        val predId = predicateId

        if (subjectId == null || predId == null || objectId == null)
            throw IllegalStateException("This should never happen!")

        return Statement(
            statementId = id,
            subject = ResourceId(subjectId),
            predicate = PredicateId(predId),
            `object` = Object.Resource(ResourceId(objectId))
        )
    }
}
