package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import java.time.LocalDate
import java.util.UUID
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

    override fun getTopCurrentContributors(): List<Contributor> {
        val userList = mutableListOf<UUID>()
        val previousMonthDate: LocalDate = LocalDate.now().minusMonths(1)

        neo4jStatsRepository.getTopCurrentContributors(previousMonthDate.toString()).map {
            userList.add(it.value)
        }

        return userRepository.findByIdIn(userList.toTypedArray()).map(UserEntity::toContributor)
    }

    override fun getRecentChangeLog(): List<String> = neo4jStatsRepository.getChangeLog()

    override fun getTrendingResearchProblems(): List<String> = neo4jStatsRepository.getTrendingResearchProblems()

    private fun extractValue(map: Map<*, *>, key: String): Long {
        return if (map.containsKey(key))
            map[key] as Long
        else
            0
    }
}
