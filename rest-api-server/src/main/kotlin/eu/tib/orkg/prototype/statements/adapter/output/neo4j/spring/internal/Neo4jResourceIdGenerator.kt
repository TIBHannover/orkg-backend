package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RepositoryBasedIdGenerator
import org.springframework.stereotype.Component

@Component
class Neo4jResourceIdGenerator(
    private val repository: Neo4jResourceIdCounterRepository
) :
    RepositoryBasedIdGenerator<ResourceId, Neo4jResourceIdCounter>() {

    override fun createCounterNode() = Neo4jResourceIdCounter()

    override fun idFromLong(value: Long) = ResourceId(value)

    override fun repository() = repository
}
