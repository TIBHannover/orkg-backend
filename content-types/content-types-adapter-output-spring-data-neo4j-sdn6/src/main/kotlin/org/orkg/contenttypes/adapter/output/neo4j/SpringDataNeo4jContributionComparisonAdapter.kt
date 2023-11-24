package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jContributionComparisonRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jContributionInfo
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.graph.domain.ContributionInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jContributionComparisonAdapter(
    val contributionComparisonRepository: Neo4jContributionComparisonRepository,
) : ContributionComparisonRepository {
    override fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo> =
        contributionComparisonRepository.findContributionsDetailsById(ids, pageable)
            .map(Neo4jContributionInfo::toContributionInfo)
}
