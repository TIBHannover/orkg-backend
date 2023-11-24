package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jClassIdGenerator(
    private val repository: Neo4jClassIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jClassIdCounter>() {

    override fun createCounterNode() = Neo4jClassIdCounter()

    override fun idFromLong(value: Long) = ThingId("C$value")

    override fun repository() = repository
}
