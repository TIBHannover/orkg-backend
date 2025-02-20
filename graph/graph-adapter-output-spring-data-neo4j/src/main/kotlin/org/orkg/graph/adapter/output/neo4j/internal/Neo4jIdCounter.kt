package org.orkg.graph.adapter.output.neo4j.internal

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node("_IdCounter")
class Neo4jIdCounter {
    @Id
    var id: String? = null

    var counter: Long = 0L
}
