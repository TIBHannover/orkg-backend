package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.ObjectController.Constants.ID_DOI_PREDICATE
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import java.util.*
import org.neo4j.ogm.annotation.Property
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

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

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {resource_id: {0}})"""

private const val MATCH_VERIFIED_PAPER =
    """MATCH (node) WHERE EXISTS(node.verified) AND node.verified = true AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNVERIFIED_PAPER =
    """MATCH (node) WHERE (NOT EXISTS(node.verified) OR node.verified = false) AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_FEATURED_PAPER =
    """MATCH (node) WHERE node.featured = true AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_NONFEATURED_PAPER =
    """MATCH (node) WHERE node.featured = false AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_UNLISTED_PAPER =
    """MATCH (node) WHERE node.unlisted = true AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val MATCH_LISTED_PAPER =
    """MATCH (node) WHERE OR node.unlisted = false AND ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): Iterable<Neo4jResource>

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
    fun findAllByClassAndCreatedBy(`class`: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: Check if the countQuery can be optimized or joined with the value query
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} AND node.created_by = {2} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label = {1} AND node.created_by = {2} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: move from Slice to Page object
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1}  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContaining(`class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} AND node.created_by = {2} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) AND node.label =~ {1} AND node.created_by = {2} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelContainingAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label = {1} WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label = {1} WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClassByLabel(classes: List<String>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label =~ {1}  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in {0} WHERE c IN labels(node)) AND node.label =~ {1}  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllExcludingClassByLabelContaining(classes: List<String>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""UNWIND {0} as r_id MATCH ()-[p:RELATED]->(node:Resource {resource_id: r_id}) WITH r_id, COUNT(p) AS cnt RETURN cnt""")
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: {0}}) RETURN n""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: {0}}) RETURN n""")
    fun findAllByDOI(doi: String): Iterable<Neo4jResource>

    fun findByLabel(label: String?): Optional<Neo4jResource>

    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}}) RETURN n""")
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Comparison {observatory_id: {0}}) RETURN n""")
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}})-[*]->(r:Problem) RETURN r UNION ALL MATCH (r:Problem {observatory_id: {0}}) RETURN r""")
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}, featured: {1}, unlisted: {2}}) RETURN n.resource_id as id, n.label as label, n.created_at as created_at, LABELS(n) as classes, n.shared as shared, n.created_by as created_by, n._class as _class, n.observatory_id as observatory_id, n.extraction_method as extraction_method, n.organization_id as organization_id, n.featured as featured, n.unlisted as unlisted, n.verified as verified""",
    countQuery = """MATCH (n:Paper {observatory_id: {0}, featured: {1}, unlisted: {2}}) RETURN COUNT(n)""")
    fun findPapersByObservatoryId(id: ObservatoryId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Comparison {observatory_id: {0}, featured: {1}, unlisted: {2}}) RETURN n.resource_id as id, n.label as label, n.created_at as created_at, LABELS(n) as classes, n.shared as shared, n.created_by as created_by, n._class as _class, n.observatory_id as observatory_id, n.extraction_method as extraction_method, n.organization_id as organization_id, n.featured as featured, n.unlisted as unlisted, n.verified as verified""",
    countQuery = """MATCH (n:Comparison {observatory_id: {0}, featured: {1}, unlisted: {2}}) RETURN COUNT(n)""")
    fun findComparisonsByObservatoryId(id: ObservatoryId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}, featured: {1}, unlisted: {2}})-[*]->(r:Problem) RETURN r.resource_id as id, r.label as label, r.created_at as created_at, LABELS(n) as classes, r.shared as shared, r.created_by as created_by, r._class as _class, r.observatory_id as observatory_id, r.extraction_method as extraction_method, r.organization_id as organization_id, r.featured as featured, r.unlisted as unlisted, r.verified as verified""",
    countQuery = """MATCH (n:Paper {observatory_id: {0}, featured: {1}, unlisted: {2}})-[*]->(r:Problem) RETURN COUNT(r)""")
    fun findProblemsByObservatoryId(id: ObservatoryId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}, unlisted: {1}}) RETURN n.resource_id as id, n.label as label, n.created_at as created_at, LABELS(n) as classes, n.shared as shared, n.created_by as created_by, n._class as _class, n.observatory_id as observatory_id, n.extraction_method as extraction_method, n.organization_id as organization_id, n.featured as featured, n.unlisted as unlisted, n.verified as verified""",
    countQuery = """MATCH (n:Paper {observatory_id: {0}, unlisted: {1}}) RETURN COUNT(n)""")
    fun findPapersByObservatoryIdWithoutFeatured(id: ObservatoryId, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Comparison {observatory_id: {0}, unlisted: {1}}) RETURN n.resource_id as id, n.label as label, n.created_at as created_at, LABELS(n) as classes, n.shared as shared, n.created_by as created_by, n._class as _class, n.observatory_id as observatory_id, n.extraction_method as extraction_method, n.organization_id as organization_id, n.featured as featured, n.unlisted as unlisted, n.verified as verified""",
    countQuery = """MATCH (n:Comparison {observatory_id: {0}, unlisted: {1}}) RETURN COUNT(n)""")
    fun findComparisonsByObservatoryIdWithoutFeatured(id: ObservatoryId, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Paper {observatory_id: {0}, unlisted: {1}})-[*]->(r:Problem) RETURN r.resource_id as id, r.label as label, r.created_at as created_at, LABELS(r) as classes, r.shared as shared, r.created_by as created_by, r._class as _class, r.observatory_id as observatory_id, r.extraction_method as extraction_method, r.organization_id as organization_id, r.featured as featured, r.unlisted as unlisted, r.verified as verified""",
    countQuery = """MATCH (n:Paper {observatory_id: {0}, unlisted: {1}})-[*]->(r:Problem) RETURN COUNT(r)""")
    fun findProblemsByObservatoryIdWithoutFeatured(id: ObservatoryId, unlisted: Boolean, pageable: Pageable): Page<DetailsPerResource>

    @Query("""MATCH (n:Resource {resource_id: {0}}) CALL apoc.path.subgraphAll(n, {relationshipFilter:'>'}) YIELD relationships UNWIND relationships as rel WITH rel AS p, startNode(rel) AS s, endNode(rel) AS o, n WHERE p.created_by <> "00000000-0000-0000-0000-000000000000" and p.created_at>=n.created_at RETURN n.resource_id AS id, (p.created_by) AS createdBy, MAX(p.created_at) AS createdAt ORDER BY createdAt""")
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>

    @Query("""MATCH (n:Resource {resource_id: {0}}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
    fun checkIfResourceHasStatements(id: ResourceId): Boolean

    fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Neo4jResource>

    fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Neo4jResource>

    fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Neo4jResource>

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

    @Query(
        value = """$MATCH_FEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_FEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_NONFEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_UNLISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE (ANY(c in {0} WHERE c IN labels(node)) AND node.unlisted={1}) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE (ANY(c in {0} WHERE c IN labels(node)) AND node.unlisted={1})  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<String>, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE (ANY(c in {0} WHERE c IN labels(node)) AND node.featured={1} AND node.unlisted={2})  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE (ANY(c in {0} WHERE c IN labels(node)) AND node.featured={1} AND node.unlisted={2}) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<String>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>
}

@QueryResult
data class DetailsPerResource(
    val id: String?,
    val label: String?,
    @JsonProperty("created_at")
    @Property("created_at")
    val createdAt: String?,
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    @JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    val featured: Boolean?,
    val unlisted: Boolean?,
    val classes: List<String>?,
    @JsonProperty("created_by")
    val createdBy: String?
)
