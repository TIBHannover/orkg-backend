package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.transaction.annotation.Transactional

private const val classes = "${'$'}classes"
private const val includeClasses = "${'$'}includeClasses"
private const val excludeClasses = "${'$'}excludeClasses"
private const val label = "${'$'}label"
private const val createdBy = "${'$'}createdBy"
private const val `class` = "${'$'}`class`"
private const val id = "${'$'}id"
private const val visibility = "${'$'}visibility"
private const val verified = "${'$'}verified"
private const val minLabelLength = "${'$'}minLabelLength"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_resource_on_label"

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
    """WITH node, node.label AS label, node.id AS id, node.created_at AS created_at"""

// Custom queries

private const val HAS_CLASSES = """ANY(collectionFields IN $classes WHERE collectionFields IN LABELS(node))"""

private const val MATCH_PAPER = """MATCH (node:`Resource`:`Paper`)"""

private const val MATCH_LISTED_PAPER = """MATCH (node:`Resource`:`Paper`) WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED")"""

private const val MATCH_PAPER_BY_ID = """MATCH (node:`Resource`:`Paper` {id: $id})"""

private const val WHERE_VISIBILITY = """WHERE node.visibility = $visibility"""

private const val VERIFIED_IS = """COALESCE(node.verified, false) = $verified"""

private const val ORDER_BY_CREATED_AT = """ORDER BY created_at"""

private const val MATCH_LISTED_RESOURCE = """MATCH (node:Resource) WHERE (node.visibility = "DEFAULT" OR node.visibility = "FEATURED")"""

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    fun existsById(id: ThingId): Boolean

    @Query("""MATCH (node:`Resource` {id: $id}) WHERE $HAS_CLASSES $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findByIdAndClassesContaining(id: ThingId, classes: Set<ThingId>): Neo4jResource?

    override fun findAll(): Iterable<Neo4jResource>

    fun findById(id: ThingId?): Optional<Neo4jResource>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WHERE SIZE(node.label) >= $minLabelLength
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH node, node.label AS label, node.id AS id, node.created_at AS created_at $RETURN_NODE""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH node, node.label AS label, node.id AS id, node.created_at AS created_at $RETURN_NODE""",
        countQuery = """MATCH (node:`Resource`) WHERE $`class` IN labels(node) AND node.created_by = $createdBy WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClassAndCreatedBy(`class`: ThingId, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label) AND $`class` IN labels(node)
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label) AND $`class` IN labels(node)
RETURN COUNT(node)""")
    fun findAllByClassAndLabel(`class`: ThingId, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WHERE SIZE(node.label) >= $minLabelLength AND $`class` IN labels(node)
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength AND $`class` IN labels(node)
RETURN COUNT(node)""")
    fun findAllByClassAndLabelContaining(`class`: ThingId, label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label) AND $`class` IN labels(node) AND node.created_by = $createdBy
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label) AND $`class` IN labels(node) AND node.created_by = $createdBy
RETURN COUNT(node)""")
    fun findAllByClassAndLabelAndCreatedBy(`class`: ThingId, label: String, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WHERE SIZE(node.label) >= $minLabelLength AND $`class` IN labels(node) AND node.created_by = $createdBy
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength AND $`class` IN labels(node) AND node.created_by = $createdBy
RETURN COUNT(node)""")
    fun findAllByClassAndLabelContainingAndCreatedBy(`class`: ThingId, label: String, minLabelLength: Int, createdBy: ContributorId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) WITH node, node.label AS label, node.id AS id, node.created_at AS created_at $RETURN_NODE""",
        countQuery = """MATCH (node:`Resource`) WITH labels(node) AS labels, node WHERE NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllIncludingAndExcludingClasses(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WITH labels(node) AS labels, node
WHERE toLower(node.label) = toLower($label) AND NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels)
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WITH labels(node) AS labels, node
WHERE toLower(node.label) = toLower($label) AND NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels)
RETURN COUNT(node)""")
    fun findAllIncludingAndExcludingClassesByLabel(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, label: String, pageable: Pageable): Page<Neo4jResource>

    @Query(value = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WITH labels(node) AS labels, node, score
WHERE SIZE(node.label) >= $minLabelLength AND NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels)
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WITH labels(node) AS labels, node
WHERE SIZE(node.label) >= $minLabelLength AND NOT ANY(c in $excludeClasses WHERE c IN labels) AND ALL(c in $includeClasses WHERE c IN labels)
RETURN COUNT(node)""")
    fun findAllIncludingAndExcludingClassesByLabelContaining(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label $RETURN_NODE LIMIT 1""")
    fun findPaperByLabel(label: String?): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper:Resource) WHERE not 'PaperDeleted' IN labels(node) AND node.label = $label $RETURN_NODE""")
    fun findAllPapersByLabel(label: String): Iterable<Neo4jResource>

    @Query("""
MATCH (n:Thing {observatory_id: $id})
WHERE $`class` in LABELS(n)
RETURN n
ORDER BY n.created_at""",
        countQuery = """
MATCH (n:Thing {observatory_id: $id})
WHERE $`class` in LABELS(n)
RETURN COUNT(n)""")
    fun findAllByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER_BY_ID $WITH_NODE_PROPERTIES $RETURN_NODE""")
    fun findPaperById(id: ThingId): Optional<Neo4jResource>

    @Query(value = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN DISTINCT n.created_by ORDER BY n.created_by ASC""",
        countQuery = """MATCH (n:`Resource`) WHERE n.created_by <> "00000000-0000-0000-0000-000000000000" RETURN COUNT(DISTINCT n.created_by) as cnt""")
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>

    // The return type has to be Iterable<Long> due to type erasure as java.lang.Long or Iterable<java.lang.Long> is
    // required by Spring, but we want to use kotlin.Long whenever possible
    @Transactional
    fun deleteById(id: ThingId): Iterable<Long>

    @Query(value = """MATCH (n:Comparison:Resource {organization_id: $id}) RETURN n""",
        countQuery = """MATCH (n:Comparison:Resource {organization_id: $id}) RETURN COUNT(n)""")
    fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER WHERE $VERIFIED_IS $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_PAPER WHERE $VERIFIED_IS $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Resource) $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """MATCH (node:Resource) $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_RESOURCE $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_RESOURCE $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListed(pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_PAPER $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_PAPER $WHERE_VISIBILITY $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllPapersByVisibility(visibility: Visibility, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_PAPER $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedPapers(pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility?, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """MATCH (node:Resource) $WHERE_VISIBILITY AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility?, id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE""",
        countQuery = """$MATCH_LISTED_RESOURCE AND ANY(c in $classes WHERE c IN labels(node)) AND node.observatory_id=$id $WITH_NODE_PROPERTIES $ORDER_BY_CREATED_AT $RETURN_NODE_COUNT""")
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>
}
