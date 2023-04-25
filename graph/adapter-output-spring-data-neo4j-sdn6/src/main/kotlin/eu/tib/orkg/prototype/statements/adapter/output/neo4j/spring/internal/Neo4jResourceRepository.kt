package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.transaction.annotation.Transactional

private const val featured = "${'$'}featured"
private const val unlisted = "${'$'}unlisted"
private const val classes = "${'$'}classes"
private const val includeClasses = "${'$'}includeClasses"
private const val excludeClasses = "${'$'}excludeClasses"
private const val label = "${'$'}label"
private const val createdBy = "${'$'}createdBy"
private const val `class` = "${'$'}`class`"
private const val id = "${'$'}id"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"

/**
 * Partial query that returns the node.
 * Queries using this partial query must use `node` as the binding name.
 */
private const val RETURN_NODE = """RETURN node"""

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

private const val HAS_CLASSES = """ANY(collectionFields IN $classes WHERE collectionFields IN LABELS(node))"""

private const val MATCH_PAPER = """MATCH (node:`Resource`:`Paper`)"""

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {resource_id: $id})"""

private const val MATCH_VERIFIED_PAPER =
    """$MATCH_PAPER WHERE node.verified IS NOT NULL AND node.verified = true"""

private const val MATCH_UNVERIFIED_PAPER =
    """$MATCH_PAPER WHERE (node.verified IS NULL OR node.verified = false)"""

private const val MATCH_FEATURED_PAPER =
    """$MATCH_PAPER WHERE node.featured = true"""

private const val MATCH_NONFEATURED_PAPER =
    """$MATCH_PAPER WHERE node.featured = false"""

private const val MATCH_UNLISTED_PAPER =
    """$MATCH_PAPER WHERE node.unlisted = true"""

private const val MATCH_LISTED_PAPER =
    """$MATCH_PAPER WHERE node.unlisted = false"""

const val IS_FEATURED = "COALESCE(node.featured, false) = $featured"
const val IS_UNLISTED = "COALESCE(node.unlisted, false) = $unlisted"

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    fun existsByResourceId(id: ResourceId): Boolean

    @Query("""MATCH (node:`Resource` {resource_id: $id}) WHERE $HAS_CLASSES $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findByIdAndClassesContaining(id: ResourceId, classes: Set<ThingId>): Neo4jResource?

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jResource>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(`class`: ClassId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndCreatedBy(`class`: ClassId, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabel(`class`: ClassId, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelMatchesRegex(`class`: ClassId, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.label =~ $label AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndLabelMatchesRegexAndCreatedBy(`class`: ClassId, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClasses(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) AND node.label = $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) AND node.label = $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClassesByLabel(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) AND node.label =~ $label WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) AND node.label =~ $label WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(includeClasses: Set<ClassId>, excludeClasses: Set<ClassId>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Paper) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label $RETURN_NODE LIMIT 1""")
    fun findByLabel(label: String?): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label $RETURN_NODE""")
    fun findAllByLabel(label: String): Iterable<Neo4jResource>

    @Query("""MATCH (n {observatory_id: ${'$'}id}) WHERE ${'$'}class in LABELS(n) RETURN n""")
    fun findByClassAndObservatoryId(`class`: ClassId, id: ObservatoryId): Iterable<Neo4jResource>

    fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Neo4jResource>

    fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Neo4jResource>

    fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Neo4jResource>

    fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findPaperByResourceId(id: ResourceId): Optional<Neo4jResource>

    @Query(
        value = """$MATCH_VERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_VERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllVerifiedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNVERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_UNVERIFIED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnverifiedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_FEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_FEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllFeaturedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_NONFEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_NONFEATURED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllNonFeaturedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_UNLISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_UNLISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllUnlistedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(
        value = """$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $RETURN_NODE_COUNT"""
    )
    fun findAllListedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_UNLISTED WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_UNLISTED  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<ClassId>, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_FEATURED AND $IS_UNLISTED  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND $IS_FEATURED AND $IS_UNLISTED WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByClass(classes: List<ClassId>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_FEATURED AND $IS_UNLISTED  WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_FEATURED AND $IS_UNLISTED WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllFeaturedResourcesByObservatoryIdAndClass(id: ObservatoryId, classes: List<ClassId>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_UNLISTED WITH node, node.label AS label, node.resource_id AS id, node.created_at AS created_at $RETURN_NODE $PAGE_PARAMS""",
        countQuery = """MATCH (node:`Resource`) WHERE ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id AND $IS_UNLISTED  WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllResourcesByObservatoryIdAndClass(id: ObservatoryId, classes: List<ClassId>, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN DISTINCT n.created_by ORDER BY n.created_by ASC $PAGE_PARAMS""",
        countQuery = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN COUNT(DISTINCT n.created_by) as cnt""")
    fun findAllContributorIds(pageable: Pageable): Page<String> // FIXME: This should be ContributorId

    // The return type has to be Iterable<Long> due to type erasure as java.lang.Long or Iterable<java.lang.Long> is
    // required by Spring, but we want to use kotlin.Long whenever possible
    @Transactional
    fun deleteByResourceId(id: ResourceId): Iterable<Long>

    @Query(value = """MATCH (n:Comparison {organization_id: $id }) RETURN n $PAGE_PARAMS""",
        countQuery = """MATCH (n:Comparison {organization_id: $id }) RETURN COUNT(n)""")
    fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>
}
