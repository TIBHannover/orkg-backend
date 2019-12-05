package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdGraphAttributeConverter
import org.neo4j.ogm.annotation.EndNode
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.RelationshipEntity
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.StartNode
import org.neo4j.ogm.annotation.typeconversion.Convert

@RelationshipEntity(type = "RELATED")
data class Neo4jStatement(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @StartNode
    @JsonIgnore
    var subject: Neo4jThing? = null

    @EndNode
    @JsonIgnore
    var `object`: Neo4jThing? = null

    @Property("statement_id")
    @Required
    @Convert(StatementIdGraphAttributeConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    var predicateId: PredicateId? = null

    constructor(statementId: StatementId, subject: Neo4jThing, predicateId: PredicateId, `object`: Neo4jThing) :
        this(null) {
        this.statementId = statementId
        this.subject = subject
        this.predicateId = predicateId
        this.`object` = `object`
    }

    override fun toString(): String {
        return "{id:$statementId}==(${subject!!.thingId} {${subject!!.label}})-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
    }
}
