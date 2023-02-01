package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ObjectService
import java.util.*
import eu.tib.orkg.prototype.statements.spi.StatementRepository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val configuration = "${'$'}configuration"
private const val subjectClass = "${'$'}subjectClass"
private const val literal = "${'$'}literal"
private const val subjectId = "${'$'}subjectId"
private const val subjectIds = "${'$'}subjectIds"
private const val objectIds = "${'$'}objectIds"
private const val predicateId = "${'$'}predicateId"
private const val objectId = "${'$'}objectId"
private const val paperId = "${'$'}paperId"
private const val id = "${'$'}id"
private const val resourceIds = "${'$'}resourceIds"
private const val doi = "${'$'}doi"
private const val ids = "${'$'}ids"

/**
 * Partial query that matches a statement.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
private const val MATCH_STATEMENT =
    """MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`)"""

private const val MATCH_STATEMENT_WITH_LITERAL =
    """MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Literal`)"""

/**
 * Partial query that returns the statement, with the id and the created at date.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
private const val RETURN_STATEMENT =
    """RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at"""

/**
 * Partial query that returns the number of statements (relationships) in [Query.countQuery] queries.
 */
private const val RETURN_COUNT = "RETURN count(rel) AS count"

/**
 * Partial query that "flattens" the object into "columns" that can be sorted by SDN.
 */
private const val WITH_SORTABLE_FIELDS =
    """WITH sub, obj, rel, rel.created_at AS created_at, rel.created_by AS created_by"""

// Custom queries

private const val BY_SUBJECT_ID =
    """WHERE sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId"""

private const val BY_OBJECT_ID =
    """WHERE obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId"""

private const val WHERE_SUBJECT_ID_IN =
    """WHERE sub.`resource_id` IN $subjectIds OR sub.`literal_id` IN $subjectIds OR sub.`predicate_id` IN $subjectIds OR sub.`class_id` IN $subjectIds"""

private const val WHERE_OBJECT_ID_IN =
    """WHERE obj.`resource_id` IN $objectIds OR obj.`literal_id` IN $objectIds OR obj.`predicate_id` IN $objectIds OR obj.`class_id` IN $objectIds"""

interface Neo4jStatementRepository :
    Neo4jRepository<Neo4jStatement, Long> {
    fun existsByStatementId(id: StatementId): Boolean

    override fun findAll(depth: Int): Iterable<Neo4jStatement>

    override fun findById(id: Long): Optional<Neo4jStatement>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatement>

    @Query("""MATCH ()-[s:`RELATED`]->() WHERE s.statement_id = $id DELETE s""")
    fun deleteByStatementId(id: StatementId)

    @Query("$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubject(subjectId: String, pagination: Pageable): Page<Neo4jStatement>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObject(objectId: String, pagination: Pageable): Page<Neo4jStatement>

    @Query("""MATCH statement=(sub:`Resource`)<-[:`RELATED`]-(:`Thing`) WHERE sub.resource_id in $resourceIds WITH sub.resource_id as resourceId, count(statement) as count RETURN resourceId, count""")
    fun countStatementsAboutResource(resourceIds: Set<ResourceId>): List<StatementsPerResource>

    /** Count all incoming statements to a resource node. This is used to calculate the "shared" property. */
    @Query("""MATCH statement=(obj:`Resource` {resource_id: $id})<-[rel:`RELATED`]-(:`Thing`) RETURN count(statement)""")
    fun countStatementsByObjectId(id: ResourceId): Long

    @Query("""MATCH (p:`Thing`)-[*]->() WHERE p.`resource_id`=$paperId OR p.`literal_id`=$paperId OR p.`predicate_id`=$paperId OR p.`class_id`=$paperId RETURN COUNT(p)""")
    fun countByIdRecursive(paperId: String): Int

    @Query("$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT WHERE (sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE sub.class_id=$subjectClass AND rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE sub.class_id=$subjectClass AND rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllBySubjects(
        subjectIds: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByObjects(
        objectIds: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """MATCH (n:Thing)
WHERE n.resource_id = $id OR n.literal_id = $id OR n.class_id = $id OR n.predicate_id = $id
CALL apoc.path.subgraphAll(n, $configuration)
YIELD relationships
UNWIND relationships as rel
RETURN startNode(rel) as subject, rel as predicate, endNode(rel) as object
ORDER BY rel.created_at DESC"""
    )
    fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<Neo4jStatement>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: 'P31'}]->(:Resource {resource_id: $id}), (n)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(L:Literal) RETURN L""")
    fun findDOIByContributionId(id: ResourceId): Optional<Neo4jLiteral>

    @Query("""OPTIONAL MATCH (:Thing)-[r1:RELATED {predicate_id: $id}]->(:Thing) OPTIONAL MATCH (:Predicate {predicate_id: $id})-[r2:RELATED]-(:Thing) WITH COUNT(DISTINCT r1) as relations, COUNT(DISTINCT r2) as nodes RETURN relations + nodes as cnt""")
    fun countPredicateUsage(id: PredicateId): Long

    @Query("""UNWIND $ids as r_id MATCH ()-[p:RELATED]->(node:Resource {resource_id: r_id}) WITH r_id, COUNT(p) AS cnt RETURN cnt""")
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>

    @Query("""MATCH (node:Paper)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(:Literal {label: $doi}) WHERE not 'PaperDeleted' in labels(node) RETURN node""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""MATCH (node:Paper)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(:Literal {label: $doi}) WHERE not 'PaperDeleted' in labels(node) RETURN node""")
    fun findAllByDOI(doi: String): Iterable<Neo4jResource>

    @Query("""MATCH (n:Paper {observatory_id: $id})-[*]->(r:Problem) RETURN r UNION ALL MATCH (r:Problem {observatory_id: $id}) RETURN r""")
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Neo4jResource>

    @Query("""MATCH (n:Resource {resource_id: "$id"})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH node
WHERE node.created_by <> "00000000-0000-0000-0000-000000000000"
WITH DISTINCT node.created_by AS created_by, node.created_at AS created_at
RETURN created_by, created_at
ORDER BY created_at DESC""",
        countQuery = """MATCH (n:Resource {resource_id: "$id"})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH node
WHERE node.created_by <> "00000000-0000-0000-0000-000000000000"
WITH DISTINCT node.created_by AS created_by, node.created_at AS created_at
RETURN COUNT(created_by) as cnt""")
    fun findContributorsByResourceId(id: ResourceId, pageable: Pageable): Page<ResourceContributor>

    @Query("""MATCH (n:Resource {resource_id: $id}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
    fun checkIfResourceHasStatements(id: ResourceId): Boolean

    @Query(value = """MATCH (n:Comparison {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem) RETURN DISTINCT p""",
        countQuery = """MATCH (n:Comparison {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem) RETURN count(DISTINCT p)""")
    fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>
}

@QueryResult
data class StatementsPerResource(
    val resourceId: String,
    val count: Long
)
