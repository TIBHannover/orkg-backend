package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.FieldsStats
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jStatsAdapter(
    private val neo4jRepository: Neo4jStatsRepository,
    private val neo4jClient: Neo4jClient,
) : StatsRepository {
    override fun getGraphMetaData(): Iterable<Map<String, Any?>> =
        neo4jClient.query("CALL apoc.meta.stats()").fetch().all()

    override fun getResearchFieldsPapersCount(): Iterable<FieldsStats> =
        neo4jRepository.getResearchFieldsPapersCount()

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        neo4jRepository.getObservatoryComparisonsCount(id)

    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        neo4jRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats> =
        neo4jRepository.findObservatoryStatsById(id)

    override fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): Optional<ResearchFieldStats> =
        if (includeSubfields) {
            neo4jRepository.findResearchFieldStatsByIdIncludingSubfields(id)
        } else {
            neo4jRepository.findResearchFieldStatsById(id)
        }

    override fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurrentContributorIdsAndContributionsCount(date, pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, date, pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id = id,
            date = date,
            pageable = pageable
        )

    override fun getChangeLog(pageable: Pageable): Page<Resource> =
        neo4jRepository.getChangeLog(pageable).map(Neo4jResource::toResource)

    override fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getChangeLogByResearchField(id, pageable).map(Neo4jResource::toResource)

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> =
        neo4jRepository.getTrendingResearchProblems(pageable)

    override fun getOrphanedNodesCount(): Long = neo4jRepository.getOrphanedNodesCount()
}
