package org.orkg.graph.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteral
import java.time.Clock
import java.time.OffsetDateTime

fun createNeo4jLiteral(
    id: ThingId = ThingId("L1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime? = null,
    createdBy: ContributorId = ContributorId.UNKNOWN,
    datatype: String = "xsd:string",
    modifiable: Boolean = true,
    clock: Clock = Clock.systemDefaultZone(),
): Neo4jLiteral = Neo4jLiteral().apply {
    this.id = id
    this.label = label
    this.datatype = datatype
    this.created_at = createdAt ?: OffsetDateTime.now(clock)
    this.created_by = createdBy
    this.modifiable = modifiable
}
