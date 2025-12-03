package org.orkg.graph.domain

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ObservatoryRepository
import org.orkg.graph.input.LegacyStatisticsUseCases
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
@TransactionalOnNeo4j
class LegacyStatisticsService(
    private val legacyStatisticsRepository: LegacyStatisticsRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val clock: Clock,
) : LegacyStatisticsUseCases {
    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        legacyStatisticsRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats {
        if (!observatoryRepository.existsById(id)) {
            throw ObservatoryNotFound(id)
        }
        return legacyStatisticsRepository.findObservatoryStatsById(id).orElseGet { ObservatoryStats(id) }
    }

    override fun getTopCurrentContributors(
        days: Long,
        pageable: Pageable,
    ): Page<LegacyContributorRecord> =
        legacyStatisticsRepository.getTopCurrentContributorIdsAndContributionsCount(calculateStartDate(daysAgo = days), pageable)

    override fun getTopCurrentContributorsByResearchField(
        id: ThingId,
        days: Long,
        pageable: Pageable,
    ): Page<LegacyContributorRecord> =
        legacyStatisticsRepository.getTopCurContribIdsAndContribCountByResearchFieldId(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    override fun getTopCurrentContributorsByResearchFieldExcludeSubFields(
        id: ThingId,
        days: Long,
        pageable: Pageable,
    ): Page<LegacyContributorRecord> =
        legacyStatisticsRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    private fun calculateStartDate(daysAgo: Long): LocalDate =
        if (daysAgo > 0) {
            LocalDate.now(clock).minusDays(daysAgo)
        } else {
            // Setting the all-time date to 2010-01-01. This date value is set to retrieve all the contributions from
            // ORKG. It is assumed that no contributions pre-date the hard-coded date.
            LocalDate.of(2010, 1, 1)
        }
}
