package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val problemId = "${'$'}problemId"
private const val id = "${'$'}id"

@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
interface Neo4jDatasetRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(cont:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]->(m:$MODEL_CLASS)
RETURN ds AS dataset, COUNT(DISTINCT m) AS totalModels, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT p) AS totalPapers
    """)
    fun findDatasetsByResearchProblem(id: ResourceId): Iterable<Neo4jDataset>

    @Query("""
MATCH (ds:$DATASET_CLASS {resource_id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$METRIC_PREDICATE'}]->(mt:$METRIC_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:$MODEL_CLASS)<-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]-(c)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
    """)
    fun summarizeDatasetQueryById(id: ResourceId): Iterable<Neo4jBenchmarkUnpacked>

    @Query("""
MATCH (ds:$DATASET_CLASS {resource_id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)-[:RELATED {predicate_id: 'P32'}]->(:Problem {resource_id: $problemId})
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$METRIC_PREDICATE'}]->(mt:$METRIC_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:$MODEL_CLASS)<-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]-(c)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
    """)
    fun summarizeDatasetQueryByIdAndProblemId(id: ResourceId, problemId: ResourceId): Iterable<Neo4jBenchmarkUnpacked>
}
