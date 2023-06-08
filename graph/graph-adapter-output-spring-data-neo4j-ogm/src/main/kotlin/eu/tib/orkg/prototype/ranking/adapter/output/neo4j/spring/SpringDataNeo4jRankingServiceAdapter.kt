package eu.tib.orkg.prototype.ranking.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.ranking.spi.RankingService
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jRankingServiceAdapter(
    private val sessionFactory: SessionFactory
) : RankingService {
    override fun findAllStatementsAboutPaper(id: ThingId): Set<Pair<ThingId, ThingId>> =
        sessionFactory.openSession().query("""
            MATCH (:Paper:Resource {id: ${'$'}id})-[r:RELATED]->(n:Thing)
            RETURN r.predicate_id AS predicate, n.id AS id""".trimIndent(),
            mapOf<String, Any>(
                "id" to id
            )
        ).mapTo(mutableSetOf()) {
            ThingId(it["predicate"] as String) to ThingId(it["id"] as String)
        }

    override fun countSumOfDistinctPredicatesForContributions(contributionIds: Set<ThingId>): Long =
        sessionFactory.openSession().query("""
            UNWIND ${'$'}ids AS id
            MATCH (n:Contribution:Resource {id: id})-[r:RELATED]->()
            WITH COUNT(DISTINCT r.predicate_id) AS pid, n
            RETURN SUM(pid) AS count""".trimIndent(),
            mapOf("ids" to contributionIds)
        ).single()["count"] as Long

    override fun countComparisonsIncludingPaper(id: ThingId): Long =
        sessionFactory.openSession().query("""
            MATCH (n:Comparison:Resource)-[:RELATED {predicate_id:"compareContribution"}]->(:Contribution:Resource)<-[:RELATED {predicate_id:"P31"}]-(:Paper:Resource {id: ${'$'}id})
            WHERE NOT (n)-[:RELATED {predicate_id:"hasPreviousVersion"}]->(:Comparison:Resource)
            RETURN COUNT(DISTINCT n.id) AS count""".trimIndent(),
            mapOf("id" to id)
        ).single()["count"] as Long

    override fun countLiteratureListsIncludingPaper(id: ThingId): Long =
        sessionFactory.openSession().query("""
            MATCH (n:LiteratureList:Resource)-[:RELATED {predicate_id: "HasSection"}]->(:ListSection:Resource)-[:RELATED {predicate_id:"HasEntry"}]->(:Resource)-[:RELATED {predicate_id:"HasPaper"}]->(:Paper:Resource {id: ${'$'}id})
            RETURN COUNT(n) AS count""".trimIndent(),
            mapOf("id" to id)
        ).single()["count"] as Long
}
