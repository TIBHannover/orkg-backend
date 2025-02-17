package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val problemId = "${'$'}problemId"
private const val id = "${'$'}id"
private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jDatasetRepository : Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""
MATCH (:Problem {id: $id})<-[:RELATED {predicate_id: 'P32'}]-(cont:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]->(m:$MODEL_CLASS)
RETURN ds AS dataset, COUNT(DISTINCT m) AS totalModels, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT p) AS totalPapers $PAGE_PARAMS""",
        countQuery = """
MATCH (:Problem {id: $id})<-[:RELATED {predicate_id: 'P32'}]-(cont:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
MATCH (cont)-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]->(:$BENCHMARK_CLASS)-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]->(ds:$DATASET_CLASS)
RETURN COUNT(ds) AS cnt""")
    fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Page<Neo4jDataset>

    @Query("""
MATCH (ds:$DATASET_CLASS {id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$QUANTITY_KIND_PREDICATE'}]->(mt:$QUANTITY_KIND_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]->(md:$MODEL_CLASS)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.id AS modelId, md.label AS model, mt.label AS metric, s.label AS score $PAGE_PARAMS
    """,
        countQuery = """
MATCH (ds:$DATASET_CLASS {id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$QUANTITY_KIND_PREDICATE'}]->(mt:$QUANTITY_KIND_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
RETURN COUNT(p) as cnt""")
    fun summarizeDatasetQueryById(id: ThingId, pageable: Pageable): Page<Neo4jDatasetSummary>

    @Query("""
MATCH (ds:$DATASET_CLASS {id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)-[:RELATED {predicate_id: 'P32'}]->(:Problem {id: $problemId})
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$QUANTITY_KIND_PREDICATE'}]->(mt:$QUANTITY_KIND_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: '$MODEL_PREDICATE'}]->(md:$MODEL_CLASS)
OPTIONAL MATCH (c)-[:RELATED {predicate_id: '$SOURCE_CODE_PREDICATE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.id AS modelId, md.label AS model, mt.label AS metric, s.label AS score $PAGE_PARAMS
    """,
        countQuery = """
MATCH (ds:$DATASET_CLASS {id: $id})<-[:RELATED {predicate_id: '$DATASET_PREDICATE'}]-(b:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(c:Contribution)-[:RELATED {predicate_id: 'P32'}]->(:Problem {id: $problemId})
MATCH (b)-[:RELATED {predicate_id: '$QUANTITY_PREDICATE'}]->(q:$QUANTITY_CLASS)
MATCH (s:Literal)<-[:RELATED {predicate_id: '$NUMERIC_VALUE_PREDICATE'}]-(qv:$QUANTITY_VALUE_CLASS)<-[:RELATED {predicate_id: '$QUANTITY_VALUE_PREDICATE'}]-(q)-[:RELATED {predicate_id: '$QUANTITY_KIND_PREDICATE'}]->(mt:$QUANTITY_KIND_CLASS)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
RETURN COUNT(p) as cnt""")
    fun findAllDatasetSummariesByIdAndResearchProblemId(id: ThingId, problemId: ThingId, pageable: Pageable): Page<Neo4jDatasetSummary>
}
