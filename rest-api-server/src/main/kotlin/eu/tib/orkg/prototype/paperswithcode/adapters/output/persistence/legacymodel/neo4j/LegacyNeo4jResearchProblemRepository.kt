package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val datasetId = "${'$'}datasetId"

interface LegacyNeo4jResearchProblemRepository : Neo4jRepository<Neo4jResource, Long> {
    @Query(
        value = """MATCH (ds:$DATASET_CLASS {resource_id: $datasetId})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(:${eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_CLASS})<-[:RELATED {predicate_id: '${eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.BENCHMARK_PREDICATE}'}]-(:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem)
                    RETURN DISTINCT problem"""
    )
    fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Neo4jResource>
}
