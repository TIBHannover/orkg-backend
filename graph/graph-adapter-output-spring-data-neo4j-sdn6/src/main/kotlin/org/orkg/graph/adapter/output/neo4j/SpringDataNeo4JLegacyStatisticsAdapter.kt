package org.orkg.graph.adapter.output.neo4j

import java.time.LocalDate
import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLegacyStatisticsRepository
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.FieldsStats
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.TrendingResearchProblems
import org.orkg.graph.output.LegacyStatisticsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4JLegacyStatisticsAdapter(
    private val neo4jRepository: Neo4jLegacyStatisticsRepository,
    private val neo4jClient: Neo4jClient,
) : LegacyStatisticsRepository {
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
        date: LocalDate,
        pageable: Pageable
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurrentContributorIdsAndContributionsCount(date.toString(), pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, date.toString(), pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id = id,
            date = date.toString(),
            pageable = pageable
        )

    override fun getChangeLog(pageable: Pageable): Page<Resource> =
        neo4jRepository.getChangeLog(pageable.withDefaultSort { Sort.by("n.created_at").descending() })
            .map(Neo4jResource::toResource)

    override fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getChangeLogByResearchField(id, pageable.withDefaultSort { Sort.by("n.created_at").descending() })
            .map(Neo4jResource::toResource)

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> =
        neo4jRepository.getTrendingResearchProblems(pageable)

    override fun getOrphanedNodesCount(): Long = neo4jRepository.getOrphanedNodesCount()
}
