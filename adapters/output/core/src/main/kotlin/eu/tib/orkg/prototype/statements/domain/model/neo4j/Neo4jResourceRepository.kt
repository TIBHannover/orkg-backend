package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.constants.ID_DOI_PREDICATE
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.className
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.classes
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.createdBy
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.doi
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.ids
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.label
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.observatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

/**
 * Partial query that returns the node as well as its ID and relationships.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE =
    """RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)"""

/**
 * Partial query that returns the node count for use in count queries.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE_COUNT = """RETURN count(node)"""

/**
 * Partial query that expands the node properties so that they can be used with pagination in custom queries.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val WITH_NODE_PROPERTIES =
    """WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at"""

// Custom queries

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {resource_id: $id})"""

private const val MATCH_VERIFIED_PAPER =
    """MATCH (node) WHERE EXISTS(node.verified) AND node.verified = true AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNVERIFIED_PAPER =
    """MATCH (node) WHERE (NOT EXISTS(node.verified) OR node.verified = false) AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): List<Neo4jResource>

    override fun findById(id: Long?): Optional<Neo4jResource>

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    fun findAllByResourceIdIn(ids: List<ResourceId>): Iterable<Neo4jResource>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jResource>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(
        value = """MATCH (node:`Resource`) WHERE $className IN labels(node) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(@Param("className") `class`: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndCreatedBy(@Param("className") `class`: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: Check if the countQuery can be optimized or joined with the value query
    @Query(value = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabel(@Param("className") `class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label = $label AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label = $label AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelAndCreatedBy(@Param("className") `class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: move from Slice to Page object
    @Query(value = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label =~ $label  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContaining(@Param("className") `class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $className IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContainingAndCreatedBy(@Param("className") `class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClassByLabel(classes: List<String>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) AND node.label =~ $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $classes WHERE c IN labels(node)) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClassByLabelContaining(classes: List<String>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""UNWIND $ids as r_id MATCH ()-[p:RELATED]->(node:Resource {resource_id: r_id}) WITH r_id, COUNT(p) AS cnt RETURN cnt""")
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: $doi}) RETURN n""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: $doi}) RETURN n""")
    fun findAllByDOI(doi: String): Iterable<Neo4jResource>

    fun findByLabel(label: String?): Optional<Neo4jResource>

    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: $observatoryId}) RETURN n""")
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Comparison {observatory_id: $observatoryId}) RETURN n""")
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: $observatoryId})-[*]->(r:Problem) RETURN r UNION ALL MATCH (r:Problem {observatory_id: $observatoryId}) RETURN r""")
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Resource {resource_id: $id}) CALL apoc.path.subgraphAll(n, {relationshipFilter:'>'}) YIELD relationships UNWIND relationships as rel WITH rel AS p, startNode(rel) AS s, endNode(rel) AS o, n WHERE p.created_by <> "00000000-0000-0000-0000-000000000000" and p.created_at>=n.created_at RETURN n.resource_id AS id, (p.created_by) AS createdBy, MAX(p.created_at) AS createdAt ORDER BY createdAt""")
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>

    @Query("""MATCH (n:Resource {resource_id: $id}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
    fun checkIfResourceHasStatements(id: ResourceId): Boolean

    fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findPaperByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_VERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_VERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllVerifiedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNVERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNVERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnverifiedPapers(pageable: Pageable): Page<Neo4jResource>
}

data class ResourceContributors(
    val id: String,

    val createdBy: String,

    val createdAt: String
)
