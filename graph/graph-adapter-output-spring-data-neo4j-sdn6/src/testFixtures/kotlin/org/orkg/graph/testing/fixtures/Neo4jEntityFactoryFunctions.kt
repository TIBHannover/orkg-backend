package org.orkg.graph.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteral

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
    this.created_at = createdAt
    this.created_by = createdBy
}
