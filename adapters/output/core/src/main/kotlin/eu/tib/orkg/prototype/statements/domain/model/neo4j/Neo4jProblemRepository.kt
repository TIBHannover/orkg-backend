package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.datasetId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.limit
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.problemId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.skip
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional
import java.util.UUID

interface Neo4jProblemRepository :
    Neo4jRepository<Neo4jResource, Long> {

    @Query("""MATCH (node:Problem {resource_id: $id}) RETURN node""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, cont, datetime() AS now
                    WHERE datetime(cont.created_at).year = now.year AND datetime(cont.created_at).month <= now.month AND datetime(cont.created_at).month > now.month - ${'$'}months
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt  DESC
                    LIMIT 5""")
    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Neo4jResource>

    @Query("""MATCH (problem:Problem)<-[:RELATED {predicate_id:'P32'}]-(cont:Contribution)
                    WITH problem, COUNT(cont) AS cnt
                    RETURN problem
                    ORDER BY cnt DESC
                    LIMIT 5""")
    fun findTopResearchProblemsAllTime(): Iterable<Neo4jResource>

    @Query(value = """MATCH (ds:Dataset {resource_id: $datasetId})<-[:RELATED {predicate_id: 'HAS_DATASET'}]-(:Benchmark)<-[:RELATED {predicate_id: 'HAS_BENCHMARK'}]-(:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Problem)
                    RETURN DISTINCT problem""")
    fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Neo4jResource>
}
