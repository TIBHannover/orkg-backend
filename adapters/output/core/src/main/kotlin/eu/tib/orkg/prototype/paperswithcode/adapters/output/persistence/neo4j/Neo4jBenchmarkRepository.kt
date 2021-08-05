package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface Neo4jBenchmarkRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (r:ResearchField {resource_id: {0}})<-[:RELATED {predicate_id: 'P30'}]-(p:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)
MATCH (cont)-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]->(:Benchmark)-[:RELATED {predicate_id: 'HAS_DATASET'}]->(ds:Dataset)
MATCH (cont)-[:RELATED {predicate_id: 'P32'}]->(pr:Problem)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
RETURN DISTINCT pr AS problem, COUNT(DISTINCT p) AS totalPapers, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT ds) AS totalDatasets
    """)
    fun summarizeBenchmarkByResearchField(id: ResourceId): Iterable<Neo4jBenchmarkSummary>
}
