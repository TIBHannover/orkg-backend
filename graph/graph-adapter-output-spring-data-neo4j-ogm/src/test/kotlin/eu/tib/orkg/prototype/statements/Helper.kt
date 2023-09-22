package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

fun createNeo4jLiteral(
    id: ThingId = ThingId("L1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
    datatype: String = "xsd:string"
): Neo4jLiteral = Neo4jLiteral().apply {
    this.id = id
    this.label = label
    this.datatype = datatype
    this.createdAt = createdAt
    this.createdBy = createdBy
}
