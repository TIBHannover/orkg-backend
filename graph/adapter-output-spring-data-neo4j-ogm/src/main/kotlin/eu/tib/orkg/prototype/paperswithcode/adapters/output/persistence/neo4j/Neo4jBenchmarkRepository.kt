package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jBenchmarkRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (:ResearchField {resource_id: $id})-[:RELATED* {predicate_id: 'P36'}]->(f:ResearchField)
WITH COLLECT(f) AS fields
MATCH (f:ResearchField {resource_id: $id})
WITH fields + f AS fields
UNWIND fields AS field
MATCH (field)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
MATCH (cont)-[:RELATED {predicate_id: 'P32'}]->(pr:Problem)
MATCH (f:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p)-[:RELATED {predicate_id: 'P31'}]->(cont)-[:RELATED {predicate_id: 'P32'}]->(pr)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN DISTINCT pr AS problem, COLLECT(DISTINCT f) AS fields, COUNT(DISTINCT p) AS totalPapers, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT ds) AS totalDatasets""",
        countQuery = """
MATCH (:ResearchField {resource_id: $id})-[:RELATED* {predicate_id: 'P36'}]->(f:ResearchField)
WITH COLLECT(f) AS fields
MATCH (f:ResearchField {resource_id: $id})
WITH fields + f AS fields
UNWIND fields AS field
MATCH (field)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
MATCH (cont)-[:RELATED {predicate_id: 'P32'}]->(pr:Problem)
MATCH (f:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p)-[:RELATED {predicate_id: 'P31'}]->(cont)-[:RELATED {predicate_id: 'P32'}]->(pr)
RETURN COUNT(DISTINCT pr) AS cnt""")
    fun summarizeBenchmarkByResearchField(id: ResourceId, pageable: Pageable): Page<Neo4jBenchmarkSummary>

    @Query("""
MATCH (f:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
MATCH (cont)-[:RELATED {predicate_id: 'P32'}]->(pr:Problem)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN DISTINCT pr AS problem, COLLECT(DISTINCT f) AS fields, COUNT(DISTINCT p) AS totalPapers, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT ds) AS totalDatasets  
    """,
        countQuery = """
MATCH (f:ResearchField)<-[:RELATED {predicate_id: 'P30'}]-(p:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
MATCH (cont)-[:RELATED {predicate_id: 'P32'}]->(pr:Problem)
RETURN COUNT(DISTINCT pr) AS cnt
    """)
    fun summarizeBenchmarkGetAll(pageable: Pageable): Page<Neo4jBenchmarkSummary>
}
