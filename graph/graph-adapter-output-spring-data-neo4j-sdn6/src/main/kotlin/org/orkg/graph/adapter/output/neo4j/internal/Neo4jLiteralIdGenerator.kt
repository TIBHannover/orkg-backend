package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jLiteralIdGenerator(
    private val repository: Neo4jLiteralIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jLiteralIdCounter>() {

    override fun createCounterNode() = Neo4jLiteralIdCounter()

    override fun idFromLong(value: Long) = ThingId("L$value")

    override fun repository() = repository
}
