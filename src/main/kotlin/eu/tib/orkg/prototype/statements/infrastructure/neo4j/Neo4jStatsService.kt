package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatsRepository
import org.kpropmap.propMapOf
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jStatsService(
    private val neo4jStatsRepository: Neo4jStatsRepository
) : StatsService {

    val mapper = jacksonObjectMapper()

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

    override fun getFieldsPerProblem(problemId: ResourceId): Iterable<Map<String, Any?>> {
        return neo4jStatsRepository.getResearchFieldsPerProblem(problemId).map {
            val result = propMapOf(it.field.toResource())
            result["freq"] = it.freq
            result
        }
    }

    private fun extractValue(map: Map<*, *>, key: String): Long {
        return if (map.containsKey(key))
            map[key] as Long
        else
            0
    }
}
