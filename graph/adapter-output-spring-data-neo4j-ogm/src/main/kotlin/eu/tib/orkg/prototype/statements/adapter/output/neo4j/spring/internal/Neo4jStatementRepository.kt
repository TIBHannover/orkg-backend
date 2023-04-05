package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ObjectService
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository.*
import java.util.*
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

    @Query("""MATCH (:`Thing`)-[r:`RELATED` {statement_id: $id}]->(o) DELETE r WITH [n IN [o] WHERE "Literal" IN LABELS(n) AND NOT (n)--()] AS l FOREACH(n IN l | DELETE n)""")
    fun deleteByStatementId(id: StatementId)

    @Query("""UNWIND $ids AS id MATCH (:`Thing`)-[r:`RELATED` {statement_id: id}]->(o) DELETE r WITH [n IN [o] WHERE "Literal" IN LABELS(n) AND NOT (n)--()] AS l FOREACH(n IN l | DELETE n)""")
    fun deleteByStatementIds(ids: Set<StatementId>)

    @Query("$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubject(subjectId: ThingId, pagination: Pageable): Page<Neo4jStatement>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObject(objectId: ThingId, pagination: Pageable): Page<Neo4jStatement>

    @Query("""MATCH statement=(sub:`Resource`)<-[:`RELATED`]-(:`Thing`) WHERE sub.resource_id in $resourceIds WITH sub.resource_id as resourceId, count(statement) as count RETURN resourceId, count""")
    fun countStatementsAboutResource(resourceIds: Set<ResourceId>): List<StatementsPerResource>

    /** Count all incoming statements to a resource node. This is used to calculate the "shared" property. */
    @Query("""MATCH statement=(obj:`Resource` {resource_id: $id})<-[rel:`RELATED`]-(:`Thing`) RETURN count(statement)""")
    fun countStatementsByObjectId(id: ResourceId): Long

    @Query("""MATCH (n:Thing)
WHERE n.`resource_id`=$id OR n.`literal_id`=$id OR n.`predicate_id`=$id OR n.`class_id`=$id
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
RETURN COUNT(rel) as cnt""")
    fun countByIdRecursive(id: ThingId): Long

    @Query("$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT WHERE (sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
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

    @Query(
        """MATCH (n:Thing)
WHERE n.resource_id = $id OR n.literal_id = $id OR n.class_id = $id OR n.predicate_id = $id
CALL apoc.path.subgraphAll(n, $configuration)
YIELD relationships
UNWIND relationships as rel
RETURN startNode(rel) as subject, rel as predicate, endNode(rel) as object
ORDER BY rel.created_at DESC"""
    )
    fun fetchAsBundle(id: ThingId, configuration: Map<String, Any>): Iterable<Neo4jStatement>

    @Query(
        """MATCH ()-[r:RELATED]->() RETURN r.predicate_id as id, COUNT(r) as count ORDER BY count DESC, id""",
        countQuery = """MATCH ()-[r:RELATED]->() RETURN COUNT(DISTINCT r.predicate_id) as cnt"""
    )
    fun countPredicateUsage(pageable: Pageable): Page<Neo4jPredicateUsageCount>

    @Query("""MATCH (n:Paper:Resource)-[:RELATED {predicate_id: 'P31'}]->(:Resource {resource_id: $id}), (n)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(L:Literal) RETURN L""")
    fun findDOIByContributionId(id: ResourceId): Optional<Neo4jLiteral>

    @Query("""OPTIONAL MATCH (:Thing)-[r1:RELATED {predicate_id: $id}]->(:Thing) OPTIONAL MATCH (:Predicate {predicate_id: $id})-[r2:RELATED]-(:Thing) WITH COUNT(DISTINCT r1) as relations, COUNT(DISTINCT r2) as nodes RETURN relations + nodes as cnt""")
    fun countPredicateUsage(id: PredicateId): Long

    @Query("""MATCH (node:Paper:Resource)-[:RELATED {predicate_id: "${ObjectService.ID_DOI_PREDICATE}"}]->(l:Literal) WHERE not 'PaperDeleted' in labels(node) AND toUpper(l.label) = toUpper($doi) RETURN node LIMIT 1""")
    fun findByDOI(doi: String): Optional<Neo4jResource>

    @Query("""
CALL {
    MATCH (:Paper:Resource {observatory_id: $id})-[:RELATED {predicate_id:"P31"}]->(:Contribution:Resource)-[:RELATED {predicate_id:"P32"}]->(r:Problem:Resource) RETURN r
    UNION
    MATCH (r:Problem:Resource {observatory_id: $id}) RETURN r
} RETURN r ORDER BY r.resource_id""",
        countQuery = """
CALL {
    MATCH (:Paper:Resource {observatory_id: $id})-[:RELATED {predicate_id:"P31"}]->(:Contribution:Resource)-[:RELATED {predicate_id:"P32"}]->(r:Problem:Resource) RETURN r
    UNION
    MATCH (r:Problem:Resource {observatory_id: $id}) RETURN r
} RETURN COUNT(r)""")
    fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (n:Resource {resource_id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH DISTINCT node.created_by AS createdBy
WHERE createdBy IS NOT NULL
RETURN createdBy
ORDER BY createdBy""",
        countQuery = """MATCH (n:Resource {resource_id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + n as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH DISTINCT node.created_by AS createdBy
WHERE createdBy IS NOT NULL
RETURN COUNT(createdBy) as cnt""")
    fun findAllContributorsByResourceId(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("""MATCH (n:Resource {resource_id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + COLLECT(n) as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH node
WHERE node.created_by IS NOT NULL AND node.created_at IS NOT NULL
WITH node.created_by AS createdBy, 
CASE
  WHEN node.created_at =~ "\d+-\d+-\d+T\d+:\d+:\d+\.\d+.*" THEN apoc.date.parse(node.created_at, "ms", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  ELSE apoc.date.parse(node.created_at, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")
END AS ms
WITH createdBy, ms - (ms % 60000) as bin
WITH DISTINCT [createdBy, apoc.date.format(bin, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")] AS edit
RETURN edit[0] as createdBy, edit[1] as createdAt
ORDER BY createdAt DESC""",
        countQuery = """MATCH (n:Resource {resource_id: $id})
CALL apoc.path.subgraphAll(n, {relationshipFilter: ">", labelFilter: "-ResearchField|-ResearchProblem|-Paper"})
YIELD relationships
UNWIND relationships AS rel
WITH rel AS p, endNode(rel) AS o, n
WITH COLLECT(p) + COLLECT(o) + COLLECT(n) as nodes
WITH DISTINCT nodes
UNWIND nodes as node
WITH node
WHERE node.created_by IS NOT NULL AND node.created_at IS NOT NULL
WITH node.created_by AS createdBy, 
CASE
  WHEN node.created_at =~ "\d+-\d+-\d+T\d+:\d+:\d+\.\d+.*" THEN apoc.date.parse(node.created_at, "ms", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  ELSE apoc.date.parse(node.created_at, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")
END AS ms
WITH createdBy, ms - (ms % 60000) as bin
WITH DISTINCT [createdBy, apoc.date.format(bin, "ms", "yyyy-MM-dd'T'HH:mm:ssXXX")] AS edit
RETURN COUNT(edit) as cnt""")
    fun findTimelineByResourceId(id: ResourceId, pageable: Pageable): Page<ResourceContributor>

    @Query("""MATCH (n:Resource {resource_id: $id}) RETURN EXISTS ((n)-[:RELATED]-(:Thing)) AS used""")
    fun checkIfResourceHasStatements(id: ResourceId): Boolean

    @Query(value = """MATCH (n:Comparison:Resource {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution:Resource)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem:Resource) RETURN DISTINCT p""",
        countQuery = """MATCH (n:Comparison:Resource {organization_id: $id })-[r:RELATED {predicate_id: 'compareContribution'}]->(rc:Contribution:Resource)-[rr:RELATED {predicate_id: 'P32'}]->(p:Problem:Resource) RETURN count(DISTINCT p)""")
    fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Neo4jResource>

    @Query("""$MATCH_STATEMENT WHERE (sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id` = $predicateId AND (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) $RETURN_STATEMENT LIMIT 1""")
    fun findBySubjectIdAndPredicateIdAndObjectId(subjectId: ThingId, predicateId: ThingId, objectId: ThingId): Optional<Neo4jStatement>

    fun findAllByStatementIdIn(statementIds: Set<StatementId>): List<Neo4jStatement>
}

@QueryResult
data class StatementsPerResource(
    val resourceId: String,
    val count: Long
)

@QueryResult
data class Neo4jPredicateUsageCount(
    val id: String,
    val count: Long
)
