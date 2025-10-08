package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import kotlin.time.measureTime

private const val UPDATE_STATEMENT_LABEL_QUERY = """MATCH (n:RosettaStoneStatement {id: ${'$'}id}) SET n.label = ${'$'}label"""
private const val FETCH_STATEMENTS_WITHOUT_LABEL_QUERY =
    """
    CALL () {
        MATCH (latest:RosettaStoneStatement:LatestVersion)
        WHERE latest.label = ""
        RETURN latest.id AS id
        UNION
        MATCH (latest:RosettaStoneStatement:LatestVersion)-[:VERSION]->(version:RosettaStoneStatement:Version)
        WHERE version.label = ""
        RETURN DISTINCT latest.id AS id
    }
    RETURN id
    LIMIT 1000
    """

@Component
@Profile("development", "docker", "production")
class RosettaStoneStatementLabelMigrator(
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
    private val neo4jClient: Neo4jClient,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        logger.info("Migrating rosetta stone statement labels...")

        val time = measureTime {
            var ids = fetchStatementIds()
            while (ids.isNotEmpty()) {
                ids.forEach { id ->
                    val statement = rosettaStoneStatementRepository.findByIdOrVersionId(id).get()
                    val versions = statement.versions.map { version ->
                        val values = (listOf(version.subjects) + version.objects)
                            .mapIndexed { index, values -> index.toString() to values.map { it.label } }
                            .toMap()
                        val label = version.dynamicLabel.render(values)
                        version.id to label
                    }
                    updateStatementLabel(statement.id, versions.last().second)
                    versions.forEach { (id, label) -> updateStatementLabel(id, label) }
                }
                ids = fetchStatementIds()
            }
        }

        logger.info("Done migrating rosetta stone statement labels. Took {}.", time)
    }

    private fun fetchStatementIds(): List<ThingId> =
        neo4jClient.query(FETCH_STATEMENTS_WITHOUT_LABEL_QUERY)
            .fetchAs<ThingId>()
            .mappedBy { _, r -> r["id"].toThingId()!! }
            .all()
            .toList()

    private fun updateStatementLabel(id: ThingId, label: String) {
        val parameters = mapOf(
            "id" to id.value,
            "label" to label
        )
        neo4jClient.query(UPDATE_STATEMENT_LABEL_QUERY)
            .bindAll(parameters)
            .run()
    }
}
