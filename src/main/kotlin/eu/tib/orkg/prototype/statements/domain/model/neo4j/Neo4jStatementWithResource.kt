package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import org.neo4j.ogm.annotation.EndNode
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.RelationshipEntity
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.StartNode
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.util.UUID

@RelationshipEntity(type = "RELATES_TO")
data class Neo4jStatementWithResource(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @StartNode
    @JsonIgnore
    var subject: Neo4jResource? = null

    @EndNode
    @JsonIgnore
    var `object`: Neo4jResource? = null

    @Property("statement_id")
    @Required
    @Convert(StatementIdGraphAttributeConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    var predicateId: PredicateId? = null

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)

    constructor(
        statementId: StatementId,
        subject: Neo4jResource,
        predicateId: PredicateId,
        `object`: Neo4jResource,
        createdBy: UUID = UUID(0, 0)
    ) :
        this(null) {
        this.statementId = statementId
        this.subject = subject
        this.predicateId = predicateId
        this.`object` = `object`
        this.createdBy = createdBy
    }

    fun toStatement(): Statement {
        return Statement(
            statementId = statementId,
            subjectId = subject!!.resourceId!!,
            predicateId = predicateId!!,
            `object` = Object.Resource(`object`!!.resourceId!!)
        )
    }
}
