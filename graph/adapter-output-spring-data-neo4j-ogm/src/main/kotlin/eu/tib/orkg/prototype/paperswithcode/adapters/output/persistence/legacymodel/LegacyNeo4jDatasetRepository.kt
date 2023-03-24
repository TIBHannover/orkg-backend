package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jBenchmarkUnpacked
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.Neo4jDataset
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

// TODO: @Qualifier?
private const val problemId = "${'$'}problemId"
private const val id = "${'$'}id"

interface LegacyNeo4jDatasetRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(cont:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
MATCH (cont)-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]->(:Benchmark)-[:RELATED {predicate_id: 'HAS_DATASET'}]->(ds:Dataset)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: 'HAS_MODEL'}]->(m:Model)
RETURN ds AS dataset, COUNT(DISTINCT m) AS totalModels, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT p) AS totalPapers
    """,
        countQuery = """
MATCH (:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(cont:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
MATCH (cont)-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]->(:Benchmark)-[:RELATED {predicate_id: 'HAS_DATASET'}]->(ds:Dataset)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
OPTIONAL MATCH (cont)-[:RELATED {predicate_id: 'HAS_MODEL'}]->(m:Model)
WITH ds AS dataset, COUNT(DISTINCT m) AS totalModels, COUNT(DISTINCT l) AS totalCodes, COUNT(DISTINCT p) AS totalPapers
RETURN COUNT(dataset) AS cnt
    """)
    fun findDatasetsByResearchProblem(id: ResourceId, pageable: Pageable): Page<Neo4jDataset>

    @Query("""
MATCH (ds:Dataset {resource_id: $id})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(b:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(c:Contribution)
MATCH (b)-[:RELATED {predicate_id: 'HAS_EVALUATION'}]->(e:Evaluation)
MATCH (s:Literal)<-[:RELATED {predicate_id: 'HAS_VALUE'}]-(e)-[:RELATED {predicate_id: 'HAS_METRIC'}]->(mt:Metric)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:Model)<-[:RELATED {predicate_id: 'HAS_MODEL'}]-(c)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
    """,
        countQuery = """
MATCH (ds:Dataset {resource_id: $id})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(b:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(c:Contribution)
MATCH (b)-[:RELATED {predicate_id: 'HAS_EVALUATION'}]->(e:Evaluation)
MATCH (s:Literal)<-[:RELATED {predicate_id: 'HAS_VALUE'}]-(e)-[:RELATED {predicate_id: 'HAS_METRIC'}]->(mt:Metric)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:Model)<-[:RELATED {predicate_id: 'HAS_MODEL'}]-(c)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
WITH p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
RETURN COUNT(paper) AS cnt
    """)
    fun summarizeDatasetQueryById(id: ResourceId, pageable: Pageable): Page<Neo4jBenchmarkUnpacked>

    @Query("""
MATCH (ds:Dataset {resource_id: $id})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(b:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(c:Contribution)-[:RELATED {predicate_id: 'P32'}]->(:Problem {resource_id: $problemId})
MATCH (b)-[:RELATED {predicate_id: 'HAS_EVALUATION'}]->(e:Evaluation)
MATCH (s:Literal)<-[:RELATED {predicate_id: 'HAS_VALUE'}]-(e)-[:RELATED {predicate_id: 'HAS_METRIC'}]->(mt:Metric)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:Model)<-[:RELATED {predicate_id: 'HAS_MODEL'}]-(c)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
RETURN p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
    """,
        countQuery = """
MATCH (ds:Dataset {resource_id: $id})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(b:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(c:Contribution)-[:RELATED {predicate_id: 'P32'}]->(:Problem {resource_id: $problemId})
MATCH (b)-[:RELATED {predicate_id: 'HAS_EVALUATION'}]->(e:Evaluation)
MATCH (s:Literal)<-[:RELATED {predicate_id: 'HAS_VALUE'}]-(e)-[:RELATED {predicate_id: 'HAS_METRIC'}]->(mt:Metric)
MATCH (c)<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (month:Literal)-[:RELATED {predicate_id: 'P28'}]-(p)-[:RELATED {predicate_id: 'P29'}]-(year:Literal)
OPTIONAL MATCH (md:Model)<-[:RELATED {predicate_id: 'HAS_MODEL'}]-(c)-[:RELATED {predicate_id: 'HAS_SOURCE_CODE'}]->(l:Literal)
WITH p AS paper, month.label AS month, year.label AS year, COLLECT(DISTINCT l.label) AS codes, md.label AS model, mt.label AS metric, s.label AS score
RETURN COUNT(paper) AS cnt
    """)
    fun summarizeDatasetQueryByIdAndProblemId(id: ResourceId, problemId: ResourceId, pageable: Pageable): Page<Neo4jBenchmarkUnpacked>
}
