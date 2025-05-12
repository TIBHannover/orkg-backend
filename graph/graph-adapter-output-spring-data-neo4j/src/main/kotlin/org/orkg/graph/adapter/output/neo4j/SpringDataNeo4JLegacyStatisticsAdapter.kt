package org.orkg.graph.adapter.output.neo4j

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLegacyStatisticsRepository
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Optional

@Component
@TransactionalOnNeo4j
class SpringDataNeo4JLegacyStatisticsAdapter(
    private val neo4jRepository: Neo4jLegacyStatisticsRepository,
) : LegacyStatisticsRepository {
    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        neo4jRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats> =
        neo4jRepository.findObservatoryStatsById(id)

    override fun getTopCurrentContributorIdsAndContributionsCount(
        date: LocalDate,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurrentContributorIdsAndContributionsCount(date.toString(), pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, date.toString(), pageable)

    override fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        neo4jRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id = id,
            date = date.toString(),
            pageable = pageable
        )
}
