package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.output.RankingService
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.data.neo4j.core.mappedBy
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jRankingServiceAdapter(
    private val neo4jClient: Neo4jClient,
) : RankingService {
    override fun findAllStatementsAboutPaper(id: ThingId): Set<Pair<ThingId, ThingId>> =
        neo4jClient.query(
            """
            MATCH (:Paper:Resource {id: ${'$'}id})-[r:RELATED]->(n:Thing)
            RETURN r.predicate_id AS predicate, n.id AS id
            """.trimIndent()
        ).bindAll(mapOf("id" to id.value))
            .mappedBy { _, record -> ThingId(record["predicate"].asString()) to ThingId(record["id"].asString()) }
            .all()
            .toSet()

    override fun countSumOfDistinctPredicatesForContributions(contributionIds: Set<ThingId>): Long =
        neo4jClient.query(
            """
            UNWIND ${'$'}ids AS id
            MATCH (n:Contribution:Resource {id: id})-[r:RELATED]->()
            WITH COUNT(DISTINCT r.predicate_id) AS pid, n
            RETURN SUM(pid) AS count
            """.trimIndent()
        ).bindAll(mapOf("ids" to contributionIds.map { it.value }))
            .fetchAs<Long>()
            .one() ?: 0

    override fun countComparisonsIncludingPaper(id: ThingId): Long =
        neo4jClient.query(
            """
            MATCH (n:ComparisonPublished:LatestVersion)-[:RELATED {predicate_id: "compareContribution"}]->(:Contribution)<-[:RELATED {predicate_id: "P31"}]-(:Paper:Resource {id: ${'$'}id})
            RETURN COUNT(DISTINCT n.id) AS count
            """.trimIndent()
        ).bindAll(mapOf("id" to id.value))
            .fetchAs<Long>()
            .one() ?: 0

    override fun countLiteratureListsIncludingPaper(id: ThingId): Long =
        neo4jClient.query(
            """
            MATCH (n:LiteratureList:Resource)-[:RELATED {predicate_id: "HasSection"}]->(:ListSection:Resource)-[:RELATED {predicate_id:"HasEntry"}]->(:Resource)-[:RELATED {predicate_id:"HasLink"}]->(:Paper:Resource {id: ${'$'}id})
            RETURN COUNT(n) AS count
            """.trimIndent()
        ).bindAll(mapOf("id" to id.value))
            .fetchAs<Long>()
            .one() ?: 0

    override fun countRosettaStoneStatementsAssociatedToPaper(id: ThingId): Long =
        neo4jClient.query(
            """
            MATCH (:Resource {id: ${'$'}id})<-[:CONTEXT]-(n:RosettaStoneStatement:LatestVersion)
            RETURN COUNT(DISTINCT n.id) AS count
            """.trimIndent()
        ).bindAll(mapOf("id" to id.value))
            .fetchAs<Long>()
            .one() ?: 0
}
