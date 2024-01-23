package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonRepository
import org.orkg.contenttypes.domain.ComparisonVersion
import org.orkg.contenttypes.output.ComparisonRepository
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jComparisonAdapter(
    private val neo4jRepository: Neo4jComparisonRepository
) : ComparisonRepository {
    override fun findVersionHistory(id: ThingId): List<ComparisonVersion> =
        neo4jRepository.findVersionHistory(id)
}
