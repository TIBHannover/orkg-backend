package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TrendingResearchProblems
import java.time.LocalDate
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jStatsService(
    private val neo4jStatsRepository: Neo4jStatsRepository,
    private val userRepository: UserRepository
) : StatsService {
    override fun getStats(): Stats {
        val metadata = neo4jStatsRepository.getGraphMetaData()
        val labels = metadata.first()["labels"] as Map<*, *>
        val resourcesCount = extractValue(labels, "Resource")
        val predicatesCount = extractValue(labels, "Predicate")
        val literalsCount = extractValue(labels, "Literal")
        val papersCount = extractValue(labels, "Paper")
        val classesCount = extractValue(labels, "Class")
        val contributionsCount = extractValue(labels, "Contribution")
        val fieldsCount = neo4jStatsRepository.getResearchFieldsCount()
        val problemsCount = extractValue(labels, "Problem")
        val relationsTypes = metadata.first()["relTypesCount"] as Map<*, *>
        val statementsCount = extractValue(relationsTypes, "RELATED")
        return Stats(statementsCount, resourcesCount, predicatesCount,
            literalsCount, papersCount, classesCount, contributionsCount,
            fieldsCount, problemsCount)
    }

    override fun getFieldsStats(): Map<String, Int> {
        val counts = neo4jStatsRepository.getResearchFieldsPapersCount()
        return counts.map { it.fieldId to it.papers.toInt() }.toMap()
    }

    override fun getObservatoryPapersCount(id: ObservatoryId): Long =
        neo4jStatsRepository.getObservatoryPapersCount(id)

    override fun getObservatoryComparisonsCount(id: ObservatoryId): Long =
        neo4jStatsRepository.getObservatoryComparisonsCount(id)

    override fun getObservatoriesPapersAndComparisonsCount(): List<ObservatoryResources> =
        neo4jStatsRepository.getObservatoriesPapersAndComparisonsCount()

    override fun getTopCurrentContributors(pageable: Pageable): Page<TopContributorsWithProfile> {
        val userList = mutableListOf<UUID>()
        val previousMonthDate: LocalDate = LocalDate.now().minusMonths(1)
        val refinedTopContributors = mutableListOf<TopContributorsWithProfile>()

        val topContributors = neo4jStatsRepository.getTopCurrentContributors(previousMonthDate.toString(), pageable)

        topContributors.map {
            userList.add(UUID.fromString(it.id))
        }

        val mapValues = userRepository.findByIdIn(userList.toTypedArray()).map(UserEntity::toContributor).groupBy(Contributor::id)

        topContributors.forEach { topContributor ->
            val contributor = mapValues[ContributorId(topContributor.id)]?.first()
            refinedTopContributors.add(TopContributorsWithProfile(topContributor.numberOfContributions,
            Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL)))
        }

        return PageImpl(refinedTopContributors, pageable, refinedTopContributors.size.toLong())
    }

    override fun getRecentChangeLog(pageable: Pageable): Page<ChangeLog> {
        val userList = mutableListOf<UUID>()
        val refinedChangeLog = mutableListOf<ChangeLog>()
        val changeLogs = neo4jStatsRepository.getChangeLog(pageable)

        changeLogs.map {
            userList.add(UUID.fromString(it.createdBy))
        }

        val mapValues = userRepository.findByIdIn(userList.toTypedArray()).map(UserEntity::toContributor).groupBy(Contributor::id)

        changeLogs.forEach { changeLogResponse ->
            val contributor = mapValues[ContributorId(changeLogResponse.createdBy)]?.first()

            val filteredClasses = changeLogResponse.classes.filter {
                it != "Thing" &&
                it != "Resource" &&
                it != "AuditableEntity" &&
                it != "FeaturedPaper" &&
                it != "PaperDeleted" &&
                it != "ContributionDeleted" &&
                it != "FeaturedComparison" &&
                it != "Sentence" }

            refinedChangeLog.add(ChangeLog(changeLogResponse.id, changeLogResponse.label, changeLogResponse.createdAt,
                filteredClasses, Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL)))
        }

        return PageImpl(refinedChangeLog, pageable, refinedChangeLog.size.toLong())
    }

    override fun getTrendingResearchProblems(pageable: Pageable): Page<TrendingResearchProblems> = neo4jStatsRepository.getTrendingResearchProblems(pageable)

    private fun extractValue(map: Map<*, *>, key: String): Long {
        return if (map.containsKey(key))
            map[key] as Long
        else
            0
    }
}

/**
 * Class containing change log details
 * along with the profile of the contributor
 */
data class ChangeLog(
    val id: String,
    val label: String,
    val createdAt: String,
    val classes: List<String>,
    val profile: Profile?
)

/**
 * Class containing top contributors along with
 * contributions count and the corresponding
 * profile
 */
data class TopContributorsWithProfile(
    val contributionsCount: Long,
    val profile: Profile?
)

/**
 * Class used only to display minimal profile details
 * of contributors
 */
data class Profile(
    val id: ContributorId?,
    val displayName: String?,
    val gravatarId: String?,
    val gravatarUrl: String?
)
