package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.ORKGUserEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.OrkgUserRepository
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import eu.tib.orkg.prototype.statements.ports.ChangeLogResponse
import eu.tib.orkg.prototype.statements.ports.ObservatoryResources
import eu.tib.orkg.prototype.statements.ports.ResultObject
import eu.tib.orkg.prototype.statements.ports.StatsRepository
import eu.tib.orkg.prototype.statements.ports.TopContributorIdentifiers
import eu.tib.orkg.prototype.statements.ports.TrendingResearchProblems
import java.lang.IllegalStateException
import java.time.LocalDate
import java.util.UUID
import java.util.logging.Logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jStatsService(
    private val statsRepository: StatsRepository,
    private val userRepository: UserRepository,
    private val observatoryRepository: PostgresObservatoryRepository,
    private val organizationRepository: PostgresOrganizationRepository,
    private val orkgUserRepository: OrkgUserRepository
) : StatsService {

    private val logger = Logger.getLogger("Logger")

    val internalClassLabels: (String) -> Boolean = { it !in setOf("Thing", "Resource", "AuditableEntity") }

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
        val templatesCount = extractValue(labels, "ContributionTemplate")
        val smartReviewsCount = extractValue(labels, "SmartReview")
        val extraCounts = extra?.associate { it to extractValue(labels, it) }
        val fieldsCount = statsRepository.getResearchFieldsCount()
        val relationsTypes = metadata.first()["relTypesCount"] as Map<*, *>
        val statementsCount = extractValue(relationsTypes, "RELATED")
        val userCount = userRepository.count()
        val observatoriesCount = observatoryRepository.count()
        val organizationsCount = organizationRepository.count()
        return Stats(statementsCount, resourcesCount, predicatesCount,
            literalsCount, papersCount, classesCount, contributionsCount,
            fieldsCount, problemsCount, comparisonsCount, visualizationsCount,
            templatesCount, smartReviewsCount, userCount, observatoriesCount,
            organizationsCount, extraCounts)
    }

    override fun getFieldsStats(): Map<String, Int> {
        val counts = statsRepository.getResearchFieldsPapersCount()
        return counts.associate { it.fieldId to it.papers.toInt() }
    }

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        statsRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        statsRepository.getObservatoryComparisonsCount(id)

    override fun getObservatoriesPapersAndComparisonsCount(): Iterable<ObservatoryResources> =
        statsRepository.getObservatoriesPapersAndComparisonsCount()

    override fun getTopCurrentContributors(pageable: Pageable): Page<TopContributorsWithProfile> {
        val previousMonthDate: LocalDate = LocalDate.now().minusMonths(1)
        return getContributorsWithProfile(statsRepository.getTopCurrentContributorIdsAndContributionsCount(
            previousMonthDate.toString(), pageable), pageable)
    }

    override fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog> {
        val changeLogs = statsRepository.getChangeLog(pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getRecentChangeLogByResearchField(id: ResourceId, pageable: Pageable): Page<ChangeLog> {
        val changeLogs = statsRepository.getChangeLogByResearchField(id, pageable)

        return getChangeLogsWithProfile(changeLogs, pageable)
    }

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> =
        statsRepository.getTrendingResearchProblems(pageable)

    override fun getTopCurrentContributorsByResearchField(
        id: ResourceId,
        days: Long
    ): Iterable<TopContributorsWithProfileAndTotalCount> {
        // Setting the all-time date to 2010-01-01
        // This date value is set to retrieve all the contributions from ORKG
        // It is assumed that no contributions pre-date the hard-coded date
        var previousMonthDate: LocalDate? = LocalDate.of(2010, 1, 1)

        if (days > 0) {
            previousMonthDate = LocalDate.now().minusDays(days)
        }

        val values = statsRepository.getTopCurContribIdsAndContribCountByResearchFieldId(id, previousMonthDate.toString())

        logger.info("$values")
        val totalContributions = extractAndCalculateContributionDetails(values)

        return getContributorsWithProfileAndTotalCount(totalContributions)
    }

    private fun getChangeLogsWithProfile(changeLogs: Page<ChangeLogResponse>, pageable: Pageable): Page<ChangeLog> {
        val refinedChangeLog = mutableListOf<ChangeLog>()

        val userIdList = changeLogs.content.map { UUID.fromString(it.createdBy) }.toTypedArray()

        //val mapValues = userRepository.findByIdIn(userIdList).map(UserEntity::toContributor).groupBy(Contributor::id)
        val mapValues = orkgUserRepository.findUserListInOldIDOrKeycloakID(userIdList).map(ORKGUserEntity::toContributor).groupBy(Contributor::id)
        changeLogs.forEach { changeLogResponse ->
            val contributor = mapValues[ContributorId(changeLogResponse.createdBy)]?.first()
            val filteredClasses = changeLogResponse.classes.filter(internalClassLabels)
            refinedChangeLog.add(ChangeLog(changeLogResponse.id, changeLogResponse.label, changeLogResponse.createdAt,
                filteredClasses, Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL)))
        }

        return PageImpl(refinedChangeLog, pageable, refinedChangeLog.size.toLong())
    }

    private fun getContributorsWithProfile(topContributors: Page<TopContributorIdentifiers>, pageable: Pageable): Page<TopContributorsWithProfile> {
        val userIdList = topContributors.content.map { UUID.fromString(it.id) }.toTypedArray()

        //val mapValues = userRepository.findByIdIn(userIdList).map(UserEntity::toContributor).groupBy(Contributor::id)
        val mapValues = orkgUserRepository.findUserListInOldIDOrKeycloakID(userIdList).map(ORKGUserEntity::toContributor).groupBy(Contributor::id)
        val refinedTopContributors =
            topContributors.content.map { topContributor ->
                val contributor = mapValues[ContributorId(topContributor.id)]?.first()
                TopContributorsWithProfile(topContributor.contributions, Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL))
            } as MutableList<TopContributorsWithProfile>

        return PageImpl(refinedTopContributors, pageable, refinedTopContributors.size.toLong())
    }

    private fun getContributorsWithProfileAndTotalCount(topContributors: List<OverallContributions>):
        List<TopContributorsWithProfileAndTotalCount> {
        val userIdList = topContributors.map { UUID.fromString(it.id) }.toTypedArray()

        //val mapValues = userRepository.findByIdIn(userIdList).map(UserEntity::toContributor).groupBy(Contributor::id)
        val mapValues = orkgUserRepository.findUserListInOldIDOrKeycloakID(userIdList).map(ORKGUserEntity::toContributor).groupBy(Contributor::id)
        return topContributors.map { topContributor ->
            val contributor = mapValues[topContributor.id?.let { ContributorId(it) }]?.first()
            TopContributorsWithProfileAndTotalCount(
                topContributor.individualContributionsCount as IndividualContributionsCount,
                Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL))
        }
    }

    private fun extractValue(map: Map<*, *>, key: String): Long {
        return if (map.containsKey(key))
            map[key] as Long
        else
            0
    }

    private fun extractAndCalculateContributionDetails(values: Iterable<Map<String, Iterable<ResultObject>>>):
        List<OverallContributions> {
        val mapLookUpResult = mutableMapOf<String, IndividualContributionsCount>()

        values.forEachIndexed { index, row ->
            row.forEach { hashMap ->
                if (hashMap != null) {
                    val key = hashMap.key
                    if (key.equals("total")) {
                    val iter = hashMap.value.iterator()
                    while (iter != null && iter.hasNext()) {
                        val mapEntry = iter.next()
                        if (mapEntry["id"] != null) {
                            val id = mapEntry["id"] as String
                            val cnt = mapEntry["cnt"] as Long

                            val counts = if (mapLookUpResult.containsKey(id)) {
                                mapLookUpResult[id]!!
                            } else {
                                IndividualContributionsCount()
                            }

                            mapLookUpResult[id] = updateCount(index, counts, cnt)
                        }
                    }
                    }
                }
            }
        }

        /*Returning a maximum of top 30 results from the list
        since post processing is not possible in UNION for
        Neo4j < 4.When Neo4j is upgraded, the limit
        should be applied in the cypher query
        Also, processing here makes sense for now since
        all the contributions need to be added to get the
        list in descending order before getting the top 30.
        Also note: Frontend needs just 30 as it fits on
        wide-body monitors*/
        return mapLookUpResult.entries.map { mapEntry ->
            OverallContributions(mapEntry.key, mapEntry.value)
        }.sortedByDescending { it.individualContributionsCount?.total }
            .subList(0, if (mapLookUpResult.size > 30) 30 else mapLookUpResult.size)
    }

    private fun updateCount(index: Int, counts: IndividualContributionsCount, value: Long?): IndividualContributionsCount {
        // 0 = Contribution; 1 = Comparison; 2 = Paper; 3 = Visualization; 4 = Problem
        when (index) {
            0 -> {
                counts.contributions = value ?: 0
            }
            1 -> {
                counts.comparisons = value ?: 0
            }
            2 -> {
                counts.papers = value ?: 0
            }
            3 -> {
                counts.visualizations = value ?: 0
            }
            4 -> {
                counts.problems = value ?: 0
            } else -> {
                throw IllegalStateException("Contributions: The query returns more indices than expected")
            }
        }

        return counts
    }
}

/**
 * Class containing change log details
 * along with the profile of the contributor
 */
data class ChangeLog(
    val id: String?,
    val label: String? = "",
    @JsonProperty("created_at")
    val createdAt: String?,
    val classes: List<String>?,
    val profile: Profile?
)

/**
 * Class containing top contributors along with
 * contributions count and the corresponding
 * profile
 */
data class TopContributorsWithProfileAndTotalCount(
    @JsonProperty("counts")
    val individualContributions: IndividualContributionsCount,
    val profile: Profile
)

/**
 * Class containing top contributors along with
 * contributions count and the corresponding
 * profile
 */
data class TopContributorsWithProfile(
    val contributions: Long,
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

/**
 * Data class containing individual contributions and id
 */
data class OverallContributions(
    @JsonProperty("id")
    var id: String? = null,
    @JsonProperty("counts")
    var individualContributionsCount: IndividualContributionsCount? = null
)

/**
 * Data class containing individual contributions
 */
data class IndividualContributionsCount(
    var contributions: Long = 0,
    var comparisons: Long = 0,
    var papers: Long = 0,
    var visualizations: Long = 0,
    var problems: Long = 0

) {
    val total: Long
        get() = contributions + comparisons + papers + visualizations + problems
}
