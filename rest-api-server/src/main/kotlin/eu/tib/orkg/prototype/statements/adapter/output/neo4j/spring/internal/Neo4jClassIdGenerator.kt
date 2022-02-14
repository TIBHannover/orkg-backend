package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RepositoryBasedIdGenerator
import org.springframework.stereotype.Component

@Component
class Neo4jClassIdGenerator(
    private val repository: Neo4jClassIdCounterRepository
) : RepositoryBasedIdGenerator<ClassId, Neo4jClassIdCounter>() {

    override fun createCounterNode() = Neo4jClassIdCounter()

    override fun idFromLong(value: Long) = ClassId(value)

    override fun repository() = repository
}
