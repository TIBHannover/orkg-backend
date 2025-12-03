package org.orkg.graph.output

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.LegacyContributorRecord
import org.orkg.graph.domain.ObservatoryStats
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional

interface LegacyStatisticsRepository {
    fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats>

    fun findObservatoryStatsById(id: ObservatoryId): Optional<ObservatoryStats>

    fun getTopCurrentContributorIdsAndContributionsCount(
        date: LocalDate,
        pageable: Pageable,
    ): Page<LegacyContributorRecord>

    fun getTopCurContribIdsAndContribCountByResearchFieldId(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable,
    ): Page<LegacyContributorRecord>

    fun getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
        id: ThingId,
        date: LocalDate,
        pageable: Pageable,
    ): Page<LegacyContributorRecord>
}
