package org.orkg.graph.output

import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributorRecord
import org.orkg.graph.domain.FieldsStats
import org.orkg.graph.domain.ObservatoryStats
import org.orkg.graph.domain.ResearchFieldStats
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.TrendingResearchProblems
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyStatisticsRepository {
    fun getGraphMetaData(): Iterable<Map<String, Any?>>
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>
    fun getObservatoryPapersCount(id: ObservatoryId): Long
    fun getObservatoryComparisonsCount(id: ObservatoryId): Long
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>
    fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats>
    fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): Optional<ResearchFieldStats>
    fun getTopCurrentContributorIdsAndContributionsCount(
        date: String,
        pageable: Pageable
    ): Page<ContributorRecord>
    fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<ContributorRecord>
    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: String,
        pageable: Pageable
    ): Page<ContributorRecord>
    fun getChangeLog(pageable: Pageable): Page<Resource>
    fun getChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<Resource>
    fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems>
    fun getOrphanedNodesCount(): Long
}
