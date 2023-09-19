package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdConverter
import java.time.OffsetDateTime
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
) {
    @StartNode
    @JsonIgnore
    var subject: Neo4jThing? = null

    @EndNode
    @JsonIgnore
    var `object`: Neo4jThing? = null

    @Property("statement_id")
    @Required
    @Convert(StatementIdConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @Required
    @Convert(ThingIdConverter::class)
    var predicateId: ThingId? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Property("index")
    var index: Int? = null

    constructor(
        statementId: StatementId,
        subject: Neo4jThing,
        predicateId: ThingId,
        `object`: Neo4jThing,
        createdBy: ContributorId = ContributorId.createUnknownContributor(),
        index: Int?
    ) :
        this(null) {
        this.statementId = statementId
        this.subject = subject
        this.predicateId = predicateId
        this.`object` = `object`
        this.createdBy = createdBy
        this.index = index
    }

    override fun toString(): String {
        return "{id:$statementId}==(${subject!!.id} {${subject!!.label}})-[$predicateId]->(${`object`!!.id} {${`object`!!.label}})=="
    }
}
