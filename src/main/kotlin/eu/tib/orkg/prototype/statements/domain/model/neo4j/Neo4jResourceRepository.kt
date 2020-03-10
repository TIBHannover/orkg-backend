package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.application.ID_DOI_PREDICATE
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional
import java.util.UUID

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): Iterable<Neo4jResource>

    override fun findById(id: Long?): Optional<Neo4jResource>

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jResource>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(`class`: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.created_by = {1} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.created_by = {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndCreatedBy(`class`: String, createdBy: UUID, pageable: Pageable): Page<Neo4jResource>

    // TODO: Check if the countQuery can be optimized or joined with the value query
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Slice<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} AND node.created_by = {2} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} AND node.created_by = {2} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelAndCreatedBy(`class`: String, label: String, createdBy: UUID, pageable: Pageable): Slice<Neo4jResource>

    // TODO: move from Slice to Page object
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1}  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContaining(`class`: String, label: String, pageable: Pageable): Slice<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} AND node.created_by = {2} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} AND node.created_by = {2} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContainingAndCreatedBy(`class`: String, label: String, createdBy: UUID, pageable: Pageable): Slice<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""")
    fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Slice<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label = {1} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""")
    fun findAllExcludingClassByLabel(classes: List<String>, label: String, pageable: Pageable): Slice<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label =~ {1}  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""")
    fun findAllExcludingClassByLabelContaining(classes: List<String>, label: String, pageable: Pageable): Slice<Neo4jResource>

    @Query("""UNWIND {0} as r_id MATCH ()-[p:RELATED]->(node:Resource {resource_id: r_id}) WITH r_id, COUNT(p) AS cnt RETURN cnt""")
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: {0}}) RETURN n""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: {0}}) RETURN n""")
    fun findAllByDOI(doi: String): Iterable<Neo4jResource>

    fun findByLabel(label: String?): Optional<Neo4jResource>

    fun findAllByLabel(label: String): Iterable<Neo4jResource>
}
