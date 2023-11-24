package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jPredicateIdGenerator(
    private val repository: Neo4jPredicateIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jPredicateIdCounter>() {

    override fun createCounterNode() = Neo4jPredicateIdCounter()

    override fun idFromLong(value: Long) = ThingId("P$value")

    override fun repository() = repository
}
