package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.EndNode
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.RelationshipEntity
import org.neo4j.ogm.annotation.StartNode
import org.neo4j.ogm.annotation.typeconversion.Convert

@RelationshipEntity(type = "SUBCLASS_OF")
data class Neo4jClassRelation(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @StartNode
    @JsonIgnore
    var child: Neo4jClass? = null

    @EndNode
    @JsonIgnore
    var parent: Neo4jClass? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    constructor(
        subject: Neo4jClass,
        parent: Neo4jClass,
        createdBy: ContributorId = ContributorId.createUnknownContributor()
    ) :
        this(null) {
        this.child = subject
        this.parent = parent
        this.createdBy = createdBy
    }

    override fun toString(): String {
        return "(${child!!.thingId} {${child!!.label}})-[SUBCLASS_OF]->(${parent!!.thingId} {${parent!!.label}})"
    }
}
