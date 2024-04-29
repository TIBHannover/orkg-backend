package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jResourceIdGenerator(
    private val repository: Neo4jResourceIdCounterRepository
) :
    RepositoryBasedIdGenerator<ThingId, Neo4jResourceIdCounter>() {

    override fun createCounterNode() = Neo4jResourceIdCounter()

    override fun idFromLong(value: Long) = ThingId("R$value")

    override fun repository() = repository
}
