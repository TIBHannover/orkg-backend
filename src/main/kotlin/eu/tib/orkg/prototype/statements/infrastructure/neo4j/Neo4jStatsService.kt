package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jStatsService(
    private val neo4jStatsRepository: Neo4jStatsRepository
) : StatsService {
    override fun getStats(): Stats {
        val metadata = neo4jStatsRepository.getGraphMetaData()
        val statementsCount = metadata.first()["relCount"] as Long
        val labels = metadata.first()["labels"] as Map<*, *>
        val resourcesCount = labels["Resource"] as Long
        val predicatesCount = labels["Predicate"] as Long
        val literalsCount = labels["Literal"] as Long
        val papersCount = labels["Paper"] as Long
        val classesCount = labels["Class"] as Long
        val contributionsCount = neo4jStatsRepository.getContributionsCount()
        val fieldsCount = neo4jStatsRepository.getResearchFieldsCount()
        val problemsCount = neo4jStatsRepository.getResearchProblemsCount()
        val relationsTypes = metadata.first()["relTypesCount"] as Map<*, *>
        val resourceStatementsCount = relationsTypes["RELATES_TO"] as Long
        val literalsStatementsCount = relationsTypes["HAS_VALUE_OF"] as Long
        return Stats(statementsCount, resourcesCount, predicatesCount,
            literalsCount, papersCount, classesCount, contributionsCount,
            fieldsCount, problemsCount, resourceStatementsCount, literalsStatementsCount)
    }


}
