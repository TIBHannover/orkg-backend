package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.LegacyNeo4jResearchFieldRepository
import org.orkg.contenttypes.output.LegacyResearchFieldRepository
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class LegacySpringDataNeo4JResearchFieldAdapter(
    private val neo4jRepository: LegacyNeo4jResearchFieldRepository,
) : LegacyResearchFieldRepository {
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id).map { it.toResource() }

    override fun findAllWithBenchmarks(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllWithBenchmarks(pageable).map { it.toResource() }
}
