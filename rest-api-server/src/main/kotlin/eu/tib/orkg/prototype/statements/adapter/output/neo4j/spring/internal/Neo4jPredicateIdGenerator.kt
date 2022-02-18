package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RepositoryBasedIdGenerator
import org.springframework.stereotype.Component

@Component
class Neo4jPredicateIdGenerator(
    private val repository: Neo4jPredicateIdCounterRepository
) : RepositoryBasedIdGenerator<PredicateId, Neo4jPredicateIdCounter>() {

    override fun createCounterNode() = Neo4jPredicateIdCounter()

    override fun idFromLong(value: Long) = PredicateId(value)

    override fun repository() = repository
}