package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jListIdGenerator(
    private val repository: Neo4jListIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jListIdCounter>() {

    override fun createCounterNode() = Neo4jListIdCounter()

    override fun idFromLong(value: Long) = ThingId("List$value")

    override fun repository() = repository
}
