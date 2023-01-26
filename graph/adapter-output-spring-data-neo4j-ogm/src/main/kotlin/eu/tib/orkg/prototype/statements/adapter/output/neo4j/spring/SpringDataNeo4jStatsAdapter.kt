package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ChangeLogResponse
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryResources
import eu.tib.orkg.prototype.statements.spi.ResultObject
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import eu.tib.orkg.prototype.statements.spi.TopContributorIdentifiers
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jStatsAdapter(
    private val neo4jRepository: Neo4jStatsRepository
) : StatsRepository {
    override fun getGraphMetaData(): Iterable<HashMap<String, Any>> =
        neo4jRepository.getGraphMetaData()

    override fun getResearchFieldsPapersCount(): Iterable<FieldsStats> =
        neo4jRepository.getResearchFieldsPapersCount()

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryComparisonsCount(id)

    override fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources> =
        neo4jRepository.getObservatoriesPapersAndComparisonsCount()

    override fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<TopContributorIdentifiers> =
        neo4jRepository.getTopCurrentContributorIdsAndContributionsCount(date, pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ResourceId,
        date: String
    ): List<List<Map<String, List<ResultObject>>>> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, date)

    override fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ResourceId,
        date: String
    ): List<List<Map<String, List<ResultObject>>>> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(id, date)

    override fun getChangeLog(pageable: Pageable): Page<ChangeLogResponse> =
        neo4jRepository.getChangeLog(pageable)

    override fun getChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLogResponse> =
        neo4jRepository.getChangeLogByResearchField(id, pageable)

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> =
        neo4jRepository.getTrendingResearchProblems(pageable)

    override fun getOrphanedNodesCount(): Long = neo4jRepository.getOrphanedNodesCount()
}
