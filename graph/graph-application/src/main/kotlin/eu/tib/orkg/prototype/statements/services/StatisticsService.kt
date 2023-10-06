package eu.tib.orkg.prototype.statements.services

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.spi.ContributorRepository
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ObservatoryStats
import eu.tib.orkg.prototype.statements.spi.ResearchFieldStats
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import eu.tib.orkg.prototype.statements.spi.TrendingResearchProblems
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StatisticsService(
    private val statsRepository: StatsRepository,
    private val userRepository: UserRepository,
    private val contributorRepository: ContributorRepository,
    private val observatoryRepository: PostgresObservatoryRepository,
    private val organizationRepository: PostgresOrganizationRepository,
    private val resourceRepository: ResourceRepository
) : RetrieveStatisticsUseCase {
    override fun getStats(extra: List<String>?): Stats {
        val metadata = statsRepository.getGraphMetaData()
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
        val userCount = userRepository.count()
        val observatoriesCount = observatoryRepository.count()
        val organizationsCount = organizationRepository.count()
        val orphanedNodesCount = statsRepository.getOrphanedNodesCount()
        return Stats(statementsCount, resourcesCount, predicatesCount,
            literalsCount, papersCount, classesCount, contributionsCount,
            fieldsCount, problemsCount, comparisonsCount, visualizationsCount,
            templatesCount, smartReviewsCount, userCount, observatoriesCount,
            organizationsCount, orphanedNodesCount, extraCounts)
    }

    override fun getFieldsStats(): Map<ThingId, Int> {
        val counts = statsRepository.getResearchFieldsPapersCount()
        return counts.associate { it.fieldId to it.papers.toInt() }
    }

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        statsRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        statsRepository.getObservatoryComparisonsCount(id)

    override fun findAllObservatoryStats(pageable: Pageable): Page<ObservatoryStats> =
        statsRepository.findAllObservatoryStats(pageable)

    override fun findObservatoryStatsById(id: ObservatoryId): ObservatoryStats {
        if (!observatoryRepository.existsById(id.value)) {
            throw ObservatoryNotFound(id)
        }
        return statsRepository.findObservatoryStatsById(id).orElseGet { ObservatoryStats(id) }
    }

    override fun findResearchFieldStatsById(id: ThingId, includeSubfields: Boolean): ResearchFieldStats =
        resourceRepository.findById(id)
            .filter { Classes.researchField in it.classes }
            .map { statsRepository.findResearchFieldStatsById(id, includeSubfields).orElseGet { ResearchFieldStats(id) } }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun getTopCurrentContributors(
        days: Long,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        statsRepository.getTopCurrentContributorIdsAndContributionsCount(calculateStartDate(daysAgo = days), pageable)

    override fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog> {
        val changeLogs = statsRepository.getChangeLog(pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getRecentChangeLogByResearchField(id: ThingId, pageable: Pageable): Page<ChangeLog> {
        val changeLogs = statsRepository.getChangeLogByResearchField(id, pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> = statsRepository.getTrendingResearchProblems(pageable)

    override fun getTopCurrentContributorsByResearchField(
        id: ThingId,
        days: Long,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        statsRepository.getTopCurContribIdsAndContribCountByResearchFieldId(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    override fun getTopCurrentContributorsByResearchFieldExcludeSubFields(
        id: ThingId,
        days: Long,
        pageable: Pageable
    ): Page<RetrieveStatisticsUseCase.ContributorRecord> =
        statsRepository.getTopCurContribIdsAndContribCountByResearchFieldIdExcludeSubFields(
            id,
            calculateStartDate(daysAgo = days),
            pageable
        )

    private fun getChangeLogsWithProfile(changeLogs: Page<Resource>, pageable: Pageable): Page<ChangeLog> {
        val refinedChangeLog = mutableListOf<ChangeLog>()
        val userIdList = changeLogs.content.map { it.createdBy }
        val mapValues = contributorRepository.findAllByIds(userIdList).groupBy(Contributor::id)

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

    private fun extractValue(map: Map<*, *>, key: String): Long {
        return if (map.containsKey(key))
            map[key] as Long
        else
            0
    }

    private fun calculateStartDate(daysAgo: Long): String =
        if (daysAgo > 0) {
            LocalDate.now().minusDays(daysAgo)
        } else {
            // Setting the all-time date to 2010-01-01. This date value is set to retrieve all the contributions from
            // ORKG. It is assumed that no contributions pre-date the hard-coded date.
            LocalDate.of(2010, 1, 1)
        }.toString()
}

/**
 * Class containing change log details
 * along with the profile of the contributor
 */
data class ChangeLog(
    val id: String?,
    val label: String?,
    @JsonProperty("created_at")
    val createdAt: String?,
    val classes: List<String>?,
    val profile: Profile?
)

/**
 * Class used only to display minimal profile details
 * of contributors
 */
data class Profile(
    val id: ContributorId?,
    @JsonProperty("display_name")
    val displayName: String?,
    @JsonProperty("gravatar_id")
    val gravatarId: String?,
    @JsonProperty("gravatar_url")
    val gravatarUrl: String?
)
