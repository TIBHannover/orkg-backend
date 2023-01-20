package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.ObjectService.Constants.ID_DOI_PREDICATE
import eu.tib.orkg.prototype.statements.spi.ResourceRepository.ResourceContributors
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.transaction.annotation.Transactional

private const val featured = "${'$'}featured"
private const val unlisted = "${'$'}unlisted"
private const val doi = "${'$'}doi"
private const val ids = "${'$'}ids"
private const val classes = "${'$'}classes"
private const val includeClasses = "${'$'}includeClasses"
private const val excludeClasses = "${'$'}excludeClasses"
private const val label = "${'$'}label"
private const val createdBy = "${'$'}createdBy"
private const val `class` = "${'$'}`class`"
private const val id = "${'$'}id"

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

private const val HAS_PAPER_CLASS = """ANY(collectionFields IN ['Paper'] WHERE collectionFields IN LABELS(node))"""

private const val HAS_CLASSES = """ANY(collectionFields IN $classes WHERE collectionFields IN LABELS(node))"""

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {resource_id: $id})"""

private const val MATCH_VERIFIED_PAPER =
    """MATCH (node) WHERE EXISTS(node.verified) AND node.verified = true AND $HAS_PAPER_CLASS"""

private const val MATCH_UNVERIFIED_PAPER =
    """MATCH (node) WHERE (NOT EXISTS(node.verified) OR node.verified = false) AND $HAS_PAPER_CLASS"""

private const val MATCH_FEATURED_PAPER =
    """MATCH (node) WHERE node.featured = true AND $HAS_PAPER_CLASS"""

private const val MATCH_NONFEATURED_PAPER =
    """MATCH (node) WHERE node.featured = false AND $HAS_PAPER_CLASS"""

private const val MATCH_UNLISTED_PAPER =
    """MATCH (node) WHERE node.unlisted = true AND $HAS_PAPER_CLASS"""

private const val MATCH_LISTED_PAPER =
    """MATCH (node) WHERE OR node.unlisted = false AND $HAS_PAPER_CLASS"""

const val IS_FEATURED = "COALESCE(node.featured, false) = $featured"
const val IS_UNLISTED = "COALESCE(node.unlisted, false) = $unlisted"

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    fun existsByResourceId(id: ResourceId): Boolean

    @Query("""MATCH (node:`Resource` {resource_id: $id}) WHERE $HAS_CLASSES $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findByIdAndClassesContaining(id: ResourceId, classes: Set<String>): Neo4jResource?

    override fun findAll(): Iterable<Neo4jResource>

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jResource>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(`class`: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndCreatedBy(`class`: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: Check if the countQuery can be optimized or joined with the value query
    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    // TODO: move from Slice to Page object
    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelMatchesRegex(`class`: String, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelMatchesRegexAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClasses(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClassesByLabel(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) AND node.label =~ $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
    countQuery = """MATCH (node:`Resource`) WHERE NOT ANY(c in $excludeClasses WHERE c IN labels(node)) AND ALL(c in $includeClasses WHERE c IN labels(node)) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""UNWIND $ids as r_id MATCH ()-[p:RELATED]->(node:Resource {resource_id: r_id}) WITH r_id, COUNT(p) AS cnt RETURN cnt""")
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>

    @Query("""MATCH (node:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: $doi}) WHERE not 'PaperDeleted' in labels(node) RETURN node""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(:Literal {label: $doi}) WHERE not 'PaperDeleted' in labels(node) RETURN node""")
    fun findAllByDOI(doi: String): Iterable<Neo4jResource>

    @Query("""MATCH (node:Paper) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label RETURN node""")
    fun findByLabel(label: String?): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label RETURN node""")
    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    @Query("""MATCH (n {observatory_id: ${'$'}id}) WHERE ${'$'}class in LABELS(n) RETURN n""")
    fun findByClassAndObservatoryId(`class`: String, id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: $id})-[*]->(r:Problem) RETURN r UNION ALL MATCH (r:Problem {observatory_id: $id}) RETURN r""")
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Resource {resource_id: $id}) CALL apoc.path.subgraphAll(n, {relationshipFilter:'>'}) YIELD relationships UNWIND relationships AS rel WITH rel AS p, startNode(rel) AS s, endNode(rel) AS o, n WITH apoc.date.format(apoc.date.parse(p.created_at, 'ms', 'yyyy-MM-dd'), 'ms', 'yyyy-MM-dd') AS date, p, o, n, s  WITH collect(n.created_by) as createdBy, collect(apoc.date.format(apoc.date.parse(n.created_at, 'ms', 'yyyy-MM-dd'), 'ms', 'yyyy-MM-dd')) as createdAt, p, o, n, s, date WITH createdBy + collect(p.created_by) AS creator, createdAt + collect(date) as date, p, o, n, s WHERE p.created_by <> "00000000-0000-0000-0000-000000000000" AND NOT 'ResearchField' in labels(s) AND NOT 'ResearchField' in labels(o) UNWIND creator as createdBy UNWIND date as createdAt RETURN DISTINCT n.resource_id AS id, createdBy, createdAt ORDER BY createdAt""")
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>

    @Query("""MATCH (n:Resource {resource_id: $id}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
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

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_UNLISTED WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_UNLISTED  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<String>, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_FEATURED AND $IS_UNLISTED  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_FEATURED AND $IS_UNLISTED WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<String>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_FEATURED AND $IS_UNLISTED  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_FEATURED AND $IS_UNLISTED WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByObservatoryIdAndClass(id: ObservatoryId, classes: List<String>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_UNLISTED WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at RETURN node, [ [ (node)<-[r_r1:`RELATED`]-(r1:`Resource`) | [ r_r1, r1 ] ], [ (node)-[r_r1:`RELATED`]->(r1:`Resource`) | [ r_r1, r1 ] ] ], ID(node)""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_UNLISTED  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllResourcesByObservatoryIdAndClass(id: ObservatoryId, classes: List<String>, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN DISTINCT n.created_by""",
        countQuery = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN COUNT(DISTINCT n.created_by) as cnt""")
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>

    // The return type has to be Iterable<Long> due to type erasure as java.lang.Long or Iterable<java.lang.Long> is
    // required by Spring, but we want to use kotlin.Long whenever possible
    @Transactional
    fun deleteByResourceId(id: ResourceId): Iterable<Long>

    @Query(value = """MATCH (n:Comparison {organization_id: $id }) RETURN n""",
        countQuery = """MATCH (n:Comparison {organization_id: $id }) RETURN COUNT(n)""")
    fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (n:Comparison {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem) RETURN DISTINCT p""",
        countQuery = """MATCH (n:Comparison {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem) RETURN count(DISTINCT p)""")
    fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>
}
