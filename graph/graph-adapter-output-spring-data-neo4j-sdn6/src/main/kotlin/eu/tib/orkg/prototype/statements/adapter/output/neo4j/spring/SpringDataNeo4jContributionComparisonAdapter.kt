package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring


import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributionComparisonRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
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
