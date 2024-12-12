package org.orkg

import org.orkg.graph.adapter.output.neo4j.toResource
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.mappedBy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ResearchFieldAssociationFixer(
    private val neo4jClient: Neo4jClient,
    private val statementUseCases: StatementUseCases
) {
    @Scheduled(cron = "0 0 * * * *")
    fun addResearchFieldToPublishedSmartReviews() {
        associateResearchFields("""
            MATCH (srp:SmartReviewPublished)<-[:RELATED {predicate_id: "hasPublishedVersion"}]-(:SmartReview)-[:RELATED {predicate_id: "P30"}]->(rsf:ResearchField)
            WHERE NOT EXISTS((srp)-[:RELATED {predicate_id: "P30"}]->(:ResearchField))
            RETURN srp AS node, rsf.id AS rsfId
        """.trimIndent())
    }

    @Scheduled(cron = "0 0 * * * *")
    fun addResearchFieldToPublishedLiteratureLists() {
        associateResearchFields("""
            MATCH (llp:LiteratureListPublished)<-[:RELATED {predicate_id: "hasPublishedVersion"}]-(:LiteratureList)-[:RELATED {predicate_id: "P30"}]->(rsf:ResearchField)
            WHERE NOT EXISTS((llp)-[:RELATED {predicate_id: "P30"}]->(:ResearchField))
            RETURN llp AS node, rsf.id AS rsfId
        """.trimIndent())
    }

    private fun associateResearchFields(query: String) {
        neo4jClient.query(query)
            .mappedBy { _, record -> record["node"].asNode().toResource() to record["rsfId"].toThingId()!! }
            .all()
            .forEach { (node, researchFieldId) ->
                statementUseCases.create(
                    userId = node.createdBy,
                    subject = node.id,
                    predicate = Predicates.hasResearchField,
                    `object` = researchFieldId
                )
            }
    }
}
