package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.graph.domain.StatementId
import org.springframework.stereotype.Component

@Component
class Neo4jStatementIdGenerator(
    repository: Neo4jIdCounterRepository,
) : RepositoryBasedIdGenerator<StatementId>("StatementId", repository) {
    override fun idFromLong(value: Long) = StatementId(value)
}
