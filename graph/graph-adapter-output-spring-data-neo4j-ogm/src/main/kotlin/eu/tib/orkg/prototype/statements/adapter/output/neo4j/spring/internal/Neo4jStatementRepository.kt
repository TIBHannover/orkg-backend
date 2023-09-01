package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.services.ObjectService
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository.*
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
private const val id = "${'$'}id"
private const val ids = "${'$'}ids"
private const val resourceIds = "${'$'}resourceIds"
private const val doi = "${'$'}doi"

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
    """WITH sub, obj, rel, rel.created_at AS created_at, rel.created_by AS created_by, rel.index AS index"""

// Custom queries

private const val BY_SUBJECT_ID =
    """WHERE sub.`id`=$subjectId"""

private const val BY_OBJECT_ID =
    """WHERE obj.`id`=$objectId"""

private const val WHERE_SUBJECT_ID_IN =
    """WHERE sub.`id` IN $subjectIds"""

private const val WHERE_OBJECT_ID_IN =
    """WHERE obj.`id` IN $objectIds"""

interface Neo4jStatementRepository :
    Neo4jRepository<Neo4jStatement, Long> {
    fun existsByStatementId(id: StatementId): Boolean

    override fun findAll(depth: Int): Iterable<Neo4jStatement>

    override fun findById(id: Long): Optional<Neo4jStatement>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatement>

    fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<Neo4jStatement>

    @Query("""
MATCH (:`Thing`)-[r:`RELATED` {statement_id: $id}]->(o)
DELETE r
WITH o
WHERE "Literal" IN LABELS(o) AND NOT (o)--()
WITH o.id AS id, o
DELETE o
RETURN id""")
    fun deleteByStatementId(id: StatementId): Optional<ThingId>

    @Query("""
UNWIND $ids AS id
MATCH (:`Thing`)-[r:`RELATED` {statement_id: id}]->(o)
DELETE r
WITH o
WHERE "Literal" IN LABELS(o) AND NOT (o)--()
WITH o.id AS id, o
DELETE o
RETURN id""")
    fun deleteByStatementIds(ids: Set<StatementId>): Set<ThingId>

    @Query("$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubject(subjectId: ThingId, pagination: Pageable): Page<Neo4jStatement>

    fun findAllByPredicateId(predicateId: ThingId, pagination: Pageable): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObject(objectId: ThingId, pagination: Pageable): Page<Neo4jStatement>

    @Query("""MATCH statement=(sub:`Resource`)<-[:`RELATED`]-(:`Thing`) WHERE sub.id in $resourceIds WITH sub.id as id, count(statement) as count RETURN id, count""")
    fun countStatementsAboutResource(resourceIds: Set<ThingId>): List<StatementsPerResource>

    /** Count all incoming statements to a resource node. This is used to calculate the "shared" property. */
    @Query("""MATCH statement=(obj:`Resource` {id: $id})<-[rel:`RELATED`]-(:`Thing`) RETURN count(statement)""")
    fun countStatementsByObjectId(id: ThingId): Long

    @Query("""MATCH (n:Thing {id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
RETURN COUNT(rel) as cnt""")
    fun countByIdRecursive(id: ThingId): Long

    @Query("$MATCH_STATEMENT WHERE obj.id=$objectId AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE obj.id=$objectId AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT WHERE sub.id=$subjectId AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE sub.id=$subjectId AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE $subjectClass IN LABELS(sub) AND rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE $subjectClass IN LABELS(sub) AND rel.`predicate_id`=$predicateId AND obj.`label`=$literal $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllBySubjects(
        subjectIds: List<ThingId>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByObjects(
        objectIds: List<ThingId>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("""
MATCH (n:Thing {id: $id})
CALL apoc.path.subgraphAll(n, $configuration)
YIELD relationships
UNWIND relationships as rel
WITH startNode(rel) as sub, rel, endNode(rel) as obj
$WITH_SORTABLE_FIELDS
ORDER BY rel.created_at DESC
$RETURN_STATEMENT""")
    fun fetchAsBundle(id: ThingId, configuration: Map<String, Any>, sort: Sort): Iterable<Neo4jStatement>

    @Query(
        """MATCH ()-[r:RELATED]->() RETURN r.predicate_id as id, COUNT(r) as count ORDER BY count DESC, id""",
        countQuery = """MATCH ()-[r:RELATED]->() RETURN COUNT(DISTINCT r.predicate_id) as cnt"""
    )
    fun countPredicateUsage(pageable: Pageable): Page<Neo4jPredicateUsageCount>

    @Query("""MATCH (n:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(:Resource {id: $id}), (n)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(L:Literal) RETURN L""")
    fun findDOIByContributionId(id: ThingId): Optional<Neo4jLiteral>

    @Query("""
CALL {
    OPTIONAL MATCH (:Thing)-[r1:RELATED {predicate_id: $id}]->(:Thing) RETURN COUNT(DISTINCT r1) AS cnt
    UNION ALL
    OPTIONAL MATCH (:Predicate {id: $id})-[r2:RELATED]-(:Thing) WHERE r2.predicate_id <> "description" RETURN COUNT(DISTINCT r2) AS cnt
} WITH cnt
RETURN SUM(cnt)""")
    fun countPredicateUsage(id: ThingId): Long

    @Query("""MATCH (node:Paper:Resource)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(l:Literal) WHERE not 'PaperDeleted' in labels(node) AND toUpper(l.label) = toUpper($doi) RETURN node LIMIT 1""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""
MATCH (node:Paper:Resource)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(l:Literal)
WHERE not 'PaperDeleted' in labels(node) AND toUpper(l.label) = toUpper($doi)
RETURN DISTINCT node""",
        countQuery = """
MATCH (node:Paper:Resource)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(l:Literal)
WHERE not 'PaperDeleted' in labels(node) AND toUpper(l.label) = toUpper($doi)
RETURN COUNT(DISTINCT node)""")
    fun findAllPapersByDOI(doi: String, pageable: Pageable): Page<Neo4jResource>

    @Query("""
CALL {
    MATCH (:Paper:Resource {observatory_id: $id})-[:RELATED {predicate_id:"P31"}]->(:Contribution:Resource)-[:RELATED {predicate_id:"P32"}]->(r:Problem:Resource) RETURN r
    UNION
    MATCH (r:Problem:Resource {observatory_id: $id}) RETURN r
} RETURN r ORDER BY r.id""",
        countQuery = """
CALL {
    MATCH (:Paper:Resource {observatory_id: $id})-[:RELATED {predicate_id:"P31"}]->(:Contribution:Resource)-[:RELATED {predicate_id:"P32"}]->(r:Problem:Resource) RETURN r
    UNION
    MATCH (r:Problem:Resource {observatory_id: $id}) RETURN r
} RETURN COUNT(r)""")
    fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (n:Resource {id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH COLLECT(rel) AS p, COLLECT(endNode(rel)) AS o, n
WITH p + o + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH DISTINCT node.created_by AS createdBy
WHERE createdBy IS NOT NULL
RETURN createdBy
ORDER BY createdBy""",
        countQuery = """MATCH (n:Resource {id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH COLLECT(rel) AS p, COLLECT(endNode(rel)) AS o, n
WITH p + o + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH DISTINCT node.created_by AS createdBy
WHERE createdBy IS NOT NULL
RETURN COUNT(createdBy) as cnt""")
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH (n:Resource {id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + COLLECT(n) as nodes, n
WITH DISTINCT nodes, n
UNWIND nodes as node
WITH node, n
WHERE node.created_by IS NOT NULL AND node.created_at IS NOT NULL AND node.created_at >= n.created_at
WITH node.created_by AS createdBy, apoc.text.regreplace(node.created_at, "^(\d+-\d+-\d+T\d+:\d+):\d+(?:\.\d+)?(.*)${'$'}", "${'$'}1:00${'$'}2") AS timestamp
WITH createdBy, apoc.date.parse(timestamp, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX") AS ms
WITH DISTINCT [createdBy, apoc.date.format(ms, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")] AS edit
RETURN edit[0] as createdBy, edit[1] as createdAt
ORDER BY createdAt DESC""",
        countQuery = """MATCH (n:Resource {id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + COLLECT(n) as nodes, n
WITH DISTINCT nodes, n
UNWIND nodes as node
WITH node, n
WHERE node.created_by IS NOT NULL AND node.created_at IS NOT NULL AND node.created_at >= n.created_at
WITH node.created_by AS createdBy, apoc.text.regreplace(node.created_at, "^(\d+-\d+-\d+T\d+:\d+):\d+(?:\.\d+)?(.*)${'$'}", "${'$'}1:00${'$'}2") AS timestamp
WITH createdBy, apoc.date.parse(timestamp, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX") AS ms
WITH DISTINCT [createdBy, apoc.date.format(ms, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")] AS edit
RETURN COUNT(edit) AS cnt""")
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>

    @Query("""MATCH (n:Resource {id: $id}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
    fun checkIfResourceHasStatements(id: ThingId): Boolean

    @Query(value = """MATCH (n:Comparison:Resource {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution:Resource)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem:Resource) RETURN DISTINCT p""",
        countQuery = """MATCH (n:Comparison:Resource {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution:Resource)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem:Resource) RETURN count(DISTINCT p)""")
    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_STATEMENT WHERE sub.`id`=$subjectId AND rel.`predicate_id` = $predicateId AND obj.`id`=$objectId $RETURN_STATEMENT LIMIT 1""")
    fun findBySubjectIdAndPredicateIdAndObjectId(subjectId: ThingId, predicateId: ThingId, objectId: ThingId): Optional<Neo4jStatement>

    fun findAllByStatementIdIn(statementIds: Set<StatementId>): List<Neo4jStatement>
}

@QueryResult
data class StatementsPerResource(
    val id: String,
    val count: Long
)

@QueryResult
data class Neo4jPredicateUsageCount(
    val id: String,
    val count: Long
)
