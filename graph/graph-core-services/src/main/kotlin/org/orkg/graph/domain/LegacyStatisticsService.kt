package org.orkg.graph.domain

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.input.LegacyStatisticsUseCases
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.List

@Service
@TransactionalOnNeo4j
class LegacyStatisticsService(
    private val legacyStatisticsRepository: LegacyStatisticsRepository,
    private val contributorRepository: ContributorRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceRepository: ResourceRepository,
    private val clock: Clock,
) : LegacyStatisticsUseCases {
    override fun getStats(extra: List<String>?): Stats {
        val metadata = legacyStatisticsRepository.getGraphMetaData()
        val labels = metadata.first()["labels"] as Map<*, *>
        val resourcesCount = extractValue(labels, "Resource")
        val predicatesCount = extractValue(labels, "Predicate")
        val literalsCount = extractValue(labels, "Literal")
        val papersCount = extractValue(labels, "Paper")
        val classesCount = extractValue(labels, "Class")
        val contributionsCount = extractValue(labels, "Contribution")
        val problemsCount = extractValue(labels, "Problem")
        val comparisonsCount = extractValue(labels, "Comparison")
        val visualizationsCount = extractValue(labels, "Visualization")
        val templatesCount = extractValue(labels, "NodeShape")
        val smartReviewsCount = extractValue(labels, "SmartReview")
        val extraCounts = extra?.associate { it to extractValue(labels, it) }
        val fieldsCount = extractValue(labels, "ResearchField")
        val relationsTypes = metadata.first()["relTypesCount"] as Map<*, *>
        val statementsCount = extractValue(relationsTypes, "RELATED")
        val userCount = contributorRepository.count()
        val observatoriesCount = observatoryRepository.count()
        val organizationsCount = organizationRepository.count()
        val orphanedNodesCount = legacyStatisticsRepository.getOrphanedNodesCount()
        return Stats(
            statementsCount,
            resourcesCount,
            predicatesCount,
            literalsCount,
            papersCount,
            classesCount,
            contributionsCount,
            fieldsCount,
            problemsCount,
            comparisonsCount,
            visualizationsCount,
            templatesCount,
            smartReviewsCount,
            userCount,
            observatoriesCount,
            organizationsCount,
            orphanedNodesCount,
            extraCounts
        )
    }

    override fun getFieldsStats(): Map<ThingId, Int> {
        val counts = legacyStatisticsRepository.getResearchFieldsPapersCount()
        return counts.associate { it.fieldId to it.papers.toInt() }
    }

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        legacyStatisticsRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        legacyStatisticsRepository.getObservatoryComparisonsCount(id)

    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        legacyStatisticsRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats {
        if (!observatoryRepository.existsById(id)) {
            throw ObservatoryNotFound(id)
        }
        return legacyStatisticsRepository.findObservatoryStatsById(id).orElseGet { ObservatoryStats(id) }
    }

    override fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): ResearchFieldStats =
        resourceRepository.findById(id)
            .filter { Classes.researchField in it.classes }
            .map { legacyStatisticsRepository.findResearchFieldStatsById(id, includeSubfields).orElseGet { ResearchFieldStats(id) } }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun getTopCurrentContributors(
        days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        legacyStatisticsRepository.getTopCurrentContributorIdsAndContributionsCount(calculateStartDate(daysAgo = days), pageable)

    override fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog> {
        val changeLogs = legacyStatisticsRepository.getChangeLog(pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getRecentChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLog> {
        val changeLogs = legacyStatisticsRepository.getChangeLogByResearchField(id, pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> = legacyStatisticsRepository.getTrendingResearchProblems(pageable)

    override fun getTopCurrentContributorsByResearchField(
        id: ThingId,
        days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        legacyStatisticsRepository.getTopCurContribIdsAndContribCountByResearchFieldId(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    override fun getTopCurrentContributorsByResearchFieldExcludeSubFields(
        id: ThingId,
        days: Long,
        pageable: Pageable,
    ): Page<ContributorRecord> =
        legacyStatisticsRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    private fun getChangeLogsWithProfile(changeLogs: Page<Resource>, pageable: Pageable): Page<ChangeLog> {
        val refinedChangeLog = mutableListOf<ChangeLog>()
        val userIdList = changeLogs.content.map { it.createdBy }
        val mapValues = contributorRepository.findAllById(userIdList).groupBy(Contributor::id)

        changeLogs.forEach { changeLogResponse ->
            val contributor = mapValues[changeLogResponse.createdBy]?.first()
            refinedChangeLog.add(
                ChangeLog(
                    id = changeLogResponse.id.value,
                    label = changeLogResponse.label,
                    createdAt = changeLogResponse.createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    classes = changeLogResponse.classes.map { it.value },
                    profile = Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL)
                )
            )
        }

        return PageImpl(refinedChangeLog, pageable, refinedChangeLog.size.toLong())
    }

    private fun extractValue(map: Map<*, *>, key: String): Long = if (map.containsKey(key)) {
        map[key] as Long
    } else {
        0
    }

    private fun calculateStartDate(daysAgo: Long): LocalDate =
        if (daysAgo > 0) {
            LocalDate.now(clock).minusDays(daysAgo)
        } else {
            // Setting the all-time date to 2010-01-01. This date value is set to retrieve all the contributions from
            // ORKG. It is assumed that no contributions pre-date the hard-coded date.
            LocalDate.of(2010, 1, 1)
        }
}
