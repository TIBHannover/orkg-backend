package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jListIdGenerator(
    private val repository: Neo4jListIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jListIdCounter>() {

    override fun createCounterNode() = Neo4jListIdCounter()

    override fun idFromLong(value: Long) = ThingId("List$value")

    override fun repository() = repository
}
